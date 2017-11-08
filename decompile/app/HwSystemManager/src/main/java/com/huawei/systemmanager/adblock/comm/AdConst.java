package com.huawei.systemmanager.adblock.comm;

public class AdConst {
    public static final String ACTION_AD_APKDL_URL_DETECTED = "com.android.intent.action.apkdl_url_detected";
    public static final String ACTION_AD_BLOCKED_TOAST = "com.huawei.systemmanager.action.AD_BLOCKED_TOAST";
    public static final String ACTION_AD_CHECK_APK_URL = "com.huawei.systemmanager.action.AD_CHECK_APK_URL";
    public static final String ACTION_AD_UPDATE = "com.huawei.systemmanager.action.AD_UPDATE";
    public static final String ACTION_AD_UPDATE_RESULT = "com.huawei.systemmanager.action.AD_UPDATE_RESULT";
    public static final int AD_UPDATE_ALL = 2;
    public static final int AD_UPDATE_NONE = 0;
    public static final int AD_UPDATE_PART = 1;
    public static final String BUNDLE_BLOCKED_MESSAGE = "blocked_message";
    public static final String BUNDLE_CHECKED_COUNT = "checkedCount";
    public static final String BUNDLE_KEY_DOWNLOADID = "downloadId";
    public static final String BUNDLE_KEY_PKG = "pkg";
    public static final String BUNDLE_KEY_STARTTIME = "startTime";
    public static final String BUNDLE_KEY_UID = "uid";
    public static final String BUNDLE_KEY_UPDATE_RESULT = "update_result";
    public static final String BUNDLE_KEY_UPDATE_TYPE = "update_type";
    public static final String BUNDLE_KEY_URL = "url";
    public static final String BUNDLE_TOTAL_COUNT = "totalCount";
    public static final String DOWNLOAD_APPS_KEY = "hw_download_non_market_apps";
    public static final int DOWNLOAD_APPS_STATUS_OFF = 0;
    public static final int DOWNLOAD_APPS_STATUS_ON = 1;
    public static final long OVERRECORD_TIME = 604800000;
    public static final String PERMISSION_AD_APKDL_STRATEGY = "com.huawei.permission.AD_APKDL_STRATEGY";
    public static final String TAG_PREFIX = "AdBlock_";
    public static final long THREE_DAYS = 259200000;

    public interface CloudRequest {
        public static final String PARAM_PKG_NAME = "pkgName";
        public static final String PARAM_VERSION = "version";
        public static final String PARAM_VERSION_CODE = "versionCode";
    }

    public interface CloudResult {
        public static final String AD_RESULT_ADSTRATEGIES = "adStrategies";
        public static final String AD_RESULT_APPNAME = "appName";
        public static final String AD_RESULT_CANCEL_BTN_TEXT = "cancelBtnText";
        public static final String AD_RESULT_CONTINUE_BTN_TEXT = "continueBtnText";
        public static final String AD_RESULT_DETAILID = "detailId";
        public static final String AD_RESULT_DLSTRATEGIES = "dlStrategies";
        public static final String AD_RESULT_DL_URL = "downloadUrl";
        public static final String AD_RESULT_HASAPP = "hasApp";
        public static final int AD_RESULT_HASAPP_OK = 1;
        public static final String AD_RESULT_ICON = "icon";
        public static final String AD_RESULT_OFFICIAL_BTN_TEXT = "officialBtnText";
        public static final String AD_RESULT_OPT_POLICY = "optPolicy";
        public static final String AD_RESULT_PKG = "pkg";
        public static final String AD_RESULT_PKG_NAME = "packageName";
        public static final String AD_RESULT_RTNCODE = "rtnCode";
        public static final int AD_RESULT_RTNCODE_OK = 0;
        public static final String AD_RESULT_SIZE = "size";
        public static final String AD_RESULT_TIPS = "tips";
        public static final String AD_RESULT_TYPE = "type";
        public static final int AD_RESULT_TYPE_BLACK = 2;
        public static final String AD_RESULT_URL = "url";
        public static final String AD_RESULT_VIEW = "views";
        public static final String AD_RESULT_VIEWSTRATEGIES = "viewStrategies";
        public static final String AD_RESULT_VIEW_ID = "viewids";
        public static final String AD_USE_TENCENT = "isUseTencent";
        public static final int OPT_POLICY_DANGEROUS = 1;
        public static final int OPT_POLICY_DROP = 4;
        public static final int OPT_POLICY_NORMAL = 0;
        public static final int OPT_POLICY_SAFE = 3;
        public static final int OPT_POLICY_TIPS = 2;
        public static final int OPT_POLICY_UNKNOW = -1;
    }
}
