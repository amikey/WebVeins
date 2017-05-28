package com.xiongbeer.webveins.api.info;

import com.xiongbeer.webveins.api.SimpleInfo;
import com.xiongbeer.webveins.api.jsondata.FilterJson;
import com.xiongbeer.webveins.api.jsondata.JData;
import com.xiongbeer.webveins.filter.BloomFileInfo;
import com.xiongbeer.webveins.saver.HDFSManager;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.ZooKeeper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by shaoxiong on 17-5-12.
 */
public class FilterInfo implements SimpleInfo {
    private HDFSManager hdfsManager;
    private List<JData> info;

    public FilterInfo(HDFSManager hdfsManager){
        this.hdfsManager = hdfsManager;
        info = new LinkedList<JData>();
    }

    public FilterInfo getBloomCacheInfo(String src) throws IOException {
        List<String> filesPath = hdfsManager.listFiles(src, false);
        for(String path:filesPath){
            File file = new File(path);
            BloomFileInfo bloomFile = new BloomFileInfo(file.getName());
            FilterJson data = new FilterJson();
            try {
                data.setSize(hdfsManager.getFileLen(path));
                data.setMtime(hdfsManager.getFileModificationTime(path));
                data.setUniqueID(bloomFile.getUniqueID());
                data.setFpp(bloomFile.getFpp());
                data.setMaxCapacity(bloomFile.getExpectedInsertions());
                data.setUrlsNum(bloomFile.getUrlCounter());
                info.add(data);
            } catch (Exception e){
                //drop
            }
        }
        return this;
    }

    @Override
    public List<JData> getInfo() {
        return info;
    }
}
