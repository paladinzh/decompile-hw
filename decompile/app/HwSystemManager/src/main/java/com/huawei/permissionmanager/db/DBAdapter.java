package com.huawei.permissionmanager.db;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.permission.HoldServiceConst;
import com.huawei.permission.IHoldService;
import com.huawei.permission.IHoldService.Stub;
import com.huawei.permission.MPermissionUtil;
import com.huawei.permission.PermissionServiceManager;
import com.huawei.permissionmanager.model.HwAppPermissions;
import com.huawei.permissionmanager.ui.Permission;
import com.huawei.permissionmanager.utils.HwPermissionInfo;
import com.huawei.permissionmanager.utils.SettingsDbUtils;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.permissionmanager.utils.ShareLib;
import com.huawei.permissionmanager.utils.SharedPrefUtils;
import com.huawei.systemmanager.addviewmonitor.AddViewAppManager;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import com.huawei.systemmanager.util.procpolicy.ProcessPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class DBAdapter {
    public static final String APP_GOOGLE = "com.android";
    public static final String APP_HUAWEI = "com.huawei";
    public static final Uri BLOCK_TABLE_NAME_URI = Uri.parse("content://com.huawei.permissionmanager.provider.PermissionDataProvider/permission");
    public static final Uri COMMON_TABLE_URI = Uri.parse("content://com.huawei.permissionmanager.provider.PermissionDataProvider/common");
    public static final String CUST_URL = "/data/cust";
    private static final String DO_FIRST_BOOT_PERMISSION_INIT = "do_newapps_permission_init";
    private static final String LOG_TAG = "PermissionDBAdapter";
    public static final int PERMISSION_TYPE_ALLOWED = 1;
    public static final int PERMISSION_TYPE_BLOCKED = 2;
    public static final int PERMISSION_TYPE_FAIL = 0;
    public static final int PERMISSION_TYPE_REMIND = 3;
    public static final Uri REPLACE_PERMISSION_URI = Uri.parse("content://com.huawei.permissionmanager.provider.PermissionDataProvider/permission_replace");
    private static final Uri RUNTIME_TABLE_URI = Uri.parse("content://com.huawei.permissionmanager.provider.PermissionDataProvider/runtimePermissions");
    public static final String SYSTEM_APP = "/system/app";
    private static List<DataChangeListener> mCallbacks = new ArrayList();
    static Object mThreadSync = new Object();
    private static DBAdapter sInstance = null;
    private ArrayList<HwPermissionInfo> CONTROLLED_PERMISSION_TYPES = null;
    private IntentFilter filter = new IntentFilter();
    private List<AppInfo> mAppList = Collections.synchronizedList(new ArrayList());
    private AppInfoVersion mCacheVersion = new AppInfoVersion();
    private Context mContext;
    private PermissionContentObserver mObserver = new PermissionContentObserver(new Handler(Looper.getMainLooper())) {
        public void onChange(boolean selfChange) {
            DBAdapter.this.refreshForChanges();
        }
    };
    private HsmPackageManager mPm;
    private PermissionServiceManager mPsm = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                Log.i(DBAdapter.LOG_TAG, "mReceiver  onReceive  intent.getAction(): " + action);
                if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                    DBAdapter.this.refreshForChanges();
                }
            }
        }
    };
    private Map<String, Boolean> mShouldMonitorMap = new HashMap();

    private static class AppInfoVersion extends ArrayList<AppInfo> {
        private static final long serialVersionUID = 1;
        private long mAppListVersion;
        private long mDbDataVersion;

        private AppInfoVersion() {
            this.mAppListVersion = -1;
            this.mDbDataVersion = 0;
        }

        public boolean expiredThan(long dbVersion) {
            return this.mAppListVersion < dbVersion;
        }

        public void incDbDataVersion() {
            this.mDbDataVersion++;
        }

        public long getDBDataVersion() {
            return this.mDbDataVersion;
        }

        public void setAppListVersion(long targetDbVersion) {
            this.mAppListVersion = targetDbVersion;
        }

        public long getAppListVersion() {
            return this.mAppListVersion;
        }
    }

    public static class ApplistState {
        ArrayList<String> newAppsList = new ArrayList();

        public ApplistState(ArrayList<String> newApps) {
            this.newAppsList = newApps;
        }

        public ArrayList<String> getNewAppsList() {
            return this.newAppsList;
        }
    }

    private class NewAppsInitThread extends Thread {
        boolean mDataCleared;
        final List<String> mNewApps = new ArrayList();

        public NewAppsInitThread(List<String> newApps, boolean dataCleared) {
            super(DBAdapter.DO_FIRST_BOOT_PERMISSION_INIT);
            this.mNewApps.addAll(newApps);
            this.mDataCleared = dataCleared;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (DBAdapter.mThreadSync) {
                if (!AppInitializer.shouldInitilizePermissionData()) {
                } else if (this.mNewApps.size() <= 0) {
                } else {
                    PackageManager pm = DBAdapter.this.mContext.getPackageManager();
                    List<ContentValues> constValuesList = new ArrayList();
                    List<String> toSyncList = new ArrayList();
                    for (String pkgName : this.mNewApps) {
                        try {
                            PackageInfo packageInfo = PackageManagerWrapper.getPackageInfo(pm, pkgName, 12288);
                            if (packageInfo != null) {
                                if (packageInfo.applicationInfo != null) {
                                    ContentValues value;
                                    if (this.mDataCleared) {
                                        value = DBAdapter.getInitialConfig(DBAdapter.this.mContext, pkgName, packageInfo.applicationInfo.uid, true);
                                    } else {
                                        value = getNewAppsCfg(packageInfo);
                                    }
                                    if (value != null) {
                                        AddViewAppManager.trustIfNeeded(DBAdapter.this.mContext, packageInfo.applicationInfo.uid, pkgName, value);
                                        if ((packageInfo.applicationInfo.flags & 1) == 0 && !this.mDataCleared) {
                                            HwLog.i(DBAdapter.LOG_TAG, "It's a 3rd app, should sync to sys data.");
                                            toSyncList.add(pkgName);
                                        }
                                        constValuesList.add(value);
                                        if (Log.HWINFO) {
                                            HwLog.i(DBAdapter.LOG_TAG, "init new apps. pkg:" + pkgName + ", code:" + value.get("permissionCode") + ", cfg:" + value.get("permissionCfg"));
                                        }
                                    }
                                }
                            }
                            HwLog.w(DBAdapter.LOG_TAG, "getNewAppsCfg get packageinfo is null and package name is " + pkgName);
                        } catch (NameNotFoundException e) {
                            HwLog.w(DBAdapter.LOG_TAG, "Get apps info  error. " + pkgName, e);
                        }
                    }
                    if (constValuesList.size() > 0) {
                        try {
                            ContentResolver contentResolver = DBAdapter.this.mContext.getContentResolver();
                            int rc = contentResolver.bulkInsert(DBAdapter.BLOCK_TABLE_NAME_URI, (ContentValues[]) constValuesList.toArray(new ContentValues[constValuesList.size()]));
                            if (Log.HWINFO) {
                                HwLog.i(DBAdapter.LOG_TAG, "insert size:" + constValuesList.size() + ", inserted size:" + rc);
                            }
                            constValuesList.clear();
                            if (rc > 0) {
                                for (String pkg : toSyncList) {
                                    HwAppPermissions.create(DBAdapter.this.mContext, pkg).setHsmDefaultValues("repair data");
                                }
                            } else {
                                HwLog.i(DBAdapter.LOG_TAG, "No new data inserted, don't sync to sys.");
                            }
                        } catch (Exception e2) {
                            HwLog.w(DBAdapter.LOG_TAG, "add apps into db fail.", e2);
                        }
                    }
                    if (this.mDataCleared) {
                        SharedPrefUtils.setDataClearedFlag(DBAdapter.this.mContext, false);
                    }
                }
            }
        }

        private ContentValues getNewAppsCfg(PackageInfo packageInfo) {
            String pkgName = packageInfo.applicationInfo.packageName;
            if ((packageInfo.applicationInfo.flags & 1) == 0) {
                return DBAdapter.getInitialConfig(DBAdapter.this.mContext, pkgName, packageInfo.applicationInfo.uid, true);
            }
            if (packageInfo.applicationInfo.targetSdkVersion <= 22) {
                return DBAdapter.getInitialConfig(DBAdapter.this.mContext, pkgName, packageInfo.applicationInfo.uid, true);
            }
            return AppInitializer.getInitialConfigFromSystemConfig(DBAdapter.this.mContext, packageInfo);
        }
    }

    private class UpgradeFromLToMThread extends Thread {
        List<AppInfo> applist = new ArrayList();
        List<String> newApps = new ArrayList();

        public UpgradeFromLToMThread(List<AppInfo> applist, List<String> newApps) {
            super("Upgrade2NThread");
            this.applist.addAll(applist);
            this.newApps.addAll(newApps);
        }

        public void run() {
            if (AppInitializer.shouldInitilizePermissionData()) {
                try {
                    syncFromHsmToSysData(this.applist, this.newApps, "hota to N");
                } catch (NullPointerException e) {
                    HwLog.e(DBAdapter.LOG_TAG, "UpgradeFromLToM error.", e);
                } catch (Exception e2) {
                    HwLog.e(DBAdapter.LOG_TAG, "UpgradeFromLToM error.", e2);
                }
            }
        }

        private void syncFromHsmToSysData(List<AppInfo> applist, List<String> newApps, String reason) {
            if (Log.HWINFO) {
                HwLog.i(DBAdapter.LOG_TAG, "hota begin. app size:" + applist.size() + ", reason:" + reason);
            }
            for (AppInfo app : applist) {
                if (!newApps.contains(app.mPkgName)) {
                    HwAppPermissions.create(DBAdapter.this.mContext, app.mPkgName).setHsmValuesToSys(reason, 15);
                    if (app.mTrust == 1) {
                        HwLog.e(DBAdapter.LOG_TAG, "hota to N allow add view, because trust:" + app.mPkgName);
                        AddViewAppManager.trust(DBAdapter.this.mContext, app.mAppUid, app.mPkgName);
                    }
                }
            }
            if (Log.HWINFO) {
                HwLog.i(DBAdapter.LOG_TAG, "hota end without sync on android N.");
            }
        }
    }

    public static android.net.Uri addToRuntimeTable(android.content.Context r11, java.lang.String r12, int r13, int r14) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00a4 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r10 = 0;
        r0 = "PermissionDBAdapter";
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "add to runtime table ";
        r1 = r1.append(r2);
        r1 = r1.append(r12);
        r2 = ", type:";
        r1 = r1.append(r2);
        r1 = r1.append(r13);
        r1 = r1.toString();
        com.huawei.systemmanager.util.HwLog.i(r0, r1);
        r6 = 0;
        r0 = r11.getContentResolver();	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r1 = RUNTIME_TABLE_URI;	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r3 = "packageName = ? and permissionType = ?";	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r2 = 2;	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r4 = new java.lang.String[r2];	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r2 = 0;	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r4[r2] = r12;	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r2 = java.lang.String.valueOf(r13);	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r5 = 1;	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r4[r5] = r2;	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r2 = 0;	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r5 = 0;	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        if (r6 == 0) goto L_0x008f;	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
    L_0x0045:
        r0 = r6.getCount();	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        if (r0 > 0) goto L_0x008f;	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
    L_0x004b:
        r9 = new android.content.ContentValues;	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r9.<init>();	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r0 = "packageName";	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r9.put(r0, r12);	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r0 = "permissionType";	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r1 = java.lang.Integer.valueOf(r13);	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r9.put(r0, r1);	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r0 = "uid";	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r1 = java.lang.Integer.valueOf(r14);	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r9.put(r0, r1);	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r0 = r11.getContentResolver();	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r1 = RUNTIME_TABLE_URI;	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r8 = r0.insert(r1, r9);	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        if (r8 != 0) goto L_0x0085;	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
    L_0x0076:
        r0 = "PermissionDBAdapter";	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r1 = "update database error";	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        com.huawei.systemmanager.util.HwLog.e(r0, r1);	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        if (r6 == 0) goto L_0x0084;
    L_0x0081:
        r6.close();
    L_0x0084:
        return r10;
    L_0x0085:
        r0 = 0;
        updateTrustValue(r11, r14, r12, r0);	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        if (r6 == 0) goto L_0x008e;
    L_0x008b:
        r6.close();
    L_0x008e:
        return r8;
    L_0x008f:
        if (r6 == 0) goto L_0x0094;
    L_0x0091:
        r6.close();
    L_0x0094:
        return r10;
    L_0x0095:
        r7 = move-exception;
        r0 = "PermissionDBAdapter";	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        r1 = "addRuntimePermission error";	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        com.huawei.systemmanager.util.HwLog.e(r0, r1, r7);	 Catch:{ Exception -> 0x0095, all -> 0x00a5 }
        if (r6 == 0) goto L_0x00a4;
    L_0x00a1:
        r6.close();
    L_0x00a4:
        return r10;
    L_0x00a5:
        r0 = move-exception;
        if (r6 == 0) goto L_0x00ab;
    L_0x00a8:
        r6.close();
    L_0x00ab:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.DBAdapter.addToRuntimeTable(android.content.Context, java.lang.String, int, int):android.net.Uri");
    }

    public static void checkUidByProvider(android.content.Context r12, int r13, java.lang.String r14) {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r6 = 0;
        r0 = r12.getContentResolver();	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r1 = BLOCK_TABLE_NAME_URI;	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r3 = "packageName = ?";	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r2 = 1;	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r4 = new java.lang.String[r2];	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r2 = 0;	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r4[r2] = r14;	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r2 = 0;	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r5 = 0;	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        if (r6 == 0) goto L_0x008d;	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
    L_0x0018:
        r0 = r6.getCount();	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        if (r0 <= 0) goto L_0x008d;	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
    L_0x001e:
        r6.moveToFirst();	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r0 = "uid";	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r10 = r6.getColumnIndex(r0);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r9 = r6.getInt(r10);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        if (r13 == r9) goto L_0x008d;	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
    L_0x002e:
        r0 = "PermissionDBAdapter";	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r1.<init>();	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r2 = "uid for ";	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r1 = r1.append(r14);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r2 = " inconsistent, correct it from ";	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r1 = r1.append(r9);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r2 = " to ";	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r1 = r1.append(r13);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r1 = r1.toString();	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        com.huawei.systemmanager.util.HwLog.i(r0, r1);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r7 = new android.content.ContentValues;	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r7.<init>();	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r0 = "packageName";	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r7.put(r0, r14);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r0 = "uid";	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r1 = java.lang.Integer.valueOf(r13);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r7.put(r0, r1);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r0 = r12.getContentResolver();	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r1 = REPLACE_PERMISSION_URI;	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r11 = r0.insert(r1, r7);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        if (r11 != 0) goto L_0x008d;	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
    L_0x007f:
        r0 = "PermissionDBAdapter";	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r1 = "insert permission data fail.";	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r2 = new java.lang.Throwable;	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r2.<init>();	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        com.huawei.systemmanager.util.HwLog.w(r0, r1, r2);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
    L_0x008d:
        if (r6 == 0) goto L_0x0092;
    L_0x008f:
        r6.close();
    L_0x0092:
        return;
    L_0x0093:
        r8 = move-exception;
        r0 = "PermissionDBAdapter";	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        r1 = "checkUid fail.";	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        com.huawei.systemmanager.util.HwLog.e(r0, r1, r8);	 Catch:{ Exception -> 0x0093, all -> 0x00a3 }
        if (r6 == 0) goto L_0x0092;
    L_0x009f:
        r6.close();
        goto L_0x0092;
    L_0x00a3:
        r0 = move-exception;
        if (r6 == 0) goto L_0x00a9;
    L_0x00a6:
        r6.close();
    L_0x00a9:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.DBAdapter.checkUidByProvider(android.content.Context, int, java.lang.String):void");
    }

    public static android.content.ContentValues getInitialConfig(android.content.Context r1, java.lang.String r2, int r3, boolean r4) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.permissionmanager.db.DBAdapter.getInitialConfig(android.content.Context, java.lang.String, int, boolean):android.content.ContentValues
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.DBAdapter.getInitialConfig(android.content.Context, java.lang.String, int, boolean):android.content.ContentValues");
    }

    public static boolean hasRuntimePermission(android.content.Context r10, java.lang.String r11, int r12) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x003f in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r8 = 1;
        r9 = 0;
        r6 = 0;
        r0 = r10.getContentResolver();	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r1 = RUNTIME_TABLE_URI;	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r3 = "packageName = ? and permissionType = ?";	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r2 = 2;	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r4 = new java.lang.String[r2];	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r2 = 0;	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r4[r2] = r11;	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r2 = java.lang.String.valueOf(r12);	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r5 = 1;	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r4[r5] = r2;	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r2 = 0;	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r5 = 0;	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        if (r6 == 0) goto L_0x002e;	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
    L_0x0021:
        r0 = r6.getCount();	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        if (r0 <= 0) goto L_0x002e;
    L_0x0027:
        r0 = r8;
    L_0x0028:
        if (r6 == 0) goto L_0x002d;
    L_0x002a:
        r6.close();
    L_0x002d:
        return r0;
    L_0x002e:
        r0 = r9;
        goto L_0x0028;
    L_0x0030:
        r7 = move-exception;
        r0 = "PermissionDBAdapter";	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r1 = "query runtimePermission error";	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        com.huawei.systemmanager.util.HwLog.e(r0, r1, r7);	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        if (r6 == 0) goto L_0x003f;
    L_0x003c:
        r6.close();
    L_0x003f:
        return r9;
    L_0x0040:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0046;
    L_0x0043:
        r6.close();
    L_0x0046:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.DBAdapter.hasRuntimePermission(android.content.Context, java.lang.String, int):boolean");
    }

    private java.util.Map<java.lang.String, com.huawei.permissionmanager.db.DBPermissionItem> loadAllDataFromDb() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00a7 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r10 = this;
        r8 = new java.util.HashMap;
        r8.<init>();
        r5 = java.util.Collections.synchronizedMap(r8);
        r5.clear();
        r0 = 0;
        r0 = r10.queryAllData();	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        if (r0 != 0) goto L_0x002b;	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
    L_0x0013:
        r8 = "PermissionDBAdapter";	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r9 = "refreshPermissionMap - cursor is null ";	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        com.huawei.systemmanager.util.HwLog.e(r8, r9);	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r8 = new java.util.HashMap;	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r8.<init>();	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r8 = java.util.Collections.synchronizedMap(r8);	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        if (r0 == 0) goto L_0x002a;
    L_0x0027:
        r0.close();
    L_0x002a:
        return r8;
    L_0x002b:
        r8 = r0.getCount();	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        if (r8 <= 0) goto L_0x0037;	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
    L_0x0031:
        r8 = r0.getColumnCount();	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        if (r8 > 0) goto L_0x004f;	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
    L_0x0037:
        r8 = "PermissionDBAdapter";	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r9 = "refreshPermissionMap - no permission data.";	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        com.huawei.systemmanager.util.HwLog.w(r8, r9);	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r8 = new java.util.HashMap;	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r8.<init>();	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r8 = java.util.Collections.synchronizedMap(r8);	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        if (r0 == 0) goto L_0x004e;
    L_0x004b:
        r0.close();
    L_0x004e:
        return r8;
    L_0x004f:
        r8 = "packageName";	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r6 = r0.getColumnIndex(r8);	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r8 = "permissionCode";	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r3 = r0.getColumnIndex(r8);	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r8 = "trust";	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r7 = r0.getColumnIndex(r8);	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r8 = "permissionCfg";	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r2 = r0.getColumnIndex(r8);	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r0.moveToFirst();	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
    L_0x006e:
        r8 = r0.isAfterLast();	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        if (r8 != 0) goto L_0x00a8;	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
    L_0x0074:
        r4 = new com.huawei.permissionmanager.db.DBPermissionItem;	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r8 = r0.getString(r6);	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r4.<init>(r8);	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r8 = r0.getInt(r3);	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r4.mPermissionCode = r8;	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r8 = r0.getInt(r7);	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r4.mTrustCode = r8;	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r8 = r0.getInt(r2);	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r4.mPermissionCfg = r8;	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r8 = r4.mPkgName;	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r5.put(r8, r4);	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r0.moveToNext();	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        goto L_0x006e;
    L_0x0098:
        r1 = move-exception;
        r8 = "PermissionDBAdapter";	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        r9 = "loadPermissionMap fial.";	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        com.huawei.systemmanager.util.HwLog.e(r8, r9, r1);	 Catch:{ Exception -> 0x0098, all -> 0x00ae }
        if (r0 == 0) goto L_0x00a7;
    L_0x00a4:
        r0.close();
    L_0x00a7:
        return r5;
    L_0x00a8:
        if (r0 == 0) goto L_0x00ad;
    L_0x00aa:
        r0.close();
    L_0x00ad:
        return r5;
    L_0x00ae:
        r8 = move-exception;
        if (r0 == 0) goto L_0x00b4;
    L_0x00b1:
        r0.close();
    L_0x00b4:
        throw r8;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.DBAdapter.loadAllDataFromDb():java.util.Map<java.lang.String, com.huawei.permissionmanager.db.DBPermissionItem>");
    }

    private java.util.List<java.lang.String> loadPermissionList() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0073 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r7 = this;
        r4 = new java.util.ArrayList;
        r4.<init>();
        r0 = 0;
        r0 = r7.queryAllData();	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        if (r0 != 0) goto L_0x001b;	 Catch:{ Exception -> 0x0064, all -> 0x007a }
    L_0x000c:
        r5 = "PermissionDBAdapter";	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        r6 = "loadPermissionList - cursor is null ";	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        com.huawei.systemmanager.util.HwLog.e(r5, r6);	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        if (r0 == 0) goto L_0x001a;
    L_0x0017:
        r0.close();
    L_0x001a:
        return r4;
    L_0x001b:
        r5 = r0.getCount();	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        if (r5 <= 0) goto L_0x0027;	 Catch:{ Exception -> 0x0064, all -> 0x007a }
    L_0x0021:
        r5 = r0.getColumnCount();	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        if (r5 > 0) goto L_0x0036;	 Catch:{ Exception -> 0x0064, all -> 0x007a }
    L_0x0027:
        r5 = "PermissionDBAdapter";	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        r6 = "loadPermissionList - invalid cursor ";	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        com.huawei.systemmanager.util.HwLog.e(r5, r6);	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        if (r0 == 0) goto L_0x0035;
    L_0x0032:
        r0.close();
    L_0x0035:
        return r4;
    L_0x0036:
        r3 = 0;
        r5 = "packageName";	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        r2 = r0.getColumnIndex(r5);	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        r5 = -1;	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        if (r5 != r2) goto L_0x0050;	 Catch:{ Exception -> 0x0064, all -> 0x007a }
    L_0x0041:
        r5 = "PermissionDBAdapter";	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        r6 = "loadPermissionList - index invalid.";	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        com.huawei.systemmanager.util.HwLog.e(r5, r6);	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        if (r0 == 0) goto L_0x004f;
    L_0x004c:
        r0.close();
    L_0x004f:
        return r4;
    L_0x0050:
        r0.moveToFirst();	 Catch:{ Exception -> 0x0064, all -> 0x007a }
    L_0x0053:
        r5 = r0.isAfterLast();	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        if (r5 != 0) goto L_0x0074;	 Catch:{ Exception -> 0x0064, all -> 0x007a }
    L_0x0059:
        r3 = r0.getString(r2);	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        r4.add(r3);	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        r0.moveToNext();	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        goto L_0x0053;
    L_0x0064:
        r1 = move-exception;
        r5 = "PermissionDBAdapter";	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        r6 = "Load permission list fail.";	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        com.huawei.systemmanager.util.HwLog.e(r5, r6, r1);	 Catch:{ Exception -> 0x0064, all -> 0x007a }
        if (r0 == 0) goto L_0x0073;
    L_0x0070:
        r0.close();
    L_0x0073:
        return r4;
    L_0x0074:
        if (r0 == 0) goto L_0x0079;
    L_0x0076:
        r0.close();
    L_0x0079:
        return r4;
    L_0x007a:
        r5 = move-exception;
        if (r0 == 0) goto L_0x0080;
    L_0x007d:
        r0.close();
    L_0x0080:
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.DBAdapter.loadPermissionList():java.util.List<java.lang.String>");
    }

    public static boolean permissionConfigExistInDb(android.content.Context r10, java.lang.String r11) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0036 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r8 = 1;
        r9 = 0;
        r4 = new java.lang.String[r8];
        r4[r9] = r11;
        r6 = 0;
        r0 = r10.getContentResolver();	 Catch:{ Exception -> 0x0027, all -> 0x0037 }
        r1 = BLOCK_TABLE_NAME_URI;	 Catch:{ Exception -> 0x0027, all -> 0x0037 }
        r3 = "packageName = ?";	 Catch:{ Exception -> 0x0027, all -> 0x0037 }
        r2 = 0;	 Catch:{ Exception -> 0x0027, all -> 0x0037 }
        r5 = 0;	 Catch:{ Exception -> 0x0027, all -> 0x0037 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0027, all -> 0x0037 }
        if (r6 == 0) goto L_0x0025;	 Catch:{ Exception -> 0x0027, all -> 0x0037 }
    L_0x0018:
        r0 = r6.getCount();	 Catch:{ Exception -> 0x0027, all -> 0x0037 }
        if (r0 <= 0) goto L_0x0025;
    L_0x001e:
        r0 = r8;
    L_0x001f:
        if (r6 == 0) goto L_0x0024;
    L_0x0021:
        r6.close();
    L_0x0024:
        return r0;
    L_0x0025:
        r0 = r9;
        goto L_0x001f;
    L_0x0027:
        r7 = move-exception;
        r0 = "PermissionDBAdapter";	 Catch:{ Exception -> 0x0027, all -> 0x0037 }
        r1 = "query permission table fail.";	 Catch:{ Exception -> 0x0027, all -> 0x0037 }
        com.huawei.systemmanager.util.HwLog.e(r0, r1, r7);	 Catch:{ Exception -> 0x0027, all -> 0x0037 }
        if (r6 == 0) goto L_0x0036;
    L_0x0033:
        r6.close();
    L_0x0036:
        return r9;
    L_0x0037:
        r0 = move-exception;
        if (r6 == 0) goto L_0x003d;
    L_0x003a:
        r6.close();
    L_0x003d:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.DBAdapter.permissionConfigExistInDb(android.content.Context, java.lang.String):boolean");
    }

    public static void setSinglePermission(android.content.Context r1, int r2, java.lang.String r3, int r4, int r5) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.permissionmanager.db.DBAdapter.setSinglePermission(android.content.Context, int, java.lang.String, int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.DBAdapter.setSinglePermission(android.content.Context, int, java.lang.String, int, int):void");
    }

    public static void updatePartsPermissions(android.content.Context r1, int r2, java.lang.String r3, int r4, int r5, int[] r6) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.permissionmanager.db.DBAdapter.updatePartsPermissions(android.content.Context, int, java.lang.String, int, int, int[]):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.DBAdapter.updatePartsPermissions(android.content.Context, int, java.lang.String, int, int, int[]):void");
    }

    public void setPermissionForAllApp(com.huawei.permissionmanager.ui.Permission r1, int r2, int r3, com.huawei.permissionmanager.model.PermissionApps r4) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.permissionmanager.db.DBAdapter.setPermissionForAllApp(com.huawei.permissionmanager.ui.Permission, int, int, com.huawei.permissionmanager.model.PermissionApps):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.DBAdapter.setPermissionForAllApp(com.huawei.permissionmanager.ui.Permission, int, int, com.huawei.permissionmanager.model.PermissionApps):void");
    }

    public static int getValue(int type, int permissionCode, int permissionCfg) {
        int cfg = permissionCfg & type;
        if ((permissionCode & type) != type) {
            return 3;
        }
        if (cfg == type) {
            return 2;
        }
        return 1;
    }

    public static synchronized DBAdapter getInstance(Context context) {
        DBAdapter dBAdapter;
        synchronized (DBAdapter.class) {
            if (sInstance == null && context != null) {
                sInstance = new DBAdapter(context);
                sInstance.refreshAllCachedData("get instance");
            }
            dBAdapter = sInstance;
        }
        return dBAdapter;
    }

    private DBAdapter(Context context) {
        this.mContext = context.getApplicationContext();
        this.CONTROLLED_PERMISSION_TYPES = ShareLib.getControlPermissions();
        this.mPm = HsmPackageManager.getInstance();
        this.mPsm = PermissionServiceManager.getInstance();
        PermissionContentObserver.startObserve(this.mContext, this.mObserver);
        this.filter.addAction("android.intent.action.LOCALE_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, this.filter);
        HwLog.d(LOG_TAG, "DBAdapter created!");
    }

    public void resetShouldMonitorMap() {
        HwLog.i(LOG_TAG, "GRule is change,should reset ShouldMonitorMap");
        this.mShouldMonitorMap.clear();
    }

    public void refreshAllCachedData(String reason) {
        refreshAllCachedData(reason, this.mCacheVersion.getDBDataVersion());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void refreshAllCachedData(String reason, long targetDbVersion) {
        if (-1 == targetDbVersion) {
            targetDbVersion = this.mCacheVersion.getDBDataVersion();
        }
        if (this.mCacheVersion.expiredThan(targetDbVersion)) {
            repairDbDataIfNeeded(refreshAppList(loadAllDataFromDb(), reason));
            this.mCacheVersion.setAppListVersion(targetDbVersion);
            notifyCacheChanged();
            if (Log.HWINFO) {
                HwLog.i(LOG_TAG, "refreshAllCachedData end! cache version updated to " + this.mCacheVersion.getAppListVersion());
            }
        }
    }

    private void notifyCacheChanged() {
        List<DataChangeListener> callbacks = new ArrayList();
        synchronized (mCallbacks) {
            callbacks.addAll(mCallbacks);
        }
        for (DataChangeListener callback : callbacks) {
            callback.onPermissionCfgChanged();
        }
    }

    public List<AppInfo> getShareAppList(String reason) {
        refreshAllCachedData(reason + " getShareAppList");
        if (isAppListInvalid()) {
            HwLog.e(LOG_TAG, "The mAppList is null or empty!");
            return null;
        }
        ArrayList<AppInfo> appList = new ArrayList();
        ArrayList<AppInfo> appListMonitor = new ArrayList();
        ArrayList<AppInfo> appListTrust = new ArrayList();
        HwLog.i(LOG_TAG, "permissionList display getShareAppList synchronized start");
        synchronized (this.mAppList) {
            for (AppInfo appInfo : this.mAppList) {
                AppInfo appInfoItem = new AppInfo(appInfo);
                if (1 == appInfo.mTrust) {
                    appListTrust.add(appInfoItem);
                    appInfoItem.mTrust = 1;
                } else {
                    appListMonitor.add(appInfoItem);
                    appInfoItem.mTrust = 0;
                }
            }
        }
        HwLog.i(LOG_TAG, "permissionList display getShareAppList synchronized end");
        appList.addAll(appListTrust);
        appList.addAll(appListMonitor);
        HwLog.i(LOG_TAG, "The trust apps list size is: " + appListTrust.size() + ", monitor apps size is: " + appListMonitor.size());
        return appList;
    }

    public DBPermissionItem getDBItemByPackage(String pkgName) {
        Exception e;
        Throwable th;
        DBPermissionItem dBPermissionItem = null;
        String[] args = new String[]{pkgName};
        long identity = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(BLOCK_TABLE_NAME_URI, null, "packageName = ?", args, null, null);
            if (cursor == null) {
                HwLog.w(LOG_TAG, "getDBItemByPackage Cursor null");
                Binder.restoreCallingIdentity(identity);
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                DBPermissionItem dbItem = new DBPermissionItem(pkgName);
                try {
                    dbItem.mPermissionCode = cursor.getInt(cursor.getColumnIndex("permissionCode"));
                    dbItem.mPermissionCfg = cursor.getInt(cursor.getColumnIndex("permissionCfg"));
                    dbItem.mTrustCode = 1 == cursor.getInt(cursor.getColumnIndex("trust")) ? 1 : 0;
                    dBPermissionItem = dbItem;
                } catch (Exception e2) {
                    e = e2;
                    dBPermissionItem = dbItem;
                    try {
                        HwLog.e(LOG_TAG, "getCursorByPackage get Exception!" + e.getMessage());
                        Binder.restoreCallingIdentity(identity);
                        if (cursor != null) {
                            cursor.close();
                        }
                        return dBPermissionItem;
                    } catch (Throwable th2) {
                        th = th2;
                        Binder.restoreCallingIdentity(identity);
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    Binder.restoreCallingIdentity(identity);
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            Binder.restoreCallingIdentity(identity);
            if (cursor != null) {
                cursor.close();
            }
            return dBPermissionItem;
        } catch (Exception e3) {
            e = e3;
            HwLog.e(LOG_TAG, "getCursorByPackage get Exception!" + e.getMessage());
            Binder.restoreCallingIdentity(identity);
            if (cursor != null) {
                cursor.close();
            }
            return dBPermissionItem;
        }
    }

    public AppInfo getSingleAppInfo(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return null;
        }
        HsmPkgInfo app = HsmPackageManager.getInstance().getPkgInfo(pkgName);
        if (app == null) {
            HwLog.w(LOG_TAG, "getSingleAppInfo pkg is null.");
            return null;
        }
        DBPermissionItem dbItem = getDBItemByPackage(pkgName);
        if (dbItem == null) {
            HwLog.w(LOG_TAG, "getSingleAppInfo dbItem is null.");
            return null;
        }
        Map<String, ArrayList<Integer>> runtimePerms = getAllAppsRuntimePerms();
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            HwLog.w(LOG_TAG, "getSingleAppInfo pm is null.");
            return null;
        }
        AppInfo appInfo = getAppInfo(pm, runtimePerms, app);
        appInfo.mPermissionCode = dbItem.mPermissionCode;
        appInfo.mPermissionCfg = dbItem.mPermissionCfg;
        appInfo.mTrust = dbItem.mTrustCode;
        return appInfo;
    }

    private Cursor queryAllData() {
        return this.mContext.getContentResolver().query(BLOCK_TABLE_NAME_URI, null, null, null, null, null);
    }

    private ArrayList<String> refreshAppList(Map<String, DBPermissionItem> dbPermissionMap, String reason) {
        HwLog.i(LOG_TAG, "refreshAppList begin refreshAppList. reason:" + reason + ", db items size:" + dbPermissionMap.size());
        ArrayList<String> newApps = new ArrayList();
        List<AppInfo> tempAppList = new ArrayList();
        if (this.mContext == null) {
            return newApps;
        }
        PackageManager pm = this.mContext.getPackageManager();
        List<HsmPkgInfo> appcationsList = this.mPm.getInstalledPackages(0);
        Map<String, ArrayList<Integer>> runtimePerms = getAllAppsRuntimePerms();
        for (HsmPkgInfo app : appcationsList) {
            if (app.mPkgName != null) {
                String pkgName = app.mPkgName;
                if (this.mShouldMonitorMap.get(pkgName) == null) {
                    this.mShouldMonitorMap.put(pkgName, Boolean.valueOf(this.mPsm.shouldMonitor(this.mContext, pkgName)));
                }
                if (((Boolean) this.mShouldMonitorMap.get(pkgName)).booleanValue()) {
                    AppInfo appInfo = getAppInfo(pm, runtimePerms, app);
                    if (appInfo.mRequestPermissions != null && appInfo.mRequestPermissions.size() > 0) {
                        DBPermissionItem obj = (DBPermissionItem) dbPermissionMap.get(pkgName);
                        if (obj != null) {
                            appInfo.mPermissionCode = obj.mPermissionCode;
                            appInfo.mPermissionCfg = obj.mPermissionCfg;
                            appInfo.mTrust = obj.mTrustCode;
                        } else {
                            newApps.add(pkgName);
                        }
                        tempAppList.add(appInfo);
                    }
                }
            }
        }
        HwLog.i(LOG_TAG, "refreshAppList, all size:" + appcationsList.size() + ", new apps:" + newApps);
        synchronized (this.mAppList) {
            this.mAppList.clear();
            this.mAppList.addAll(tempAppList);
            tempAppList.clear();
            Collections.sort(this.mAppList, AppInfo.PERMISSIONMANAGER_ALP_COMPARATOR);
        }
        appcationsList.clear();
        return newApps;
    }

    private AppInfo getAppInfo(PackageManager pm, Map<String, ArrayList<Integer>> runtimePerms, HsmPkgInfo app) {
        AppInfo appInfo = new AppInfo(app);
        try {
            PackageInfo packageInfo = PackageManagerWrapper.getPackageInfo(pm, app.mPkgName, 12288);
            if (!(packageInfo == null || packageInfo.requestedPermissions == null)) {
                int permissionInHsm = AppInfo.getCodeMaskByRequestedPermissions(packageInfo.requestedPermissions);
                for (HwPermissionInfo info : this.CONTROLLED_PERMISSION_TYPES) {
                    if ((info.mPermissionStr == null || info.mPermissionStr.length == 0) && info.misUnit) {
                        appInfo.mRequestPermissions.add(info);
                    } else if ((info.mPermissionCode & permissionInHsm) != 0) {
                        appInfo.mRequestPermissions.add(info);
                    } else {
                        ArrayList<Integer> pkgRuntimePerms = (ArrayList) runtimePerms.get(app.mPkgName);
                        if (pkgRuntimePerms != null && hasRuntimePermission(info, pkgRuntimePerms)) {
                            appInfo.mRequestPermissions.add(info);
                        }
                    }
                }
            }
        } catch (NameNotFoundException e) {
            HwLog.e(LOG_TAG, "updateAppInfo NameNotFoundException for " + app.mPkgName);
        }
        return appInfo;
    }

    private Map<String, ArrayList<Integer>> getAllAppsRuntimePerms() {
        Map<String, ArrayList<Integer>> allAppPerms = new HashMap();
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(RUNTIME_TABLE_URI, null, null, null, null);
            if (cursor != null) {
                int colIndexPkgType = cursor.getColumnIndex("permissionType");
                int colIndexPkgName = cursor.getColumnIndex("packageName");
                while (cursor.moveToNext()) {
                    ArrayList<Integer> perms = new ArrayList();
                    String permPkgName = cursor.getString(colIndexPkgName);
                    perms.add(Integer.valueOf(cursor.getInt(colIndexPkgType)));
                    allAppPerms.put(permPkgName, perms);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception ex) {
            HwLog.e(LOG_TAG, "getRuntimePermsByApp error", ex);
            return allAppPerms;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return allAppPerms;
    }

    private void repairDbDataIfNeeded(ArrayList<String> newApps) {
        boolean firstBoot = SettingsDbUtils.isFirstBootForPermissionInit(this.mContext);
        boolean upgrade2N = SharedPrefUtils.getMPermissionUpgradeFlag(this.mContext);
        boolean dataCleared = SharedPrefUtils.getDataClearedFlag(this.mContext);
        if (Log.HWINFO) {
            HwLog.i(LOG_TAG, "repairDbDataIfNeeded,new apps count:" + newApps.size() + " upgradeFromL2M:" + upgrade2N + ", first init:" + firstBoot + ", cleard data:" + dataCleared);
        }
        clearInitFlags(firstBoot);
        if ((firstBoot || upgrade2N || dataCleared) && !ProcessPolicy.shouldDoFirstPermissionInit()) {
            HwLog.i(LOG_TAG, "repairDbDataIfNeeded this process don't run init.");
        } else if (newApps.size() > 0 || upgrade2N) {
            if (upgrade2N) {
                SharedPrefUtils.setMPermissionUpgradeFlag(this.mContext, false);
                List<AppInfo> applist = new ArrayList();
                synchronized (this.mAppList) {
                    applist.addAll(this.mAppList);
                }
                new UpgradeFromLToMThread(applist, newApps).start();
            }
            if (newApps.size() > 0) {
                if (Log.HWINFO) {
                    HwLog.i(LOG_TAG, "Init new apps:" + newApps);
                }
                new NewAppsInitThread(newApps, dataCleared).start();
            }
        }
    }

    private void clearInitFlags(boolean firstBoot) {
        if (firstBoot) {
            SettingsDbUtils.setNotFirstBootForPermissionInit(this.mContext);
        }
    }

    private boolean hasRuntimePermission(HwPermissionInfo info, ArrayList<Integer> permissions) {
        int length = permissions.size();
        for (int i = 0; i < length; i++) {
            if (((Integer) permissions.get(i)).equals(Integer.valueOf(info.mPermissionCode))) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<AppInfo> getAppListByPermission(Permission permission, String reason) {
        refreshAllCachedData(reason + " getAppListByPermission");
        if (isAppListInvalid()) {
            return null;
        }
        ArrayList<AppInfo> appList = new ArrayList();
        synchronized (this.mAppList) {
            int permissionType = permission.getPermissionCode();
            if (permissionType == 67108864) {
                ArrayList<String> packageList = getAppsByRuntimePermission(permissionType);
                for (AppInfo appInfo : this.mAppList) {
                    if (packageList.contains(appInfo.mPkgName)) {
                        appList.add(new AppInfo(appInfo));
                    }
                }
                return appList;
            }
            for (AppInfo appInfo2 : this.mAppList) {
                if (!permission.isPermissionRequested(appInfo2.mPermissionCode)) {
                    for (HwPermissionInfo info : appInfo2.mRequestPermissions) {
                        if (permission.getPermissionCode() == info.mPermissionCode) {
                            appList.add(new AppInfo(appInfo2));
                            break;
                        }
                    }
                }
                appList.add(new AppInfo(appInfo2));
            }
            return appList;
        }
    }

    private ArrayList<String> getAppsByRuntimePermission(int permissionType) {
        ArrayList<String> appList = new ArrayList();
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(RUNTIME_TABLE_URI, null, "permissionType = ?", new String[]{String.valueOf(permissionType)}, null);
            if (cursor != null) {
                int colIndexPkg = cursor.getColumnIndex("packageName");
                while (cursor.moveToNext()) {
                    appList.add(cursor.getString(colIndexPkg));
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception ex) {
            HwLog.e(LOG_TAG, "getAppsByRuntimePermission error", ex);
            return appList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return appList;
    }

    public AppInfo getAppInfoFromSystem(int uid) {
        AppInfo appInfo = null;
        List<ApplicationInfo> appcationsList = this.mContext.getPackageManager().getInstalledApplications(8192);
        for (ApplicationInfo app : appcationsList) {
            if ((app.flags & 1) == 1) {
                if (app.sourceDir.contains(CUST_URL)) {
                    if (app.processName == null || !(app.processName.contains(APP_HUAWEI) || app.processName.contains(APP_GOOGLE))) {
                        HwLog.d(LOG_TAG, "Cust app processName = " + app.processName);
                    } else {
                        HwLog.d(LOG_TAG, "huawei cust app = " + app.processName);
                    }
                } else if (app.sourceDir.contains("/system/app")) {
                    continue;
                } else if (app.processName != null) {
                    if (app.processName.contains(APP_HUAWEI)) {
                        continue;
                    } else if (app.processName.contains(APP_GOOGLE)) {
                    }
                }
            }
            if (app.uid == uid) {
                appInfo = new AppInfo(this.mContext, app);
                break;
            }
        }
        appcationsList.clear();
        return appInfo;
    }

    public AppInfo getAppByPkgName(String pkgName, String reason) {
        if (pkgName == null) {
            return null;
        }
        refreshAllCachedData(reason + " getAppByPkgName");
        AppInfo appInfo = null;
        if (isAppListInvalid()) {
            return null;
        }
        synchronized (this.mAppList) {
            for (AppInfo app : this.mAppList) {
                if (pkgName.equals(app.mPkgName)) {
                    appInfo = new AppInfo(app);
                    break;
                }
            }
        }
        return appInfo;
    }

    private boolean isAppListInvalid() {
        boolean isEmpty;
        synchronized (this.mAppList) {
            isEmpty = this.mAppList.isEmpty();
        }
        return isEmpty;
    }

    public void getPermissionCounts(List<Permission> permissionList) {
        if (permissionList != null && !permissionList.isEmpty()) {
            for (Permission permission : permissionList) {
                permission.setPermissionCount(getAppCountByRequestedPermission(permission));
            }
        }
    }

    public int getAppCountByRequestedPermission(Permission permission) {
        ArrayList<AppInfo> appList = getAppListByPermission(permission, "getAppCountByRequestedPermission");
        if (appList != null) {
            return appList.size();
        }
        return 0;
    }

    public void updateApplist(int uid, String pkgName, boolean trust) {
        refreshAllCachedData("updateApplist");
        if (isAppListInvalid()) {
            HwLog.e(LOG_TAG, "The updateApplist mAppList is null or empty!");
            return;
        }
        synchronized (this.mAppList) {
            for (AppInfo appInfoInList : this.mAppList) {
                if (appInfoInList.mAppUid == uid) {
                    appInfoInList.mPkgName = pkgName;
                    appInfoInList.mTrust = trust ? 1 : 0;
                }
            }
        }
    }

    private boolean isPermissionWithoutChange(int permissionCode, int permissionCfg, int permissionType) {
        if (permissionType == (permissionCode & permissionType) && (permissionCfg & permissionType) == 0) {
            return true;
        }
        return false;
    }

    public void checkConsistency() {
        HwLog.i(LOG_TAG, "permission db checkConsistency.");
        List<HsmPkgInfo> installed = this.mPm.getAllPackages();
        List<String> dbList = loadPermissionList();
        for (HsmPkgInfo app : installed) {
            if (hasTrashRecord(app.mPkgName, dbList)) {
                HwLog.i(LOG_TAG, "find trash record:" + app.mPkgName);
                deleteRecord(this.mContext, app.mPkgName);
            }
        }
    }

    private boolean hasTrashRecord(String pkg, List<String> dbList) {
        if (GRuleManager.getInstance().shouldMonitor(this.mContext, MonitorScenario.SCENARIO_PERMISSION, pkg)) {
            return false;
        }
        return dbList.contains(pkg);
    }

    public void deleteRecord(Context context, String pkgName) {
        HwLog.i(LOG_TAG, "delete permission record:" + pkgName);
        Cursor cursor = null;
        try {
            cursor = queryOneDataByPkgName(context, pkgName);
            if (!(cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst())) {
                this.mContext.getContentResolver().delete(BLOCK_TABLE_NAME_URI, "packageName = ?", new String[]{pkgName});
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            HwLog.w(LOG_TAG, "delete data fail.", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        IHoldService holdService = getHoldService();
        if (holdService != null) {
            try {
                holdService.removeRuntimePermissions(pkgName);
            } catch (RemoteException e2) {
                HwLog.i(LOG_TAG, "error", e2);
            }
        }
    }

    private IHoldService getHoldService() {
        int myUid = UserHandle.myUserId();
        String servicekey = HoldServiceConst.HOLD_SERVICE_NAME;
        if (myUid != 0) {
            servicekey = servicekey + myUid;
        }
        try {
            IBinder binder = ServiceManager.getService(servicekey);
            if (binder != null) {
                return Stub.asInterface(binder);
            }
            HwLog.e(LOG_TAG, "binder is null");
            return null;
        } catch (Exception e) {
            HwLog.e(LOG_TAG, "getHoldService get Exception!", e);
            return null;
        }
    }

    public void deleteRecords(List<String> pkgNameList) {
        HwLog.i(LOG_TAG, "delete permission records:" + pkgNameList);
        if (pkgNameList != null && !pkgNameList.isEmpty()) {
            for (String pkgName : pkgNameList) {
                try {
                    this.mContext.getContentResolver().delete(BLOCK_TABLE_NAME_URI, "packageName = ?", new String[]{pkgName});
                } catch (Exception e) {
                    HwLog.w(LOG_TAG, "delete record fail.", e);
                }
            }
        }
    }

    public void addRecords(List<String> pkgNameList) {
        if (pkgNameList != null && !pkgNameList.isEmpty()) {
            List<HsmPkgInfo> installedList = this.mPm.getAllPackages();
            List<ContentValues> contentValuesList = new ArrayList();
            for (HsmPkgInfo appInfo : installedList) {
                if (pkgNameList.contains(appInfo.mPkgName)) {
                    ContentValues contentValue = getInitialConfig(this.mContext, appInfo.mPkgName, appInfo.mUid, true);
                    if (contentValue != null) {
                        contentValuesList.add(contentValue);
                        AddViewAppManager.trustIfNeeded(this.mContext, appInfo.mUid, appInfo.mPkgName, contentValue);
                    }
                }
            }
            if (!contentValuesList.isEmpty()) {
                this.mContext.getContentResolver().bulkInsert(BLOCK_TABLE_NAME_URI, (ContentValues[]) contentValuesList.toArray(new ContentValues[contentValuesList.size()]));
            }
        }
    }

    public void syncHsmDataToSys(String reason, HashSet<String> restoredApps, int oldVersion) {
        HwLog.i(LOG_TAG, "syncHsmDataToMPermissionData begin.");
        final HashSet<String> tmp = new HashSet();
        tmp.addAll(restoredApps);
        final String str = reason;
        final int i = oldVersion;
        new Thread("db_correct_permission_consistency") {
            public void run() {
                DBAdapter.this.syncToSys(str, tmp, i);
            }
        }.start();
    }

    private void syncToSys(String reason, HashSet<String> restoredApps, int oldVersion) {
        List<AppInfo> appList = getShareAppList(reason);
        if (appList == null) {
            HwLog.w(LOG_TAG, "shared app list is null?");
            return;
        }
        for (AppInfo app : appList) {
            if ("com.huawei.android.backup".equals(app.mPkgName)) {
                HwLog.w(LOG_TAG, "backup_restore:Don't recover backup itself.");
            } else if ("com.hicloud.android.clone".equals(app.mPkgName)) {
                HwLog.w(LOG_TAG, "backup_restore:Don't recover com.hicloud.android.clone itself.");
            } else if (restoredApps.contains(app.mPkgName)) {
                HwLog.i(LOG_TAG, "backup_restore: sync to sys:" + app.mPkgName);
                HwAppPermissions.create(this.mContext, app.mPkgName).setHsmValuesToSys(reason, oldVersion);
            } else {
                HwLog.i(LOG_TAG, "backup_restore:This app is not backuped so don't restore:" + app.mPkgName);
            }
        }
    }

    public static void removeFromRuntimeTable(Context context, String packageName) {
        HwLog.i(LOG_TAG, "remove from runtim table:" + packageName);
        try {
            if (context.getContentResolver().delete(RUNTIME_TABLE_URI, "packageName = ?", new String[]{packageName}) == 0) {
                HwLog.w(LOG_TAG, "remove from runtim table:" + packageName + ", not deleted.");
            }
        } catch (Exception ex) {
            HwLog.e(LOG_TAG, "removeRuntimePermission error", ex);
        }
    }

    public static boolean assureAppExistInited(Context context, int uid, String pkgName) {
        if (!PermissionServiceManager.getInstance().shouldMonitor(context, pkgName)) {
            return false;
        }
        if (permissionConfigExistInDb(context, pkgName)) {
            return true;
        }
        HwLog.i(LOG_TAG, "assureAppExistInDb, init new app:" + pkgName);
        return AppInitializer.initilizeNewApp(context, pkgName, uid);
    }

    public static void setSinglePermissionAndSyncToSys(HwAppPermissions aps, Context context, int uid, String pkgName, int permissionType, int operation, String reason) {
        if (operation == 0) {
            HwLog.w(LOG_TAG, "Ignoring setting app operation remind.");
        } else if (!PermissionServiceManager.getInstance().shouldMonitor(context, pkgName)) {
            HwLog.w(LOG_TAG, "Ignoring setting app operation for a not monitored app:" + pkgName);
        } else if (MPermissionUtil.isClassEType(permissionType)) {
            setSinglePermission(context, uid, pkgName, permissionType, operation);
            HwLog.i(LOG_TAG, "setSinglePermissionAndSyncToSys, set E type permission type:" + permissionType + ",name:" + ((String) MPermissionUtil.typeToSinglePermission.get(permissionType)) + ", for pakcage:" + pkgName + ", value:" + operation);
        } else {
            if (MPermissionUtil.isClassAType(permissionType) || MPermissionUtil.isClassBType(permissionType)) {
                int value = ((Integer) ShareCfg.userOperation2Value.get(operation, Integer.valueOf(-1))).intValue();
                if (Log.HWINFO) {
                    HwLog.i(LOG_TAG, "setSinglePermissionAndSyncToSys, set android permissions. type:" + permissionType + ",name:" + ((String) MPermissionUtil.typeToSinglePermission.get(permissionType)) + ", for pakcage:" + pkgName + ", value:" + value);
                }
                if (!(-1 == value || aps == null)) {
                    aps.setSystemPermission(permissionType, value, false, reason);
                }
            }
        }
    }

    public static Cursor queryOneDataByUid(Context context, int uid) {
        return context.getContentResolver().query(BLOCK_TABLE_NAME_URI, null, "uid = ?", new String[]{String.valueOf(uid)}, null);
    }

    public static Cursor queryOneDataByPkgName(Context context, String pkgName) {
        return context.getContentResolver().query(BLOCK_TABLE_NAME_URI, null, "packageName = ?", new String[]{String.valueOf(pkgName)}, null);
    }

    public static int updateTrustValue(Context context, int uid, String pkgName, int trust) {
        if (Log.HWINFO) {
            HwLog.i(LOG_TAG, "updateTrustValue " + pkgName + ", trust:" + trust);
        }
        Cursor cursor = null;
        try {
            cursor = queryOneDataByPkgName(context, pkgName);
            if (cursor == null || cursor.getCount() <= 0) {
                if (cursor != null) {
                    cursor.close();
                }
                return 0;
            }
            cursor.moveToFirst();
            if (cursor.getInt(cursor.getColumnIndex("trust")) == trust) {
                if (cursor != null) {
                    cursor.close();
                }
                return 0;
            }
            if (cursor != null) {
                cursor.close();
            }
            String[] args = new String[]{String.valueOf(uid), pkgName};
            ContentValues values = new ContentValues();
            values.put("trust", Integer.valueOf(trust));
            try {
                return context.getContentResolver().update(BLOCK_TABLE_NAME_URI, values, "uid = ? and packageName = ?", args);
            } catch (Exception e) {
                HwLog.w(LOG_TAG, "update trust code fail.", e);
                return 0;
            }
        } catch (Exception e2) {
            HwLog.w(LOG_TAG, "get cursor for trust fail.", e2);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void updatePartsPermissions(Context context, int uid, String pkgName, int[] updateTypes, int operation) {
        int srcPermCode = 0;
        int srcPermCfg = 0;
        if (1 == operation) {
            srcPermCode = -1;
            srcPermCfg = 0;
        } else if (2 == operation) {
            srcPermCode = -1;
            srcPermCfg = -1;
        } else if (operation == 0) {
            srcPermCode = 0;
            srcPermCfg = 0;
        }
        updatePartsPermissions(context, uid, pkgName, srcPermCode, srcPermCfg, updateTypes);
    }

    public static void updateAppPermission(Context context, int uid, String pkgName, int permissionCode, int permissionCfg, boolean trust) {
        int i = 1;
        ContentValues values = new ContentValues();
        values.put("permissionCode", Integer.valueOf(permissionCode));
        values.put("permissionCfg", Integer.valueOf(permissionCfg));
        String str = "trust";
        if (!trust) {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        try {
            HwLog.i(LOG_TAG, "update rows:" + context.getContentResolver().update(BLOCK_TABLE_NAME_URI, values, "uid= ? and packageName = ?", new String[]{String.valueOf(uid), pkgName}) + ", value:" + values);
        } catch (Exception e) {
            e.printStackTrace();
            HwLog.e(LOG_TAG, "uptatePermissionCodeForTrust update Exception", e);
        }
    }

    public static int updateAppsPermissions(Context context, List<ContentValues> contentValues) {
        if (contentValues == null || contentValues.isEmpty()) {
            HwLog.w(LOG_TAG, "updatePermissions : Invalid params");
            return -1;
        }
        try {
            return context.getContentResolver().bulkInsert(REPLACE_PERMISSION_URI, (ContentValues[]) contentValues.toArray(new ContentValues[contentValues.size()]));
        } catch (Exception e) {
            HwLog.e(LOG_TAG, "updatePermissions Exception: ", e);
            return 0;
        }
    }

    public static boolean getToastSwitchOpenStatus(Context context) {
        int ret = 1;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(COMMON_TABLE_URI, null, "key = 20140626", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                ret = cursor.getInt(cursor.getColumnIndex(DBHelper.VALUE));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            HwLog.e(LOG_TAG, "get toast switch value fail.", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (ret == 0) {
            return true;
        }
        return false;
    }

    public static long setToastSwitchStatus(Context context, int value) {
        long ret = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(COMMON_TABLE_URI, null, "key = 20140626", null, null);
            if (cursor != null) {
                ContentValues values = new ContentValues();
                values.put("key", Integer.valueOf(DBHelper.TOAST_SWITCH_KEY));
                values.put(DBHelper.VALUE, Integer.valueOf(value));
                if (cursor.getCount() > 0) {
                    ret = (long) context.getContentResolver().update(COMMON_TABLE_URI, values, "key = 20140626", null);
                    if (0 == ret) {
                        HwLog.e(LOG_TAG, "update database error");
                    }
                } else {
                    ret = (long) (context.getContentResolver().insert(COMMON_TABLE_URI, values) == null ? 0 : 1);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            HwLog.e(LOG_TAG, "get toast switch value fail.", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ret;
    }

    private void refreshForChanges() {
        this.mCacheVersion.incDbDataVersion();
        new Thread("refresh_permission_list") {
            public void run() {
                DBAdapter.this.refreshAllCachedData("data changed", -1);
                DBAdapter.this.refreshCommonCacheData();
            }
        }.start();
    }

    private void refreshCommonCacheData() {
        try {
            RecommendDBHelper.getInstance(this.mContext).refresh();
        } catch (RuntimeException e) {
            HwLog.w(LOG_TAG, "refreshCommonData RuntimeException", e);
        }
    }

    public static void registerDataChangeListener(DataChangeListener listener) {
        if (listener != null) {
            synchronized (listener) {
                mCallbacks.add(listener);
            }
        }
    }

    public static void unregisterDataChangeListener(DataChangeListener listener) {
        if (listener != null) {
            synchronized (listener) {
                mCallbacks.remove(listener);
            }
        }
    }

    public static boolean getAwakedAppNotifySwitchOpenStatus(Context context) {
        int ret = 1;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(COMMON_TABLE_URI, null, "key = 20160906", null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                ret = cursor.getInt(cursor.getColumnIndex(DBHelper.VALUE));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            HwLog.e(LOG_TAG, "get awaked app notify switch value fail.", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (ret == 0) {
            return true;
        }
        return false;
    }

    public static long setAwakedAppNotifySwitchStatus(Context context, int value) {
        long ret = 0;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(COMMON_TABLE_URI, null, "key = 20160906", null, null);
            if (cursor != null) {
                ContentValues values = new ContentValues();
                values.put("key", Integer.valueOf(DBHelper.AWAKED_APP_NOTIFY_SWITCH_KEY));
                values.put(DBHelper.VALUE, Integer.valueOf(value));
                if (cursor.getCount() > 0) {
                    ret = (long) context.getContentResolver().update(COMMON_TABLE_URI, values, "key = 20160906", null);
                    if (0 == ret) {
                        HwLog.e(LOG_TAG, "update database error");
                    }
                } else {
                    ret = (long) (context.getContentResolver().insert(COMMON_TABLE_URI, values) == null ? 0 : 1);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            HwLog.e(LOG_TAG, "get toast switch value fail.", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ret;
    }
}
