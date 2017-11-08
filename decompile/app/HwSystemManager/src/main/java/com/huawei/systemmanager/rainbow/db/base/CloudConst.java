package com.huawei.systemmanager.rainbow.db.base;

import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

public class CloudConst {
    private static final String ADDVIEW_PERMISSION = "addViewPermission";
    private static final String ALLOW_STATUS = "true";
    public static final String AUTHORITY = "com.huawei.systemmanager.rainbow.rainbowprovider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/");
    private static final String BACKGROUND_PERMISSION = "backgroundPermission";
    private static final String BACKSTARTUP_PERMISSION = "backStartupPermission";
    private static final String BOOTSTARTUP_PERMISSION = "bootStartupPermission";
    private static final String FORBID_STATUS = "false";
    private static final String GETAPPLIST_PERMISSION = "getapplistPermission";
    private static final String NETWORK_DATA_PERMISSION = "netDataPermission";
    private static final String NETWORK_WIFI_PERMISSION = "netWifiPermission";
    private static final String NOTIFICATION_PERMISSION = "notifticationPermission";
    private static final String PACKAGE_MD5 = "packageMD5";
    private static final String PACKAGE_NAME = "packageName";
    private static final String PACKAGE_VERSION = "packageVersion";
    private static final String PERMISSION_CFG = "permissionCfg";
    private static final String PERMISSION_CODE = "permissionCode";
    private static final String PERMISSION_TRUST = "trust";
    private static final String PERMISSION_TYPE = "type";
    public static final String QUERY_NAME_VERSION_MATCH = "packageName = ? AND packageVersion = ? ";
    public static final String VALUE_2 = "2";
    public static final String VALUE_FALSE = "1";
    public static final String VALUE_NOT_FOUND = "not found";
    public static final String VALUE_TRUE = "0";
    private static List<String> mCommonFeature = null;
    private static List<CommonFeatureColumn> mCommonFeatureColumn = null;
    private static List<PermissionColumn> mPermissionColumnList = null;
    private static List<String> mPermissionNameList = null;

    public interface AddViewValues {
        public static final String ADDVIEW_PERMISSION = "permissionCfg";
        public static final Uri CONTENT_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/addviewOuterTable");
        public static final String OUTERTABLE_NAME = "addviewOuterTable";
        public static final String PACKAGE_NAME = "packageName";
        public static final int PERMISSION_ALLOW_VALUE = 0;
        public static final int PERMISSION_FORBID_VALUE = 1;
        public static final String TABLE_NAME = "addviewTable";
    }

    public interface BackgroundValues {
        public static final String ALLOW_STATUS = "true";
        public static final String COL_IS_CONTROLLED = "isControlled";
        public static final String COL_IS_KEY_TASK = "isKeyTask";
        public static final String COL_IS_PROTECTED = "isProtected";
        public static final Uri CONTENT_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/backgroundTable");
        public static final Uri CONTENT_URI = Uri.parse("content://com.huawei.android.batteryspriteprovider/backgroundwhiteapps");
        public static final String LOCAL_TABLE_NAME = "backgroundwhiteapps";
        public static final String OUTERTABLE_NAME = "backgroundTable";
        public static final String PACKAGENAME = "packageName";
        public static final String PACKAGE_NAME = "package_name";
        public static final int PERMISSION_ALLOW_VALUE = 0;
        public static final int PERMISSION_FORBID_VALUE = 1;
        public static final String TABLE_NAME = "backgroundCloudTable";
        public static final String XML_PATH = "cloud/config/background.xml";
    }

    public interface BackstartupValues {
        public static final String TABLE_NAME = "backstartupTable";
    }

    public interface BootstartupValues {
        public static final String ALLOW_STATUS = "true";
        public static final String BOOTSTARTUP_PERMISSION = "permissionCfg";
        public static final Uri CONTENT_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/bootstartupOuterTable");
        public static final Uri CONTENT_VIEW_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/bootstartupTable");
        public static final String OUTERTABLE_NAME = "bootstartupOuterTable";
        public static final String PACKAGE_MD5 = "packageMD5";
        public static final String PACKAGE_NAME = "packageName";
        public static final String PACKAGE_VERSION = "packageVersion";
        public static final int PERMISSION_ALLOW_VALUE = 0;
        public static final int PERMISSION_FORBID_VALUE = 1;
        public static final String TABLE_NAME = "bootstartupTable";
    }

    public interface CloudCommonValue {
        public static final String ADDVIEW_DEFALUT_VALUE_COL_NAME = "addviewDefaultValue";
        public static final String ADDVIEW_OUTERVIEW_NAME = "addviewOuterView";
        public static final Uri ADDVIEW_OUTERVIEW_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/addviewOuterView");
        public static final String BOOTSTARTUP_DEFALUT_VALUE_COL_NAME = "bootstartupDefaultValue";
        public static final String BOOTSTARTUP_OUTERVIEW_NAME = "bootstartupOuterView";
        public static final Uri BOOTSTARTUP_OUTERVIEW_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/bootstartupOuterView");
        public static final String COMMON_COLUMN_ADDVIEW = "ADDVIEW_COL";
        public static final String COMMON_COLUMN_BOOTSTARTUP = "BOOTSTARTUP_COL";
        public static final String COMMON_COLUMN_GET_APPLIST = "GET_APPLIST_COL";
        public static final String COMMON_COLUMN_NOTIFICATION_SIGNAL = "NOTIFICATION_SIGNAL_COL";
        public static final String COMMON_COLUMN_SEND_NOTIFICATION = "SEND_NOTIFICATION_COL";
        public static final String COMMON_VIEW_ADDVIEW = "6";
        public static final String COMMON_VIEW_BOOTSTARTUP = "5";
        public static final String COMMON_VIEW_GET_APPLIST = "4";
        public static final String COMMON_VIEW_NOTIFICATION_SIGNAL = "7";
        public static final String COMMON_VIEW_SEND_NOTIFICATION = "3";
        public static final String GET_APPLIST_DEFALUT_VALUE_COL_NAME = "getapplistDefaultValue";
        public static final String GET_APPLIST_OUTERVIEW_NAME = "getapplistOuterView";
        public static final Uri GET_APPLIST_OUTERVIEW_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/getapplistOuterView");
        public static final String NOTIFICATION_SIGNAL_DEFALUT_VALUE_COL_NAME = "notificationSignalDefaultValue";
        public static final String NOTIFICATION_SIGNAL_OUTERVIEW_NAME = "notificationSignalOuterView";
        public static final Uri NOTIFICATION_SIGNAL_OUTERVIEW_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/notificationSignalOuterView");
        public static final String SEND_NOTIFICATION_DEFALUT_VALUE_COL_NAME = "sendNotificationDefaultValue";
        public static final String SEND_NOTIFICATION_OUTERVIEW_NAME = "sendNotificationOuterView";
        public static final Uri SEND_NOTIFICATION_OUTERVIEW_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/sendNotificationOuterView");
    }

    public interface CloudOuterValues {
        public static final String ADDVIEW_COLUMN_NAME = "addviewDefaultValue";
        public static final String BOOTSTARTUP_COLUMN_NAME = "bootstartupDefaultValue";
        public static final String GETAPPLIST_COLUMN_NAME = "getapplistDefaultValue";
        public static final String NETWORK_DATA_COLUMN_NAME = "netDataPermission";
        public static final String NETWORK_WIFI_COLUMN_NAME = "netWifiPermission";
        public static final String NOTIFICATION_SIGNAL_COLUMN_NAME = "notificationSignalDefaultValue";
        public static final String PACKAGE_NAME_COLUMN_NAME = "packageName";
        public static final String PERMISSION_CFG_COLUMN_NAME = "permissionCfg";
        public static final String PERMISSION_CODE_COLUMN_NAME = "permissionCode";
        public static final String PERMISSION_TRUST_COLUMN_NAME = "trust";
        public static final String SEND_NOTIFICATION_COLUMN_NAME = "sendNotificationDefaultValue";
    }

    public interface CloudVagueValues {
        public static final Uri PERMISSION_FEATURE_CONTENT_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/CloudVaguePermission");
        public static final String PERMISSION_FEATURE_TABLE_NAME = "CloudVaguePermission";
        public static final String PERMISSION_INNER_VIEW_NAME = "VaguePermissionFeatureInnerView";
        public static final Uri PERMISSION_OUTERVIEW_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/VaguePermissionOuterView");
        public static final String PERMISSION_OUTER_VIEW_NAME = "VaguePermissionOuterView";
        public static final String PERMISSION_REAL_TABLE_PREFIX = "CloudVaguePermission";
        public static final String PERMISSION_TEMP_FEATURE_VIEW_PREFIX = "VaguePermission";
    }

    public interface CloudValues {
        public static final String ADDVIEW_PERMISSION = "addViewPermission";
        public static final String BACKGROUND_PERMISSION = "backgroundPermission";
        public static final String BACKSTARTUP_PERMISSION = "backStartupPermission";
        public static final String BOOTSTARTUP_PERMISSION = "bootStartupPermission";
        public static final String CLOUD_SETTINGS_ID = "_id";
        public static final String CLOUD_SETTINGS_TABLE = "cloudSettingsCfg";
        public static final Uri CONTENT_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/cloudSettingsCfg");
        public static final String GETAPPLIST_PERMISSION = "getapplistPermission";
        public static final String NETWORK_DATA_PERMISSION = "netDataPermission";
        public static final String NETWORK_WIFI_PERMISSION = "netWifiPermission";
        public static final String NOTIFICATION_PERMISSION = "notifticationPermission";
        public static final String PACKAGE_MD5 = "packageMD5";
        public static final String PACKAGE_NAME = "packageName";
        public static final String PACKAGE_VERSION = "packageVersion";
        public static final String PERMISSION_CFG = "permissionCfg";
        public static final String PERMISSION_CODE = "permissionCode";
        public static final String PERMISSION_TRUST = "trust";
        public static final String PERMISSION_TYPE = "type";
    }

    public interface CompetitorConfigFile {
        public static final String BLACK_LIST_CHANGE_ACTION = "com.rainbow.blacklist.change";
        public static final Uri CONTENT_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/competitorConfigTable");
        public static final String OUTERTABLE_NAME = "competitorConfigTable";
        public static final String PACKAGE_NAME = "packageName";
        public static final String XML_PATH = "cloud/config/competitor.xml";
    }

    public interface ControlRangeBlackList {
        public static final String BLACK_LIST_CHANGE_ACTION = "com.rainbow.blacklist.change";
        public static final Uri CONTENT_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/controlRangeBlackTable");
        public static final String OUTERTABLE_NAME = "controlRangeBlackTable";
        public static final String PACKAGE_NAME = "packageName";
    }

    public interface ControlRangeWhiteList {
        public static final String CHANGED_ADD_LIST_KEY = "changed_add_pkglist_key";
        public static final String CHANGED_MINUS_LIST_KEY = "changed_minus_pkglist_key";
        public static final Uri CONTENT_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/controlRangeWhiteTable");
        public static final String OUTERTABLE_NAME = "controlRangeWhiteTable";
        public static final String PACKAGE_NAME = "packageName";
        public static final String WHITE_LIST_CHANGE_ACTION = "com.rainbow.whitelist.change";
    }

    public interface DefaultConfigureValue {
        public static final String COLUMN_APPLIST = "applist";
        public static final String COLUMN_DATA_ALLOW = "allowData";
        public static final String COLUMN_NOTIFICATION = "notification";
        public static final String COLUMN_PERMISSION_CFG = "permissionCfg";
        public static final String COLUMN_PERMISSION_CODE = "permissionCode";
        public static final String COLUMN_WIFI_ALLOW = "allowWifi";
        public static final Uri CONTENT_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/defaultPermissionCfg");
        public static final String TABLE_NAME = "defaultPermissionCfg";
    }

    public interface GetapplistValues {
        public static final Uri CONTENT_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/getapplistOuterTable");
        public static final Uri CONTENT_VIEW_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/getapplistView");
        public static final String GETAPPLIST_PERMISSION = "permissionCfg";
        public static final String OUTERTABLE_NAME = "getapplistOuterTable";
        public static final String PACKAGE_NAME = "packageName";
        public static final int PERMISSION_ALLOW_VALUE = 0;
        public static final int PERMISSION_FORBID_VALUE = 1;
        public static final int PERMISSION_REMIND_VALUE = 2;
        public static final String VIEW_NAME = "getapplistView";
    }

    public interface MessageSafeConfigFile {
        public static final String BLACK_LIST_CHANGE_ACTION = "com.rainbow.blacklist.change";
        public static final String COL_MESSAGE_NUMBER = "messageNo";
        public static final String COL_PARTNER = "partner";
        public static final String COL_SECURE_LINK = "secureLink";
        public static final String COL_UPDATE_STATUS = "status";
        public static final Uri CONTENT_OUTERTABLE_LINK_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/messageSafeLinkConfigTable");
        public static final Uri CONTENT_OUTERTABLE_NUMBER_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/messageSafeNumberConfigTable");
        public static final Uri CONTENT_OUTERVIEW_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/vMessageSafe");
        public static final String LINK_OUTERTABLE_NAME = "messageSafeLinkConfigTable";
        public static final String NUMBER_OUTERTABLE_NAME = "messageSafeNumberConfigTable";
        public static final String OUTERVIEW_NAME = "vMessageSafe";
        public static final String XML_PATH = "cloud/config/messageSafe.xml";
    }

    public interface NetworkValues {
        public static final String ALLOW_STATUS = "true";
        public static final Uri CONTENT_VIEW_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/networkView");
        public static final String FORBID_STATUS = "false";
        public static final String NETWORK_COLUMN_DATA = "NETWORK_DATA_COL";
        public static final String NETWORK_COLUMN_WIFI = "NETWORK_WIFI_COL";
        public static final String NETWORK_DATA_PERMISSION = "netDataPermission";
        public static final String NETWORK_VIEW_DATA = "2";
        public static final String NETWORK_VIEW_WIFI = "1";
        public static final String NETWORK_WIFI_PERMISSION = "netWifiPermission";
        public static final String OUTER_TABLE_NAME = "networkOuterTable";
        public static final Uri OUTER_TABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/networkOuterTable");
        public static final String OUTER_VIEW_NAME = "networkOuterView";
        public static final Uri OUTER_VIEW_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/networkOuterView");
        public static final String PACKAGE_MD5 = "packageMD5";
        public static final String PACKAGE_NAME = "packageName";
        public static final String PACKAGE_VERSION = "packageVersion";
        public static final String VIEW_NAME = "networkView";
    }

    public interface NotificationConfigFile {
        public static final String BLACK_LIST_CHANGE_ACTION = "com.rainbow.blacklist.change";
        public static final String COL_CAN_FORBIDDEN = "canForbidden";
        public static final String COL_HEADSUB = "headsubCfg";
        public static final String COL_IS_CONTROLLED = "isControlled";
        public static final String COL_LOCKSCREEN = "lockscreenCfg";
        public static final String COL_NOTIFICATION = "notificationCfg";
        public static final String COL_STATUSBAR = "statusbarCfg";
        public static final Uri CONTENT_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/notificationConfigTable");
        public static final String OUTERTABLE_NAME = "notificationConfigTable";
        public static final String PACKAGE_NAME = "packageName";
        public static final String XML_PATH = "cloud/config/notification.xml";
    }

    public interface NotificationExValues {
        public static final Uri CONTENT_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/notificationExOuterTable");
        public static final String NOTIFICATIONEX_PERMISSION = "permissionCfg";
        public static final String OUTERTABLE_NAME = "notificationExOuterTable";
        public static final String PACKAGE_NAME = "packageName";
        public static final int PERMISSION_ALLOW_VALUE = 0;
        public static final int PERMISSION_FORBID_VALUE = 1;
        public static final int PERMISSION_REMIND_VALUE = 2;
    }

    public interface NotificationTip {
        public static final Uri CONTENT_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/notificationTipTable");
        public static final int NEED_SEND_TIPS = 0;
        public static final int NONEED_SEND_TIPS = 1;
        public static final String NOTIFICATION_TIP_STATUS = "notificationTipStatus";
        public static final String OUTERTABLE_NAME = "notificationTipTable";
        public static final String PACKAGE_NAME = "packageName";
    }

    public interface NotificationValues {
        public static final Uri CONTENT_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/notificationOuterTable");
        public static final Uri CONTENT_VIEW_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/notificationView");
        public static final String NOTIFICATION_PERMISSION = "permissionCfg";
        public static final String OUTERTABLE_NAME = "notificationOuterTable";
        public static final String PACKAGE_NAME = "packageName";
        public static final int PERMISSION_ALLOW_VALUE = 0;
        public static final int PERMISSION_FORBID_VALUE = 1;
        public static final int PERMISSION_REMIND_VALUE = 2;
        public static final String VIEW_NAME = "notificationView";
    }

    public interface PermissionValues {
        public static final Uri CONTENT_URI = Uri.parse("content://com.huawei.permissionmanager.provider.PermissionDataProvider/prePermission");
        public static final Uri CONTENT_VIEW_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/permissionView");
        public static final String LOCAL_TABLE_NAME = "prePermissionCfg";
        public static final String PACKAGE_NAME = "packageName";
        public static final String PERMISSION_CFG = "permissionCfg";
        public static final String PERMISSION_CODE = "permissionCode";
        public static final String PERMISSION_COLUMN_DELETE_CALLLOG = "PERMISSION_DELETE_CALLLOG_COL";
        public static final String PERMISSION_COLUMN_DELETE_CONTACT = "PERMISSION_DELETE_CONTACT_COL";
        public static final String PERMISSION_COLUMN_DELETE_MESSAGE = "PERMISSION_DELETE_MESSAGE_COL";
        public static final String PERMISSION_COLUMN_EDIT_SHORTCUT = "PERMISSION_EDIT_SHORTCUT_COL";
        public static final String PERMISSION_COLUMN_MAKE_CALL = "PERMISSION_MAKE_CALL_COL";
        public static final String PERMISSION_COLUMN_MODIFY_CALLLOG = "PERMISSION_MODIFY_CALLLOG_COL";
        public static final String PERMISSION_COLUMN_MODIFY_CONTACT = "PERMISSION_MODIFY_CONTACT_COL";
        public static final String PERMISSION_COLUMN_MODIFY_MESSAGE = "PERMISSION_MODIFY_MESSAGE_COL";
        public static final String PERMISSION_COLUMN_OPEN_BT = "PERMISSION_OPEN_BT_COL";
        public static final String PERMISSION_COLUMN_OPEN_DATA = "PERMISSION_OPEN_DATA_COL";
        public static final String PERMISSION_COLUMN_OPEN_WIFI = "PERMISSION_OPEN_WIFI_COL";
        public static final String PERMISSION_COLUMN_READ_APPLIST = "PERMISSION_READ_APPLIST_COL";
        public static final String PERMISSION_COLUMN_READ_CALENDAR = "PERMISSION_READ_CALENDAR_COL";
        public static final String PERMISSION_COLUMN_READ_CALLLOG = "PERMISSION_READ_CALLLOG_COL";
        public static final String PERMISSION_COLUMN_READ_CONTACT = "PERMISSION_READ_CONTACT_COL";
        public static final String PERMISSION_COLUMN_READ_LOCATION = "PERMISSION_READ_LOCATION_COL";
        public static final String PERMISSION_COLUMN_READ_MESSAGE = "PERMISSION_READ_MESSAGE_COL";
        public static final String PERMISSION_COLUMN_READ_PHONE_CODE = "PERMISSION_READ_PHONE_CODE_COL";
        public static final String PERMISSION_COLUMN_RHD = "PERMISSION_RHD_COL";
        public static final String PERMISSION_COLUMN_RMD = "PERMISSION_RMD_COL";
        public static final String PERMISSION_COLUMN_SEND_MMS = "PERMISSION_SEND_MMS_COL";
        public static final String PERMISSION_COLUMN_SEND_SMS = "PERMISSION_SEND_SMS_COL";
        public static final String PERMISSION_COLUMN_SOUND_RECORDER = "PERMISSION_SOUND_RECORDER_COL";
        public static final String PERMISSION_COLUMN_TAKE_PHOTO = "PERMISSION_TAKE_PHOTO_COL";
        public static final String PERMISSION_COLUMN_TRUST = "PERMISSION_TRUST_COL";
        public static final Uri PERMISSION_FEATURE_CONTENT_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/CloudPermission");
        public static final String PERMISSION_FEATURE_TABLE_NAME = "CloudPermission";
        public static final String PERMISSION_INNER_VIEW_NAME = "PermissionFeatureInnerView";
        public static final Uri PERMISSION_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/PermissionOuterTable");
        public static final Uri PERMISSION_OUTERVIEW_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/PermissionOuterView");
        public static final String PERMISSION_OUTER_TABLE_NAME = "PermissionOuterTable";
        public static final String PERMISSION_OUTER_VIEW_NAME = "PermissionOuterView";
        public static final String PERMISSION_REAL_TABLE_PREFIX = "CloudPermission";
        public static final String PERMISSION_TEMP_FEATURE_VIEW_PREFIX = "Permission";
        public static final String PERMISSION_TRUST = "trust";
        public static final String PERMISSION_TRUST_VALUE = "true";
        public static final String PERMISSION_TYPE = "type";
        public static final String PERMISSION_UNTRUST_VALUE = "false";
        public static final String PERMISSION_VIEW_DELETE_CALLLOG = "22";
        public static final String PERMISSION_VIEW_DELETE_CONTACT = "20";
        public static final String PERMISSION_VIEW_DELETE_MESSAGE = "21";
        public static final String PERMISSION_VIEW_EDIT_SHORTCUT = "31";
        public static final String PERMISSION_VIEW_MAKE_CALL = "23";
        public static final String PERMISSION_VIEW_MODIFY_CALLLOG = "19";
        public static final String PERMISSION_VIEW_MODIFY_CONTACT = "17";
        public static final String PERMISSION_VIEW_MODIFY_MESSAGE = "18";
        public static final String PERMISSION_VIEW_OPEN_BT = "30";
        public static final String PERMISSION_VIEW_OPEN_DATA = "29";
        public static final String PERMISSION_VIEW_OPEN_WIFI = "28";
        public static final String PERMISSION_VIEW_READ_APPLIST = "32";
        public static final String PERMISSION_VIEW_READ_CALENDAR = "14";
        public static final String PERMISSION_VIEW_READ_CALLLOG = "13";
        public static final String PERMISSION_VIEW_READ_CONTACT = "11";
        public static final String PERMISSION_VIEW_READ_LOCATION = "15";
        public static final String PERMISSION_VIEW_READ_MESSAGE = "12";
        public static final String PERMISSION_VIEW_READ_PHONE_CODE = "16";
        public static final String PERMISSION_VIEW_RHD = "34";
        public static final String PERMISSION_VIEW_RMD = "33";
        public static final String PERMISSION_VIEW_SEND_MMS = "25";
        public static final String PERMISSION_VIEW_SEND_SMS = "24";
        public static final String PERMISSION_VIEW_SOUND_RECORDER = "27";
        public static final String PERMISSION_VIEW_TAKE_PHOTO = "26";
        public static final String PERMISSION_VIEW_TRUST = "PERMISSION_TRUST";
        public static final String VIEW_NAME = "permissionView";
    }

    public interface PhoneNumberList {
        public static final Uri CONTENT_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/phoneNumberTable");
        public static final String OUTERTABLE_NAME = "phoneNumberTable";
        public static final String PACKAGE_NAME = "packageName";
    }

    public interface PushBlackList {
        public static final Uri CONTENT_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/pushBlackTable");
        public static final String OUTERTABLE_NAME = "pushBlackTable";
        public static final String PACKAGE_NAME = "packageName";
    }

    public interface SecurityBlackList {
        public static final Uri CONTENT_VIEW_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/securityBlackTable");
        public static final String TABLE_NAME = "securityBlackTable";
    }

    public interface StartupConfigFile {
        public static final String BLACK_LIST_CHANGE_ACTION = "com.rainbow.blacklist.change";
        public static final String COL_IS_CONTROLLED = "isControlled";
        public static final String COL_RECEIVER = "receiver";
        public static final String COL_SERVICE_PROVIDER = "serviceProvider";
        public static final Uri CONTENT_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/startupConfigTable");
        public static final String OUTERTABLE_NAME = "startupConfigTable";
        public static final String PACKAGE_NAME = "packageName";
        public static final int PERMISSION_ALLOW_VALUE = 0;
        public static final int PERMISSION_FORBID_VALUE = 1;
        public static final String XML_PATH = "cloud/config/startup.xml";
    }

    public interface UnifiedPowerAppsConfigConfigFile {
        public static final String BLACK_LIST_CHANGE_ACTION = "com.rainbow.blacklist.change";
        public static final String COL_IS_PROTECTED = "isProtected";
        public static final String COL_IS_SHOW = "isShow";
        public static final Uri CONTENT_OUTERTABLE_URI = Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider/unifiedPowerAppsConfigTable");
        public static final String OUTERTABLE_NAME = "unifiedPowerAppsConfigTable";
        public static final String PACKAGE_NAME = "packageName";
        public static final String XML_PATH = "cloud/config/unifiedPowerApps.xml";
    }

    public static synchronized List<CommonFeatureColumn> getCommonFeatureColumnList() {
        synchronized (CloudConst.class) {
            if (mCommonFeatureColumn != null) {
                List<CommonFeatureColumn> list = mCommonFeatureColumn;
                return list;
            }
            mCommonFeatureColumn = new ArrayList();
            mCommonFeatureColumn.add(new CommonFeatureColumn(CloudCommonValue.COMMON_COLUMN_BOOTSTARTUP, "5", CloudCommonValue.BOOTSTARTUP_OUTERVIEW_NAME, "bootstartupDefaultValue"));
            mCommonFeatureColumn.add(new CommonFeatureColumn(CloudCommonValue.COMMON_COLUMN_ADDVIEW, "6", CloudCommonValue.ADDVIEW_OUTERVIEW_NAME, "addviewDefaultValue"));
            mCommonFeatureColumn.add(new CommonFeatureColumn(CloudCommonValue.COMMON_COLUMN_SEND_NOTIFICATION, "3", CloudCommonValue.SEND_NOTIFICATION_OUTERVIEW_NAME, "sendNotificationDefaultValue"));
            mCommonFeatureColumn.add(new CommonFeatureColumn(CloudCommonValue.COMMON_COLUMN_NOTIFICATION_SIGNAL, "7", CloudCommonValue.NOTIFICATION_SIGNAL_OUTERVIEW_NAME, "notificationSignalDefaultValue"));
            mCommonFeatureColumn.add(new CommonFeatureColumn(CloudCommonValue.COMMON_COLUMN_GET_APPLIST, "4", CloudCommonValue.GET_APPLIST_OUTERVIEW_NAME, "getapplistDefaultValue"));
            list = mCommonFeatureColumn;
            return list;
        }
    }

    public static synchronized List<String> getCommonFeatureList() {
        synchronized (CloudConst.class) {
            if (mCommonFeature != null) {
                List<String> list = mCommonFeature;
                return list;
            }
            mCommonFeature = new ArrayList();
            mCommonFeature.add("5");
            mCommonFeature.add("6");
            mCommonFeature.add("3");
            mCommonFeature.add("7");
            mCommonFeature.add("4");
            list = mCommonFeature;
            return list;
        }
    }

    public static synchronized List<PermissionColumn> getPermissionColumnList() {
        synchronized (CloudConst.class) {
            if (mPermissionColumnList != null) {
                List<PermissionColumn> list = mPermissionColumnList;
                return list;
            }
            mPermissionColumnList = new ArrayList();
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_READ_CONTACT, "11", 1));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_READ_MESSAGE, "12", 4));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_READ_CALLLOG, "13", 2));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_READ_CALENDAR, "14", 2048));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_READ_LOCATION, "15", 8));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_READ_PHONE_CODE, "16", 16));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_MODIFY_CONTACT, "17", 16384));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_MODIFY_MESSAGE, "18", 65536));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_MODIFY_CALLLOG, "19", 32768));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_DELETE_CONTACT, "20", 131072));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_DELETE_MESSAGE, "21", 524288));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_DELETE_CALLLOG, "22", 262144));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_MAKE_CALL, "23", 64));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_SEND_SMS, "24", 32));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_SEND_MMS, "25", 8192));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_TAKE_PHOTO, "26", 1024));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_SOUND_RECORDER, "27", 128));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_OPEN_BT, "30", 8388608));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_EDIT_SHORTCUT, "31", 16777216));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_READ_APPLIST, "32", 33554432));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_RMD, "33", 67108864));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_RHD, "34", 134217728));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_OPEN_WIFI, "28", 2097152));
            mPermissionColumnList.add(new PermissionColumn(PermissionValues.PERMISSION_COLUMN_OPEN_DATA, "29", 4194304));
            list = mPermissionColumnList;
            return list;
        }
    }

    public static synchronized List<String> getPermissionNameList() {
        synchronized (CloudConst.class) {
            if (mPermissionNameList != null) {
                List<String> list = mPermissionNameList;
                return list;
            }
            mPermissionNameList = new ArrayList();
            mPermissionNameList.add("11");
            mPermissionNameList.add("12");
            mPermissionNameList.add("13");
            mPermissionNameList.add("14");
            mPermissionNameList.add("15");
            mPermissionNameList.add("16");
            mPermissionNameList.add("17");
            mPermissionNameList.add("18");
            mPermissionNameList.add("19");
            mPermissionNameList.add("20");
            mPermissionNameList.add("21");
            mPermissionNameList.add("22");
            mPermissionNameList.add("23");
            mPermissionNameList.add("24");
            mPermissionNameList.add("25");
            mPermissionNameList.add("26");
            mPermissionNameList.add("27");
            mPermissionNameList.add("30");
            mPermissionNameList.add("28");
            mPermissionNameList.add("29");
            mPermissionNameList.add("31");
            mPermissionNameList.add("32");
            mPermissionNameList.add("33");
            mPermissionNameList.add("34");
            list = mPermissionNameList;
            return list;
        }
    }
}
