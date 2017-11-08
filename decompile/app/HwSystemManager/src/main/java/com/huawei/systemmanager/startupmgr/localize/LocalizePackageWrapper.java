package com.huawei.systemmanager.startupmgr.localize;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.startupmgr.db.StartupProvider.StartupLocalizeNameProvider;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.List;

public class LocalizePackageWrapper {
    private static final String TAG = "LocalizeWrapper";

    public static void resetAllLocalizeTableData(Context ctx) {
        try {
            List<PackageInfo> pkgInfoList = PackageManagerWrapper.getInstalledPackages(ctx.getPackageManager(), 8706);
            List<ContentValues> cvs = Lists.newArrayList();
            for (PackageInfo pkgInfo : pkgInfoList) {
                cvs.add(genContentValue(pkgInfo.packageName));
            }
            writeLocalizeDataToDB(ctx, cvs);
        } catch (Exception e) {
            HwLog.e(TAG, "resetAllLocalizeTableData can't load application list.");
        }
    }

    public static void resetSingleLocalizeInfo(Context ctx, String pkgName) {
        List<ContentValues> cvs = Lists.newArrayList();
        cvs.add(genContentValue(pkgName));
        writeLocalizeDataToDB(ctx, cvs);
    }

    public static String getSinglePackageLocalizeName(Context ctx, String pkgName) {
        if ("android".equals(pkgName)) {
            return ctx.getString(R.string.process_kernel_label);
        }
        String labelName = HsmPackageManager.getInstance().getLabel(pkgName);
        if (labelName == null || pkgName.equals(labelName)) {
            HwLog.w(TAG, "getSinglePackageLocalizeName package " + pkgName + " may be removed already.");
            labelName = readLocalizeData(ctx, pkgName);
        }
        return labelName;
    }

    private static ContentValues genContentValue(String pkgName) {
        ContentValues cv = new ContentValues();
        cv.put("packageName", pkgName);
        cv.put(LocalizePackageNameTable.COL_LOCALIZE_NAME, HsmPackageManager.getInstance().getLabel(pkgName));
        return cv;
    }

    private static void writeLocalizeDataToDB(Context ctx, List<ContentValues> cvs) {
        try {
            ctx.getContentResolver().bulkInsert(StartupLocalizeNameProvider.CONTENT_URI, (ContentValues[]) cvs.toArray(new ContentValues[cvs.size()]));
        } catch (SQLiteException ex) {
            ex.printStackTrace();
            HwLog.e(TAG, "writeLocalizeDataToDB catch SQLiteException: " + ex.getMessage());
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e(TAG, "writeLocalizeDataToDB catch Exception: " + ex2.getMessage());
        }
    }

    private static String readLocalizeData(Context ctx, String pkgName) {
        String string;
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(StartupLocalizeNameProvider.CONTENT_URI, new String[]{LocalizePackageNameTable.COL_LOCALIZE_NAME}, "packageName = ? ", new String[]{pkgName}, null);
            if (cursor == null || cursor.getCount() <= 0) {
                CursorHelper.closeCursor(cursor);
                return pkgName;
            }
            cursor.moveToNext();
            string = cursor.getString(0);
            return string;
        } catch (SQLiteException ex) {
            string = TAG;
            HwLog.w(string, "readLocalizeData catch SQLiteException " + ex.getMessage());
        } catch (Exception ex2) {
            string = TAG;
            HwLog.w(string, "readLocalizeData catch Exception " + ex2.getMessage());
        } finally {
            CursorHelper.closeCursor(cursor);
        }
    }
}
