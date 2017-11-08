package com.huawei.systemmanager.rainbow.client.base;

import com.huawei.systemmanager.rainbow.client.connect.result.ClientServerSync;

public class ClientConstant {
    public static final String IS_INCREASE_SUPPORT = "1";
    public static final String IWARE_FILE_PATH = "/data/system/iaware";
    public static final String SYSTEM_CLOUD_CLOSE = "close";
    public static final String SYSTEM_CLOUD_OPEN = "open";

    public interface CloudActions {
        public static final String ACTION_REPLY_RECOMMEND_SINGLE_APK = "com.huawei.systemmanager.action.REPLY_RECOMMEND_SINGLE_APK";
        public static final String INTENT_CLOUD_RECOMMEND_MULTI_APK = "cloud_recommend_multi_apk";
        public static final String INTENT_CLOUD_RECOMMEND_SINGLE_APK = "cloud_recommend_single_apk";
        public static final String INTENT_INIT_CLOUDDB = "init_cloudDB";
        public static final String INTENT_INSTALL_ACTION = "app_install";
        public static final String INTENT_UNINSTALL_ACTION = "app_uninstall";
        public static final String INTENT_UPDATE_ALL_DATA = "update_all_data";
    }

    public interface CloudTimeConst {
        public static final String CHECKVERSION_CYCLE_SPF = "checkVersionCycle";
        public static final int LONG_RANDOM_MINITES = 21600000;
        public static final long ONE_DAY = 86400000;
        public static final long ONE_HOUR = 3600000;
        public static final int RANDOM_MINUTES = 240000;
        public static final long THREE_DAYS = 259200000;
    }

    public static class DelayTimeArray {
        private static final int BASE_ITEM = 4;
        public static final int DEFAULT_RECONNECT_COUNT = 0;
        public static final int DELAY_TIME_ARRAY_LENGTH = 5;
        private static final long FOUR_MINITE = 240000;

        public static long getDelayTime(int count) {
            if (count < 0) {
                return FOUR_MINITE;
            }
            if (5 <= count) {
                return ClientServerSync.getIntervalTimeFromServer();
            }
            return ((long) Math.pow(4.0d, (double) count)) * FOUR_MINITE;
        }
    }

    public static class SharedPrefrenceKeys {
    }
}
