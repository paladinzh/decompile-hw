package com.huawei.systemmanager.hsmstat;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.PackageParserException;
import android.content.pm.PackageUserState;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.InstallerPkgDisplay;
import com.huawei.systemmanager.service.MainService.HsmService;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class HandleInstalledPackageInfoService implements HsmService {
    private static final String ACTION_GET_INSTALLER_PACKAGE_INFO = "com.huawei.android.action.GET_INSTALLER_PACKAGE_INFO";
    private static final String ACTION_GET_PACKAGE_INSTALLATION_INFO = "com.huawei.android.action.GET_PACKAGE_INSTALLATION_INFO";
    private static final int ADB_INSTALLATION = 2;
    private static final int APP_MARKET_INSTALLATION = 1;
    private static final String INSTALLATION_EXTRA_PACKAGE_INSTALLER_PID = "pkgInstallerPid";
    private static final String INSTALLATION_EXTRA_PACKAGE_INSTALLER_UID = "pkgInstallerUid";
    private static final String INSTALLATION_EXTRA_PACKAGE_INSTALL_RESULT = "pkgInstallResult";
    private static final String INSTALLATION_EXTRA_PACKAGE_NAME = "pkgName";
    private static final String INSTALLATION_EXTRA_PACKAGE_UPDATE = "pkgUpdate";
    private static final String INSTALLATION_EXTRA_PACKAGE_URI = "pkgUri";
    private static final String INSTALLATION_EXTRA_PACKAGE_VERSION_CODE = "pkgVersionCode";
    private static final String INSTALLATION_EXTRA_PACKAGE_VERSION_NAME = "pkgVersionName";
    private static final String INSTALL_RESOURCE_FOR_ADB = "adb install";
    private static final int INSTALL_RESULT_CANCEL = 2;
    private static final int INSTALL_RESULT_FAIL = 3;
    private static final int INSTALL_RESULT_SUCCEED = 1;
    private static final int MAX_ITEM_COUNT = 10;
    private static final int NOT_UPDATE_INSTALL = 0;
    private static final int PACKAGEINSTALLER_INSTALLATION = 3;
    private static final String PACKAGE_ANDROID_PACKAGEINSTALLER = "com.android.packageinstaller";
    private static final String TAG = HandleInstalledPackageInfoService.class.getSimpleName();
    private static final int UPDATE_INSTALL = 1;
    private static final Map<String, StatStruc> mCache = new HashMap();
    private BroadcastReceiver mBroadCastReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwLog.e(HandleInstalledPackageInfoService.TAG, "intent is null");
                return;
            }
            String action = intent.getAction();
            HwLog.i(HandleInstalledPackageInfoService.TAG, "onReceive  action is " + action);
            if (HandleInstalledPackageInfoService.ACTION_GET_INSTALLER_PACKAGE_INFO.equals(action)) {
                Bundle extras_installer = intent.getExtras();
                if (extras_installer == null) {
                    HwLog.e(HandleInstalledPackageInfoService.TAG, "first action: extras_installer is null");
                    return;
                }
                int installerPkgUid = extras_installer.getInt(HandleInstalledPackageInfoService.INSTALLATION_EXTRA_PACKAGE_INSTALLER_UID, -1);
                int installerPkgPid = extras_installer.getInt(HandleInstalledPackageInfoService.INSTALLATION_EXTRA_PACKAGE_INSTALLER_PID, -1);
                String pkgInstalledUri = extras_installer.getString(HandleInstalledPackageInfoService.INSTALLATION_EXTRA_PACKAGE_URI, null);
                if (pkgInstalledUri == null) {
                    HwLog.w(HandleInstalledPackageInfoService.TAG, "pkgInstalledUri is null");
                    return;
                }
                String installerPkgName = HandleInstalledPackageInfoService.this.getAppInfoByUidAndPid(HandleInstalledPackageInfoService.this.mContext, installerPkgUid, installerPkgPid);
                if ("com.android.packageinstaller".equals(installerPkgName)) {
                    HwLog.w(HandleInstalledPackageInfoService.TAG, "this is PackageInstaller branch, so return directly");
                    return;
                }
                StatStruc dataColl = new StatStruc();
                dataColl.setPkguri(pkgInstalledUri);
                dataColl.setPkgsource(installerPkgName);
                int count = HandleInstalledPackageInfoService.this.getPackageInfoCount();
                HwLog.i(HandleInstalledPackageInfoService.TAG, "get count = " + count);
                if (count > 10) {
                    HandleInstalledPackageInfoService.this.clearInstallationItems();
                } else {
                    HandleInstalledPackageInfoService.this.putInstallationCache(dataColl);
                }
            } else if (HandleInstalledPackageInfoService.ACTION_GET_PACKAGE_INSTALLATION_INFO.equals(action)) {
                Bundle extras = intent.getExtras();
                if (extras == null) {
                    HwLog.e(HandleInstalledPackageInfoService.TAG, "second action: extras is null");
                    return;
                }
                String pkgUri = extras.getString(HandleInstalledPackageInfoService.INSTALLATION_EXTRA_PACKAGE_URI, null);
                StatStruc pkgData = HandleInstalledPackageInfoService.this.getInstallationCache(pkgUri);
                if (pkgData == null) {
                    HwLog.e(HandleInstalledPackageInfoService.TAG, "second action: pkgData is null");
                    return;
                }
                String pkgName = extras.getString("pkgName", "");
                if (pkgName == null || TextUtils.isEmpty(pkgName)) {
                    pkgName = HandleInstalledPackageInfoService.this.getPackageName(pkgUri);
                }
                int pkgVersionCode = extras.getInt(HandleInstalledPackageInfoService.INSTALLATION_EXTRA_PACKAGE_VERSION_CODE, 0);
                String pkgVersionName = extras.getString(HandleInstalledPackageInfoService.INSTALLATION_EXTRA_PACKAGE_VERSION_NAME, "");
                boolean pkgUpdate = extras.getBoolean(HandleInstalledPackageInfoService.INSTALLATION_EXTRA_PACKAGE_UPDATE, false);
                int pkgInstallResult = extras.getInt(HandleInstalledPackageInfoService.INSTALLATION_EXTRA_PACKAGE_INSTALL_RESULT, 0);
                pkgData.setPkgname(pkgName);
                pkgData.setPkgversioncode(pkgVersionCode);
                pkgData.setPkgversionname(pkgVersionName);
                if (pkgUpdate) {
                    pkgData.setPkgupdate(1);
                } else {
                    pkgData.setPkgupdate(0);
                }
                if (pkgInstallResult == 1) {
                    pkgData.setPkgresult(1);
                } else {
                    pkgData.setPkgresult(3);
                    pkgData.setPkgfailedreason(pkgInstallResult);
                }
                String installerName = pkgData.getPkgsource();
                if ("com.android.packageinstaller".equals(installerName)) {
                    pkgData.setPkgsilentinstallation(false);
                } else {
                    pkgData.setPkgsilentinstallation(true);
                }
                pkgData.setPkginstallationmethod(HandleInstalledPackageInfoService.this.getInstallationMethod(installerName));
                HandleInstalledPackageInfoService.this.statE(pkgData.getPkguri());
                HandleInstalledPackageInfoService.this.removeInstallationItem(pkgData.getPkguri());
            } else {
                HwLog.e(HandleInstalledPackageInfoService.TAG, "second action: this branch cann't be reached");
            }
        }
    };
    private Context mContext = null;
    private PackageManager mPm;
    private boolean mRegistered = false;

    static class StatStruc {
        private int pkgfailedreason = 0;
        private int pkginstallationmethod = -1;
        private String pkgname = "";
        private int pkgresult = -1;
        private boolean pkgsilentinstallation = false;
        private String pkgsource = "";
        private int pkgupdate = -1;
        private String pkguri = "";
        private int pkgversioncode = -1;
        private String pkgversionname = "";

        StatStruc() {
        }

        private String toDcString() {
            return HsmStatConst.constructValue("n", this.pkgname, InstallerPkgDisplay.KEY_INSTALLED_PACKAGE_VERSION_CODE, String.valueOf(this.pkgversioncode), InstallerPkgDisplay.KEY_INSTALLED_PACKAGE_VERSION_NAME, this.pkgversionname, "s", this.pkgsource, InstallerPkgDisplay.KEY_PACKAGE_INSTALL_UPDATE, String.valueOf(this.pkgupdate), "r", String.valueOf(this.pkgresult), InstallerPkgDisplay.KEY_PACKAGE_INSTALL_FAILED_REASON, String.valueOf(this.pkgfailedreason), InstallerPkgDisplay.KEY_SILENT_INSTALLATION, String.valueOf(this.pkgsilentinstallation), InstallerPkgDisplay.KEY_INSTALLATION_METHOD, String.valueOf(this.pkginstallationmethod));
        }

        private String getPkgsource() {
            return this.pkgsource;
        }

        private void setPkgsource(String pkgsource) {
            this.pkgsource = pkgsource;
        }

        private void setPkgresult(int pkgresult) {
            this.pkgresult = pkgresult;
        }

        private void setPkgname(String pkgname) {
            this.pkgname = pkgname;
        }

        private void setPkgversioncode(int pkgversioncode) {
            this.pkgversioncode = pkgversioncode;
        }

        private void setPkgversionname(String pkgversionname) {
            this.pkgversionname = pkgversionname;
        }

        private void setPkgupdate(int pkgupdate) {
            this.pkgupdate = pkgupdate;
        }

        private void setPkgfailedreason(int pkgfailedreason) {
            this.pkgfailedreason = pkgfailedreason;
        }

        private void setPkgsilentinstallation(boolean pkgsilentinstallation) {
            this.pkgsilentinstallation = pkgsilentinstallation;
        }

        private void setPkginstallationmethod(int pkginstallationmethod) {
            this.pkginstallationmethod = pkginstallationmethod;
        }

        private String getPkguri() {
            return this.pkguri;
        }

        private void setPkguri(String pkguri) {
            this.pkguri = pkguri;
        }
    }

    public HandleInstalledPackageInfoService(Context context) {
        this.mContext = context;
    }

    public void init() {
        if (this.mRegistered) {
            HwLog.w(TAG, "already register!");
            return;
        }
        HwLog.i(TAG, "register broadcast!");
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(ACTION_GET_INSTALLER_PACKAGE_INFO);
        this.mContext.registerReceiver(this.mBroadCastReciever, filter1, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(ACTION_GET_PACKAGE_INSTALLATION_INFO);
        this.mContext.registerReceiver(this.mBroadCastReciever, filter2, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        this.mRegistered = true;
    }

    public void onDestroy() {
        if (this.mRegistered) {
            this.mContext.unregisterReceiver(this.mBroadCastReciever);
            this.mRegistered = false;
            HwLog.i(TAG, "unregister broadcast!");
        }
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
    }

    private String getPackageName(String pkgUri) {
        String packageName = "";
        PackageInfo pkgInfo = getInstalledAppInfo(pkgUri);
        if (pkgInfo != null) {
            return pkgInfo.packageName;
        }
        HwLog.w(TAG, "pkgInfo is null, and pkgName is empty");
        return packageName;
    }

    private int getInstallationMethod(String installerPkgName) {
        if (installerPkgName == null || TextUtils.isEmpty(installerPkgName)) {
            return -1;
        }
        int installMethod;
        if (INSTALL_RESOURCE_FOR_ADB.equals(installerPkgName)) {
            installMethod = 2;
        } else if ("com.android.packageinstaller".equals(installerPkgName)) {
            installMethod = 3;
        } else {
            installMethod = 1;
        }
        return installMethod;
    }

    private String getAppInfoByUidAndPid(Context context, int uid, int pid) {
        long time1 = System.currentTimeMillis();
        String pkgName = "";
        if (uid == 2000 || uid == 0) {
            return INSTALL_RESOURCE_FOR_ADB;
        }
        for (RunningAppProcessInfo runningInfo : ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses()) {
            int runningPid = runningInfo.pid;
            int runningUid = runningInfo.uid;
            if (runningPid == pid && runningUid == uid) {
                String[] pkgNameList = runningInfo.pkgList;
                if (pkgNameList != null && pkgNameList.length > 0) {
                    pkgName = pkgNameList[0];
                }
                HwLog.i(TAG, "getAppInfoByUidAndPid getAppInfoByUidAndPid time consume:" + (System.currentTimeMillis() - time1) + ", pkgName = " + pkgName);
                return pkgName;
            }
        }
        return pkgName;
    }

    private PackageInfo getInstalledAppInfo(String pkgUri) {
        PackageInfo pkgInfo = null;
        Uri packageURI = Uri.fromFile(new File(pkgUri));
        if ("package".equals(packageURI.getScheme())) {
            try {
                pkgInfo = PackageManagerWrapper.getPackageInfo(this.mPm, packageURI.getSchemeSpecificPart(), 12288);
            } catch (NameNotFoundException e) {
            }
            if (pkgInfo == null) {
                HwLog.w(TAG, "Requested package " + packageURI.getScheme() + " not available. Discontinuing installation");
                return null;
            }
        }
        Package parsed = getPackageInfo(new File(packageURI.getPath()));
        if (parsed == null) {
            HwLog.w(TAG, "Parse error when parsing manifest. Discontinuing installation");
            return null;
        }
        pkgInfo = PackageParser.generatePackageInfo(parsed, null, 4096, 0, 0, null, new PackageUserState());
        if (pkgInfo == null) {
            HwLog.w(TAG, "Parse error when generating PackageInfo. Discontinuing installation");
            return null;
        }
        return pkgInfo;
    }

    private Package getPackageInfo(File sourceFile) {
        try {
            return new PackageParser().parseMonolithicPackage(sourceFile, 0);
        } catch (PackageParserException e) {
            return null;
        }
    }

    private void putInstallationCache(StatStruc d) {
        mCache.put(d.pkguri, d);
    }

    private StatStruc getInstallationCache(String pkgUri) {
        return (StatStruc) mCache.get(pkgUri);
    }

    private void removeInstallationItem(String pkgUri) {
        mCache.remove(pkgUri);
    }

    private int getPackageInfoCount() {
        return mCache.size();
    }

    private void clearInstallationItems() {
        mCache.clear();
    }

    private void statE(String pkgUri) {
        if (getInstallationCache(pkgUri) == null) {
            HwLog.e(TAG, "statE  d is null");
            return;
        }
        HsmStat.statE(InstallerPkgDisplay.ACTION_GET_INSTALLATION_INFO, d.toDcString());
    }
}
