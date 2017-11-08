package com.huawei.systemmanager.comm.grule.rules.cloud;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.UserHandle;
import android.text.TextUtils;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeBlackList;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.ControlRangeWhiteList;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class CloudControlRangeNamesHelper {
    private static final String LOG_TAG = "CloudNameListHelper";
    private static CloudControlRangeNamesHelper sInstance;
    private ArrayList<String> mBlackApps = null;
    private Object mBlackAppsSync = new Object();
    private Context mContext = null;
    private ArrayList<String> mWhiteApps = null;
    private Object mWhiteAppsSync = new Object();

    private class BlackListChangeObserver extends ContentObserver {
        public BlackListChangeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(CloudControlRangeNamesHelper.LOG_TAG, "BlackListChangeObserver  onChange resetShouldMonitorMap ");
            DBAdapter.getInstance(CloudControlRangeNamesHelper.this.mContext).resetShouldMonitorMap();
            Cursor cursor = CloudControlRangeNamesHelper.this.mContext.getContentResolver().query(ControlRangeBlackList.CONTENT_OUTERTABLE_URI, null, null, null, null);
            if (CursorHelper.checkCursorValid(cursor)) {
                ArrayList<String> blackApps = new ArrayList();
                int pkgNameIndex = cursor.getColumnIndex("packageName");
                while (cursor.moveToNext()) {
                    blackApps.add(cursor.getString(pkgNameIndex));
                }
                cursor.close();
                synchronized (CloudControlRangeNamesHelper.this.mBlackAppsSync) {
                    if (CloudControlRangeNamesHelper.this.mBlackApps == null) {
                        CloudControlRangeNamesHelper.this.mBlackApps = new ArrayList();
                    }
                    CloudControlRangeNamesHelper.this.sendListChangeBroadcast("com.rainbow.blacklist.change", CloudControlRangeNamesHelper.this.mBlackApps, blackApps);
                    CloudControlRangeNamesHelper.this.mBlackApps.clear();
                    CloudControlRangeNamesHelper.this.mBlackApps.addAll(blackApps);
                }
            }
        }
    }

    private class WhiteListChangeObserver extends ContentObserver {
        public WhiteListChangeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HwLog.i(CloudControlRangeNamesHelper.LOG_TAG, "WhiteListChangeObserver  onChange resetShouldMonitorMap ");
            DBAdapter.getInstance(CloudControlRangeNamesHelper.this.mContext).resetShouldMonitorMap();
            Cursor cursor = CloudControlRangeNamesHelper.this.mContext.getContentResolver().query(ControlRangeWhiteList.CONTENT_OUTERTABLE_URI, null, null, null, null);
            if (CursorHelper.checkCursorValid(cursor)) {
                ArrayList<String> whiteApps = new ArrayList();
                int pkgNameIndex = cursor.getColumnIndex("packageName");
                while (cursor.moveToNext()) {
                    whiteApps.add(cursor.getString(pkgNameIndex));
                }
                cursor.close();
                synchronized (CloudControlRangeNamesHelper.this.mWhiteAppsSync) {
                    if (CloudControlRangeNamesHelper.this.mWhiteApps == null) {
                        CloudControlRangeNamesHelper.this.mWhiteApps = new ArrayList();
                    }
                    CloudControlRangeNamesHelper.this.sendListChangeBroadcast(ControlRangeWhiteList.WHITE_LIST_CHANGE_ACTION, CloudControlRangeNamesHelper.this.mWhiteApps, whiteApps);
                    CloudControlRangeNamesHelper.this.mWhiteApps.clear();
                    CloudControlRangeNamesHelper.this.mWhiteApps.addAll(whiteApps);
                }
            }
        }
    }

    private CloudControlRangeNamesHelper(Context context) {
        this.mContext = context.getApplicationContext();
        HandlerThread handlerThread = new HandlerThread(LOG_TAG);
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        this.mContext.getContentResolver().registerContentObserver(ControlRangeWhiteList.CONTENT_OUTERTABLE_URI, true, new WhiteListChangeObserver(new Handler(looper)));
        this.mContext.getContentResolver().registerContentObserver(ControlRangeBlackList.CONTENT_OUTERTABLE_URI, true, new BlackListChangeObserver(new Handler(looper)));
    }

    public static synchronized CloudControlRangeNamesHelper getInstance(Context context) {
        CloudControlRangeNamesHelper cloudControlRangeNamesHelper;
        synchronized (CloudControlRangeNamesHelper.class) {
            if (sInstance == null && context != null) {
                sInstance = new CloudControlRangeNamesHelper(context);
            }
            cloudControlRangeNamesHelper = sInstance;
        }
        return cloudControlRangeNamesHelper;
    }

    public boolean isPkgInWhiteList(String pkgName) {
        if (pkgName == null || TextUtils.isEmpty(pkgName)) {
            return false;
        }
        boolean result;
        initWhitePkgList();
        synchronized (this.mWhiteAppsSync) {
            result = this.mWhiteApps.contains(pkgName);
        }
        return result;
    }

    public boolean isPkgInBlackList(String pkgName) {
        if (pkgName == null || TextUtils.isEmpty(pkgName)) {
            return false;
        }
        boolean result;
        initBlackPkgList();
        synchronized (this.mBlackAppsSync) {
            result = this.mBlackApps.contains(pkgName);
        }
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void initWhitePkgList() {
        synchronized (this.mWhiteAppsSync) {
            if (this.mWhiteApps != null) {
                return;
            }
            this.mWhiteApps = new ArrayList();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void initBlackPkgList() {
        synchronized (this.mBlackAppsSync) {
            if (this.mBlackApps != null) {
                return;
            }
            this.mBlackApps = new ArrayList();
        }
    }

    private void sendListChangeBroadcast(String action, ArrayList<String> oldList, ArrayList<String> newList) {
        Intent intent = new Intent(action);
        intent.setPackage(this.mContext.getPackageName());
        HwLog.d(LOG_TAG, "sendListChangeBroadcast with oldList: " + oldList.toString() + " newList:" + newList);
        ArrayList<String> retain = new ArrayList(oldList);
        retain.retainAll(newList);
        newList.removeAll(retain);
        if (!newList.isEmpty()) {
            intent.putStringArrayListExtra(ControlRangeWhiteList.CHANGED_ADD_LIST_KEY, newList);
        }
        oldList.removeAll(retain);
        if (!oldList.isEmpty()) {
            intent.putStringArrayListExtra(ControlRangeWhiteList.CHANGED_MINUS_LIST_KEY, oldList);
        }
        if (newList.isEmpty() && oldList.isEmpty()) {
            HwLog.e(LOG_TAG, "sendListChangeBroadcast the addList and minusList are all empty!");
        } else {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.OWNER);
        }
    }
}
