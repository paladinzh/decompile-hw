package com.huawei.systemmanager.antivirus.cache;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.optimize.smcs.DatabaseHelper;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant.VirusTableConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class VirusAppsManager {
    private static String TAG = "VirusAppsManager";
    private static VirusAppsManager mInstance;
    private DatabaseHelper mDatabaseHelper;

    public static synchronized VirusAppsManager getIntance() {
        VirusAppsManager virusAppsManager;
        synchronized (VirusAppsManager.class) {
            if (mInstance == null) {
                mInstance = new VirusAppsManager();
            }
            virusAppsManager = mInstance;
        }
        return virusAppsManager;
    }

    public void insertVirusApp(Context context, ScanResultEntity result) {
        HwLog.i(TAG, "begin insertVirusApp");
        if (isVirusAppExist(context, result.packageName)) {
            HwLog.i(TAG, "update the exist virus app, count = " + context.getContentResolver().update(VirusTableConst.URI, setContentValues(result, context), "package_name = ?", new String[]{packageName}));
        } else {
            HwLog.i(TAG, "insert virus app");
            context.getContentResolver().insert(VirusTableConst.URI, setContentValues(result, context));
        }
        HwLog.i(TAG, "end insertVirusApp");
    }

    public void deleteVirusApp(String packageName) {
        HwLog.i(TAG, "begin deleteVirusApp");
        Context context = GlobalContext.getContext();
        if (isVirusAppExist(context, packageName)) {
            HwLog.i(TAG, "deleteVirusApp, count = " + context.getContentResolver().delete(VirusTableConst.URI, "package_name = ? ", new String[]{packageName}));
        }
        HwLog.i(TAG, "end deleteVirusApp");
    }

    public boolean isVirusAppExist(Context context, String packageName) {
        HwLog.i(TAG, "begin isVirusAppExist");
        Cursor cursor = context.getContentResolver().query(VirusTableConst.URI, null, "package_name = ?", new String[]{packageName}, null);
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    HwLog.i(TAG, "cursor has result");
                    return true;
                }
                cursor.close();
                return false;
            } finally {
                cursor.close();
            }
        } else {
            HwLog.e(TAG, "query error : cursor is null");
            return false;
        }
    }

    public void refreshExitTable(Context context, ArrayList<ScanResultEntity> list) {
        HwLog.i(TAG, "begin refreshExitTable");
        int size = list.size();
        ContentValues[] contentValues = new ContentValues[size];
        for (int i = 0; i < size; i++) {
            contentValues[i] = setContentValues((ScanResultEntity) list.get(i), context);
        }
        HwLog.i(TAG, "end refreshExitTable, count = " + context.getContentResolver().bulkInsert(VirusTableConst.URI, contentValues));
    }

    private ContentValues setContentValues(ScanResultEntity result, Context context) {
        HwLog.i(TAG, "begin setContentValues");
        int uid = 0;
        try {
            uid = context.getPackageManager().getApplicationInfo(result.packageName, 0).uid;
        } catch (NameNotFoundException e) {
            HwLog.e(TAG, "NameNotFoundException");
        }
        ContentValues value = new ContentValues();
        value.put("package_name", result.packageName);
        value.put(VirusTableConst.APPNAME, result.appName);
        value.put("type", Integer.valueOf(result.type));
        value.put(VirusTableConst.APKFILEPATH, result.apkFilePath);
        value.put("virus_name", result.virusName);
        value.put(VirusTableConst.VIRUSINFO, result.virusInfo);
        value.put(VirusTableConst.PLUGNAMES, "");
        value.put(VirusTableConst.PLUGURL, "");
        value.put("version", result.mVersion);
        value.put(VirusTableConst.SCANTYPE, "");
        value.put("uid", Integer.valueOf(uid));
        return value;
    }

    public int queryVirusLevel(String packageName) {
        HwLog.i(TAG, "begin queryVirusLevel");
        this.mDatabaseHelper = new DatabaseHelper(GlobalContext.getContext());
        String[] queryColumn = new String[]{"type"};
        Cursor cursor = this.mDatabaseHelper.getReadableDatabase().query(VirusTableConst.VIRUS_TABLE, queryColumn, "package_name = ?", new String[]{packageName}, null, null, null);
        if (cursor == null) {
            HwLog.e(TAG, "error : cursor is null");
            return -1;
        }
        try {
            int type;
            if (cursor.moveToNext()) {
                type = cursor.getInt(0);
            } else {
                type = 0;
            }
            cursor.close();
            HwLog.i(TAG, "end queryVirusLevel");
            return type;
        } catch (Throwable th) {
            cursor.close();
        }
    }
}
