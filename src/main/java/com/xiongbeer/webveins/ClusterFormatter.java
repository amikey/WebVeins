package com.xiongbeer.webveins;

import com.xiongbeer.webveins.saver.HDFSManager;
import com.xiongbeer.webveins.utils.Color;

import com.xiongbeer.webveins.utils.InitLogger;
import org.apache.zookeeper.*;

import java.io.IOException;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * Created by shaoxiong on 17-4-9.
 */
public class ClusterFormatter implements Watcher{
    static private ClusterFormatter clusterFormatter;
    private ZooKeeper client;
    private HDFSManager hdfsManager;
    /**
     * 初始化znode基础设置，生成3个永久的znode
     * /
     * |--- webveins
     *     |--- wvTasks
     *     |--- wvWorkers
     *     |--- wvManagers
     */
    public void initZK()
            throws KeeperException, InterruptedException {
        try {
            createParent(ZnodeInfo.WORKERS_PATH);
            createParent(ZnodeInfo.MANAGERS_PATH);
            createParent(ZnodeInfo.TASKS_PATH);
        } catch (KeeperException.NodeExistsException e){
            //pass
        }
    }

    /**
     * 初始化hdfs的目录树
     * /
     * |--- webveins
	 *      |---bloom
	 *      |---tasks
	 *      	|---newurls
	 *      	|---waitingtasks
	 *      	|---finishedtasks
     * 
     * @throws IOException
     */
    public void initHDFS() throws IOException {
        hdfsManager.mkdir(Configuration.BLOOM_BACKUP_PATH);
        hdfsManager.mkdir(Configuration.NEW_TASKS_URLS);
        hdfsManager.mkdir(Configuration.WAITING_TASKS_URLS);
        hdfsManager.mkdir(Configuration.FINISHED_TASKS_URLS);
    }

    /**
     * 强制初始化ZooKeeper(会清除原来存在的节点)
     */
    public void formatZK()
            throws KeeperException, InterruptedException {
        deleteParent(ZnodeInfo.WORKERS_PATH);
        deleteParent(ZnodeInfo.MANAGERS_PATH);
        deleteParent(ZnodeInfo.TASKS_PATH);
        initZK();
    }

    /**
     * 强制初始化hdfs的目录树(会清除原来存在的文件)
     *
     * @throws IOException
     */
    public void formatHDFS() throws IOException {
        hdfsManager.delete(Configuration.BLOOM_BACKUP_PATH, true);
        hdfsManager.delete(Configuration.NEW_TASKS_URLS, true);
        hdfsManager.delete(Configuration.WAITING_TASKS_URLS, true);
        hdfsManager.delete(Configuration.FINISHED_TASKS_URLS, true);
        initHDFS();
    }

    public static synchronized ClusterFormatter getInstance() {
        if (clusterFormatter == null) {
            Configuration.getInstance();
            clusterFormatter = new ClusterFormatter();
        }
        return clusterFormatter;
    }

    private void createParent(String path)
            throws KeeperException, InterruptedException {
        client.create(path, "".getBytes(),
                OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    private void deleteParent(String path)
            throws KeeperException, InterruptedException {
        client.delete(path, -1);
    }

    private ClusterFormatter(){
        Configuration.getInstance();
        try {
            client = new ZooKeeper(Configuration.ZK_CONNECT_STRING
                    , Configuration.ZK_SESSION_TIMEOUT, this);
            hdfsManager = new HDFSManager(Configuration.HDFS_SYSTEM_CONF
                    , Configuration.HDFS_SYSTEM_PATH);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args){
        InitLogger.init();
        try {
            ClusterFormatter formatter = ClusterFormatter.getInstance();
            if(args.length > 0 && args[0].equals("-f")){
            	System.out.println(Color.error("delete all old setting and  initialize?(y/n)"));
                char choice = (char) System.in.read();
                if(choice == 'y' || choice == 'Y'){
                    System.out.println("Format Zookeeper...");
                    formatter.formatZK();
                    System.out.println("Format HDFS...");
                    formatter.formatHDFS();
                    System.out.println("Done.");
                }
            } else{
                System.out.println("Init Zookeeper...");
                formatter.initZK();
                System.out.println("Init HDFS...");
                formatter.initHDFS();
                System.out.println("Done.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {}
}
