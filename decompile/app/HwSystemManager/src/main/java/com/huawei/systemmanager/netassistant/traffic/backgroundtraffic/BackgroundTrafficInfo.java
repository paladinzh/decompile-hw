package com.huawei.systemmanager.netassistant.traffic.backgroundtraffic;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.NetworkPolicyManager;
import android.net.Uri;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.netassistant.traffic.appinfo.SpecialUid;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.HsmPkgUtils;
import java.util.List;

public class BackgroundTrafficInfo {
    private static final String BACKGROUND_TRAFFIC_APP_NAME = "appname";
    private static final String BACKGROUND_TRAFFIC_IS_RISTRICT = "value";
    private static final String BACKGROUND_TRAFFIC_TABLE_NAME = "background";
    public static final Uri BACKGROUND_TRAFFIC_URI = Uri.parse("content://com.huawei.systemmanager.netassistant.db.traffic.TrafficDBProvider/background");
    public static final String TAG = "BackgroundTrafficInfo";

    public static Cursor getBackgroundTrafficCursor() {
        List<HsmPkgInfo> apps = HsmPackageManager.getInstance().getAllPackages();
        if (Utility.isNullOrEmptyList(apps)) {
            return null;
        }
        NetworkPolicyManager policyManager = NetworkPolicyManager.from(GlobalContext.getContext());
        MatrixCursor cursor = new MatrixCursor(new String[]{"appname", "value"});
        for (HsmPkgInfo appInfo : apps) {
            int appUid = appInfo.getUid();
            if (!SpecialUid.isWhiteListUid(appUid)) {
                boolean checked = (policyManager.getUidPolicy(appUid) & 1) == 0;
                cursor.addRow(new Object[]{appInfo.mPkgName, String.valueOf(checked)});
            }
        }
        HwLog.i(TAG, "getBackgroundTrafficCursor, size is " + cursor.getCount());
        return cursor;
    }

    public static int setBackgroundTrafficPreference(ContentValues[] values) {
        if (values == null) {
            HwLog.w(TAG, "setBackgroundTrafficPreference ,  Invalid values");
            return -1;
        }
        HwLog.i(TAG, "setBackgroundTrafficPreference, size is " + values.length);
        NetworkPolicyManager policyManager = NetworkPolicyManager.from(GlobalContext.getContext());
        for (ContentValues value : values) {
            String appName = value.getAsString("appname");
            Boolean checked = value.getAsBoolean("value");
            int appUid = HsmPkgUtils.getPackageUid(appName);
            if (!(-1 == appUid || SpecialUid.isWhiteListUid(appUid) || policyManager == null)) {
                policyManager.setUidPolicy(appUid, checked.booleanValue() ? 0 : 1);
            }
        }
        return 1;
    }

    public static void setBackgroundTrafficPreference(ContentValues value) {
        if (value == null) {
            HwLog.w(TAG, "setBackgroundTrafficPreference ,  Invalid values");
            return;
        }
        NetworkPolicyManager policyManager = NetworkPolicyManager.from(GlobalContext.getContext());
        String appName = value.getAsString("appname");
        Boolean checked = value.getAsBoolean("value");
        int appUid = HsmPkgUtils.getPackageUid(appName);
        if (-1 != appUid && !SpecialUid.isWhiteListUid(appUid)) {
            if (policyManager != null) {
                policyManager.setUidPolicy(appUid, checked.booleanValue() ? 0 : 1);
                HwLog.i(TAG, "setBackgroundTrafficPreference , app name is: " + appName + "  status is:  " + checked);
            } else {
                HwLog.i(TAG, "setBackgroundTrafficPreference , policyManager is null");
            }
        }
    }
}
