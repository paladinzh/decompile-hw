package com.huawei.systemmanager.startupmgr.comm;

public class StartupDBConst {
    public static final String SHARED_REAL_TABLE = "StartupMgr";
    public static final String STATUS_VALUE_NO = "0";
    public static final String STATUS_VALUE_YES = "1";

    public interface AwakedViewKeys {
        public static final String AWAKED_STARTUP_INFO_VIEW = "AwakedStartupInfo";
        public static final String AWAKED_TMP_VIEW_PREFIX = "StartupAwaked_TMP";
        public static final String CALLER_PKG_SET_COL = "callerpackageset";
        public static final String CALLER_PKG_SET_STORE = "a_cp";
        public static final String LAST_CALLER_PKG_COL = "lastcallerpackage";
        public static final String LAST_CALLER_PKG_STORE = "a_lcp";
        public static final String STATUS_COL = "status";
        public static final String STATUS_STORE = "a_s";
        public static final String USER_CHANGED_COL = "userchanged";
        public static final String USER_CHANGED_STORE = "a_ucf";
    }

    public interface NormalViewKeys {
        public static final String ACTIONS_COL = "actions";
        public static final String ACTIONS_STORE = "n_a";
        public static final String NORMAL_STARTUP_INFO_VIEW = "NormalStartupInfo";
        public static final String NORMAL_TMP_VIEW_PREFIX = "StartupNormal_TMP";
        public static final int STARTUP_MASK_AUTO_START = 2;
        public static final int STARTUP_MASK_BOOT_START = 1;
        public static final int STARTUP_MASK_SELF_DEFINE = 4;
        public static final String STATUS_COL = "status";
        public static final String STATUS_STORE = "n_s";
        public static final String TYPE_COL = "type";
        public static final String TYPE_STORE = "n_t";
        public static final String USER_CHANGED_COL = "userchanged";
        public static final String USER_CHANGED_STORE = "n_ucf";
    }

    public interface StartupRecordKeys {
        public static final String COL_PACKAGE_NAME = "packageName";
        public static final String COL_STARTUP_RESULT = "startupResult";
        public static final String COL_TIME_OF_DAY = "timeOfDay";
        public static final String COL_TIME_OF_LAST_EXACT = "timeOfLastExact";
        public static final String COL_TOTAL_COUNT = "totalCount";
        public static final int HISTORY_RECORD_QUERY_DAY_BEFORE = 7;
        public static final int RECORD_CHECK_INTERVAL = 1000;
        public static final int RECORD_MAX_COUNT = 5000;
    }
}
