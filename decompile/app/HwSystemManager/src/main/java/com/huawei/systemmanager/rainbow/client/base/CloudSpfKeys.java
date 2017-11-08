package com.huawei.systemmanager.rainbow.client.base;

public class CloudSpfKeys {
    public static final String CLOUD_XML_DATA_INITED = "cloudXmlDataInited";
    public static final String FILE_NAME = "hwCloudSecurity";
    public static final String GET_CLOUD_SERVER_DATA_SUCCESS = "getCloudServerDataSuccess";
    public static final String LAST_ALARM_TIME = "lastAlarmTime";
    public static final String LAST_CONNECT_CHANGE_AVAILABLE_TIME = "lastConnectChangeToAvailableTime";
    public static final String LAST_RECOMMEND_MULTI_APK_SUCCESS_TIME = "lastRecommendMultiApkSuccessTime";
    public static final String MUTIL_APPS_CHANGE = "mutilAppsChange";
    public static final String NEED_CLOUD_SYNC_COMPLETE_NOTIFIED = "needCloudSyncCompleteNotified";
    public static final String RECONNECT_CLOUD_SERVER_COUNT = "reconnectCloudServerCount";
    public static final String SYSTEM_MANAGER_CLOUD = "SystemManageCloud";

    public interface CloudReqVerSpfKeys {
        public static final String BACKGROUND_LIST_VERSION_SPF = "backgroundVersion";
        public static final String COMPETITOR_SPF = "competitorVersion";
        public static final String CONTROL_BLACK_LIST_VERSION_SPF = "controlBlackVersion";
        public static final String CONTROL_WHITE_LIST_VERSION_SPF = "controlWhiteVersion";
        public static final String MESSAGE_SAFE_SPF = "messageSafeVersion";
        public static final String NOTIFICATION_SPF = "notificationVersion";
        public static final String PHONE_LIST_VERSION_SPF = "phoneVersion";
        public static final String PUSH_LIST_VERSION_SPF = "pushVersion";
        public static final String RECOMMEND_RIGHTS_SPF = "recommendRightsVersion";
        public static final String RIGHT_LIST_VERSION_SPF = "appsRightsVersion";
        public static final String STARTUP_SPF = "startupVersion";
        public static final String UNIFIED_POWER_APPS_SPF = "unifiedPowerAppsVersion";
    }
}
