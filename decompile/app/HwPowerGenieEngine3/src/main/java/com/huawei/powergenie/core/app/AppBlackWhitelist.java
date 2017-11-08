package com.huawei.powergenie.core.app;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.util.Log;
import com.huawei.powergenie.R;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.XmlHelper;
import com.huawei.powergenie.integration.adapter.AppStandbyDozeAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppBlackWhitelist {
    private static final Uri UNIFIED_POWER_APP_TABLE_URI = Uri.parse("content://com.huawei.android.smartpowerprovider/unifiedpowerapps");
    private static final boolean mAddCleanProtectedAppToStandbyWhitelist = SystemProperties.getBoolean("ro.config.cleanapptodoze", false);
    private static ArrayList<String> mForceCleanApps = new ArrayList();
    private static final ArrayList<String> mForeignSuperApps = new ArrayList();
    private static ArrayList<String> mIgnoreAudioApps = new ArrayList();
    private static final boolean mIsForeignSuperAppPolicy = SystemProperties.getBoolean("ro.config.oversea_app_policy", false);
    private static ArrayList<String> mKillProcList = new ArrayList();
    private static AppBlackWhitelist sInstance;
    private AppManager mAppManager;
    private final ContentObserver mAppStaticDBObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.i("AppBlackWhitelist", "App Static database change !");
            AppBlackWhitelist.this.mHandler.removeMessages(1002);
            AppBlackWhitelist.this.mHandler.sendMessageDelayed(AppBlackWhitelist.this.mHandler.obtainMessage(1002), 10000);
        }
    };
    private boolean mCleanDBExist = true;
    private ContentObserver mCleanDBObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.i("AppBlackWhitelist", "Clean database changed!");
            AppBlackWhitelist.this.mHandler.removeMessages(1000);
            AppBlackWhitelist.this.mHandler.sendMessageDelayed(AppBlackWhitelist.this.mHandler.obtainMessage(1000), 5000);
        }
    };
    private ArrayList<String> mCleanProtectedApps = new ArrayList();
    private ArrayList<String> mCleanUnprotectedApps = new ArrayList();
    private final Context mContext;
    private ContentObserver mExtrModeV2AppObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            if (AppBlackWhitelist.this.mIPolicy.isExtremeModeV2()) {
                Log.d("AppBlackWhitelist", "Extreme launcher app changed!");
                AppBlackWhitelist.this.updateExtrModeV2Apps();
                AppBlackWhitelist.this.mHandler.removeMessages(1004);
                AppBlackWhitelist.this.mHandler.sendMessageDelayed(AppBlackWhitelist.this.mHandler.obtainMessage(1004), 0);
            }
        }
    };
    private ArrayList<String> mExtrModeV2ReserveApps = new ArrayList();
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            long j = 0;
            switch (msg.what) {
                case 1000:
                    AppBlackWhitelist.this.updateCleanApps();
                    AppBlackWhitelist.this.mAppManager.dispatchStateAction(276);
                    AppBlackWhitelist.this.sendChangeDozeWhisteListMsg(0);
                    return;
                case 1001:
                    boolean delay = AppBlackWhitelist.this.mUpdateStandbyDBImmediately;
                    AppBlackWhitelist.this.mUpdateStandbyDBImmediately = false;
                    AppBlackWhitelist.this.updateStandbyApps();
                    AppBlackWhitelist.this.mAppManager.dispatchStateAction(277);
                    AppBlackWhitelist appBlackWhitelist = AppBlackWhitelist.this;
                    if (delay) {
                        j = 5000;
                    }
                    appBlackWhitelist.sendChangeDozeWhisteListMsg(j);
                    return;
                case 1002:
                    AppBlackWhitelist.this.mAppManager.dispatchStateAction(278);
                    return;
                case 1003:
                    AppBlackWhitelist.this.changeDozeWhiteList();
                    return;
                case 1004:
                    AppBlackWhitelist.this.mAppManager.dispatchStateAction(282);
                    return;
                case 1005:
                    AppBlackWhitelist.this.mNFCPayApp = AppBlackWhitelist.this.updateNFCPayApp();
                    AppBlackWhitelist.this.mAppManager.dispatchStateAction(283);
                    return;
                default:
                    return;
            }
        }
    };
    private IPolicy mIPolicy;
    private final ArrayList<String> mIgnoreGpsApps = new ArrayList();
    private final ContentObserver mNFCDefaultAppObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.i("AppBlackWhitelist", "NFC default pay change !");
            AppBlackWhitelist.this.mHandler.removeMessages(1005);
            AppBlackWhitelist.this.mHandler.sendMessageDelayed(AppBlackWhitelist.this.mHandler.obtainMessage(1005), 5000);
        }
    };
    private String mNFCPayApp = null;
    private boolean mStandbyDBExist = true;
    private ContentObserver mStandbyDBObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.i("AppBlackWhitelist", "Standby database changed!");
            AppBlackWhitelist.this.mHandler.removeMessages(1001);
            Message msg = AppBlackWhitelist.this.mHandler.obtainMessage(1001);
            if (AppBlackWhitelist.this.mUpdateStandbyDBImmediately) {
                AppBlackWhitelist.this.mHandler.sendMessageDelayed(msg, 200);
            } else {
                AppBlackWhitelist.this.mHandler.sendMessageDelayed(msg, 5000);
            }
        }
    };
    private ArrayList<String> mStandbyProtectedApps = new ArrayList();
    private ArrayList<String> mStandbyUnprotectedApps = new ArrayList();
    private boolean mUpdateStandbyDBImmediately = false;

    private java.util.ArrayList<java.lang.String> getNotShowPkgs() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0048 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r11 = this;
        r0 = 1;
        r1 = 0;
        r9 = new java.util.ArrayList;
        r9.<init>();
        r6 = 0;
        r3 = "is_show=?";
        r4 = new java.lang.String[r0];
        r0 = "0";
        r4[r1] = r0;
        r0 = r11.mContext;	 Catch:{ Exception -> 0x003f, all -> 0x004f }
        r0 = r0.getContentResolver();	 Catch:{ Exception -> 0x003f, all -> 0x004f }
        r1 = UNIFIED_POWER_APP_TABLE_URI;	 Catch:{ Exception -> 0x003f, all -> 0x004f }
        r2 = 1;	 Catch:{ Exception -> 0x003f, all -> 0x004f }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x003f, all -> 0x004f }
        r5 = "pkg_name";	 Catch:{ Exception -> 0x003f, all -> 0x004f }
        r10 = 0;	 Catch:{ Exception -> 0x003f, all -> 0x004f }
        r2[r10] = r5;	 Catch:{ Exception -> 0x003f, all -> 0x004f }
        r5 = 0;	 Catch:{ Exception -> 0x003f, all -> 0x004f }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x003f, all -> 0x004f }
        if (r6 != 0) goto L_0x0030;
    L_0x002a:
        if (r6 == 0) goto L_0x002f;
    L_0x002c:
        r6.close();
    L_0x002f:
        return r9;
    L_0x0030:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x003f, all -> 0x004f }
        if (r0 == 0) goto L_0x0049;	 Catch:{ Exception -> 0x003f, all -> 0x004f }
    L_0x0036:
        r0 = 0;	 Catch:{ Exception -> 0x003f, all -> 0x004f }
        r8 = r6.getString(r0);	 Catch:{ Exception -> 0x003f, all -> 0x004f }
        r9.add(r8);	 Catch:{ Exception -> 0x003f, all -> 0x004f }
        goto L_0x0030;
    L_0x003f:
        r7 = move-exception;
        r7.printStackTrace();	 Catch:{ Exception -> 0x003f, all -> 0x004f }
        if (r6 == 0) goto L_0x0048;
    L_0x0045:
        r6.close();
    L_0x0048:
        return r9;
    L_0x0049:
        if (r6 == 0) goto L_0x0048;
    L_0x004b:
        r6.close();
        goto L_0x0048;
    L_0x004f:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0055;
    L_0x0052:
        r6.close();
    L_0x0055:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.powergenie.core.app.AppBlackWhitelist.getNotShowPkgs():java.util.ArrayList<java.lang.String>");
    }

    public static AppBlackWhitelist getInstance(ICoreContext coreContext, AppManager appManager) {
        AppBlackWhitelist appBlackWhitelist;
        synchronized (AppBlackWhitelist.class) {
            if (sInstance == null) {
                sInstance = new AppBlackWhitelist(coreContext, appManager);
            }
            appBlackWhitelist = sInstance;
        }
        return appBlackWhitelist;
    }

    private AppBlackWhitelist(ICoreContext coreContext, AppManager appManager) {
        this.mContext = coreContext.getContext();
        this.mAppManager = appManager;
        this.mIPolicy = (IPolicy) coreContext.getService("policy");
        initConfigUpdated();
    }

    public void handleStart() {
        Uri cleanUri = Uri.parse("content://smcs/st_protected_pkgs_table");
        if (this.mContext.getContentResolver().acquireProvider(cleanUri) != null) {
            this.mContext.getContentResolver().registerContentObserver(cleanUri, true, this.mCleanDBObserver, -1);
        } else {
            this.mCleanDBExist = false;
            Log.w("AppBlackWhitelist", "App clean database is not exist!");
        }
        Uri standbyUri = Uri.parse("content://com.huawei.android.smartpowerprovider/unifiedpowerapps");
        if (this.mContext.getContentResolver().acquireProvider(standbyUri) != null) {
            this.mContext.getContentResolver().registerContentObserver(standbyUri, true, this.mStandbyDBObserver, -1);
        } else {
            this.mStandbyDBExist = false;
            Log.w("AppBlackWhitelist", "App standby database is not exist!");
        }
        this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.huawei.powergenie.stats/appstatic"), true, this.mAppStaticDBObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("nfc_payment_default_component"), true, this.mNFCDefaultAppObserver);
        this.mNFCPayApp = updateNFCPayApp();
        updateBlackWhitelist();
        sendChangeDozeWhisteListMsg(5000);
        if (this.mIPolicy.isSupportExtrModeV2()) {
            updateExtrModeV2Apps();
            this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("super_power_save_notification_whitelist"), true, this.mExtrModeV2AppObserver, -1);
        }
        initForeignSuperAppList();
    }

    private String updateNFCPayApp() {
        String nfcComponent = Secure.getString(this.mContext.getContentResolver(), "nfc_payment_default_component");
        if (nfcComponent == null) {
            return null;
        }
        String[] pkgs = nfcComponent.split("/");
        if (pkgs != null) {
            String app = pkgs[0];
            Log.d("AppBlackWhitelist", "NFC default pay app: " + app);
            return app;
        }
        Log.e("AppBlackWhitelist", "not find default NFC");
        return null;
    }

    public void handlePackageState(boolean added, String pkgName) {
        this.mUpdateStandbyDBImmediately = true;
    }

    public String getNFCPayApp() {
        return this.mNFCPayApp;
    }

    public boolean isCleanDBExist() {
        return this.mCleanDBExist;
    }

    public boolean isStandbyDBExist() {
        return this.mStandbyDBExist;
    }

    public boolean isStandbyProtectApp(String pkg) {
        return this.mStandbyProtectedApps.contains(pkg);
    }

    public boolean isStandbyUnprotectApp(String pkg) {
        return this.mStandbyUnprotectedApps.contains(pkg);
    }

    public boolean isCleanProtectApp(String pkg) {
        return this.mCleanProtectedApps.contains(pkg);
    }

    public boolean isCleanUnprotectApp(String pkg) {
        return this.mCleanUnprotectedApps.contains(pkg);
    }

    public ArrayList<String> getCleanProtectApps() {
        return this.mCleanProtectedApps;
    }

    public ArrayList<String> getCleanUnprotectApps() {
        return this.mCleanUnprotectedApps;
    }

    public void updateBlackWhitelist() {
        updateCleanApps();
        updateStandbyApps();
    }

    public boolean isExtrModeV2ReserveApp(String pkg) {
        if (this.mIPolicy.isSupportExtrModeV2() && pkg != null && this.mExtrModeV2ReserveApps.contains(pkg)) {
            return true;
        }
        return false;
    }

    public ArrayList<String> getExtrModeV2ReserveApps() {
        if (this.mIPolicy.isSupportExtrModeV2()) {
            return this.mExtrModeV2ReserveApps;
        }
        Log.i("AppBlackWhitelist", "cannot support extreme mode v2...");
        return null;
    }

    private void updateExtrModeV2Apps() {
        if (this.mIPolicy.isSupportExtrModeV2()) {
            String apps = Secure.getString(this.mContext.getContentResolver(), "super_power_save_notification_whitelist");
            if (apps != null) {
                this.mExtrModeV2ReserveApps.clear();
                String[] pkgs = apps.split(";");
                if (pkgs != null) {
                    for (String name : pkgs) {
                        this.mExtrModeV2ReserveApps.add(name);
                    }
                }
            }
            Log.d("AppBlackWhitelist", "Extreme Reserve apps : " + this.mExtrModeV2ReserveApps);
            return;
        }
        Log.i("AppBlackWhitelist", "cannot support extreme mode v2...");
    }

    private void updateCleanApps() {
        ArrayList<String> cleanProtectedApps = getDataBaseAppList(1);
        if (cleanProtectedApps != null) {
            this.mCleanProtectedApps = cleanProtectedApps;
        }
        ArrayList<String> cleanUnprotectedApps = getDataBaseAppList(0);
        if (cleanUnprotectedApps != null) {
            this.mCleanUnprotectedApps = cleanUnprotectedApps;
        }
        Log.i("AppBlackWhitelist", "Clean protected apps: " + this.mCleanProtectedApps);
        Log.i("AppBlackWhitelist", "Clean unprotected apps: " + this.mCleanUnprotectedApps);
    }

    private void updateStandbyApps() {
        ArrayList<String> unShowPkgs = getNotShowPkgs();
        Log.i("AppBlackWhitelist", "start refresh apps, unShown pkgs:" + unShowPkgs);
        if (unShowPkgs.contains("com.tencent.mm") && !unShowPkgs.contains("com.android.calculator2")) {
            Log.i("AppBlackWhitelist", "cannot care cloud config mm...");
            unShowPkgs.remove("com.tencent.mm");
        }
        try {
            Bundle standbyBundle = this.mContext.getContentResolver().call(Uri.parse("content://" + this.mAppManager.getCurUserId() + "@com.huawei.android.smartpowerprovider"), "hsm_get_freeze_list", "all", null);
            if (standbyBundle == null) {
                Log.i("AppBlackWhitelist", "updateStandbyApps,  smcs provider does not implement call.");
                return;
            }
            ArrayList<String> standbyProtectedApps = standbyBundle.getStringArrayList("frz_protect");
            if (standbyProtectedApps != null) {
                this.mStandbyProtectedApps = standbyProtectedApps;
            }
            ArrayList<String> standbyUnprotectedApps = standbyBundle.getStringArrayList("frz_unprotect");
            if (standbyUnprotectedApps != null) {
                this.mStandbyUnprotectedApps = standbyUnprotectedApps;
            }
            this.mStandbyProtectedApps.removeAll(unShowPkgs);
            this.mStandbyUnprotectedApps.removeAll(unShowPkgs);
            Log.i("AppBlackWhitelist", "Standby protected apps: " + this.mStandbyProtectedApps);
            Log.i("AppBlackWhitelist", "Standby unprotected apps: " + this.mStandbyUnprotectedApps);
        } catch (IllegalArgumentException e) {
            Log.e("AppBlackWhitelist", "updateStandbyApps failed:" + e.toString());
        }
    }

    private ArrayList<String> getDataBaseAppList(int selectValue) {
        String column = "pkg_name";
        Uri tableUri = Uri.parse("content://" + this.mAppManager.getCurUserId() + "@smcs/st_protected_pkgs_table");
        String[] projection = new String[]{"pkg_name"};
        String[] selectionArgs = new String[]{Integer.toString(selectValue)};
        ArrayList<String> pkgList = new ArrayList();
        Cursor cursor = this.mContext.getContentResolver().query(tableUri, projection, "is_checked=?", selectionArgs, null);
        if (cursor == null) {
            Log.w("AppBlackWhitelist", "selection table is not exist.");
            return null;
        }
        try {
            int colIndext = cursor.getColumnIndex(column);
            while (cursor.moveToNext()) {
                String pkgName = cursor.getString(colIndext);
                if (pkgName != null) {
                    pkgList.add(pkgName);
                } else {
                    Log.w("AppBlackWhitelist", "Unknown packagename.");
                }
            }
        } catch (RuntimeException ex) {
            Log.e("AppBlackWhitelist", "RuntimeException:", ex);
        } finally {
            cursor.close();
        }
        return pkgList;
    }

    private void initForeignSuperAppList() {
        if (!this.mIPolicy.isChinaMarketProduct() && mIsForeignSuperAppPolicy) {
            XmlHelper.loadResAppList(this.mContext, R.xml.foreign_protect_apps, null, mForeignSuperApps);
            Log.i("AppBlackWhitelist", "initForeignSuperAppList , size : " + mForeignSuperApps.size());
        }
    }

    public boolean isForeignSuperApp(String pkg) {
        return mForeignSuperApps.contains(pkg);
    }

    public boolean isForeignSuperAppPolicy() {
        return mIsForeignSuperAppPolicy;
    }

    protected boolean isIgnoreGpsApp(String pkg) {
        boolean z;
        synchronized (this) {
            z = this.mIgnoreGpsApps.contains(pkg);
        }
        return z;
    }

    protected boolean isIgnoreAudioApp(String pkg) {
        boolean z;
        synchronized (this) {
            z = mIgnoreAudioApps.contains(pkg);
        }
        return z;
    }

    protected boolean isForceCleanApp(String pkg) {
        boolean z;
        synchronized (this) {
            z = mForceCleanApps.contains(pkg);
        }
        return z;
    }

    protected boolean isFroceKillProc(String procName) {
        boolean z;
        synchronized (this) {
            z = mKillProcList.contains(procName);
        }
        return z;
    }

    private void sendChangeDozeWhisteListMsg(long delay) {
        if (mAddCleanProtectedAppToStandbyWhitelist && this.mIPolicy.isChinaMarketProduct()) {
            this.mHandler.removeMessages(1003);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1003), delay);
            return;
        }
        Log.i("AppBlackWhitelist", "not enable, don't chg doze list");
    }

    private void changeDozeWhiteList() {
        long now = SystemClock.uptimeMillis();
        AppStandbyDozeAdapter adapter = AppStandbyDozeAdapter.getInstance(this.mContext);
        String[] array = adapter.getWhiteList();
        List nowWhistList = null;
        if (array != null) {
            nowWhistList = Arrays.asList(array);
        }
        List<String> list = getProtectedPkgInCleanDBButNotInStandbyDB();
        if (list.size() > 0) {
            Log.i("AppBlackWhitelist", "changeDozeWhiteList add: " + list);
            for (String pkg : list) {
                if ((nowWhistList != null && !nowWhistList.contains(pkg)) || nowWhistList == null) {
                    adapter.addWhiteList(pkg);
                }
            }
        }
        list = getUnProtectedPkgInCleanDBAndStandbyDB();
        if (list.size() > 0 && nowWhistList != null) {
            Log.i("AppBlackWhitelist", "changeDozeWhiteList remove: " + list);
            for (String pkg2 : list) {
                if (nowWhistList.contains(pkg2)) {
                    adapter.removeWhiteList(pkg2);
                }
            }
        }
        Log.i("AppBlackWhitelist", "changeDozeWhiteList use: " + (SystemClock.uptimeMillis() - now));
    }

    private List<String> getProtectedPkgInCleanDBButNotInStandbyDB() {
        ArrayList<String> list = new ArrayList();
        for (String pkg : this.mCleanProtectedApps) {
            if (!this.mStandbyProtectedApps.contains(pkg)) {
                list.add(pkg);
            }
        }
        return list;
    }

    private List<String> getUnProtectedPkgInCleanDBAndStandbyDB() {
        ArrayList<String> list = new ArrayList();
        for (String pkg : this.mCleanUnprotectedApps) {
            if (this.mStandbyUnprotectedApps.contains(pkg)) {
                list.add(pkg);
            }
        }
        return list;
    }

    public void handleAction(PowerAction action) {
        switch (action.getActionId()) {
            case 358:
                handleUpdateConfig(action.getExtraValString("pushType"), action.getExtraValString("uri"));
                return;
            default:
                return;
        }
    }

    public void handleUpdateConfig(String type, String uri) {
        if (type.equals("pg_config_list")) {
            initConfigUpdated();
        }
    }

    private void initConfigUpdated() {
        loadIgnoreGpsApps();
        loadIgnoreAudioApps();
        loadForceCleanApps();
        loadKillProcList();
    }

    private void loadIgnoreGpsApps() {
        synchronized (this) {
            this.mIgnoreGpsApps.clear();
            XmlHelper.loadResAppList(this.mContext, R.xml.ignore_gps_apps, null, this.mIgnoreGpsApps);
            this.mIPolicy.updateConfigList("ignore_gps_apps", this.mIgnoreGpsApps);
            Log.i("AppBlackWhitelist", "IgnoreGpsApps blacklist: " + this.mIgnoreGpsApps);
        }
    }

    private void loadIgnoreAudioApps() {
        synchronized (this) {
            mIgnoreAudioApps.clear();
            this.mIPolicy.updateConfigList("ignore_audio_apps", mIgnoreAudioApps);
            Log.i("AppBlackWhitelist", "mIgnoreAudioApps: " + mIgnoreAudioApps);
        }
    }

    private void loadForceCleanApps() {
        synchronized (this) {
            mForceCleanApps.clear();
            this.mIPolicy.updateConfigList("force_clean_apps", mForceCleanApps);
            Log.i("AppBlackWhitelist", "mForceCleanApps: " + mForceCleanApps);
        }
    }

    private void loadKillProcList() {
        synchronized (this) {
            mKillProcList.clear();
            this.mIPolicy.updateConfigList("kill_proc_list", mKillProcList);
            Log.i("AppBlackWhitelist", "mKillProcList: " + mKillProcList);
        }
    }
}
