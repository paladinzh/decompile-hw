package com.huawei.powergenie.core.policy;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;

public final class DBWrapper {
    private static Object mLock = new Object();
    private Context mContext;
    private int mCurScrOffItemNum = -1;
    private ContentResolver mResolver;

    public static class AppInfoItem {
        public String appName;
        public int appType;
        public int hasIcon;
        public int ownerCom;
        public int signature;
        public int sysApp;
        public int useHardware;
    }

    public static class AppScrOffItem {
        public String appName;
        public long bgCpuTime;
        public long bgUseTime;
        public int consumptionRank;
        public long gpsTime;
        public long mobileRx;
        public long mobileTx;
        public String updateTime;
        public long wakeups;
        public long wifiRx;
        public long wifiScan;
        public long wifiTx;
        public long wkTime;
    }

    public static class ScrOffItem {
        public int avgPower;
        public long gpsTime;
        public long mobileRx;
        public long mobileTx;
        public int powerUsage;
        public String reason;
        public String startTime;
        public long totalTime;
        public long wakeups;
        public long wifiRx;
        public long wifiScan;
        public long wifiTx;
        public long wkTime;
    }

    public DBWrapper(Context context) {
        this.mContext = context;
        this.mResolver = this.mContext.getContentResolver();
    }

    public Uri addPowerLevel(int level, int context, int state, int mode) {
        ContentValues values = new ContentValues(5);
        values.put("power_level", Integer.valueOf(level));
        values.put("context", Integer.valueOf(context));
        values.put("state", Integer.valueOf(state));
        values.put("mode", Integer.valueOf(mode));
        return this.mResolver.insert(PolicyProvider.PWLEVEL_URI, values);
    }

    public Uri addSwitcher(int actionId, int switcherId, int value, int flag) {
        ContentValues values = new ContentValues(4);
        values.put("power_level", Integer.valueOf(actionId));
        values.put("switcher_id", Integer.valueOf(switcherId));
        values.put("switcher_value", Integer.valueOf(value));
        values.put("switcher_flag", Integer.valueOf(flag));
        return this.mResolver.insert(PolicyProvider.SWITCHER_URI, values);
    }

    public Uri addCpuPolicy(int actionId, int powerMode, int policy, int value, String pkg, String extend) {
        ContentValues values = new ContentValues(6);
        values.put("action_id", Integer.valueOf(actionId));
        values.put("power_mode", Integer.valueOf(powerMode));
        values.put("policy_type", Integer.valueOf(policy));
        values.put("policy_value", Integer.valueOf(value));
        values.put("pkg_name", pkg);
        values.put("extend", extend);
        return this.mResolver.insert(PolicyProvider.CPU_POLICY_URI, values);
    }

    public Uri addSysLoad(int mode, int load, int times, int space, int offset, int maxTimes, String extend) {
        ContentValues values = new ContentValues(7);
        values.put("check_mode", Integer.valueOf(mode));
        values.put("up_load", Integer.valueOf(load));
        values.put("up_check_times", Integer.valueOf(times));
        values.put("up_check_space", Integer.valueOf(space));
        values.put("up_offset", Integer.valueOf(offset));
        values.put("max_check_times", Integer.valueOf(maxTimes));
        values.put("extend", extend);
        return this.mResolver.insert(PolicyProvider.SYS_LOAD_URI, values);
    }

    public Uri addBacklightPolicy(int actionId, int powerMode, int policy, int value, String pkg) {
        ContentValues values = new ContentValues(5);
        values.put("action_id", Integer.valueOf(actionId));
        values.put("power_mode", Integer.valueOf(powerMode));
        values.put("policy_type", Integer.valueOf(policy));
        values.put("policy_value", Integer.valueOf(value));
        if (pkg != null) {
            values.put("pkg_name", pkg);
        }
        return this.mResolver.insert(PolicyProvider.BACKLIGHT_URI, values);
    }

    public Uri addScrOffItem(ScrOffItem item) {
        if (item == null) {
            Log.e("DBWrapper", "addScrOnItem item is null.");
            return null;
        }
        ContentValues values = new ContentValues(16);
        values.put("startTime", item.startTime);
        values.put("totalTime", Long.valueOf(item.totalTime));
        values.put("wkTime", Long.valueOf(item.wkTime));
        values.put("powerUsage", Integer.valueOf(item.powerUsage));
        values.put("avgPower", Integer.valueOf(item.avgPower));
        values.put("wakeups", Long.valueOf(item.wakeups));
        values.put("mobileRx", Long.valueOf(item.mobileRx));
        values.put("mobileTx", Long.valueOf(item.mobileTx));
        values.put("wifiRx", Long.valueOf(item.wifiRx));
        values.put("wifiTx", Long.valueOf(item.wifiTx));
        values.put("wifiScan", Long.valueOf(item.wifiScan));
        values.put("gpsTime", Long.valueOf(item.gpsTime));
        values.put("reason", item.reason);
        Uri result = this.mResolver.insert(IntelligentProvider.SCROFF_URI, values);
        checkScrOffItemsLimit();
        return result;
    }

    private void checkScrOffItemsLimit() {
        if (this.mCurScrOffItemNum < 0) {
            Cursor cursor = null;
            try {
                cursor = this.mResolver.query(IntelligentProvider.SCROFF_URI, new String[]{"startTime"}, null, null, null);
            } catch (SQLiteException e) {
                Log.e("DBWrapper", "query ScrOff datebase", e);
            }
            if (cursor == null) {
                Log.w("DBWrapper", "scron table is not exist.");
                return;
            } else {
                this.mCurScrOffItemNum = cursor.getCount();
                cursor.close();
            }
        } else {
            this.mCurScrOffItemNum++;
        }
        if (this.mCurScrOffItemNum > 50) {
            deleteOverflowItems(IntelligentProvider.SCROFF_URI, 20);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void deleteOverflowItems(Uri tableUri, int delNum) {
        Log.i("DBWrapper", "delete overflow: " + tableUri + " num: " + delNum);
        Cursor cursor = null;
        try {
            Uri uri = tableUri;
            cursor = this.mResolver.query(uri, new String[]{"startTime"}, null, null, "startTime ASC");
        } catch (SQLiteException e) {
            Log.e("DBWrapper", "query over flow datebase", e);
        }
        if (cursor == null) {
            Log.w("DBWrapper", "table is not exist.");
            return;
        }
        try {
            String where = "startTime=? ";
            int colIndex = cursor.getColumnIndex("startTime");
            while (cursor.moveToNext() && delNum > 0) {
                delNum--;
                this.mResolver.delete(tableUri, where, new String[]{cursor.getString(colIndex)});
            }
            cursor.close();
        } catch (RuntimeException ex) {
            Log.e("DBWrapper", "RuntimeException:", ex);
        } catch (Throwable th) {
            cursor.close();
        }
    }

    public Uri addAppInfo(AppInfoItem item) {
        if (item == null) {
            Log.e("DBWrapper", "AppInfoItem item is null.");
            return null;
        }
        ContentValues values = new ContentValues(15);
        values.put("appName", item.appName);
        values.put("sysApp", Integer.valueOf(item.sysApp));
        values.put("signature", Integer.valueOf(item.signature));
        values.put("useHardware", Integer.valueOf(item.useHardware));
        values.put("appType", Integer.valueOf(item.appType));
        values.put("ownerCom", Integer.valueOf(item.ownerCom));
        values.put("hasIcon", Integer.valueOf(item.hasIcon));
        return this.mResolver.insert(IntelligentProvider.APPSINFO_URI, values);
    }

    public int deleteAppInfo(String pkgName) {
        if (pkgName == null) {
            return -1;
        }
        String[] whereArgs = new String[]{pkgName};
        return this.mResolver.delete(IntelligentProvider.APPSINFO_URI, "appName=?", whereArgs);
    }

    public boolean hasAppInfo(String pkgName) {
        if (pkgName == null) {
            return true;
        }
        Cursor cursor = null;
        try {
            String[] projection = new String[]{"appName"};
            String[] whereArgs = new String[]{pkgName};
            cursor = this.mResolver.query(IntelligentProvider.APPSINFO_URI, projection, "appName=?", whereArgs, null);
            if (cursor == null || cursor.getCount() <= 0) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            if (cursor != null) {
                cursor.close();
            }
            return true;
        } catch (SQLiteException e) {
            Log.e("DBWrapper", "query datebase", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int updateAppTypeToDb(Context context, String pkgName, int type) {
        ContentValues values = new ContentValues();
        String[] args = new String[]{pkgName};
        values.put("appType", Integer.valueOf(type));
        return context.getContentResolver().update(IntelligentProvider.APPSINFO_URI, values, "appName=?", args);
    }

    public static ArrayList<String> getAppTypeFromDb(Context context, int pkgType) {
        RuntimeException ex;
        String[] projection = new String[]{"appName"};
        String selection = "appType=?";
        String[] selectionArgs = new String[]{Integer.toString(pkgType)};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(IntelligentProvider.APPSINFO_URI, projection, selection, selectionArgs, null);
        } catch (SQLiteException e) {
            Log.e("DBWrapper", "query app static datebase", e);
        }
        if (cursor == null) {
            Log.w("DBWrapper", "selection table is not exist.");
            return null;
        }
        ArrayList<String> arrayList = null;
        try {
            int colIndext = cursor.getColumnIndex("appName");
            ArrayList<String> pkgList = null;
            while (cursor.moveToNext()) {
                try {
                    String pkgName = cursor.getString(colIndext);
                    if (pkgName != null) {
                        if (pkgList == null) {
                            arrayList = new ArrayList();
                        } else {
                            arrayList = pkgList;
                        }
                        arrayList.add(pkgName);
                    } else {
                        arrayList = pkgList;
                    }
                    pkgList = arrayList;
                } catch (RuntimeException e2) {
                    ex = e2;
                    arrayList = pkgList;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    arrayList = pkgList;
                }
            }
            cursor.close();
            arrayList = pkgList;
        } catch (RuntimeException e3) {
            ex = e3;
        }
        return arrayList;
        try {
            Log.e("DBWrapper", "RuntimeException:", ex);
            cursor.close();
            return arrayList;
        } catch (Throwable th3) {
            th2 = th3;
            cursor.close();
            throw th2;
        }
    }

    public Uri addAppScrOffItem(AppScrOffItem item) {
        if (item == null) {
            Log.e("DBWrapper", "addAppScrOffItem item is null.");
            return null;
        }
        ContentValues values = new ContentValues(15);
        values.put("appName", item.appName);
        values.put("wakeups", Long.valueOf(item.wakeups));
        values.put("bgCpuTime", Long.valueOf(item.bgCpuTime));
        values.put("bgUseTime", Long.valueOf(item.bgUseTime));
        values.put("wkTime", Long.valueOf(item.wkTime));
        values.put("wifiScan", Long.valueOf(item.wifiScan));
        values.put("gpsTime", Long.valueOf(item.gpsTime));
        values.put("mobileRx", Long.valueOf(item.mobileRx));
        values.put("mobileTx", Long.valueOf(item.mobileTx));
        values.put("wifiRx", Long.valueOf(item.wifiRx));
        values.put("wifiTx", Long.valueOf(item.wifiTx));
        values.put("consumptionRank", Integer.valueOf(item.consumptionRank));
        values.put("updateTime", item.updateTime);
        return this.mResolver.insert(IntelligentProvider.APP_SCROFF_URI, values);
    }

    public int updateAppScrOffItem(AppScrOffItem item) {
        if (item == null) {
            Log.e("DBWrapper", "updateAppScrOffItem item is null.");
            return 0;
        }
        ContentValues values = new ContentValues(15);
        values.put("appName", item.appName);
        values.put("wakeups", Long.valueOf(item.wakeups));
        values.put("bgCpuTime", Long.valueOf(item.bgCpuTime));
        values.put("bgUseTime", Long.valueOf(item.bgUseTime));
        values.put("wkTime", Long.valueOf(item.wkTime));
        values.put("wifiScan", Long.valueOf(item.wifiScan));
        values.put("gpsTime", Long.valueOf(item.gpsTime));
        values.put("mobileRx", Long.valueOf(item.mobileRx));
        values.put("mobileTx", Long.valueOf(item.mobileTx));
        values.put("wifiRx", Long.valueOf(item.wifiRx));
        values.put("wifiTx", Long.valueOf(item.wifiTx));
        values.put("consumptionRank", Integer.valueOf(item.consumptionRank));
        values.put("updateTime", item.updateTime);
        return this.mResolver.update(IntelligentProvider.APP_SCROFF_URI, values, "appName=? ", new String[]{item.appName});
    }

    public HashMap<String, AppScrOffItem> getAppScrOffItems() {
        Cursor cursor = null;
        try {
            cursor = this.mResolver.query(IntelligentProvider.APP_SCROFF_URI, null, null, null, null);
        } catch (Throwable e) {
            Log.e("DBWrapper", "query app scroff datebase", e);
        }
        if (cursor == null) {
            Log.w("DBWrapper", "app scroff table is not exist.");
            return null;
        }
        HashMap<String, AppScrOffItem> itemList = new HashMap();
        try {
            int colNameIndex = cursor.getColumnIndex("appName");
            int colWkCountIndex = cursor.getColumnIndex("wakeups");
            int colCpuTimeIndex = cursor.getColumnIndex("bgCpuTime");
            int colUseTimeIndex = cursor.getColumnIndex("bgUseTime");
            int colWkTimeIndex = cursor.getColumnIndex("wkTime");
            int colWifiScanIndex = cursor.getColumnIndex("wifiScan");
            int colGpsTimeIndex = cursor.getColumnIndex("gpsTime");
            int colMobileRxIndex = cursor.getColumnIndex("mobileRx");
            int colMobileTxIndex = cursor.getColumnIndex("mobileTx");
            int colWifiRxIndex = cursor.getColumnIndex("wifiRx");
            int colWifiTxIndex = cursor.getColumnIndex("wifiTx");
            int colConsumptionIndex = cursor.getColumnIndex("consumptionRank");
            int colUpdateTimeIndex = cursor.getColumnIndex("updateTime");
            while (cursor.moveToNext()) {
                AppScrOffItem item = new AppScrOffItem();
                item.appName = cursor.getString(colNameIndex);
                item.wakeups = cursor.getLong(colWkCountIndex);
                item.bgCpuTime = cursor.getLong(colCpuTimeIndex);
                item.bgUseTime = cursor.getLong(colUseTimeIndex);
                item.wkTime = cursor.getLong(colWkTimeIndex);
                item.wifiScan = cursor.getLong(colWifiScanIndex);
                item.gpsTime = cursor.getLong(colGpsTimeIndex);
                item.mobileRx = cursor.getLong(colMobileRxIndex);
                item.mobileTx = cursor.getLong(colMobileTxIndex);
                item.wifiRx = cursor.getLong(colWifiRxIndex);
                item.wifiTx = cursor.getLong(colWifiTxIndex);
                item.consumptionRank = cursor.getInt(colConsumptionIndex);
                item.updateTime = cursor.getString(colUpdateTimeIndex);
                itemList.put(item.appName, item);
            }
        } catch (Throwable ex) {
            Log.e("DBWrapper", "RuntimeException:", ex);
        } finally {
            cursor.close();
        }
        return itemList;
    }
}
