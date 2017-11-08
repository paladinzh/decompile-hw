package com.huawei.systemmanager.rainbow.client.base;

import android.net.Uri;
import android.util.SparseArray;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys.CloudReqVerSpfKeys;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.BackgroundValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CompetitorConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeBlackList;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeWhiteList;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.PhoneNumberList;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.PushBlackList;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.StartupConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.UnifiedPowerAppsConfigConfigFile;

public class GetAppListBasic {
    private static SparseArray<String> mActionSparseArray = null;
    private static SparseArray<String> mAppsSparseArray = null;
    private static SparseArray<Uri> mAppsUriSparseArray = null;

    public interface CloudAppListType {
        public static final int BACKGROUND_TYPE = 35;
        public static final int COMPETITOR_TYPE = 34;
        public static final int CONTROL_BLACK_TYPE = 9;
        public static final int CONTROL_WHITE_TYPE = 10;
        public static final int MESSAGE_SAFE_TYPE = 36;
        public static final int NOTIFICATION_TYPE = 33;
        public static final int PHONE_TYPE = 30;
        public static final int PUSH_TYPE = 11;
        public static final int STARTUP_TYPE = 32;
        public static final int UNIFIED_POWER_APPS_TYPE = 31;
    }

    public interface CloudUpdateAction {
        public static final String BACKGROUND_DATA_UPDATE_ACTION = "com.huawei.systemmanager.action.background";
        public static final String COMPETITOR_DATA_UPDATE_ACTION = "com.huawei.systemmanager.action.competitor";
        public static final String STARTUP_DATA_UPDATE_ACTION = "com.huawei.systemmanager.action.startup";
        public static final String UNIFIED_POWER_DATA_UPDATE_ACTION = "com.huawei.systemmanager.action.unifiledpower";
    }

    public static synchronized SparseArray<String> getActionMaps() {
        SparseArray<String> sparseArray;
        synchronized (GetAppListBasic.class) {
            if (mActionSparseArray == null) {
                mActionSparseArray = new SparseArray();
                mActionSparseArray.put(35, CloudUpdateAction.BACKGROUND_DATA_UPDATE_ACTION);
                mActionSparseArray.put(32, CloudUpdateAction.STARTUP_DATA_UPDATE_ACTION);
                mActionSparseArray.put(34, CloudUpdateAction.COMPETITOR_DATA_UPDATE_ACTION);
                mActionSparseArray.put(31, "com.huawei.systemmanager.action.unifiledpower");
            }
            sparseArray = mActionSparseArray;
        }
        return sparseArray;
    }

    public static synchronized SparseArray<String> getBlackWhiteMaps() {
        SparseArray<String> sparseArray;
        synchronized (GetAppListBasic.class) {
            if (mAppsSparseArray == null) {
                mAppsSparseArray = new SparseArray();
                mAppsSparseArray.put(9, CloudReqVerSpfKeys.CONTROL_BLACK_LIST_VERSION_SPF);
                mAppsSparseArray.put(10, CloudReqVerSpfKeys.CONTROL_WHITE_LIST_VERSION_SPF);
                mAppsSparseArray.put(35, CloudReqVerSpfKeys.BACKGROUND_LIST_VERSION_SPF);
                mAppsSparseArray.put(11, CloudReqVerSpfKeys.PUSH_LIST_VERSION_SPF);
                mAppsSparseArray.put(30, CloudReqVerSpfKeys.PHONE_LIST_VERSION_SPF);
                mAppsSparseArray.put(30, CloudReqVerSpfKeys.PHONE_LIST_VERSION_SPF);
                mAppsSparseArray.put(30, CloudReqVerSpfKeys.PHONE_LIST_VERSION_SPF);
                mAppsSparseArray.put(30, CloudReqVerSpfKeys.PHONE_LIST_VERSION_SPF);
                mAppsSparseArray.put(31, CloudReqVerSpfKeys.UNIFIED_POWER_APPS_SPF);
                mAppsSparseArray.put(32, CloudReqVerSpfKeys.STARTUP_SPF);
                mAppsSparseArray.put(33, CloudReqVerSpfKeys.NOTIFICATION_SPF);
                mAppsSparseArray.put(34, CloudReqVerSpfKeys.COMPETITOR_SPF);
                mAppsSparseArray.put(36, CloudReqVerSpfKeys.MESSAGE_SAFE_SPF);
            }
            sparseArray = mAppsSparseArray;
        }
        return sparseArray;
    }

    public static synchronized SparseArray<Uri> getBlackWhiteUriMaps() {
        SparseArray<Uri> sparseArray;
        synchronized (GetAppListBasic.class) {
            if (mAppsUriSparseArray == null) {
                mAppsUriSparseArray = new SparseArray();
                mAppsUriSparseArray.put(9, ControlRangeBlackList.CONTENT_OUTERTABLE_URI);
                mAppsUriSparseArray.put(10, ControlRangeWhiteList.CONTENT_OUTERTABLE_URI);
                mAppsUriSparseArray.put(35, BackgroundValues.CONTENT_OUTERTABLE_URI);
                mAppsUriSparseArray.put(11, PushBlackList.CONTENT_OUTERTABLE_URI);
                mAppsUriSparseArray.put(30, PhoneNumberList.CONTENT_OUTERTABLE_URI);
                mAppsUriSparseArray.put(31, UnifiedPowerAppsConfigConfigFile.CONTENT_OUTERTABLE_URI);
                mAppsUriSparseArray.put(32, StartupConfigFile.CONTENT_OUTERTABLE_URI);
                mAppsUriSparseArray.put(33, NotificationConfigFile.CONTENT_OUTERTABLE_URI);
                mAppsUriSparseArray.put(34, CompetitorConfigFile.CONTENT_OUTERTABLE_URI);
            }
            sparseArray = mAppsUriSparseArray;
        }
        return sparseArray;
    }

    public static boolean isAllDataUpdated(int listType) {
        switch (listType) {
            case 31:
                return false;
            case 32:
                return false;
            case 33:
                return false;
            case 35:
                return false;
            default:
                return true;
        }
    }
}
