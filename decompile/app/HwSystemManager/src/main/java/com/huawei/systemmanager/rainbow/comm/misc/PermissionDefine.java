package com.huawei.systemmanager.rainbow.comm.misc;

import com.huawei.systemmanager.rainbow.db.base.CloudConst.PermissionValues;
import java.util.HashMap;
import java.util.Map;

public class PermissionDefine {
    public static final String ADD_VIEW = "6";
    public static final String APP_TRUST = "isT";
    public static final String BOOT_STARTUP = "5";
    public static final String DATA_NETWORK = "2";
    public static final String DELETE_CALLLOG = "22";
    public static final int DELETE_CALLLOG_BIT_INDEX = 262144;
    public static final String DELETE_CONTACT = "20";
    public static final int DELETE_CONTACT_BIT_INDEX = 131072;
    public static final String DELETE_MESSAGE = "21";
    public static final int DELETE_MESSAGE_BIT_INDEX = 524288;
    public static final String EDIT_SHORTCUT = "31";
    public static final int EDIT_SHORTCUT_BIT_INDEX = 16777216;
    public static final String GET_APPLIST = "4";
    public static final String MAKE_CALL = "23";
    public static final int MAKE_CALL_BIT_INDEX = 64;
    public static final String MODIFY_CALLLOG = "19";
    public static final int MODIFY_CALLLOG_BIT_INDEX = 32768;
    public static final String MODIFY_CONTACT = "17";
    public static final int MODIFY_CONTACT_BIT_INDEX = 16384;
    public static final String MODIFY_MESSAGE = "18";
    public static final int MODIFY_MESSAGE_BIT_INDEX = 65536;
    public static final String NETWORK_ALLOW = "true";
    public static final String NETWORK_FORBID = "false";
    public static final String NOTIFICATION_SIGNAL = "7";
    public static final String OPEN_BT = "30";
    public static final int OPEN_BT_BIT_INDEX = 8388608;
    public static final String OPEN_DATA = "29";
    public static final int OPEN_DATA_BIT_INDEX = 4194304;
    public static final String OPEN_WIFI = "28";
    public static final int OPEN_WIFI_BIT_INDEX = 2097152;
    public static final int PERMISSION_ALLOW_VALUE = 0;
    public static final int PERMISSION_FORBID_VALUE = 1;
    public static final int PERMISSION_REMIND_VALUE = 2;
    public static final String READ_APPLIST = "32";
    public static final int READ_APPLIST_BIT_INDEX = 33554432;
    public static final String READ_CALENDAR = "14";
    public static final int READ_CALENDAR_BIT_INDEX = 2048;
    public static final String READ_CALLLOG = "13";
    public static final int READ_CALLLOG_BIT_INDEX = 2;
    public static final String READ_CONTACT = "11";
    public static final int READ_CONTACT_BIT_INDEX = 1;
    public static final String READ_HEALTH_DATA = "34";
    public static final String READ_LOCATION = "15";
    public static final int READ_LOCATION_BIT_INDEX = 8;
    public static final String READ_MESSAGE = "12";
    public static final int READ_MESSAGE_BIT_INDEX = 4;
    public static final String READ_MOTION_DATA = "33";
    public static final String READ_PHONE_CODE = "16";
    public static final int READ_PHONE_CODE_BIT_INDEX = 16;
    public static final int RHD_BIT_INDEX = 134217728;
    public static final int RMD_BIT_INDEX = 67108864;
    public static final String SEND_MMS = "25";
    public static final int SEND_MMS_BIT_INDEX = 8192;
    public static final String SEND_NOTIFICATION = "3";
    public static final String SEND_SMS = "24";
    public static final int SEND_SMS_BIT_INDEX = 32;
    public static final String SOUND_RECORDER = "27";
    public static final int SOUND_RECORDER_BIT_INDEX = 128;
    public static final String TAKE_PHOTO = "26";
    public static final int TAKE_PHOTO_BIT_INDEX = 1024;
    public static final int TRUST_PERMISSIONCFG = 0;
    public static final int TRUST_PERMISSIONCODE = 267382015;
    public static final String WIFI_NETWORK = "1";
    private static Map<String, String> mFeaturePermissionTypeMap = null;
    private static Map<String, Integer> mOtherPermissionMap = null;
    private static Map<String, Integer> mPermissionTypeMap = null;

    public static synchronized Map<String, Integer> getPermissionTypeMaps() {
        Map<String, Integer> map;
        synchronized (PermissionDefine.class) {
            if (mPermissionTypeMap == null) {
                mPermissionTypeMap = new HashMap();
                mPermissionTypeMap.put("11", Integer.valueOf(1));
                mPermissionTypeMap.put("12", Integer.valueOf(4));
                mPermissionTypeMap.put("13", Integer.valueOf(2));
                mPermissionTypeMap.put("14", Integer.valueOf(2048));
                mPermissionTypeMap.put("15", Integer.valueOf(8));
                mPermissionTypeMap.put("16", Integer.valueOf(16));
                mPermissionTypeMap.put("17", Integer.valueOf(16384));
                mPermissionTypeMap.put("18", Integer.valueOf(65536));
                mPermissionTypeMap.put("19", Integer.valueOf(32768));
                mPermissionTypeMap.put("20", Integer.valueOf(131072));
                mPermissionTypeMap.put("21", Integer.valueOf(524288));
                mPermissionTypeMap.put("22", Integer.valueOf(262144));
                mPermissionTypeMap.put("23", Integer.valueOf(64));
                mPermissionTypeMap.put("24", Integer.valueOf(32));
                mPermissionTypeMap.put("25", Integer.valueOf(8192));
                mPermissionTypeMap.put("26", Integer.valueOf(1024));
                mPermissionTypeMap.put("27", Integer.valueOf(128));
                mPermissionTypeMap.put("30", Integer.valueOf(8388608));
                mPermissionTypeMap.put("31", Integer.valueOf(16777216));
                mPermissionTypeMap.put("32", Integer.valueOf(33554432));
                mPermissionTypeMap.put("33", Integer.valueOf(67108864));
                mPermissionTypeMap.put("34", Integer.valueOf(134217728));
                mPermissionTypeMap.put("28", Integer.valueOf(2097152));
                mPermissionTypeMap.put("29", Integer.valueOf(4194304));
            }
            map = mPermissionTypeMap;
        }
        return map;
    }

    public static synchronized Map<String, Integer> getOtherPmerssionTypeMaps() {
        Map<String, Integer> map;
        synchronized (PermissionDefine.class) {
            if (mOtherPermissionMap == null) {
                mOtherPermissionMap = new HashMap();
                mOtherPermissionMap.put("1", Integer.valueOf(0));
                mOtherPermissionMap.put("2", Integer.valueOf(0));
                mOtherPermissionMap.put("3", Integer.valueOf(2));
                mOtherPermissionMap.put("4", Integer.valueOf(2));
            }
            map = mOtherPermissionMap;
        }
        return map;
    }

    public static synchronized Map<String, String> getFeaturePermissionMaps() {
        Map<String, String> map;
        synchronized (PermissionDefine.class) {
            if (mFeaturePermissionTypeMap == null) {
                mFeaturePermissionTypeMap = new HashMap();
                mFeaturePermissionTypeMap.put(APP_TRUST, PermissionValues.PERMISSION_VIEW_TRUST);
                mFeaturePermissionTypeMap.put("11", "11");
                mFeaturePermissionTypeMap.put("12", "12");
                mFeaturePermissionTypeMap.put("13", "13");
                mFeaturePermissionTypeMap.put("14", "14");
                mFeaturePermissionTypeMap.put("15", "15");
                mFeaturePermissionTypeMap.put("16", "16");
                mFeaturePermissionTypeMap.put("17", "17");
                mFeaturePermissionTypeMap.put("19", "19");
                mFeaturePermissionTypeMap.put("20", "20");
                mFeaturePermissionTypeMap.put("22", "22");
                mFeaturePermissionTypeMap.put("23", "23");
                mFeaturePermissionTypeMap.put("24", "24");
                mFeaturePermissionTypeMap.put("25", "25");
                mFeaturePermissionTypeMap.put("26", "26");
                mFeaturePermissionTypeMap.put("27", "27");
                mFeaturePermissionTypeMap.put("30", "30");
                mFeaturePermissionTypeMap.put("31", "31");
                mFeaturePermissionTypeMap.put("32", "32");
                mFeaturePermissionTypeMap.put("33", "33");
                mFeaturePermissionTypeMap.put("34", "34");
                mFeaturePermissionTypeMap.put("28", "28");
                mFeaturePermissionTypeMap.put("29", "29");
                mFeaturePermissionTypeMap.put("3", "3");
                mFeaturePermissionTypeMap.put("4", "4");
                mFeaturePermissionTypeMap.put("5", "5");
                mFeaturePermissionTypeMap.put("6", "6");
                mFeaturePermissionTypeMap.put("7", "7");
                mFeaturePermissionTypeMap.put("2", "2");
                mFeaturePermissionTypeMap.put("1", "1");
            }
            map = mFeaturePermissionTypeMap;
        }
        return map;
    }
}
