package com.xiongbeer;

/**
 * Created by shaoxiong on 17-4-11.
 */
public class Configuration {
    public static final String BLOOM_SAVE_PATH    = "/media/shaoxiong/EmdeBoas/urlbloomvalue.dat";
    public static final String C_BLOOM_SAVE_PATH  = "/media/shaoxiong/EmdeBoas/urlbloomvalue_compressed.dat";
    public static final int BLOOM_WRITE_BLOCK     = 1024*1024;

    public static final int CHECK_TIME            = 30;

    public static final String HDFS_ROOT          = "/webveins";
    public static final String WAITING_TASKS_URLS = HDFS_ROOT + "/tasks/waitingtasks";
    public static final String FINNSED_TASKS_URLS = HDFS_ROOT + "/tasks/finnsedtasks";
    public static final String NEW_TASKS_URLS     = HDFS_ROOT + "/tasks/newurls";

    public static final String TEMP_DIR           = "temp";

    public static final String TEMP_SUFFIX        = ".bak";

    public static final int WORKER_DEAD_TIME      = 5;
}
