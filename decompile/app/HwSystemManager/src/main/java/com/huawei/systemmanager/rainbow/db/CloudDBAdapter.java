package com.huawei.systemmanager.rainbow.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.optimize.process.SmcsDbHelper;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys;
import com.huawei.systemmanager.rainbow.db.base.CloudConst;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.AddViewValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.BackgroundValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CompetitorConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.GetapplistValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NetworkValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NotificationConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.PermissionValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.StartupConfigFile;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.UnifiedPowerAppsConfigConfigFile;
import com.huawei.systemmanager.rainbow.db.bean.AddviewBean;
import com.huawei.systemmanager.rainbow.db.bean.BackgroundConfigBean;
import com.huawei.systemmanager.rainbow.db.bean.FetchAppListBean;
import com.huawei.systemmanager.rainbow.db.bean.NetworkBean;
import com.huawei.systemmanager.rainbow.db.bean.NotificationConfigBean;
import com.huawei.systemmanager.rainbow.db.bean.PermissionManagerBean;
import com.huawei.systemmanager.rainbow.db.bean.StartupConfigBean;
import com.huawei.systemmanager.rainbow.db.bean.UnifiedPowerAppsConfigBean;
import com.huawei.systemmanager.rainbow.service.PackagePermissionInfo;
import com.huawei.systemmanager.rainbow.util.PackageInfoConst;
import com.huawei.systemmanager.rainbow.util.PackagePermissions;
import com.huawei.systemmanager.rainbow.vaguerule.VagueManager;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudDBAdapter {
    static final String LOG_TAG = "CloudDBAdapter";
    private static final long MAX_WAIT = 10000;
    private static CloudDBAdapter sInstance = null;
    private List<String> mCompetitorList = null;
    private CloudCompetitorChangeObserver mCompetitorObserver = null;
    private Object mCompetitorSyncObj = new Object();
    private Context mContext = null;
    private boolean mInitOk = false;
    private Object mInitSync = new Object();
    private SharedPreferences sp = null;
    CloudInitSharedListener spListener = null;

    private class CloudCompetitorChangeObserver extends ContentObserver {
        public CloudCompetitorChangeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            synchronized (CloudDBAdapter.this.mCompetitorSyncObj) {
                CloudDBAdapter.this.mCompetitorList = null;
                CloudDBAdapter.this.getCompetitorList();
            }
        }
    }

    class CloudInitSharedListener implements OnSharedPreferenceChangeListener {
        CloudInitSharedListener() {
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (CloudSpfKeys.CLOUD_XML_DATA_INITED.equals(key)) {
                boolean ok = sharedPreferences.getBoolean(CloudSpfKeys.CLOUD_XML_DATA_INITED, false);
                synchronized (CloudDBAdapter.this.mInitSync) {
                    CloudDBAdapter.this.mInitOk = ok;
                }
                if (ok) {
                    HwLog.d(CloudDBAdapter.LOG_TAG, "notify begin");
                    try {
                        synchronized (CloudDBAdapter.this.mInitSync) {
                            CloudDBAdapter.this.mInitOk = true;
                            CloudDBAdapter.this.mInitSync.notifyAll();
                        }
                    } catch (Exception e) {
                        HwLog.e(CloudDBAdapter.LOG_TAG, "CloudDBAdapter notify error", e);
                    }
                    HwLog.d(CloudDBAdapter.LOG_TAG, "notify end");
                }
            }
        }
    }

    public static int[] applyDefaultPolicy(android.content.Context r1, int r2, java.lang.String r3, int r4, int r5) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.systemmanager.rainbow.db.CloudDBAdapter.applyDefaultPolicy(android.content.Context, int, java.lang.String, int, int):int[]
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.rainbow.db.CloudDBAdapter.applyDefaultPolicy(android.content.Context, int, java.lang.String, int, int):int[]");
    }

    private CloudDBAdapter(Context context) {
        this.mContext = context.getApplicationContext();
        HandlerThread handlerThread = new HandlerThread(LOG_TAG);
        handlerThread.start();
        this.mCompetitorObserver = new CloudCompetitorChangeObserver(new Handler(handlerThread.getLooper()));
        this.mContext.getContentResolver().registerContentObserver(CompetitorConfigFile.CONTENT_OUTERTABLE_URI, true, this.mCompetitorObserver);
    }

    public static synchronized CloudDBAdapter getInstance(Context context) {
        CloudDBAdapter cloudDBAdapter;
        synchronized (CloudDBAdapter.class) {
            if (sInstance == null && context != null) {
                sInstance = new CloudDBAdapter(context);
                sInstance.synchrdata();
                sInstance.getCompetitorList();
            }
            cloudDBAdapter = sInstance;
        }
        return cloudDBAdapter;
    }

    private void synchrdata() {
        try {
            synchronized (this.mInitSync) {
                this.sp = this.mContext.getSharedPreferences(CloudSpfKeys.FILE_NAME, 0);
                this.mInitOk = this.sp.getBoolean(CloudSpfKeys.CLOUD_XML_DATA_INITED, false);
                if (!this.mInitOk && this.spListener == null) {
                    this.spListener = new CloudInitSharedListener();
                    this.sp.registerOnSharedPreferenceChangeListener(this.spListener);
                }
                while (!this.mInitOk) {
                    HwLog.d(LOG_TAG, "CloudDBAdapter init wait begin");
                    this.mInitSync.wait(10000);
                    this.mInitOk = true;
                    HwLog.d(LOG_TAG, "CloudDBAdapter init wait end");
                }
                this.sp.unregisterOnSharedPreferenceChangeListener(this.spListener);
                this.spListener = null;
            }
        } catch (InterruptedException e) {
            HwLog.e(LOG_TAG, "CloudDBAdapter init error", e);
        }
    }

    public PackagePermissionInfo getPermissionsByPkgName(String pkgName) {
        PackagePermissionInfo pkgInfo = new PackagePermissionInfo();
        pkgInfo.packageName = pkgName;
        Map<String, Integer> permissionMap = new HashMap();
        getPermissionManager(pkgName, permissionMap);
        getBackground(pkgName, permissionMap);
        pkgInfo.permission = permissionMap;
        return pkgInfo;
    }

    private void getPermissionManager(String pkgName, Map<String, Integer> permissionMap) {
        Cursor cursor = getPermissionManagerCursor(pkgName);
        if (cursor == null || cursor.getCount() <= 0) {
            permissionMap.put(PackageInfoConst.PERMISSION_VALID_KEY, Integer.valueOf(10));
        } else {
            cursor.moveToFirst();
            int codeIndex = cursor.getColumnIndex("permissionCode");
            int cfgIndex = cursor.getColumnIndex("permissionCfg");
            int trustIndex = cursor.getColumnIndex("trust");
            int compareCode = AppInfo.getComparePermissionCode(this.mContext, pkgName);
            int permissionCode = compareCode & cursor.getInt(codeIndex);
            int permissionCfg = cursor.getInt(cfgIndex);
            if ("true".equals(cursor.getString(trustIndex))) {
                permissionMap.put(PackageInfoConst.PERMISSION_TRUST_KEY, Integer.valueOf(1));
            } else {
                permissionMap.put(PackageInfoConst.PERMISSION_TRUST_KEY, Integer.valueOf(0));
            }
            int[] res = applyDefaultPolicy(this.mContext, compareCode, pkgName, permissionCode, permissionCfg);
            HwLog.i(LOG_TAG, "getPermissionManager for " + pkgName + ", previous:" + permissionCode + SqlMarker.COMMA_SEPARATE + permissionCfg + ", after:" + res[0] + ConstValues.SEPARATOR_KEYWORDS_EN + res[1]);
            permissionCode = res[0];
            permissionCfg = res[1];
            permissionMap.put(PackageInfoConst.PERMISSION_CODES_KEY, Integer.valueOf(permissionCode));
            permissionMap.put(PackageInfoConst.PERMISSION_CFGS_KEY, Integer.valueOf(permissionCfg));
            permissionMap.put(PackageInfoConst.PERMISSION_VALID_KEY, Integer.valueOf(9));
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private void getBackground(String pkgName, Map<String, Integer> permissionMap) {
        Cursor cursor = getBackgroundCursor(pkgName);
        if (cursor == null || cursor.getCount() <= 0) {
            permissionMap.put(PackageInfoConst.BACKGROUND_KEY, Integer.valueOf(1));
        } else {
            permissionMap.put(PackageInfoConst.BACKGROUND_KEY, Integer.valueOf(0));
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public Cursor getPermissionManagerCursor(String pkgName) {
        Cursor cursor = getCursorbyUri(pkgName, PermissionValues.PERMISSION_OUTERTABLE_URI, "packageName");
        if (CursorHelper.checkCursorValid(cursor)) {
            return cursor;
        }
        cursor = this.mContext.getContentResolver().query(PermissionValues.CONTENT_URI, null, "packageName = \"" + pkgName + SqlMarker.QUOTATION, null, null);
        if (CursorHelper.checkCursorValid(cursor)) {
            return cursor;
        }
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"packageName", "type", "trust", "permissionCode", "permissionCfg"});
        String trust = VagueManager.getInstance(this.mContext).getTrustPermission(pkgName);
        int compareCode = PackagePermissions.getComparePermissionCode(this.mContext, pkgName);
        int permissionCfg = compareCode & VagueManager.getInstance(this.mContext).getPermissionCfg(pkgName);
        HwLog.i(LOG_TAG, "get vague value, code:" + (compareCode & VagueManager.getInstance(this.mContext).getPermissionCode(pkgName)) + ", cfg:" + permissionCfg);
        matrixCursor.addRow(new Object[]{pkgName, "", trust, Integer.valueOf(permissionCode), Integer.valueOf(permissionCfg)});
        return matrixCursor;
    }

    public PermissionManagerBean getSinglePermissionManager(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            Cursor c = getPermissionManagerCursor(pkgName);
            if (c != null) {
                PermissionManagerBean fromCursor;
                try {
                    if (c.moveToFirst()) {
                        fromCursor = PermissionManagerBean.fromCursor(c, c.getColumnIndex("packageName"), c.getColumnIndex("trust"), c.getColumnIndex("permissionCode"), c.getColumnIndex("permissionCfg"));
                        return fromCursor;
                    }
                } catch (Exception e) {
                    fromCursor = LOG_TAG;
                    HwLog.e(fromCursor, "getSinglePermissionManager error", e);
                } finally {
                    CursorHelper.closeCursor(c);
                }
            }
            CursorHelper.closeCursor(c);
        }
        return null;
    }

    private Cursor getBackgroundCursor(String pkgName) {
        String value = getProtectedValueInCloud(pkgName);
        if ("0".equals(value)) {
            MatrixCursor matrixCursor = new MatrixCursor(new String[]{"fakeCursor"});
            matrixCursor.addRow(new Object[]{"fakeCursor"});
            return matrixCursor;
        } else if ("1".equals(value)) {
            return null;
        } else {
            return SmcsDbHelper.getPkgChecked(this.mContext, pkgName);
        }
    }

    private Cursor getBackgroundConfigCursor(String pkgName) {
        Cursor cursor = getCursorbyUri(pkgName, BackgroundValues.CONTENT_OUTERTABLE_URI, "packageName");
        if (CursorHelper.checkCursorValid(cursor)) {
            return cursor;
        }
        return null;
    }

    public BackgroundConfigBean getSingleBackgroundConfig(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            Cursor c = getBackgroundConfigCursor(pkgName);
            if (c != null) {
                BackgroundConfigBean fromCursor;
                try {
                    if (c.moveToFirst()) {
                        fromCursor = BackgroundConfigBean.fromCursor(c, c.getColumnIndex("packageName"), c.getColumnIndex("isControlled"), c.getColumnIndex("isProtected"), c.getColumnIndex(BackgroundValues.COL_IS_KEY_TASK));
                        return fromCursor;
                    }
                } catch (Exception e) {
                    fromCursor = LOG_TAG;
                    HwLog.e(fromCursor, "getSingleBackgroundConfig error", e);
                } finally {
                    CursorHelper.closeCursor(c);
                }
            }
            CursorHelper.closeCursor(c);
        }
        return null;
    }

    public List<BackgroundConfigBean> getAllBackgroundConfig() {
        List<BackgroundConfigBean> results = new ArrayList();
        Cursor cursor = getCursorbyUri(BackgroundValues.CONTENT_OUTERTABLE_URI);
        if (CursorHelper.checkCursorValid(cursor)) {
            try {
                int pkgIndex = cursor.getColumnIndex("packageName");
                int ctlIndex = cursor.getColumnIndex("isControlled");
                int proIndex = cursor.getColumnIndex("isProtected");
                int keyIndex = cursor.getColumnIndex(BackgroundValues.COL_IS_KEY_TASK);
                while (cursor.moveToNext()) {
                    results.add(BackgroundConfigBean.fromCursor(cursor, pkgIndex, ctlIndex, proIndex, keyIndex));
                }
            } catch (Exception e) {
                HwLog.e(LOG_TAG, "getAllBackgroundConfig error", e);
            } finally {
                CursorHelper.closeCursor(cursor);
            }
        }
        return results;
    }

    public Cursor getNetworkCursor(String pkgName) {
        Cursor cursor = getCursorbyUri(pkgName, NetworkValues.OUTER_TABLE_URI, "packageName");
        if (CursorHelper.checkCursorValid(cursor)) {
            return cursor;
        }
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"packageName", "netWifiPermission", "netDataPermission"});
        String wifiConnectPermission = VagueManager.getInstance(this.mContext).getNetworkWifiPermission(pkgName);
        String dataConnectPermission = VagueManager.getInstance(this.mContext).getNetworkDataPermission(pkgName);
        matrixCursor.addRow(new Object[]{pkgName, wifiConnectPermission, dataConnectPermission});
        return matrixCursor;
    }

    public NetworkBean getSingleNetwork(String pkgName) {
        NetworkBean fromCursor;
        if (!TextUtils.isEmpty(pkgName)) {
            Cursor c = getNetworkCursor(pkgName);
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        fromCursor = NetworkBean.fromCursor(c, c.getColumnIndex("packageName"), c.getColumnIndex("netWifiPermission"), c.getColumnIndex("netDataPermission"));
                        return fromCursor;
                    }
                } catch (Exception e) {
                    fromCursor = LOG_TAG;
                    HwLog.e(fromCursor, "getNetwork error", e);
                } finally {
                    CursorHelper.closeCursor(c);
                }
            }
            CursorHelper.closeCursor(c);
        }
        return null;
    }

    public Cursor getAddviewCursor(String pkgName) {
        Cursor cursor = getCursorbyUri(pkgName, AddViewValues.CONTENT_OUTERTABLE_URI, "packageName");
        if (CursorHelper.checkCursorValid(cursor)) {
            return cursor;
        }
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"packageName", "permissionCfg"});
        int addViewPermission = VagueManager.getInstance(this.mContext).getAddViewPermission(pkgName);
        matrixCursor.addRow(new Object[]{pkgName, Integer.valueOf(addViewPermission)});
        return matrixCursor;
    }

    public AddviewBean getSingleAddView(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            Cursor c = getAddviewCursor(pkgName);
            if (c != null) {
                AddviewBean fromCursor;
                try {
                    if (c.moveToFirst()) {
                        fromCursor = AddviewBean.fromCursor(c, c.getColumnIndex("packageName"), c.getColumnIndex("permissionCfg"));
                        return fromCursor;
                    }
                } catch (Exception e) {
                    fromCursor = LOG_TAG;
                    HwLog.e(fromCursor, "getSingleAddView error", e);
                } finally {
                    CursorHelper.closeCursor(c);
                }
            }
            CursorHelper.closeCursor(c);
        }
        return null;
    }

    public Cursor getFetchAppListCursor(String pkgName) {
        Cursor cursor = getCursorbyUri(pkgName, GetapplistValues.CONTENT_OUTERTABLE_URI, "packageName");
        if (CursorHelper.checkCursorValid(cursor)) {
            return cursor;
        }
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"packageName", "permissionCfg"});
        int getapplistPermission = VagueManager.getInstance(this.mContext).getFetchapplistPermission(pkgName);
        matrixCursor.addRow(new Object[]{pkgName, Integer.valueOf(getapplistPermission)});
        return matrixCursor;
    }

    public FetchAppListBean getSingleFetchAppList(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            Cursor c = getFetchAppListCursor(pkgName);
            if (c != null) {
                FetchAppListBean fromCursor;
                try {
                    if (c.moveToFirst()) {
                        fromCursor = FetchAppListBean.fromCursor(c, c.getColumnIndex("packageName"), c.getColumnIndex("permissionCfg"));
                        return fromCursor;
                    }
                } catch (Exception e) {
                    fromCursor = LOG_TAG;
                    HwLog.e(fromCursor, "getSingleFetchAppList error", e);
                } finally {
                    CursorHelper.closeCursor(c);
                }
            }
            CursorHelper.closeCursor(c);
        }
        return null;
    }

    private Cursor getCursorbyUri(String pkgName, Uri uri, String columnName) {
        return this.mContext.getContentResolver().query(uri, null, columnName + " = \"" + pkgName + SqlMarker.QUOTATION, null, null);
    }

    private Cursor getCursorbyUri(Uri uri) {
        return this.mContext.getContentResolver().query(uri, null, null, null, null);
    }

    @Deprecated
    public boolean isProtectInCloud(String pkgName) {
        if (CursorHelper.checkCursorValidAndClose(getCursorbyUri(pkgName, BackgroundValues.CONTENT_OUTERTABLE_URI, "packageName"))) {
            return true;
        }
        return false;
    }

    public String getProtectedValueInCloud(String pkgName) {
        Cursor cursor = getCursorbyUri(pkgName, BackgroundValues.CONTENT_OUTERTABLE_URI, "packageName");
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    int controlIndex = cursor.getColumnIndex("isControlled");
                    int protectIndex = cursor.getColumnIndex("isProtected");
                    if (cursor.moveToNext()) {
                        String isControlled = cursor.getString(controlIndex);
                        String isProtected = cursor.getString(protectIndex);
                        String str;
                        if ("1".equals(isControlled) || ("0".equals(isControlled) && "0".equals(isProtected))) {
                            str = "0";
                            if (cursor != null) {
                                cursor.close();
                            }
                            return str;
                        }
                        str = "1";
                        if (cursor != null) {
                            cursor.close();
                        }
                        return str;
                    }
                }
            } catch (Exception ex) {
                HwLog.e(LOG_TAG, "getProtectedValueInCloud error", ex);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return CloudConst.VALUE_NOT_FOUND;
    }

    public Cursor getNotificationConfigCursor(String pkgName) {
        Cursor cursor = getCursorbyUri(pkgName, NotificationConfigFile.CONTENT_OUTERTABLE_URI, "packageName");
        if (CursorHelper.checkCursorValid(cursor)) {
            return cursor;
        }
        return null;
    }

    public NotificationConfigBean getSingleNotificationConfig(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            Cursor c = getNotificationConfigCursor(pkgName);
            if (c != null) {
                NotificationConfigBean fromCursor;
                try {
                    if (c.moveToFirst()) {
                        fromCursor = NotificationConfigBean.fromCursor(c, c.getColumnIndex("packageName"), c.getColumnIndex(NotificationConfigFile.COL_CAN_FORBIDDEN), c.getColumnIndex("notificationCfg"), c.getColumnIndex(NotificationConfigFile.COL_STATUSBAR), c.getColumnIndex(NotificationConfigFile.COL_LOCKSCREEN), c.getColumnIndex(NotificationConfigFile.COL_HEADSUB), c.getColumnIndex("isControlled"));
                        return fromCursor;
                    }
                } catch (Exception e) {
                    fromCursor = LOG_TAG;
                    HwLog.e(fromCursor, "getSingleNotificationConfig error", e);
                } finally {
                    CursorHelper.closeCursor(c);
                }
            }
            CursorHelper.closeCursor(c);
        }
        return null;
    }

    private Cursor getUnifiedPowerAppsConfigCursor(String pkgName) {
        Cursor cursor = getCursorbyUri(pkgName, UnifiedPowerAppsConfigConfigFile.CONTENT_OUTERTABLE_URI, "packageName");
        if (CursorHelper.checkCursorValid(cursor)) {
            return cursor;
        }
        return null;
    }

    public UnifiedPowerAppsConfigBean getSingleUnifiedPowerAppsConfigBean(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            Cursor c = getUnifiedPowerAppsConfigCursor(pkgName);
            if (c != null) {
                UnifiedPowerAppsConfigBean fromCursor;
                try {
                    if (c.moveToFirst()) {
                        fromCursor = UnifiedPowerAppsConfigBean.fromCursor(c, c.getColumnIndex("packageName"), c.getColumnIndex("isShow"), c.getColumnIndex("isProtected"));
                        return fromCursor;
                    }
                } catch (Exception e) {
                    fromCursor = LOG_TAG;
                    HwLog.e(fromCursor, "getSingleUnifiedPowerAppsConfigBean error", e);
                } finally {
                    CursorHelper.closeCursor(c);
                }
            }
            CursorHelper.closeCursor(c);
        }
        return null;
    }

    public Cursor getStartupConfigCursor(String pkgName) {
        Cursor cursor = getCursorbyUri(pkgName, StartupConfigFile.CONTENT_OUTERTABLE_URI, "packageName");
        if (CursorHelper.checkCursorValid(cursor)) {
            return cursor;
        }
        return null;
    }

    public StartupConfigBean getSingleStartupConfig(String pkgName) {
        if (!TextUtils.isEmpty(pkgName)) {
            Cursor c = getStartupConfigCursor(pkgName);
            if (c != null) {
                StartupConfigBean fromCursor;
                try {
                    if (c.moveToFirst()) {
                        fromCursor = StartupConfigBean.fromCursor(c, c.getColumnIndex("packageName"), c.getColumnIndex(StartupConfigFile.COL_RECEIVER), c.getColumnIndex(StartupConfigFile.COL_SERVICE_PROVIDER), c.getColumnIndex("isControlled"));
                        return fromCursor;
                    }
                } catch (Exception e) {
                    fromCursor = LOG_TAG;
                    HwLog.e(fromCursor, "getStartupConfig error", e);
                } finally {
                    CursorHelper.closeCursor(c);
                }
            }
            CursorHelper.closeCursor(c);
        }
        return null;
    }

    public Cursor getCompetitorCursor() {
        Cursor cursor = this.mContext.getContentResolver().query(CompetitorConfigFile.CONTENT_OUTERTABLE_URI, null, null, null, null);
        if (CursorHelper.checkCursorValid(cursor)) {
            return cursor;
        }
        return null;
    }

    public List<String> getCompetitorList() {
        Cursor cursor;
        synchronized (this.mCompetitorSyncObj) {
            if (this.mCompetitorList != null) {
                List<String> list = this.mCompetitorList;
                return list;
            }
            this.mCompetitorList = new ArrayList();
            cursor = getCompetitorCursor();
            List<String> pkgNameList = new ArrayList();
            if (cursor != null) {
                try {
                    int pkgNameIndex = cursor.getColumnIndex("packageName");
                    while (cursor.moveToNext()) {
                        pkgNameList.add(cursor.getString(pkgNameIndex));
                    }
                } catch (Exception ex) {
                    HwLog.e(LOG_TAG, "getCompetitorList error", ex);
                    pkgNameList.clear();
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            if (pkgNameList.size() > 0) {
                this.mCompetitorList.clear();
                this.mCompetitorList.addAll(pkgNameList);
            }
            list = this.mCompetitorList;
            return list;
        }
    }

    public boolean isCompetitor(String pkgName) {
        boolean contains;
        synchronized (this.mCompetitorSyncObj) {
            contains = this.mCompetitorList.contains(pkgName);
        }
        return contains;
    }

    public List<String> getAllCompetitors() {
        List newArrayList;
        synchronized (this.mCompetitorSyncObj) {
            getCompetitorList();
            newArrayList = Lists.newArrayList(this.mCompetitorList);
        }
        return newArrayList;
    }
}
