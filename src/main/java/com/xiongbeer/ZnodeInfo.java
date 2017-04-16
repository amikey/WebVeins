package com.xiongbeer;

/**
 * Created by shaoxiong on 17-4-6.
 */
public class ZnodeInfo {

    //各目录path
    public static final String ROOT_PATH            = "/";
    public static final String WORKERS_PATH         = ROOT_PATH + "wvWorkers";
    public static final String TASKS_PATH           = ROOT_PATH + "wvTasks";
    public static final String MANAGERS_PATH        = ROOT_PATH + "wvManagers";
    public static final String NEW_WORKER_PATH      = WORKERS_PATH  + "/worker_";
    public static final String ACTIVE_MANAGER_PATH  = MANAGERS_PATH + "/active_manager";
    public static final String STANDBY_MANAGER_PATH = MANAGERS_PATH + "/standby_manager_";
    public static final String NEW_TASK_PATH        = TASKS_PATH    + "/";

    public static final int TICKTIME                = 100;

    /**
     * Manager的状态
     * Null:        刚初始化，还未进行选举
     * ELECTED:     主节点
     * NOTELECTED:  从节点
     */
    public static final int MANAGER_NULL            = 2;
    public static final int MANAGER_ELECTED         = 1;
    public static final int MANAGER_NOTELECTED      = 0;

    public static final int JITTER_DELAY            = 5;

    public static final int TASK_DEAD_DELAY         = 30;
}
