package com.huawei.systemmanager.netassistant.traffic.roamingtraffic;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.NetworkPolicyManager;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.SparseArray;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comparator.AlpComparator;
import com.huawei.systemmanager.netassistant.db.comm.DBTable;
import com.huawei.systemmanager.netassistant.db.traffic.TrafficDBProvider;
import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppInfo;
import com.huawei.systemmanager.netassistant.traffic.appinfo.SpecialUid;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class RoamingAppInfo {
    public static final AlpComparator<RoamingAppInfo> ABS_NET_APP_ALP_COMPARATOR = new AlpComparator<RoamingAppInfo>() {
        public String getStringKey(RoamingAppInfo t) {
            if (t == null || t.appInfo.mAppLabel == null) {
                return "";
            }
            return t.appInfo.mAppLabel;
        }
    };
    private static final String TAG = RoamingAppInfo.class.getSimpleName();
    static DBTable mTable = new Tables();
    NetAppInfo appInfo;
    private boolean isBackgroundAccess;
    boolean isNetAccess;
    private NetworkPolicyManager mPolicyManager = NetworkPolicyManager.from(GlobalContext.getContext());

    public static class Tables extends DBTable {
        static final String COL_ID = "id";
        static final String COL_PACKAGE = "pkgname";
        static final String COL_UID = "uid";
        public static final String TABLE_NAME = "roamingtraffic";

        public String getTableCreateCmd() {
            return "create table if not exists roamingtraffic ( id integer primary key autoincrement, pkgname text, uid long);";
        }

        public String getTableDropCmd() {
            return "DROP TABLE IF EXISTS roamingtraffic";
        }

        public String getPrimaryColumn() {
            return "id";
        }

        public String getTableName() {
            return TABLE_NAME;
        }

        public String getAuthority() {
            return TrafficDBProvider.AUTHORITY;
        }
    }

    public RoamingAppInfo(NetAppInfo appInfo, boolean isNetAccess) {
        boolean z = false;
        this.appInfo = appInfo;
        this.isNetAccess = isNetAccess;
        if (appInfo != null) {
            if ((this.mPolicyManager.getUidPolicy(appInfo.mUid) & 1) == 0) {
                z = true;
            }
            this.isBackgroundAccess = z;
        }
    }

    public void setBackgroundChecked(boolean value) {
        if (this.appInfo == null || this.mPolicyManager == null) {
            HwLog.i(TAG, "setBackgroundChecked , arg is wrong return");
        } else if (SpecialUid.isSpecialNameUid(this.appInfo.mUid) || !UserHandle.isApp(this.appInfo.mUid)) {
            HwLog.i(TAG, this.appInfo.mUid + "  is special uid");
        } else {
            int i;
            this.isBackgroundAccess = value;
            HwLog.i(TAG, "setBackgroundChecked , uid = " + this.appInfo.mUid + " , value = " + value);
            NetworkPolicyManager networkPolicyManager = this.mPolicyManager;
            int i2 = this.appInfo.mUid;
            if (value) {
                i = 0;
            } else {
                i = 1;
            }
            networkPolicyManager.setUidPolicy(i2, i);
        }
    }

    public Drawable getIcon() {
        return this.appInfo == null ? null : this.appInfo.getIcon();
    }

    public boolean getNetAccess() {
        return this.isNetAccess;
    }

    public boolean isBackgroundAccess() {
        return this.isBackgroundAccess;
    }

    public String getAppLabel() {
        return this.appInfo.mAppLabel;
    }

    public static SparseArray<RoamingAppInfo> get(boolean removableApp) {
        SparseArray<RoamingAppInfo> sparseArray = new SparseArray();
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        Cursor cursor = cr.query(mTable.getUri(), null, null, null, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            HwLog.e(TAG, "result is empty");
            return sparseArray;
        }
        if (cursor.moveToNext()) {
            int pkgNameIndex = cursor.getColumnIndex("pkgname");
            int uidIndex = cursor.getColumnIndex("uid");
            do {
                String pkgName = cursor.getString(pkgNameIndex);
                int uid = cursor.getInt(uidIndex);
                HsmPkgInfo pkgInfo = HsmPackageManager.getInstance().getPkgInfo(pkgName);
                if (pkgInfo != null) {
                    int curUid = pkgInfo.getUid();
                    if (uid != curUid) {
                        HwLog.e(TAG, "uid has changed, old uid is " + uid + " curuid = " + curUid);
                        String[] args = new String[]{pkgName};
                        ContentValues cv = new ContentValues();
                        cv.put("uid", Integer.valueOf(curUid));
                        cr.update(mTable.getUri(), cv, "pkgname = ? ", args);
                    }
                    NetAppInfo netApp = NetAppInfo.buildInfo(curUid);
                    boolean isRemovable = HsmPackageManager.getInstance().isRemovable(pkgName);
                    if (isRemovable == removableApp) {
                        sparseArray.put(curUid, new RoamingAppInfo(netApp, false));
                        HwLog.i(TAG, "add roaming app to list: " + netApp.mAppLabel + " " + netApp.mUid + " isRemovable: " + isRemovable);
                    } else {
                        HwLog.w(TAG, "not add to list, because app type not fit");
                    }
                }
            } while (cursor.moveToNext());
        }
        HwLog.d(TAG, "finish get list from db, size is " + sparseArray.size());
        cursor.close();
        return sparseArray;
    }

    public boolean save() {
        int i = 0;
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        ContentValues cv = new ContentValues();
        String[] pkgNames = GlobalContext.getContext().getPackageManager().getPackagesForUid(this.appInfo.mUid);
        if (pkgNames == null) {
            HwLog.e(TAG, "there is no pkg for uid " + this.appInfo.mUid);
            return false;
        }
        int length = pkgNames.length;
        while (i < length) {
            String pkgName = pkgNames[i];
            if (pkgName != null) {
                cv.put("pkgname", pkgName);
                cv.put("uid", Integer.valueOf(this.appInfo.mUid));
                cr.insert(mTable.getUri(), cv);
            }
            i++;
        }
        return true;
    }

    public boolean clear() {
        String[] args = new String[]{String.valueOf(this.appInfo.mUid)};
        if (GlobalContext.getContext().getContentResolver().delete(mTable.getUri(), "uid = ?", args) > 0) {
            return true;
        }
        return false;
    }

    public static void clearOnPkgRemoved(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            HwLog.i(TAG, "clearOnPkgRemoved ,arg is wrong");
            return;
        }
        String[] args = new String[]{pkgName};
        GlobalContext.getContext().getContentResolver().delete(mTable.getUri(), "pkgname = ?", args);
    }

    public static RoamingAppInfo get(int mUid) {
        NetAppInfo appInfo = NetAppInfo.buildInfo(mUid);
        String[] args = new String[]{String.valueOf(mUid)};
        Cursor cursor = GlobalContext.getContext().getContentResolver().query(mTable.getUri(), null, "uid = ?", args, null);
        boolean netAccess = false;
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            netAccess = true;
        } else {
            cursor.close();
        }
        return new RoamingAppInfo(appInfo, netAccess);
    }
}
