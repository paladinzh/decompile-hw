package com.huawei.systemmanager.netassistant.netapp.datasource;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.HwNetworkPolicyManager;
import android.os.UserManager;
import android.util.SparseArray;
import com.huawei.cust.HwCustUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comparator.AlpComparator;
import com.huawei.systemmanager.netassistant.db.comm.DBTable;
import com.huawei.systemmanager.netassistant.db.traffic.TrafficDBHelper;
import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppUtils;
import com.huawei.systemmanager.netassistant.traffic.roamingtraffic.RoamingAppInfo.Tables;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.io.File;
import java.util.List;

public class NetAppManager {
    private static final String TAG = "NetAppManager";
    private static final HwCustNetAppManager mCustNetAppManager = ((HwCustNetAppManager) HwCustUtils.createObj(HwCustNetAppManager.class, new Object[0]));

    private static class DBSQLiteOpenHelper extends SQLiteOpenHelper {
        public DBSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public void onCreate(SQLiteDatabase db) {
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    public static class UidDetail {
        public static final AlpComparator<UidDetail> UIDDETAIL_ALP_COMPARATOR = new AlpComparator<UidDetail>() {
            public String getStringKey(UidDetail t) {
                if (t == null || t.getLabel() == null) {
                    return "";
                }
                return t.getLabel();
            }
        };
        boolean isMultiPkg;
        String label;
        boolean mobileAccess;
        int uid;
        boolean wifiAccess;

        public UidDetail(int uid, String la, boolean mobile, boolean wifi) {
            this.uid = uid;
            this.mobileAccess = mobile;
            this.label = la;
            this.wifiAccess = wifi;
        }

        public UidDetail(int uid, boolean mobile, boolean wifi) {
            this.uid = uid;
            this.mobileAccess = mobile;
            this.label = GlobalContext.getContext().getPackageManager().getNameForUid(uid);
            this.wifiAccess = wifi;
        }

        public boolean isWifiAccess() {
            return this.wifiAccess;
        }

        public void setWifiAccess(boolean wifiAccess) {
            this.wifiAccess = wifiAccess;
        }

        public boolean isMobileAccess() {
            return this.mobileAccess;
        }

        public void setMobileAccess(boolean mobileAccess) {
            this.mobileAccess = mobileAccess;
        }

        public boolean isMultiPkg() {
            return this.isMultiPkg;
        }

        public void setMultiPkg(boolean multiPkg) {
            this.isMultiPkg = multiPkg;
        }

        public int getUid() {
            return this.uid;
        }

        public String getLabel() {
            return this.label;
        }

        public void changeWifiAccess() {
            this.wifiAccess = !this.wifiAccess;
        }

        public void changeMobileAccess() {
            this.mobileAccess = !this.mobileAccess;
        }

        public static UidDetail create(int uid) {
            int policy = HwNetworkPolicyManager.from(GlobalContext.getContext()).getHwUidPolicy(uid);
            return new UidDetail(uid, (policy & 1) == 0, (policy & 2) == 0);
        }
    }

    public static SparseArray<UidDetail> getAllInstalledAppWithUid(boolean removableApp) {
        PackageManager pm = GlobalContext.getContext().getPackageManager();
        SparseArray<UidDetail> uidDetails = new SparseArray();
        List<HsmPkgInfo> pkgInfos = HsmPackageManager.getInstance().getInstalledPackages(8192);
        HwNetworkPolicyManager networkPolicyManager = HwNetworkPolicyManager.from(GlobalContext.getContext());
        int appsSize = pkgInfos.size();
        for (int j = 0; j < appsSize; j++) {
            HsmPkgInfo pkgInfo = (HsmPkgInfo) pkgInfos.get(j);
            if (removableApp == pkgInfo.isRemovable() && packageCanAccessInternet(pm, pkgInfo.getPackageName()) && !isHwSpecialApp(pkgInfo) && (mCustNetAppManager == null || !mCustNetAppManager.isPackageDisabledForNetwork(GlobalContext.getContext(), pkgInfo.getPackageName()))) {
                mergeUidToUidDetail(uidDetails, pkgInfo, networkPolicyManager.getHwUidPolicy(pkgInfo.getUid()));
            }
        }
        return uidDetails;
    }

    private static void mergeUidToUidDetail(SparseArray<UidDetail> uidDetails, HsmPkgInfo pkgInfo, int policy) {
        int uid = pkgInfo.getUid();
        UidDetail detail = (UidDetail) uidDetails.get(uid);
        if (detail != null) {
            detail.setMultiPkg(true);
        } else {
            uidDetails.put(uid, new UidDetail(uid, getUidDetailLabel(pkgInfo), (policy & 1) == 0, (policy & 2) == 0));
        }
    }

    private static String getUidDetailLabel(HsmPkgInfo pkgInfo) {
        int uid = pkgInfo.getUid();
        String label = pkgInfo.label();
        if (uid == -4 || uid == -5) {
            int labelId;
            if (UserManager.supportsMultipleUsers()) {
                labelId = R.string.data_usage_uninstalled_apps_users;
            } else {
                labelId = R.string.data_usage_uninstalled_apps;
            }
            return GlobalContext.getString(labelId);
        } else if (uid == 1000) {
            return GlobalContext.getString(R.string.process_kernel_label);
        } else {
            return label;
        }
    }

    public static boolean isHwSpecialApp(HsmPkgInfo pkgInfo) {
        if (pkgInfo.getUid() >= 10000 || !pkgInfo.isRemovable()) {
            return false;
        }
        return true;
    }

    public static PackageInfo getPackageInfoByPackageName(String pkgName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = PackageManagerWrapper.getPackageInfo(GlobalContext.getContext().getPackageManager(), pkgName, 4096);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo;
    }

    public static boolean packageCanAccessInternet(PackageManager pm, String pkgName) {
        PackageInfo pkgInfo = getPackageInfoByPackageName(pkgName);
        if (pkgInfo == null) {
            return false;
        }
        String[] rp = pkgInfo.requestedPermissions;
        if (rp != null && rp.length > 0) {
            for (Object equals : rp) {
                if (NetAppUtils.NET_PERMISSION.equals(equals)) {
                    HwLog.d(TAG, "packageCanAccessInternet " + pkgName + "has INTERNET permission");
                    int enableSetting = 2;
                    try {
                        enableSetting = pm.getApplicationEnabledSetting(pkgName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (2 != enableSetting) {
                        HwLog.d(TAG, "packageCanAccessInternet component enable");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void transeDataFromBefore() {
        String DB_NAME = "net_permission.db";
        String BLOCK_TABLE_NAME = "permissionCfg";
        String BLOCK_TABLE_NAME_SYSTEM = "permissionCfg_system";
        SQLiteOpenHelper helper = new DBSQLiteOpenHelper(GlobalContext.getContext(), "net_permission.db", null, 3);
        File file = GlobalContext.getContext().getDatabasePath("net_permission.db");
        if (file == null || !file.exists()) {
            HwLog.i(TAG, "no db file : net_permission.db");
        } else {
            HwLog.i(TAG, "transeDataFromBefore db exit : file = " + file.getAbsolutePath());
            syncNetworkPermissionWithBeforeData("permissionCfg", helper.getWritableDatabase());
            syncNetworkPermissionWithBeforeData("permissionCfg_system", helper.getWritableDatabase());
            helper.close();
            HwLog.d(TAG, "transeDataFromBefore delete:" + file.delete());
        }
        String TRAFFIC_DB_NAME = "traffic.db";
        String TRAFFIC_ROAMING_BLOCK_TABLE_NAME = Tables.TABLE_NAME;
        File trafficFile = GlobalContext.getContext().getDatabasePath("traffic.db");
        SQLiteOpenHelper trafficHelper = TrafficDBHelper.getInstance(GlobalContext.getContext(), "traffic.db", 2, new DBTable[]{new Tables()});
        if (trafficFile == null || !trafficFile.exists()) {
            HwLog.i(TAG, "no db file : traffic.db");
            return;
        }
        HwLog.i(TAG, "transeDataFromBefore db exit : trafficFile = " + trafficFile.getAbsolutePath());
        sysncRoamingPermissionWithBeforeData(Tables.TABLE_NAME, trafficHelper.getWritableDatabase());
    }

    private static void sysncRoamingPermissionWithBeforeData(String tbName, SQLiteDatabase db) {
        String COLUMN_PKGNAME = "pkgname";
        Cursor cursor = null;
        try {
            cursor = db.query(tbName, new String[]{"pkgname"}, null, null, null, null, null);
        } catch (Exception e) {
            HwLog.e(TAG, "query error : tbName = " + tbName + " ; e = " + e.getMessage());
        }
        if (isNullOrEmptyCursor(cursor, true)) {
            HwLog.e(TAG, "sysncRoamingPermissionWithBeforeData no data");
            return;
        }
        HwNetworkPolicyManager manager = HwNetworkPolicyManager.from(GlobalContext.getContext());
        while (cursor.moveToNext()) {
            String pkgName = cursor.getString(cursor.getColumnIndex("pkgname"));
            HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkgName);
            if (info == null || manager == null) {
                HwLog.e(TAG, "sysncRoamingPermissionWithBeforeData no pkginfo for " + pkgName);
            } else {
                int uid = info.getUid();
                HwLog.i(TAG, "sysncRoamingPermissionWithBeforeData permission roaming uid = " + uid);
                manager.addHwUidPolicy(uid, 4);
            }
        }
        try {
            cursor.close();
        } catch (Exception e2) {
            HwLog.e(TAG, "", e2);
        }
        try {
            db.execSQL("DROP TABLE IF EXISTS " + tbName);
        } catch (Exception e22) {
            HwLog.e(TAG, "delete error : TRAFFIC_ROAMING_BLOCK_TABLE_NAME : " + tbName + " ; e = " + e22.getMessage());
        }
    }

    private static void syncNetworkPermissionWithBeforeData(String tbName, SQLiteDatabase db) {
        HwLog.e(TAG, "syncNetworkPermissionWithBeforeData tb = " + tbName);
        String COLUMN_PKGNAME = "packageName";
        String COLUMN_PERM_CODE = "net_permissionCfg";
        Cursor cursor = null;
        try {
            cursor = db.query(tbName, new String[]{"packageName", "net_permissionCfg"}, null, null, null, null, null);
        } catch (Exception e) {
            HwLog.e(TAG, "query error : tbName = " + tbName, e);
        }
        if (isNullOrEmptyCursor(cursor, true)) {
            HwLog.e(TAG, "syncNetworkPermissionWithBeforeData no data");
            return;
        }
        HwNetworkPolicyManager manager = HwNetworkPolicyManager.from(GlobalContext.getContext());
        while (cursor.moveToNext()) {
            String pkgName = cursor.getString(cursor.getColumnIndex("packageName"));
            int permCode = cursor.getInt(cursor.getColumnIndex("net_permissionCfg"));
            boolean wifiPerm = (permCode & 16384) != 0;
            boolean mobilePerm = (permCode & 8192) != 0;
            HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkgName);
            if (info == null || manager == null) {
                HwLog.e(TAG, "no pkginfo for " + pkgName);
            } else {
                int uid = info.getUid();
                if (mobilePerm) {
                    HwLog.i(TAG, "syncNetworkPermissionWithBeforeData permission mobile uid = " + uid);
                    manager.addHwUidPolicy(uid, 1);
                }
                if (wifiPerm) {
                    HwLog.i(TAG, "syncNetworkPermissionWithBeforeData permission wifi uid = " + uid);
                    manager.addHwUidPolicy(uid, 2);
                }
            }
        }
        try {
            cursor.close();
        } catch (Exception e2) {
            HwLog.e(TAG, "", e2);
        }
    }

    public static boolean isNullOrEmptyCursor(Cursor cursor, boolean isCloseIfEmpty) {
        if (cursor == null) {
            return true;
        }
        if (cursor.getCount() > 0) {
            return false;
        }
        if (isCloseIfEmpty) {
            try {
                cursor.close();
            } catch (Exception e) {
                HwLog.e(TAG, "", e);
            }
        }
        return true;
    }
}
