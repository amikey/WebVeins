package com.xiongbeer.webveins.zk.manager;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.xiongbeer.webveins.*;
import com.xiongbeer.webveins.exception.VeinsException;
import com.xiongbeer.webveins.filter.UrlFilter;
import com.xiongbeer.webveins.saver.HDFSManager;
import com.xiongbeer.webveins.utils.Async;
import com.xiongbeer.webveins.utils.MD5Maker;
import com.xiongbeer.webveins.zk.AsyncOpThreadPool;
import com.xiongbeer.webveins.zk.task.Epoch;
import com.xiongbeer.webveins.zk.task.Task;
import com.xiongbeer.webveins.zk.task.TaskManager;
import com.xiongbeer.webveins.zk.worker.WorkersWatcher;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.zookeeper.*;
import org.apache.zookeeper.AsyncCallback.*;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * @author shaoxiong
 * Manager用于管理整个事务
 * 其中active_manager为活动的Server，而standby manager
 * 监听active manager，一旦活动节点失效则接管其工作
 */
public class Manager {
    /**
     * Manager的状态
     * Initializing:  刚初始化，还未进行选举
     * ELECTED:       主节点
     * NOT_ELECTED:   从节点
     * RECOVERING:    检测到主节点死亡，尝试恢复中
     */
    public enum Status{
        Initializing, ELECTED, NOT_ELECTED, RECOVERING
    }
    public static Manager manager;
    private CuratorFramework client;
    private String serverId;

    private String managerPath;
    private Status status;
    private ScheduledExecutorService delayExector = Executors.newScheduledThreadPool(1);
    private ExecutorService asyncOpThreadPool = AsyncOpThreadPool.getInstance().getThreadPool();

    private WorkersWatcher workersWatcher;
    private TaskManager taskManager;
    private HDFSManager hdfsManager;

    private Map<String, String> workerList = new HashMap<String, String>();

    /* 未完成的任务指RUNNING状态的任务 */
    private Map<String, Epoch> unfinishedTaskList = new HashMap<String, Epoch>();

    private UrlFilter filter;

    private Logger logger = LoggerFactory.getLogger(Manager.class);

    private Manager(CuratorFramework client, String serverId,
                   HDFSManager hdfsManager, UrlFilter filter){
        status = Status.Initializing;
        this.client = client;
        this.serverId = serverId;
        taskManager = new TaskManager(client);
        this.hdfsManager = hdfsManager;
        workersWatcher = new WorkersWatcher(client);
        this.filter = filter;
        toBeActive();
    }

    public static synchronized Manager getInstance(CuratorFramework client, String serverId,
                                      HDFSManager hdfsManager, UrlFilter filter){
        if(manager == null){
            manager = new Manager(client, serverId, hdfsManager, filter);
        }
        return manager;
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
     * 核心方法，manager定期进行manage：
     *      1.刷新任务列表
     *      2.检查Worker
     *      3.发布新的任务
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws VeinsException.FilterOverflowException bloomFilter的容量已满
     */
    public void manage() throws InterruptedException
            , IOException, VeinsException.FilterOverflowException {
        if(status == Status.ELECTED) {
            logger.debug("start manage process...");
            checkTasks();
            checkWorkers();
            publishNewTasks();
        }
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
        if(status == Status.NOT_ELECTED){

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
            if(status == Status.NOT_ELECTED) {
                logger.warn("standby manager deleted, now trying to recover it. by server."
                        + serverId + " ...");
                toBeStandBy();
            }
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
                    managerPath = ZnodeInfo.ACTIVE_MANAGER_PATH;
                    logger.info("Recover active manager success. now server." + serverId
                            + " is active manager.");
                    activeManagerExists();
                    break;
                case NODEEXISTS:
                    status = Status.NOT_ELECTED;
                    logger.info("Active manager has already recover by other server.");
                    activeManagerExists();
                    break;
                default:
                    status = Status.NOT_ELECTED;
                    logger.error("Something went wrong when recoving for active manager.",
                            KeeperException.create(Code.get(rc), path));
                    break;
            }
        }
    };

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
        process.add(Op.delete(managerPath, -1));
        process.add(Op.create(
                ZnodeInfo.ACTIVE_MANAGER_PATH,
                "".getBytes(),
                OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL
                )
        );

        if(status == Status.NOT_ELECTED) {
            status = Status.RECOVERING;
            delayExector.schedule(new Runnable(){
                @Override
                public void run(){
                    try {
                        client.getZookeeperClient().getZooKeeper().multi(
                                process,
                                recoverMultiCallback,
                                null
                        );
                    } catch (Exception e) {
                        logger.warn("Unknow error.", e);
                    }

                }
            }, ZnodeInfo.JITTER_DELAY, TimeUnit.SECONDS);
        }
        else {
            toBeActive();
        }
    }
    
    private BackgroundCallback actManagerExistsCallback = new BackgroundCallback() {
        @Override
        public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
            int rc = curatorEvent.getResultCode();
            Stat stat = curatorEvent.getStat();
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    activeManagerExists();
                    break;
                case OK:
                    if(stat == null){
                        recoverActiveManager();
                        break;
                    }
                    break;
                default:
                    checkActiveManager();
                    break;
            }
        }
    };
    
    /**
     * 检查active_manager节点是否还存在
     * 并且设置监听点
     */
    @Async
    private void activeManagerExists(){
        Watcher watcher = status==Status.NOT_ELECTED?
                actManagerExistsWatcher:null;
        try {
            client.checkExists()
                    .usingWatcher(watcher)
                    .inBackground(actManagerExistsCallback, asyncOpThreadPool)
                    .forPath(ZnodeInfo.ACTIVE_MANAGER_PATH);
        } catch (Exception e) {
            logger.warn("Unknow error.", e);
        }
    }
    
    private BackgroundCallback stdManagerExistsCallback = new BackgroundCallback() {
        @Override
        public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
            int rc = curatorEvent.getResultCode();
            String path = curatorEvent.getPath();
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    standbyManagerExists();
                    break;
                case OK:
                    // pass
                    break;
                case NONODE:
                    /* 有可能是standy节点转为了active状态，那个时候便不需要重新设置standby节点 */
                    if(status == Status.NOT_ELECTED) {
                        toBeStandBy();
                        logger.warn("standby manager deleted, now trying to recover it. by server."
                                + serverId + " ...");
                    }
                    break;
                default:
                    logger.error("Something went wrong when check standby manager itself.",
                            KeeperException.create(Code.get(rc), path));
                    break;
            }
        }
    };
    
    /**
     * 检查自身standby_manager节点是否还存在
     * 并且设置监听点
     */
    @Async
    private void standbyManagerExists(){
        try {
            client.checkExists()
                    .usingWatcher(stdManagerExistsWatcher)
                    .inBackground(stdManagerExistsCallback, asyncOpThreadPool)
                    .forPath(managerPath);
        } catch (Exception e) {
            logger.warn("Unknow error.", e);
        }
    }
    
    private BackgroundCallback actManagerCreateCallback = new BackgroundCallback() {
        @Override
        public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
            int rc = curatorEvent.getResultCode();
            String path = curatorEvent.getPath();
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
                    break;
            }
        }
    };
    
    /**
     * 激活active_manager
     */
    @Async
    private void toBeActive(){
        try {
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .inBackground(actManagerCreateCallback, asyncOpThreadPool)
                    .forPath(ZnodeInfo.ACTIVE_MANAGER_PATH);
        } catch (Exception e) {
            logger.warn("unknow error.", e);
        }
    }
    
    private BackgroundCallback stdManagerCreateCallback = new BackgroundCallback() {
        @Override
        public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
            int rc = curatorEvent.getResultCode();
            String path = curatorEvent.getPath();
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    toBeStandBy();
                    break;
                case OK:
                    status = Status.NOT_ELECTED;
                    managerPath = path;
                    logger.info("Server." + serverId + " registered. at {}", new Date().toString());
                    activeManagerExists();
                    standbyManagerExists();
                    break;
                case NODEEXISTS:
                    //TODO
                    break;
                default:
                    logger.error("Something went wrong when running for stand manager.",
                            KeeperException.create(Code.get(rc), path));
                    break;
            }
        }
    };

    /**
     * 激活standby_manager
     */
    @Async
    private void toBeStandBy(){
        try {
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .inBackground(stdManagerCreateCallback, asyncOpThreadPool)
                    .forPath(ZnodeInfo.STANDBY_MANAGER_PATH + serverId);
        } catch (Exception e) {
            logger.warn("unknow error.", e);
        }
    }
    
    private BackgroundCallback actCheckCallback = new BackgroundCallback() {
        @Override
        public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
            int rc = curatorEvent.getResultCode();
            String path = curatorEvent.getPath();
            switch (Code.get(rc)){
                case CONNECTIONLOSS:
                    checkActiveManager();
                    break;
                case NONODE:
                    recoverActiveManager();
                    break;
                default:
                    logger.error("Something went wrong when check active manager.",
                            KeeperException.create(Code.get(rc), path));
                    break;
            }
        }
    };

    /**
     * 检查active_manager的状态
     */
    @Async
    private void checkActiveManager(){
        try {
            client.getData()
                    .inBackground(actCheckCallback, asyncOpThreadPool)
                    .forPath(ZnodeInfo.ACTIVE_MANAGER_PATH);
        } catch (Exception e) {
            logger.warn("unknow error.", e);
        }
    }
    
    
    /**
     * 检查已经完成的任务，把对应的
     * 存放url的文件移出等待队列
     */
    private void checkTasks() throws InterruptedException, IOException {
        syncWaitingTasks();
        /* 更新tasksInfo状态表 */
        taskManager.checkTasks();
        /* TODO 用克隆的map进行迭代，fail-fast会影响直接用原map操作 */
        @SuppressWarnings("unchecked")
		Map<String, Epoch> tasks = taskManager.getTasksInfo();
        Iterator<Entry<String, Epoch>> iterator = tasks.entrySet().iterator();
        while(iterator.hasNext()){
            @SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry)iterator.next();
            String key = (String) entry.getKey();
            Epoch value = (Epoch) entry.getValue();

            if(value.getStatus() == Task.Status.FINISHED){
                if(unfinishedTaskList.containsKey(key)){
                    unfinishedTaskList.remove(key);
                }
                hdfsManager.moveHDFSFile(Configuration.WAITING_TASKS_URLS + "/" + key,
                        Configuration.FINISHED_TASKS_URLS + "/" + key);
                taskManager.releaseTask(ZnodeInfo.TASKS_PATH + '/' + key);
            }
            else if(value.getStatus() == Task.Status.RUNNING){
                unfinishedTaskList.put(key, value);
            } else{
                unfinishedTaskList.remove(key);
            }
        }
    }

    /**
     * 用于防止用户误将znode下task删除导致任务永久失效的情况
     *
     * 保证hdfs中waitingtasks与znode中wvTasks一致
     * 但是无法保证znode中wvTasks与hdfs中waitingtasks中一致
     */
    private void syncWaitingTasks() throws IOException {
        List<String> files = hdfsManager.listFiles(Configuration.WAITING_TASKS_URLS, false);
        for(String filePath:files){
            String fileName = Files.getNameWithoutExtension(filePath);
            taskManager.submit(fileName);
        }
    }

    /**
     * 检查Workers的状态，
     * 若它失效则需要重置
     * 它之前领取的任务。
     */
    private void checkWorkers() throws InterruptedException {
        Iterator<Entry<String, Epoch>> iterator = unfinishedTaskList.entrySet().iterator();
        workersWatcher.getWorkers();
        workersWatcher.reflushWorkerStatus();
        workerList = workersWatcher.getWorkersList();
        while(iterator.hasNext()){
            @SuppressWarnings("rawtypes")
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
        String tempSavePath = Configuration.BLOOM_TEMP_DIR;
        List<String> hdfsUrlFiles = hdfsManager.listFiles(
                Configuration.NEW_TASKS_URLS,false);
        if(hdfsUrlFiles.size() == 0){
            /* 没有需要处理的新URL文件 */
            return;
        }


        List<String> urlFiles = downloadTaskFiles(hdfsUrlFiles, tempSavePath);

        /*
            后续整体工作流程：
            对每个文件进行逐个按行读取，在开始读的同时也会
            在本地新建一个同名的.bak文件每读一行后会尝试将
            其录入过滤器，若成功则说明此url是新的url，会将
            其写入.bak文件中。完毕后会删除其他非.bak的文件
            ，再去掉.bak文件的.bak后缀。
            然后将处理后的所有文件上传到hdfs上，确保上传成
            功后才会到znode中发布任务
        */
        filterUrlAndSave(urlFiles, tempSavePath);
        File file = new File(tempSavePath);
        deleteNormalFiles(file);
        removeTempSuffix(file);
        submitNewTasks(file);

        /*
            TODO：
            当文件很大时会占用大量IO
            需要另外一种方式来备份
            目前想参照fsimage-edits的模式
            这个需要阅读其实现源码
        */
        /* 备份 */
        backUpFilterCache();

        /*
            在hdfs上删除处理
            完毕的的new url文件
         */
        for(String urlPath:hdfsUrlFiles){
            hdfsManager.delete(urlPath, false);
        }
    }

    /**
     * 删除多余文件(不以Configuration.TEMP_SUFFIX结尾的文件)
     *
     * @param tempSaveDir
     */
    private void deleteNormalFiles(File tempSaveDir){
        File[] urls = tempSaveDir.listFiles();
        for(File url:urls){
            if(url.isFile()){
                String path = url.getAbsolutePath();
                if(!path.endsWith(Configuration.TEMP_SUFFIX)){
                    url.delete();
                }
            }
        }
    }

    /**
     * 去除文件的Configuration.TEMP_SUFFIX后缀
     *
     * @param dir
     */
    private void removeTempSuffix(File dir){
        File[] files = dir.listFiles();
        for(File file:files){
            if(file.isFile()){
                String path = file.getAbsolutePath();
                if(path.endsWith(Configuration.TEMP_SUFFIX)){
                    file.renameTo(new File(path.substring(0,
                            path.length() - Configuration.TEMP_SUFFIX.length())));
                }
            }
        }
    }

    /**
     * 上传新任务到HDFS，然后发布任务到ZooKeeper中
     *
     * @param dir
     * @throws IOException
     */
    private void submitNewTasks(File dir) throws IOException {
        File[] files = dir.listFiles();
        for(File file:files){
            if(!file.isDirectory()) {
                String filePath = file.getAbsolutePath();
                /*
                    若文件已存在则直接跳过
                    可以这么做的原因是文件名是根据内容生成的md5码
                    相同则基本可以确定就是同一个文件，没必要重复上传
                */
                if(!hdfsManager.exist(filePath)) {
                    hdfsManager.upLoad(filePath,
                            Configuration.WAITING_TASKS_URLS + '/' + file.getName());
                }
                taskManager.submit(file.getName());
            }
        }
    }

    /**
     * 从hdfs下新任务文件下载到本地
     *
     * @param urlFiles hdfs中的文件路径
     * @param savePath 保存路径
     * @return
     * @throws IOException
     */
    private List<String> downloadTaskFiles(List<String> urlFiles
            , String savePath) throws IOException {
        List<String> localUrlFiles = new LinkedList<String>();
        for(String filePath:urlFiles){
            /*
                若文件已存在则直接跳过
                可以这么做的原因是文件名是根据内容生成的md5码
                相同则基本可以确定就是同一个文件，没必要重复下载
            */
            File temp = new File(filePath);
            File localFile = new File(savePath
                    + File.separator + temp.getName());
            if(!localFile.exists()) {
                hdfsManager.downLoad(filePath, savePath);
            }
            localUrlFiles.add(localFile.getAbsolutePath());
        }
        return localUrlFiles;
    }

    /**
     * 备份filter的缓存文件到hdfs
     * 注意：目前而言，会删除原来的旧缓存文件（无论是本地还是hdfs中）
     *
     * @throws IOException
     */
    public void backUpFilterCache() throws IOException {
        /* 备份之前删除原来的缓存文件 */
        File localSave = new File(Configuration.BLOOM_SAVE_PATH);
        File[] localFiles = localSave.listFiles();
        for(File file:localFiles){
            if(file.isFile()){
                file.delete();
            }
        }
        /* 备份至本地 */
        String bloomFilePath = filter.save(Configuration.BLOOM_SAVE_PATH);
        /* 上传至hdfs */
        hdfsManager.upLoad(bloomFilePath,
                Configuration.BLOOM_BACKUP_PATH);

        /* 删除hdfs上旧的缓存文件，去除新缓存文件的TEMP_SUFFIX后缀 */
        List<String> cacheFiles
                = hdfsManager.listFiles(Configuration.BLOOM_BACKUP_PATH, false);
        for(String cache:cacheFiles){
            if(!cache.endsWith(Configuration.TEMP_SUFFIX)) {
                hdfsManager.delete(cache, false);
            }
            else{
                String newName = cache.substring(0,
                        cache.length() - Configuration.TEMP_SUFFIX.length());
                hdfsManager.moveHDFSFile(cache, newName);
            }
        }
    }

    /**
     * 遍历下载下来的保存着url的文件
     * 以行为单位将其放入过滤器
     * 过滤后的url会被以固定的数量
     * 切分为若干个文件
     *
     * @param urlFiles
     * @throws IOException
     */
    private void filterUrlAndSave(List<String> urlFiles
            , final String saveDir) throws IOException {
        /* 用AtomicLong的原因只是为了能在匿名类中计数 */
        final AtomicLong newUrlCounter = new AtomicLong(0);
        final StringBuilder newUrls = new StringBuilder();
        final MD5Maker md5 = new MD5Maker();
        for(String filePath:urlFiles) {
            File file = new File(filePath);
            /* 每读一定数量的URLS就将其写入新的文件 */
            Files.readLines(file,
                    Charset.defaultCharset(), new LineProcessor<Object>() {
                @Override
                public boolean processLine(String line) throws IOException {
                    String newLine = line + System.getProperty("line.separator");
                    /* 到filter中确认url是不是已经存在，已经存在就丢弃 */
                    if(filter.put(line)){
                        md5.update(newLine);
                        if(newUrlCounter.get() <= Configuration.TASK_URLS_NUM) {
                            newUrls.append(newLine);
                            newUrlCounter.incrementAndGet();
                        } else {
                            /* 文件名是根据其内容生成的md5值 */
                            String urlFileName = saveDir + File.separator
                                    + md5.toString()
                                    + Configuration.TEMP_SUFFIX;
                            Files.write(newUrls.toString().getBytes()
                                    , new File(urlFileName));
                            newUrls.delete(0, newUrls.length());
                            newUrlCounter.set(0);
                            md5.reset();
                        }
                    }
                    return true;
                }

                @Override
                public Object getResult() {
                    /* 处理残留的urls */
                    if(newUrls.length() > 0){
                        String urlFileName = saveDir + File.separator
                                + md5.toString()
                                + Configuration.TEMP_SUFFIX;
                        try {
                            Files.write(newUrls.toString().getBytes()
                                    , new File(urlFileName));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        newUrls.delete(0, newUrls.length());
                        newUrlCounter.set(0);
                    }
                    return null;
                }
            });
        }
    }
}
