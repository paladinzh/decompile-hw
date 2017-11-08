package com.huawei.permissionmanager.utils;

import android.os.SystemProperties;
import android.util.SparseArray;
import com.huawei.systemmanager.comm.misc.Utility;

public class ShareCfg {
    public static final HwPermissionInfo ACCESS_BROWSER_RECORDS = new HwPermissionInfo(ACCESS_BROWSER_RECORDS_INDEX, 1073741824, new String[]{READ_HISTORY_BOOKMARKS_PERMISSION}, false);
    public static final int ACCESS_BROWSER_RECORDS_INDEX;
    public static final HwPermissionInfo ACCESS_CALENDAR = new HwPermissionInfo(READ_CALENDAR_INDEX, 2048, new String[]{CALENDAR_PERMISSION}, false);
    public static final int ADDVIEW_INDEX;
    public static final String APP_LABEL = "AppLabel";
    private static int BASE_INDEX = 0;
    public static final String CALENDAR_PERMISSION = "android.permission.READ_CALENDAR";
    public static final String CALENDAR_WRITE_PERMISSION = "android.permission.WRITE_CALENDAR";
    public static final int CALLLOG_INDEX;
    public static final HwPermissionInfo CALLLOG_INFO = new HwPermissionInfo(CALLLOG_INDEX, 2, new String[]{CALLLOG_RECORD_READ_PERMISSION}, false);
    public static final int CALLLOG_PERMISSION = 2;
    public static final String CALLLOG_RECORD_READ_PERMISSION = "android.permission.READ_CALL_LOG";
    public static final String CALLLOG_RECORD_WRITE_PERMISSION = "android.permission.WRITE_CALL_LOG";
    public static final String CALL_AND_CONT_READ_PERMISSION = "android.permission.READ_CONTACTS";
    public static final String CALL_AND_CONT_WRITE_PERMISSION = "android.permission.WRITE_CONTACTS";
    public static final HwPermissionInfo CALL_FORWARD = new HwPermissionInfo(CALL_FORWARD_INDEX, 1048576, new String[]{CALL_PHONE_PERMISSION}, false);
    public static final int CALL_FORWARD_INDEX;
    public static final int CALL_LISTENER = 128;
    public static final int CALL_LISTENER_INDEX;
    public static final HwPermissionInfo CALL_LISTENER_INFO = new HwPermissionInfo(CALL_LISTENER_INDEX, 128, new String[]{RECORD_AUDIO_PERMISSION}, false);
    public static final int CALL_PHONE = 64;
    public static final int CALL_PHONE_INDEX;
    public static final HwPermissionInfo CALL_PHONE_INFO = new HwPermissionInfo(CALL_PHONE_INDEX, 64, new String[]{CALL_PHONE_PERMISSION}, false);
    public static final String CALL_PHONE_PERMISSION = "android.permission.CALL_PHONE";
    public static final int CAMERA = 1024;
    public static final int CAMERA_INDEX;
    public static final HwPermissionInfo CAMERA_INFO = new HwPermissionInfo(CAMERA_INDEX, 1024, new String[]{CAMERA_PERMISSION}, false);
    public static final String CAMERA_PERMISSION = "android.permission.CAMERA";
    public static final String CHANGE_NETWORK_PERMISSION = "android.permission.MODIFY_PHONE_STATE";
    public static final HwPermissionInfo CHANGE_NETWORK_STATE = new HwPermissionInfo(CHANGE_NETWORK_STATE_INDEX, 4194304, new String[]{CHANGE_NETWORK_PERMISSION}, false);
    public static final int CHANGE_NETWORK_STATE_INDEX;
    public static final String CHANGE_WIFI_PERMISSION = "android.permission.CHANGE_WIFI_STATE";
    public static final HwPermissionInfo CHANGE_WIFI_STATE = new HwPermissionInfo(OPEN_WIFI_INDEX, 2097152, new String[]{CHANGE_WIFI_PERMISSION}, false);
    public static final String CLASS_AUTOSTARTUP_APP = "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity";
    public static final String CLASS_DROPZONE = "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity";
    public static final String CLASS_GOOGLE_BASIC_APP = "com.android.packageinstaller.permission.ui.ManagePermissionsActivity";
    public static final int CONTACTS_INDEX;
    public static final HwPermissionInfo CONTACTS_INFO = new HwPermissionInfo(CONTACTS_INDEX, 1, new String[]{CALL_AND_CONT_READ_PERMISSION}, false);
    public static final int CONTACTS_PERMISSION = 1;
    public static final String DB_LOG_NAME = "logRecord";
    public static final String DB_PERMISSION_CFG_NAME = "permissionCfg";
    public static final int DELETE_MSG_PERMISSION = 524288;
    public static final HwPermissionInfo EDIT_SHORTCUT = new HwPermissionInfo(EDIT_SHORTCUT_INDEX, 16777216, new String[]{INSTALL_SHORTCUT_PERMISSION, UNINSTALL_SHORTCUT_PERMISSION}, false);
    public static final int EDIT_SHORTCUT_INDEX;
    public static final int EMUI5_NEW_TYPES = 1074790400;
    public static final String EXTRA_HIDE_INFO_BUTTON = "hideInfoButton";
    public static final HwPermissionInfo GET_PACKAGE_LIST = new HwPermissionInfo(GET_PACKAGE_LIST_INDEX, 33554432, new String[0], true);
    public static final int GET_PACKAGE_LIST_INDEX;
    public static final String INSTALL_SHORTCUT_PERMISSION = "com.android.launcher.permission.INSTALL_SHORTCUT";
    public static final int INTELLIGENT_NOTIFICATION_ID = 1073741826;
    public static final String KEY_DEFAULT_SMS_APP = "hsm_default_sms_app";
    public static final String LOCATION_COARSE_PERMISSION = "android.permission.ACCESS_COARSE_LOCATION";
    public static final String LOCATION_FINE_PERMISSION = "android.permission.ACCESS_FINE_LOCATION";
    public static final int LOCATION_INDEX;
    public static final HwPermissionInfo LOCATION_INFO = new HwPermissionInfo(LOCATION_INDEX, 8, new String[]{LOCATION_FINE_PERMISSION, LOCATION_COARSE_PERMISSION}, false);
    public static final int LOCATION_PERMISSION = 8;
    public static final int L_MASK = -14680065;
    public static final HwPermissionInfo MODIFY_CALENDAR = new HwPermissionInfo(MODIFY_CALENDAR_INDEX, PERMISSION_MODIFY_CALENDAR, new String[]{CALENDAR_WRITE_PERMISSION}, false);
    public static final int MODIFY_CALENDAR_INDEX;
    public static final int MSG_INDEX;
    public static final HwPermissionInfo MSG_INFO = new HwPermissionInfo(MSG_INDEX, 4, new String[]{MSG_RECORD_READ_PERMISSION}, false);
    public static final int MSG_PERMISSION = 4;
    public static final String MSG_RECORD_READ_PERMISSION = "android.permission.READ_SMS";
    public static final String MSG_RECORD_WRITE_PERMISSION = "android.permission.WRITE_SMS";
    public static final int M_MASK = 1192239104;
    public static final int NOTIFY_REMIND_TYPE = 0;
    public static final boolean NOT_UNIT = false;
    public static final String OPEN_BLUE_TOOTH = "android.permission.BLUETOOTH_ADMIN";
    public static final HwPermissionInfo OPEN_BT = new HwPermissionInfo(OPEN_BT_INDEX, 8388608, new String[]{OPEN_BLUE_TOOTH}, false);
    public static final int OPEN_BT_INDEX;
    public static final int OPEN_WIFI_INDEX;
    @Deprecated
    public static final int OPERATION_TYPE_ALLOWED = 1;
    @Deprecated
    public static final int OPERATION_TYPE_BLOCKED = 2;
    @Deprecated
    public static final int OPERATION_TYPE_REMIND = 0;
    @Deprecated
    public static final int OPERATION_TYPE_UNKNOWN = 3;
    public static final String PACKAGE_INSTALLATION_UNINSTALLATION = "PACKAGE_ADDED_OR_REMOVED";
    public static final int PAY_PROTECTED = 32;
    public static final int PERMISSION_ACCESS_BROWSER_RECORDS = 1073741824;
    public static final int PERMISSION_ADDVIEW = 536870912;
    public static final int PERMISSION_A_CLASS = 268486775;
    public static final int PERMISSION_BLOCK_NOTIFICATION_ID = 1073741825;
    public static final int PERMISSION_BLUETOOTH = 8388608;
    public static final int PERMISSION_B_CLASS = 134218888;
    public static final int PERMISSION_CALENDAR = 2048;
    public static final int PERMISSION_CALL_FORWARD = 1048576;
    public static final int PERMISSION_D_CLASS = 14680064;
    public static final int PERMISSION_D_CLASS_ORIG = 1206919168;
    public static final int PERMISSION_EDIT_SHORTCUT = 16777216;
    public static final int PERMISSION_E_CLASS = 1192239104;
    public static final int PERMISSION_GET_PACKAGE_LIST = 33554432;
    public static final int PERMISSION_GROUP_CALENDAR = 268437504;
    public static final int PERMISSION_GROUP_CONTACT = 16385;
    public static final int PERMISSION_GROUP_MSG = 36;
    public static final int PERMISSION_GROUP_PHONE = 32850;
    public static final int PERMISSION_MMS = 8192;
    public static final int PERMISSION_MOBILEDATE = 4194304;
    public static final int PERMISSION_MODIFY_CALENDAR = 268435456;
    public static final int PERMISSION_READ_CALENDAR = 2048;
    public static final int PERMISSION_STORAGE = 256;
    public static final int PERMISSION_WIFI = 2097152;
    public static final int PHONE_CODE_INDEX;
    public static final HwPermissionInfo PHONE_CODE_INFO = new HwPermissionInfo(PHONE_CODE_INDEX, 16, new String[]{PHONE_STATE_PERMISSION}, false);
    public static final int PHONE_CODE_PERMISSION = 16;
    public static final String PHONE_STATE_PERMISSION = "android.permission.READ_PHONE_STATE";
    public static final int PRIVACYACCESS_NOTIFICATION = 1073741824;
    public static final int READ_CALENDAR_INDEX;
    public static final int READ_HEALTH_DATA_INDEX;
    public static final String READ_HISTORY_BOOKMARKS_PERMISSION = "com.android.browser.permission.READ_HISTORY_BOOKMARKS";
    public static final int READ_MOTION_DATA_INDEX;
    public static final String READ_STORAGE_PERMISSION = "android.permission.READ_EXTERNAL_STORAGE";
    public static final String RECORD_AUDIO_PERMISSION = "android.permission.RECORD_AUDIO";
    public static final HwPermissionInfo RHD_HW_PERMISSION_INFO = new HwPermissionInfo(READ_HEALTH_DATA_INDEX, 134217728, new String[]{USE_BODY_SENSORS}, true);
    public static final int RHD_PERMISSION_CODE = 134217728;
    public static final HwPermissionInfo RMD_HW_PERMISSION_INFO = new HwPermissionInfo(READ_MOTION_DATA_INDEX, 67108864, new String[]{RMD_PERMISSION_STRING}, true);
    public static final int RMD_PERMISSION_CODE = 67108864;
    public static final String RMD_PERMISSION_STRING = "huawei.permission.READ_MOTION_DATA";
    public static final int SEND_GROUP_MMS = 1001;
    public static final int SEND_GROUP_SMS = 1000;
    public static final int SEND_MMS = 8192;
    public static final int SEND_MMS_INDEX;
    public static final HwPermissionInfo SEND_MMS_INFO = new HwPermissionInfo(SEND_MMS_INDEX, 8192, new String[]{SEND_SHORT_MESSAGE_PERMISSION}, false);
    public static final String SEND_SHORT_MESSAGE_PERMISSION = "android.permission.SEND_SMS";
    public static final int SEND_SMS = 32;
    public static final int SEND_SMS_INDEX;
    public static final HwPermissionInfo SEND_SMS_INFO = new HwPermissionInfo(SEND_SMS_INDEX, 32, new String[]{SEND_SHORT_MESSAGE_PERMISSION}, false);
    public static final String SINGLE_APP_LABEL = "SingleAppLabel";
    public static final String SINGLE_APP_PKGNAME = "SinglePkgName";
    public static final String SINGLE_APP_UID = "SingleAppUid";
    public static final int SONUD_RECORDER = 128;
    public static final int SPINNER_ALLOW_POSITION = 0;
    public static final int SPINNER_PROHIBIT_POSITION = 2;
    public static final int SPINNER_REMIND_POSITION = 1;
    public static final int SPINNER_UNKNOWN_POSITION = 3;
    public static final HwPermissionInfo STORAGE = new HwPermissionInfo(STORAGE_INDEX, 256, new String[]{READ_STORAGE_PERMISSION, WRITE_STORAGE_PERMISSION}, false);
    public static final int STORAGE_INDEX;
    public static final int TRUST_CODE = 1594944767;
    public static final int TRUST_SOFTWARE = 1;
    public static final String UNINSTALL_SHORTCUT_PERMISSION = "com.android.launcher.permission.UNINSTALL_SHORTCUT";
    public static final int UNTRUST_SOFTWARE = 0;
    public static final String USE_BODY_SENSORS = "android.permission.BODY_SENSORS";
    public static final int WRITE_CALLLOG_INDEX;
    public static final HwPermissionInfo WRITE_CALLLOG_INFO = new HwPermissionInfo(WRITE_CALLLOG_INDEX, 32768, new String[]{CALLLOG_RECORD_WRITE_PERMISSION}, false);
    public static final int WRITE_CALLLOG_PERMISSION = 32768;
    public static final int WRITE_CONTACTS_INDEX;
    public static final HwPermissionInfo WRITE_CONTACTS_INFO = new HwPermissionInfo(WRITE_CONTACTS_INDEX, 16384, new String[]{CALL_AND_CONT_WRITE_PERMISSION}, false);
    public static final int WRITE_CONTACTS_PERMISSION = 16384;
    public static final int WRITE_MSG_PERMISSION = 65536;
    public static final String WRITE_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final boolean isControl = SystemProperties.getBoolean("ro.config.hw_wirenetcontrol", true);
    public static final SparseArray<Integer> userOperation2Value = new SparseArray();
    public static final SparseArray<Integer> value2UserOperation = new SparseArray();

    static {
        BASE_INDEX = 1;
        int i = BASE_INDEX;
        BASE_INDEX = i + 1;
        MSG_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        SEND_SMS_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        SEND_MMS_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        CALL_PHONE_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        CALLLOG_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        WRITE_CALLLOG_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        PHONE_CODE_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        CONTACTS_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        WRITE_CONTACTS_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        READ_CALENDAR_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        MODIFY_CALENDAR_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        CAMERA_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        CALL_LISTENER_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        READ_HEALTH_DATA_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        LOCATION_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        STORAGE_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        CALL_FORWARD_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        READ_MOTION_DATA_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        ACCESS_BROWSER_RECORDS_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        GET_PACKAGE_LIST_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        EDIT_SHORTCUT_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        CHANGE_NETWORK_STATE_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        OPEN_WIFI_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        OPEN_BT_INDEX = i;
        i = BASE_INDEX;
        BASE_INDEX = i + 1;
        ADDVIEW_INDEX = i;
        userOperation2Value.put(1, Integer.valueOf(1));
        userOperation2Value.put(2, Integer.valueOf(2));
        userOperation2Value.put(0, Integer.valueOf(1));
        value2UserOperation.put(1, Integer.valueOf(1));
        value2UserOperation.put(2, Integer.valueOf(2));
        value2UserOperation.put(3, Integer.valueOf(1));
        value2UserOperation.put(0, Integer.valueOf(1));
    }

    public static boolean isPermissionFrozen(int permission) {
        switch (permission) {
            case 2:
            case 64:
            case 32768:
                break;
            case 4:
            case 32:
            case 8192:
            case 65536:
            case 524288:
            case 4194304:
                if (Utility.isDataOnlyMode()) {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }
}
