package com.huawei.systemmanager.applock.datacenter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import com.google.android.collect.Maps;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.applock.datacenter.AppLockProvider.LockStatusProvider;
import com.huawei.systemmanager.applock.datacenter.tbl.AppLockStatusTable;
import com.huawei.systemmanager.applock.utils.AppLockFilterOutUtils;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.Map;
import java.util.Set;

public class LoadingMonitorListThread extends Thread {
    private static final String TAG = "LoadingMonitorListThread";
    private Context mContext = null;

    public LoadingMonitorListThread(Context context) {
        super("applock_LoadingMonitorListThread");
        this.mContext = context;
    }

    public void run() {
        Map<String, Integer> databaseCache = Maps.newHashMap();
        Map<String, Integer> pmCache = Maps.newHashMap();
        Set<String> delCache = Sets.newHashSet();
        loadDataFromDatabse(databaseCache);
        loadDataFromPm(pmCache, delCache);
        HwLog.d(TAG, "run loadDataFromDatabse:" + databaseCache);
        if (databaseCache.isEmpty()) {
            HwLog.d(TAG, "run loadDataFromPm size:" + pmCache.size() + "----" + pmCache);
            initDataToDatabase(pmCache);
            return;
        }
        handleDeltaData(databaseCache, pmCache, delCache);
    }

    private void loadDataFromDatabse(Map<String, Integer> cacheData) {
        try {
            Cursor cursor = this.mContext.getContentResolver().query(LockStatusProvider.CONTENT_URI, new String[]{"packageName", AppLockStatusTable.COL_LOCK_STATUS}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    cacheData.put(cursor.getString(0), Integer.valueOf(cursor.getInt(1)));
                }
            }
            CursorHelper.closeCursor(cursor);
        } catch (Throwable th) {
            CursorHelper.closeCursor(null);
        }
    }

    private void loadDataFromPm(Map<String, Integer> includeCache, Set<String> excludeCache) {
        Intent resolveIntent = new Intent("android.intent.action.MAIN", null);
        resolveIntent.addCategory("android.intent.category.LAUNCHER");
        for (ResolveInfo temp : PackageManagerWrapper.queryIntentActivities(this.mContext.getPackageManager(), resolveIntent, 0)) {
            filteroutOrAdd(includeCache, excludeCache, temp.activityInfo.packageName);
        }
    }

    private void filteroutOrAdd(Map<String, Integer> cacheData, Set<String> excludeCache, String pkgName) {
        if (AppLockFilterOutUtils.needFilterOut(this.mContext, pkgName)) {
            excludeCache.add(pkgName);
        } else {
            cacheData.put(pkgName, Integer.valueOf(0));
        }
    }

    private void initDataToDatabase(Map<String, Integer> rowDatas) {
        if (!MultiUserUtils.isInMultiUserMode()) {
            HwLog.d(TAG, "initDataToDatabase size: " + rowDatas.size());
            AppLockDBHelper.getInstance(this.mContext).batchReplaceDefaultLockStatus(rowDatas.keySet());
        }
    }

    private void handleDeltaData(Map<String, Integer> databaseCache, Map<String, Integer> pmCache, Set<String> delCache) {
        if (!MultiUserUtils.isInMultiUserMode()) {
            PackageManager pm = this.mContext.getPackageManager();
            if (pm == null || !pm.isSafeMode()) {
                Set delPkgs = Sets.newHashSet((Iterable) delCache);
                delPkgs.addAll(databaseCache.keySet());
                delPkgs.removeAll(pmCache.keySet());
                if (!delPkgs.isEmpty()) {
                    HwLog.d(TAG, "handleDeltaData delPkg: " + delPkgs);
                    AppLockDBHelper.getInstance(this.mContext).deleteLockData(delPkgs);
                }
                Set<String> addPkgs = Sets.newHashSet();
                addPkgs.addAll(pmCache.keySet());
                addPkgs.removeAll(databaseCache.keySet());
                if (!addPkgs.isEmpty()) {
                    HwLog.d(TAG, "handleDeltaData addPkg: " + addPkgs);
                    AppLockDBHelper.getInstance(this.mContext).batchReplaceDefaultLockStatus(addPkgs);
                }
            }
        }
    }
}
