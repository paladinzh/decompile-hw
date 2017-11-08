package com.huawei.systemmanager.rainbow.db;

import android.util.SparseArray;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.AddViewValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.BackgroundValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.BootstartupValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CloudCommonValue;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CloudVagueValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CompetitorConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeBlackList;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeWhiteList;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.GetapplistValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.MessageSafeConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NetworkValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationExValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationTip;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.PermissionValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.PushBlackList;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.StartupConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.UnifiedPowerAppsConfigConfigFile;

public class CloudDBTableMap {
    protected static final int ADDVIEW_OUTER_TABLE = 21;
    protected static final int ADDVIEW_OUTER_VIEW = 20;
    protected static final int BACKGROUND_OUTER_TABLE = 30;
    protected static final int BOOTSTARTUP_OUTER_TABLE = 19;
    protected static final int BOOTSTARTUP_OUTER_VIEW = 18;
    protected static final int COMPETITOR_TABLE = 38;
    protected static final int GET_APPLIST_OUTER_TABLE = 27;
    protected static final int GET_APPLIST_OUTER_VIEW = 26;
    protected static final int MESSAGTE_SAFE_LINK_TABLE = 40;
    protected static final int MESSAGTE_SAFE_NUMBER_TABLE = 39;
    protected static final int MESSAGTE_SAFE_VIEW = 41;
    protected static final int NETWORK_OUTER_TABLE = 34;
    protected static final int NETWORK_OUTER_VIEW = 33;
    protected static final int NOTIFICATION_SIGNAL_OUTER_TABLE = 25;
    protected static final int NOTIFICATION_SIGNAL_OUTER_VIEW = 24;
    protected static final int NOTIFICATION_TABLE = 35;
    protected static final int PERMISSION_FEATURE_TABLE = 13;
    protected static final int PERMISSION_OUTER_TABLE = 16;
    protected static final int PERMISSION_OUTER_VIEW = 15;
    protected static final int PHONENUMBER_OUTER_TABLE = 32;
    protected static final int PUSH_OUTER_TABLE = 31;
    protected static final int RANGEBLACK_OUTER_TABLE = 29;
    protected static final int RANGEWHITE_OUTER_TABLE = 28;
    protected static final int RECOMMEND_RECORD_TABLE = 12;
    protected static final int SEND_NOTIFICATION_OUTER_TABLE = 23;
    protected static final int SEND_NOTIFICATION_OUTER_VIEW = 22;
    protected static final int STARTUP_TABLE = 36;
    protected static final int UNIFIED_POWER_APPS_TABLE = 37;
    protected static final int VAGUE_PERMISSION_FEATURE_TABLE = 14;
    protected static final int VAGUE_PERMISSION_OUTER_VIEW = 17;
    private static SparseArray<String> mTableSparseArray = null;

    public static synchronized SparseArray<String> getTablesMap() {
        SparseArray<String> sparseArray;
        synchronized (CloudDBTableMap.class) {
            if (mTableSparseArray == null) {
                mTableSparseArray = new SparseArray();
                mTableSparseArray.put(12, NotificationTip.OUTERTABLE_NAME);
                mTableSparseArray.put(13, "CloudPermission");
                mTableSparseArray.put(14, "CloudVaguePermission");
                mTableSparseArray.put(15, PermissionValues.PERMISSION_OUTER_VIEW_NAME);
                mTableSparseArray.put(16, PermissionValues.PERMISSION_OUTER_TABLE_NAME);
                mTableSparseArray.put(17, CloudVagueValues.PERMISSION_OUTER_VIEW_NAME);
                mTableSparseArray.put(18, CloudCommonValue.BOOTSTARTUP_OUTERVIEW_NAME);
                mTableSparseArray.put(19, BootstartupValues.OUTERTABLE_NAME);
                mTableSparseArray.put(20, CloudCommonValue.ADDVIEW_OUTERVIEW_NAME);
                mTableSparseArray.put(21, AddViewValues.OUTERTABLE_NAME);
                mTableSparseArray.put(22, CloudCommonValue.SEND_NOTIFICATION_OUTERVIEW_NAME);
                mTableSparseArray.put(23, NotificationValues.OUTERTABLE_NAME);
                mTableSparseArray.put(24, CloudCommonValue.NOTIFICATION_SIGNAL_OUTERVIEW_NAME);
                mTableSparseArray.put(25, NotificationExValues.OUTERTABLE_NAME);
                mTableSparseArray.put(26, CloudCommonValue.GET_APPLIST_OUTERVIEW_NAME);
                mTableSparseArray.put(27, GetapplistValues.OUTERTABLE_NAME);
                mTableSparseArray.put(28, ControlRangeWhiteList.OUTERTABLE_NAME);
                mTableSparseArray.put(29, ControlRangeBlackList.OUTERTABLE_NAME);
                mTableSparseArray.put(30, BackgroundValues.OUTERTABLE_NAME);
                mTableSparseArray.put(31, PushBlackList.OUTERTABLE_NAME);
                mTableSparseArray.put(32, "phoneNumberTable");
                mTableSparseArray.put(33, NetworkValues.OUTER_VIEW_NAME);
                mTableSparseArray.put(34, NetworkValues.OUTER_TABLE_NAME);
                mTableSparseArray.put(35, NotificationConfigFile.OUTERTABLE_NAME);
                mTableSparseArray.put(36, StartupConfigFile.OUTERTABLE_NAME);
                mTableSparseArray.put(37, UnifiedPowerAppsConfigConfigFile.OUTERTABLE_NAME);
                mTableSparseArray.put(38, CompetitorConfigFile.OUTERTABLE_NAME);
                mTableSparseArray.put(39, MessageSafeConfigFile.NUMBER_OUTERTABLE_NAME);
                mTableSparseArray.put(40, MessageSafeConfigFile.LINK_OUTERTABLE_NAME);
                mTableSparseArray.put(41, MessageSafeConfigFile.OUTERVIEW_NAME);
            }
            sparseArray = mTableSparseArray;
        }
        return sparseArray;
    }
}
