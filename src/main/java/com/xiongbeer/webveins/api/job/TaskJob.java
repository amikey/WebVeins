package com.xiongbeer.webveins.api.job;

import com.xiongbeer.webveins.Configuration;
import com.xiongbeer.webveins.ZnodeInfo;
import com.xiongbeer.webveins.api.SimpleJob;
import com.xiongbeer.webveins.exception.VeinsException;
import com.xiongbeer.webveins.saver.HDFSManager;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shaoxiong on 17-5-12.
 */
public class TaskJob implements SimpleJob {
    private ZooKeeper zk;
    private HDFSManager hdfsManager;
    private static int MAX_OUTPUT_INFO = 10;

    public TaskJob(ZooKeeper zk, HDFSManager hdfsManager){
        this.zk = zk;
        this.hdfsManager = hdfsManager;
    }

    public String removeTasks(String regex){
        String separator = System.getProperty("line.separator");
        StringBuilder builder = new StringBuilder(separator);
        int counter = 0;
        try {
            Pattern pattern = Pattern.compile(regex);
            for (String task : getTasksName()) {
                Matcher matcher = pattern.matcher(task);
                while (matcher.find()) {
                    removeFromHDFS(task);
                    removeFromZnode(task);
                    if(counter < MAX_OUTPUT_INFO) {
                        builder.append("[info] delete " + task
                                + separator);
                    }
                    else if(counter == MAX_OUTPUT_INFO){
                        builder.append("      ....."+separator);
                    }
                    counter++;
                    break;
                }
            }
            builder.append("[info] total " + counter + " items" + separator);
        } catch (KeeperException.ConnectionLossException e) {
            throw new VeinsException.OperationFailedException("[Error] Connection loss" +
                    ", you may have to wait for a while.");
        } catch (KeeperException.AuthFailedException e) {
            throw new VeinsException.OperationFailedException("[Error] Authentication failed.");
        } catch (KeeperException.NoAuthException e) {
            throw new VeinsException.OperationFailedException("[Error] Permission denied.");
        } catch (KeeperException e) {
            throw new VeinsException.OperationFailedException("[Error] Unknow reason.");
        } catch (InterruptedException e) {
            throw new VeinsException.OperationFailedException("[Error] Interrupted.");
        } catch (IOException e) {
            throw new VeinsException.OperationFailedException(e.getMessage());
        }
        return builder.toString();
    }

    public void removeFromZnode(String taskName)
            throws KeeperException, InterruptedException {
        zk.delete(ZnodeInfo.TASKS_PATH + '/' + taskName, -1);
    }

    public void removeFromHDFS(String taskName)
            throws IOException {
        hdfsManager.deleteHDFSFile(Configuration.WAITING_TASKS_URLS + '/' + taskName);
    }

    public List<String> getTasksName()
            throws KeeperException, InterruptedException {
        return zk.getChildren(ZnodeInfo.TASKS_PATH, false);
    }

    @Override
    public void sumbit() {

    }
}
