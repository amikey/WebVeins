package com.xiongbeer.manager;

import com.xiongbeer.*;
import com.xiongbeer.filter.bloom.RamBloomTable;
import com.xiongbeer.filter.bloom.UrlFilter;
import com.xiongbeer.saver.HDFSManager;
import com.xiongbeer.task.Epoch;
import com.xiongbeer.task.Task;
import com.xiongbeer.task.TaskManager;

import com.xiongbeer.worker.WorkersWatcher;
import org.apache.zookeeper.*;
import org.apache.zookeeper.AsyncCallback.*;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * @author shaoxiong
 * Manager用于管理整个事务
 * 其中active_manager为活动的Server，而standby manager
 * 监听active manager，一旦活动节点失效则接管其工作。
 */
public class Manager {
    /**
     * Manager的状态
     * Initializing: 刚初始化，还未进行选举
     * ELECTED:      主节点
     * NOTELECTED:   从节点
     */
    public enum Status{
        Initializing, ELECTED, NOTELECTED
    }
    private ZooKeeper client;
    private String serverId;

    private String managerPath;
    private Status status;
    private ScheduledExecutorService delayExector = Executors.newScheduledThreadPool(1);

    private WorkersWatcher workersWatcher;
    private TaskManager taskManager;
    private HDFSManager hdfsManager;

    private HashMap<String, String> workerList = new HashMap<String, String>();

    /* 未完成的任务指RUNNING状态的任务 */
    private HashMap<String, Epoch> unfinnsedTaskList = new HashMap<String, Epoch>();

    private UrlFilter filter;

    private Logger logger = LoggerFactory.getLogger(Manager.class);

    public Manager(ZooKeeper zk, String serverId,
                   String hdfsFileSystem, UrlFilter filter){
        status = Status.Initializing;
        client = zk;
        this.serverId = serverId;

        taskManager = new TaskManager(zk);
        hdfsManager = new HDFSManager(hdfsFileSystem);
        workersWatcher = new WorkersWatcher(zk);
        this.filter = filter;

        toBeActive();
    }

    public Status getStatus(){
        return status;
    }

    public String getManagerPath(){
        return managerPath;
    }

    public String getServerId(){
        return serverId;
    }

    public void setServerId(String serverId){
        this.serverId = serverId;
    }

    /**
     *
     */
    public void manage() throws InterruptedException, IOException, VeinsException.FilterOverflowException {
        checkTasks();
        checkWorkers();
        publishNewTasks();

        System.out.println(unfinnsedTaskList);
        System.out.println(workerList);
    }

    /**
     * TODO
     * 接管active职责
     *
     * 平稳地将当前active manager注销，然后
     * 让standby manager接管它的工作
     */
    public void takeOverResponsibility(){
        // 只有standby节点才能接管
        if(status == Status.NOTELECTED){

        }
    }

    /**
     *  activeManager的监听器
     *
     * 当其被删除时(失效时)，就开始尝试让
     * 活动的standbyManager(中的某一个)
     * 来接管失效的activeManager
     */
    private Watcher actManagerExistsWatcher = new Watcher() {
        public void process(WatchedEvent watchedEvent) {
            if(watchedEvent.getType() == Event.EventType.NodeDeleted){
                assert ZnodeInfo.ACTIVE_MANAGER_PATH.equals(watchedEvent.getPath());
                logger.warn("Active manager deleted, now trying to activate manager again. by server."
                        + serverId + " ...");
                recoverActiveManager();
            }
        }
    };

    /**
     *  standbyManager的监听器
     *
     * 失效时，尝试重新连接
     */
    private Watcher stdManagerExistsWatcher = new Watcher() {
        public void process(WatchedEvent watchedEvent) {
            assert managerPath.equals(watchedEvent.getPath());
            logger.warn("standby manager deleted, now trying to recover it. by server."
                    + serverId + " ...");
            toBeStandBy();
        }
    };

    /**
     * 集合操作的Callback，尝试恢复activeManager
     *
     * 需要先删除之前自身创建的
     * standby_manager节点，然后
     * 创建active_manager节点。
     * 这2个操作中的任何一个操作失败，
     * 则整个操作失败。
     */
    private MultiCallback recoverMultiCallback = new MultiCallback() {
        public void processResult(int rc, String path, Object ctx, List<OpResult> list) {
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    logger.warn("CONNECTIONLOSS, retrying to recover active manager. server."
                            + serverId + " ...");
                    recoverActiveManager();
                    break;
                case OK:
                    status = Status.ELECTED;
                    logger.info("Recover active manager success. now server." + serverId +
                            " is active manager.");
                    activeManagerExists();
                    break;
                case NODEEXISTS:
                    logger.info("Active manager has already recover by other server.");
                    activeManagerExists();
                    break;
                default:
                    logger.error("Something went wrong when recoving for active manager.",
                            KeeperException.create(Code.get(rc), path));
            }
        }
    };

    private StatCallback actManagerExistsCallback = new StatCallback() {
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    activeManagerExists();
                    break;
                case OK:
                    if(stat == null){
                        recoverActiveManager();
                        break;
                    }
                    if(status == Status.ELECTED){
                        logger.info("Active manager working well. watching by itself");
                    }
                    else {
                        logger.info("Active manager working well. watching by " + serverId);
                    }
                    break;
                default:
                    checkActiveManager();
                    break;
            }
        }
    };

    private StatCallback stdManagerExistsCallback = new StatCallback() {
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    standbyManagerExists();
                    break;
                case OK:
                    if(stat == null){
                        toBeStandBy();
                        logger.warn("standby manager deleted, now trying to recover it. by server."
                                + serverId + " ...");
                        break;
                    }
                    break;
                default:

            }
        }
    };

    private StringCallback actManagerCreateCallback = new StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    checkActiveManager();
                    break;
                case OK:
                    logger.info("Active manager created success. at {}", new Date().toString());
                    managerPath = path;
                    status = Status.ELECTED;
                    activeManagerExists();
                    break;
                case NODEEXISTS:
                    logger.info("Active manger already exists, turn to set standby manager...");
                    toBeStandBy();

                    break;
                default:
                    logger.error("Something went wrong when running for active manager.",
                            KeeperException.create(Code.get(rc), path));

            }
        }
    };

    private StringCallback stdManagerCreateCallback = new StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    toBeStandBy();
                    break;
                case OK:
                    status = Status.NOTELECTED;
                    managerPath = path;
                    logger.info("Server." + serverId + " registered. at {}", new Date().toString());
                    activeManagerExists();
                    standbyManagerExists();
                    break;
                default:
                    logger.error("Something went wrong when running for stand manager.",
                            KeeperException.create(Code.get(rc), path));
            }
        }
    };


    private DataCallback actCheckCallback = new DataCallback() {
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    checkActiveManager();
                    break;
                case NONODE:
                    recoverActiveManager();
                    break;
            }
        }
    };

    /**
     * 激活active_manager
     */
    @Async
    private void toBeActive(){
        client.create(
                ZnodeInfo.ACTIVE_MANAGER_PATH,
                serverId.getBytes(),
                OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL,
                actManagerCreateCallback,
                null
        );
    }

    /**
     * 激活standby_manager
     */
    @Async
    private void toBeStandBy(){
        client.create(
                ZnodeInfo.STANDBY_MANAGER_PATH + serverId,
                serverId.getBytes(),
                OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL,
                stdManagerCreateCallback,
                null
        );
    }

    /**
     * 检查active_manager的状态
     */
    @Async
    private void checkActiveManager(){
        client.getData(
                ZnodeInfo.ACTIVE_MANAGER_PATH,
                false,
                actCheckCallback,
                null
        );
    }

    /**
     * 检查active_manager节点是否还存在
     * 并且设置监听点
     */
    @Async
    private void activeManagerExists(){
        client.exists(
                ZnodeInfo.ACTIVE_MANAGER_PATH,
                actManagerExistsWatcher,
                actManagerExistsCallback,
                null
        );
    }

    /**
     * 检查自身standby_manager节点是否还存在
     * 并且设置监听点
     */
    @Async
    private void standbyManagerExists(){
        client.exists(
                managerPath,
                stdManagerExistsWatcher,
                stdManagerExistsCallback,
                null
        );
    }


    /*
     * 恢复active_manager
     *
     * status记录了此节点之前是否是active状态，
     * 是则立刻重新获取active权利，否则先等待
     * JITTER_DELAY秒，然后尝试获取active权利
     *
     * 这样做的原因是为了防止网络抖动造成的
     * active_manager被误杀
     */
    @Async
    private void recoverActiveManager(){
        final ArrayList<Op> process = new ArrayList<Op>();
        process.add(Op.delete(managerPath, 0));
        process.add(Op.create(
                        ZnodeInfo.ACTIVE_MANAGER_PATH,
                        serverId.getBytes(),
                        OPEN_ACL_UNSAFE,
                        CreateMode.EPHEMERAL
                    )
        );

        if(status == Status.NOTELECTED) {
            delayExector.schedule(new Runnable(){
                public void run(){
                    client.multi(
                            process,
                            recoverMultiCallback,
                            null
                    );
                }
            }, ZnodeInfo.JITTER_DELAY, TimeUnit.SECONDS);
        }
        else {
            toBeActive();
        }
    }


    /**
     * 检查已经完成的任务，把对应的
     * 存放url的文件移出等待队列
     */
    private void checkTasks() throws InterruptedException, IOException {
        Tracker tracker = new Tracker();
        taskManager.checkTasks(tracker);
        while(tracker.getStatus() == Tracker.WAITING){
            /* 等待完成 */
            Thread.sleep(50);
        }
        HashMap<String, Epoch> tasks = taskManager.getTasksInfo();
        Iterator iterator = tasks.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry entry = (Map.Entry)iterator.next();
            String key = (String) entry.getKey();
            Epoch value = (Epoch) entry.getValue();
            if(value.getStatus().equals(Task.FINISHED)){
                if(unfinnsedTaskList.containsKey(key)){
                    unfinnsedTaskList.remove(key);
                }
                hdfsManager.moveHDFSFile(Configuration.WAITING_TASKS_URLS + "/" + key,
                        Configuration.FINNSED_TASKS_URLS + "/" + key);
                taskManager.releaseTask(ZnodeInfo.TASKS_PATH + '/' + key);

            }
            else if(value.getStatus().equals(Task.RUNNING)){
                unfinnsedTaskList.put(key, value);
            }
        }
    }

    /**
     * 检查Workers的状态，
     * 若它失效则需要重置
     * 它之前领取的任务。
     */
    private void checkWorkers() throws InterruptedException {
        Tracker tracker = new Tracker();
        HashMap<String, Epoch> tasks = taskManager.getTasksInfo();
        Iterator iterator = unfinnsedTaskList.entrySet().iterator();
        tracker.setStatus(Tracker.WAITING);
        workersWatcher.getWorkers(tracker);
        while(tracker.getStatus() == Tracker.WAITING){
            /* 等待完成 */
            Thread.sleep(50);
        }
        workersWatcher.reflushWorkerStatus();
        workerList = workersWatcher.getWorkersList();
        while(iterator.hasNext()){
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            // 检查执行对应任务的worker是不是挂了
            if(! workerList.containsValue(name)){
                /*
                  挂了就需要查看最后一次修改时间与检查时间的差值，如果
                  差值超过预设的值，就认为任务该worker失效，需要重置任务
                 */
                if(((Epoch) entry.getValue()).getDifference() > Configuration.WORKER_DEAD_TIME){
                    taskManager.resetTask(ZnodeInfo.TASKS_PATH + "/" + name);
                    logger.warn("The owner of task: " + name + " has dead, now reset it...");
                }
            }
        }
    }

    /**
     *  发布新的任务
     */
    private void  publishNewTasks() throws IOException, VeinsException.FilterOverflowException {
        LinkedList<String> urlFiles = hdfsManager.listChildren(
                Configuration.NEW_TASKS_URLS,false);
        if(urlFiles.size() == 0){
            logger.info("No new urls to assign");
            return;
        }

        /*
            将hdfs下新任务文件下载到本地
         */
        for(String filePath:urlFiles){
            hdfsManager.downLoad(filePath, Configuration.TEMP_DIR);
        }


        /*
             TODO 目前的方案不是很合理

            对每个文件进行逐个按行读取，在开始读的同时也会
            在本地新建一个同名的.bak文件每读一行后会尝试将
            其录入过滤器，若成功则说明此url是新的url，会将
            其写入.bak文件中。完毕后会删除其他非.bak的文件
            ，并去掉.bak文件的.bak后缀。
            然后将处理后的所有文件上传到hdfs上，确保上传成
            功后才会到znode中发布任务

            特别注意：读取的URL中目前不支持中文
        */
        for(String filePath:urlFiles) {
            String filName = getFileName(filePath);
            FileInputStream fis = new FileInputStream(Configuration.TEMP_DIR +
                                        '/' + filName);
            FileChannel inChannel = fis.getChannel();
            ByteBuffer inBuffer = ByteBuffer.allocate(1024);

            FileOutputStream fos = new FileOutputStream(
                                Configuration.TEMP_DIR + '/'+ filName + ".bak");
            FileChannel outChannel = fos.getChannel();
            ByteBuffer outBuffer = ByteBuffer.allocate(1024);


            StringBuilder builder = new StringBuilder();
            String line;

            char ch;
            while(inChannel.read(inBuffer) != -1){
                inBuffer.flip();
                while(inBuffer.hasRemaining()){
                    ch = (char) inBuffer.get();
                    if(ch != '\n'){
                        builder.append(ch);
                    }
                    else{
                        line = builder.toString();
                        /*
                            向过率器添加URL，成功
                            则说明之前不存在，将其
                            加入新任务文件中
                        */
                        if(filter.add(line)){
                            line += '\n';
                            byte[] bytes = line.getBytes();
                            int len = bytes.length;
                            int limit = outBuffer.limit();
                            int index = 0;
                            while(index < len) {
                                while (index < len && outBuffer.position() < limit){
                                    outBuffer.put(bytes[index++]);
                                }
                                outBuffer.flip();
                                outChannel.write(outBuffer);
                                outBuffer.clear();
                            }
                        }
                        builder = new StringBuilder();
                    }
                }
                inBuffer.clear();
            }
            inChannel.close();
            outChannel.close();
            fis.close();
            fos.close();
        }



        /* 删除多余文件 */
        File  file = new File(Configuration.TEMP_DIR);
        File[] urls = file.listFiles();
        for(File url:urls){
            if(url.isFile()){
                String path = url.getAbsolutePath();
                if(!path.endsWith(Configuration.TEMP_SUFFIX)){
                    url.delete();
                }
            }
        }

        /* 去除.bak后缀 */
        urls = file.listFiles();
        for(File url:urls){
            if(url.isFile()){
                String path = url.getAbsolutePath();
                if(path.endsWith(Configuration.TEMP_SUFFIX)){
                    url.renameTo(new File(path.substring(0,
                            path.length() - Configuration.TEMP_SUFFIX.length())));
                }
            }
        }


        /*
            上传新任务
            成功后发布任务
         */
        urls = file.listFiles();
        for(File url:urls){
            if(!url.isDirectory()) {
                hdfsManager.upLoad(url.getAbsolutePath(),
                        Configuration.WAITING_TASKS_URLS +  '/' + url.getName());
                taskManager.submit(url.getName());
            }
        }


        /*
            如果bloom过滤器是ram类型的，还需要备份它

            注意：如果存储的url达到过千万级别，
            而且要求精度较高，请不要使用ram_bloom
         */
        if(filter.getMode().equals(UrlFilter.CreateMode.RAM)){
            RamBloomTable table = (RamBloomTable) filter.getTable();
            table.save(Configuration.R_BLOOM_SAVE_PATH);
            hdfsManager.upLoad(Configuration.R_BLOOM_SAVE_PATH,
                    Configuration.BLOOM_BACKUP_PATH);
        }

        /*
            在hdfs上删除处理
            完毕的的new url文件
         */

        for(String urlPath:urlFiles){
            hdfsManager.deleteHDFSFile(urlPath);
        }
    }


    /**
     * 提取path中的FileName
     *
     * @param path
     * @return
     */
    private String getFileName(String path){
        String[] items = path.split("/");
        if(items == null){
            return null;
        }
        return items[items.length-1];
    }
}
