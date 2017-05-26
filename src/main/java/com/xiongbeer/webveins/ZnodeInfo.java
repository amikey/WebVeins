package com.xiongbeer.webveins;

/**
 * Created by shaoxiong on 17-4-6.
 */
public class ZnodeInfo {
    public static final String ROOT_PATH            = "/webveins/";
    public static final String WORKERS_PATH         = ROOT_PATH + "wvWorkers";
    public static final String TASKS_PATH           = ROOT_PATH + "wvTasks";
    public static final String MANAGERS_PATH        = ROOT_PATH + "wvManagers";
    public static final String NEW_WORKER_PATH      = WORKERS_PATH  + "/worker_";
    public static final String ACTIVE_MANAGER_PATH  = MANAGERS_PATH + "/active_manager";
    public static final String STANDBY_MANAGER_PATH = MANAGERS_PATH + "/standby_manager_";
    public static final String NEW_TASK_PATH        = TASKS_PATH    + "/";
    public static final int JITTER_DELAY            = 10;
}
