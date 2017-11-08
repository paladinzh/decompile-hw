package com.huawei.systemmanager.netassistant.traffic.notrafficapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.SparseIntArray;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.netassistant.db.comm.DBTable;
import com.huawei.systemmanager.netassistant.db.comm.ITableInfo;
import com.huawei.systemmanager.netassistant.db.traffic.TrafficDBProvider;
import com.huawei.systemmanager.netassistant.netapp.bean.NoTrafficAppInfo;
import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppInfo;
import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoTrafficAppDbInfo extends ITableInfo {
    public static final String TAG = "NoTrafficAppDbInfo";
    public static final Uri URI = new Tables().getUri();
    public DBTable dbTable;
    private String mImsi;
    private SparseIntArray mUidListFromDb = new SparseIntArray();
    private List<NoTrafficAppInfo> mUidListFromMemory = new ArrayList();

    public static class Tables extends DBTable {
        public static final String COL_ID = "id";
        public static final String COL_IMSI = "imsi";
        public static final String COL_PKGNAME = "packagename";
        public static final String COL_UID = "uid";
        public static final String TABLE_NAME = "notrafficapp";

        public String getTableCreateCmd() {
            return "create table if not exists notrafficapp ( id integer primary key autoincrement, imsi text, packagename text, uid int);";
        }

        public String getTableDropCmd() {
            return "DROP TABLE IF EXISTS notrafficapp";
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

    public NoTrafficAppDbInfo(String imsi) {
        this.mImsi = imsi;
        this.dbTable = new Tables();
    }

    public void initDbData() {
        HwLog.i(TAG, "initDbData");
        checkUidPkg();
        initListFromDb();
    }

    public void initAllData() {
        HwLog.i(TAG, "initAllData");
        checkUidPkg();
        initListFromMemory();
        initListFromDb();
        setListCheckedState();
        Collections.sort(this.mUidListFromMemory, NoTrafficAppInfo.NO_TRAFFIC_APP_COMPARATOR);
    }

    public boolean isNoTrafficApp(int uid) {
        boolean z = false;
        if (this.mUidListFromDb == null || this.mUidListFromDb.size() == 0) {
            HwLog.i(TAG, "isNoTrafficApp , mUidListFromDb is empty");
            return false;
        }
        if (this.mUidListFromDb.indexOfValue(uid) != -1) {
            z = true;
        }
        return z;
    }

    public boolean save(int uid) {
        boolean z = false;
        if (uid <= 0) {
            return false;
        }
        List<ContentValues> listValues = new ArrayList();
        String[] apps = GlobalContext.getContext().getPackageManager().getPackagesForUid(uid);
        if (apps == null || apps.length == 0) {
            return false;
        }
        for (String name : apps) {
            ContentValues values = new ContentValues();
            values.put("imsi", this.mImsi);
            values.put("packagename", name);
            values.put("uid", Integer.valueOf(uid));
            listValues.add(values);
        }
        try {
            int size = GlobalContext.getContext().getContentResolver().bulkInsert(this.dbTable.getUri(), (ContentValues[]) listValues.toArray(new ContentValues[listValues.size()]));
            GlobalContext.getContext().getContentResolver().notifyChange(URI, null);
            HwLog.i(TAG, "save , save size is:  " + size);
            this.mUidListFromDb.put(uid, uid);
            setListCheckedState();
            if (size > 0) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            HwLog.e(TAG, "save fail ,e = " + e.getMessage());
            return false;
        }
    }

    public boolean clear(int uid) {
        String[] whereArg = new String[]{this.mImsi, String.valueOf(uid)};
        int size = GlobalContext.getContext().getContentResolver().delete(this.dbTable.getUri(), "imsi = ? and uid = ?", whereArg);
        GlobalContext.getContext().getContentResolver().notifyChange(URI, null);
        HwLog.i(TAG, "clear , clear size is:  " + size);
        this.mUidListFromDb.delete(uid);
        setListCheckedState();
        if (size > 0) {
            return true;
        }
        return false;
    }

    public void saveAll() {
        List<ContentValues> listValues = new ArrayList();
        for (NoTrafficAppInfo info : this.mUidListFromMemory) {
            if (this.mUidListFromDb.indexOfValue(info.getUid()) == -1) {
                String[] apps = GlobalContext.getContext().getPackageManager().getPackagesForUid(info.getUid());
                if (!(apps == null || apps.length == 0)) {
                    for (String name : apps) {
                        ContentValues values = new ContentValues();
                        values.put("imsi", this.mImsi);
                        values.put("packagename", name);
                        values.put("uid", Integer.valueOf(info.getUid()));
                        listValues.add(values);
                    }
                }
            }
        }
        if (listValues.size() <= 0) {
            HwLog.i(TAG, "saveAll , size is 0, return");
            return;
        }
        GlobalContext.getContext().getContentResolver().bulkInsert(this.dbTable.getUri(), (ContentValues[]) listValues.toArray(new ContentValues[listValues.size()]));
        GlobalContext.getContext().getContentResolver().notifyChange(URI, null);
        for (NoTrafficAppInfo info2 : this.mUidListFromMemory) {
            this.mUidListFromDb.put(info2.getUid(), info2.getUid());
        }
        resetListCheckedState(true);
        HwLog.i(TAG, "saveAll, size is:  " + listValues.size());
    }

    public void clearAll() {
        String[] whereArg = new String[]{this.mImsi};
        int size = GlobalContext.getContext().getContentResolver().delete(this.dbTable.getUri(), "imsi = ?", whereArg);
        GlobalContext.getContext().getContentResolver().notifyChange(URI, null);
        this.mUidListFromDb.clear();
        resetListCheckedState(false);
        HwLog.i(TAG, "clearAll, size is:  " + size);
    }

    public ITableInfo get() {
        return this;
    }

    public ITableInfo save(Object[] obj) {
        return this;
    }

    public ITableInfo clear() {
        return this;
    }

    public List<NoTrafficAppInfo> getAllUidList() {
        return this.mUidListFromMemory;
    }

    public SparseIntArray getNoTrafficList() {
        return this.mUidListFromDb;
    }

    public int getNoTrafficSize() {
        return this.mUidListFromDb == null ? 0 : this.mUidListFromDb.size();
    }

    private void setListCheckedState() {
        for (NoTrafficAppInfo info : this.mUidListFromMemory) {
            if (this.mUidListFromDb.indexOfValue(info.getUid()) != -1) {
                info.setChecked(true);
            } else {
                info.setChecked(false);
            }
        }
    }

    private void resetListCheckedState(boolean isChecked) {
        for (NoTrafficAppInfo info : this.mUidListFromMemory) {
            info.setChecked(isChecked);
        }
    }

    private void checkUidPkg() {
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        String[] projection = new String[]{"uid", "packagename"};
        String[] whereArg = new String[]{this.mImsi};
        Cursor cursor = cr.query(this.dbTable.getUri(), projection, "imsi = ?", whereArg, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            HwLog.i(TAG, "checkUidPkg , result is empty");
            if (TextUtils.isEmpty(this.mImsi)) {
                HwLog.i(TAG, "checkUidPkg , for the key is empty");
            }
            return;
        }
        if (cursor.moveToFirst()) {
            int uidIndex = cursor.getColumnIndex("uid");
            int pkgIndex = cursor.getColumnIndex("packagename");
            do {
                String pkgName = cursor.getString(pkgIndex);
                int uid = cursor.getInt(uidIndex);
                HsmPkgInfo pkgInfo = HsmPackageManager.getInstance().getPkgInfo(pkgName);
                if (pkgInfo == null) {
                    HwLog.e(TAG, "pkg: " + pkgName + "  can not get HsmPkgInfo");
                } else {
                    int curUid = pkgInfo.getUid();
                    if (uid != curUid) {
                        HwLog.e(TAG, "pkg: " + pkgName + "  uid has changed, old uid is " + uid + " curuid = " + curUid);
                        String[] args = new String[]{pkgName};
                        ContentValues cv = new ContentValues();
                        cv.put("uid", Integer.valueOf(curUid));
                        cr.update(this.dbTable.getUri(), cv, "packagename = ? ", args);
                    }
                }
            } while (cursor.moveToNext());
        }
    }

    private void initListFromDb() {
        String[] projection = new String[]{"uid"};
        String[] whereArg = new String[]{this.mImsi};
        Cursor cursor = GlobalContext.getContext().getContentResolver().query(this.dbTable.getUri(), projection, "imsi = ?", whereArg, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            HwLog.i(TAG, "initListFromDb , result is empty");
            if (TextUtils.isEmpty(this.mImsi)) {
                HwLog.i(TAG, "initListFromDb , for the key is empty");
            }
            return;
        }
        if (cursor.moveToFirst()) {
            int uidIndex = cursor.getColumnIndex("uid");
            do {
                int value = cursor.getInt(uidIndex);
                this.mUidListFromDb.put(value, value);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void initListFromMemory() {
        SparseIntArray sparseIntArray = NetAppUtils.getAllNetRemovableUid();
        int size = sparseIntArray.size();
        for (int i = 0; i < size; i++) {
            this.mUidListFromMemory.add(getNoTrafficAppInfo(sparseIntArray.valueAt(i)));
        }
    }

    private static NoTrafficAppInfo getNoTrafficAppInfo(int uid) {
        return new NoTrafficAppInfo(NetAppInfo.buildInfo(uid));
    }

    public static void clearOnPkgRemoved(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            HwLog.i(TAG, "clearOnPkgRemoved ,arg is wrong");
            return;
        }
        String[] whereArg = new String[]{packageName};
        if (GlobalContext.getContext().getContentResolver().delete(URI, "packagename = ?", whereArg) >= 0) {
            GlobalContext.getContext().getContentResolver().notifyChange(URI, null);
            HwLog.i(TAG, "clearOnPkgRemoved ,packageName:  " + packageName + "is removed");
        }
    }
}
