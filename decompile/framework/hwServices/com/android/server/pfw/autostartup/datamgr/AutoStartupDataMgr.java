package com.android.server.pfw.autostartup.datamgr;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import com.android.server.pfw.autostartup.comm.AutoStartupUtil;
import com.android.server.pfw.autostartup.comm.DefaultXmlParsedResult;
import com.android.server.pfw.autostartup.xmlparser.StartupParsers;
import com.android.server.pfw.log.HwPFWLogger;
import huawei.android.pfw.HwPFWStartupSetting;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class AutoStartupDataMgr {
    private static final boolean DEBUG_DATAMGR_DETAIL = false;
    private static final String TAG = "AutoStartupDataMgr";
    private HsmCacheData mCacheMgr = new HsmCacheData();
    private Pattern mCtsPattern = Pattern.compile(".*android.*cts.*");
    private Set<String> mExtSystemBlackPkgs = new HashSet();
    private Set<String> mExtThirdWhitePkgs = new HashSet();
    private ForbidenLaunchedActivitySet mForbidenActivitySet = new ForbidenLaunchedActivitySet();
    private Set<String> mSpecialCtsPkgs = new HashSet();
    private SystemUnRemovableUidCache mSpecialUidSet = new SystemUnRemovableUidCache();
    private boolean mStopAfterBootOpFinish = false;
    private DefaultXmlParsedResult mXmlData = new DefaultXmlParsedResult();

    public void initData(Context ctx) {
        this.mXmlData.appendData(StartupParsers.parseAssetFile(ctx));
        this.mCacheMgr.loadCache();
        this.mCacheMgr.loadWidgetCache();
        loadSpecialCtsPackages();
    }

    public void setStopAfterBootOpFinish() {
        HwPFWLogger.d(TAG, "setStopAfterBootOpFinish called");
        this.mStopAfterBootOpFinish = true;
    }

    public boolean whiteReceiverForAll(String action) {
        return this.mXmlData.whiteReceiverAction(action);
    }

    public boolean isWhiteReceiverStartupRequest(ApplicationInfo applicationInfo) {
        return !shouldTryFilter(applicationInfo);
    }

    public boolean isWhiteApplicationInfo(ApplicationInfo applicationInfo) {
        return !shouldTryFilter(applicationInfo);
    }

    public boolean isWhiteStartupRequest(ApplicationInfo applicationInfo, int callerUid) {
        boolean z = true;
        if (isTargetAndCallerSameUID(applicationInfo, callerUid)) {
            return true;
        }
        boolean shouldTryFilter = shouldTryFilter(applicationInfo);
        if (isSpecialCaller(callerUid)) {
            if (shouldTryFilter) {
                HwPFWLogger.i(TAG, "isWhiteStartupRequest: allow " + applicationInfo.packageName + " to be started by " + callerUid);
            }
            return true;
        }
        if (shouldTryFilter) {
            z = false;
        }
        return z;
    }

    public List<String> copyOfFwkSystemBlackPkgs() {
        return this.mXmlData.copyOfSystemBlackPkgs();
    }

    public List<String> copyOfFwkThirdWhitePkgs() {
        return this.mXmlData.copyOfThirdWhitePkgs();
    }

    public void appendExtSystemBlackPkgs(List<String> pkgs) {
        this.mExtSystemBlackPkgs.addAll(pkgs);
        HwPFWLogger.d(TAG, "appendExtSystemBlackPkgs: " + this.mExtSystemBlackPkgs);
    }

    public void appendExtThirdWhitePkgs(List<String> pkgs) {
        this.mExtThirdWhitePkgs.addAll(pkgs);
        HwPFWLogger.d(TAG, "appendExtThirdWhitePkgs: " + this.mExtThirdWhitePkgs);
    }

    public void updateStartupSettings(List<HwPFWStartupSetting> settings, boolean clearFirst) {
        this.mCacheMgr.updateStartupSettings(settings, clearFirst);
    }

    public void removeStartupSetting(String pkgName) {
        this.mCacheMgr.removeStartupSetting(pkgName);
    }

    public int retrieveStartupSettings(String pkgName, int type) {
        int result = this.mCacheMgr.retrieveStartupSetting(pkgName, type);
        HwPFWLogger.d(TAG, "retrieveStartupSettings type " + type + " of " + pkgName + " is " + result);
        return result;
    }

    public void updateWidgetList(List<String> pkgList) {
        this.mCacheMgr.updateWidgetPkgList(pkgList);
    }

    public void flushCacheDataToDisk() {
        this.mCacheMgr.flushCacheDataToDisk();
    }

    public void loadSystemUid(Context ctx) {
        this.mSpecialUidSet.loadSystemUid(ctx);
    }

    public void loadForbidenActivity() {
        this.mForbidenActivitySet.loadForbidenActivitySet();
    }

    public boolean checkActivityExist(String clsName) {
        return this.mForbidenActivitySet.checkActivityExist(clsName);
    }

    public boolean isAllowBootStartup(String pkgName) {
        if (isPackageInThirdWhiteList(pkgName) || this.mCacheMgr.isWidgetExistPkg(pkgName) || 1 == this.mCacheMgr.retrieveStartupSetting(pkgName, 0)) {
            return true;
        }
        return isCtsPackage(pkgName);
    }

    private boolean shouldTryFilter(ApplicationInfo applicationInfo) {
        if (AutoStartupUtil.isAppStopped(applicationInfo)) {
            if (isUnderControll(applicationInfo)) {
                return true;
            }
        } else if (!this.mStopAfterBootOpFinish && isUnderControll(applicationInfo)) {
            return true;
        }
        return false;
    }

    private boolean isUnderControll(ApplicationInfo applicationInfo) {
        if (applicationInfo.uid < 10000) {
            return false;
        }
        if (isCtsPackage(applicationInfo.packageName)) {
            HwPFWLogger.d(TAG, "isCtsPackage : " + applicationInfo.packageName);
            return false;
        } else if (this.mCacheMgr.isWidgetExistPkg(applicationInfo.packageName)) {
            HwPFWLogger.d(TAG, "isWidgetExistPkg: " + applicationInfo.packageName);
            return false;
        } else {
            if (AutoStartupUtil.isSystemUnRemovablePkg(applicationInfo)) {
                if (isPackageInSystemBlackList(applicationInfo.packageName)) {
                    HwPFWLogger.d(TAG, "isUnderControll system in blacklist: " + applicationInfo.packageName);
                    return true;
                }
            } else if (!isPackageInThirdWhiteList(applicationInfo.packageName)) {
                HwPFWLogger.d(TAG, "isUnderControll third party not in whitelist: " + applicationInfo.packageName);
                return true;
            }
            return false;
        }
    }

    private void loadSpecialCtsPackages() {
        this.mSpecialCtsPkgs.add("android.tests.devicesetup");
        this.mSpecialCtsPkgs.add("android.voicesettings");
        this.mSpecialCtsPkgs.add("android.voiceinteraction");
        this.mSpecialCtsPkgs.add("android.externalservice.service");
    }

    private boolean isCtsPackage(String pkgName) {
        if (this.mCtsPattern.matcher(pkgName).matches() || this.mSpecialCtsPkgs.contains(pkgName)) {
            return true;
        }
        return false;
    }

    public boolean isPackageInSystemBlackList(String pkgName) {
        return !this.mXmlData.isSystemBlackPkgs(pkgName) ? this.mExtSystemBlackPkgs.contains(pkgName) : true;
    }

    private boolean isPackageInThirdWhiteList(String pkgName) {
        return !this.mXmlData.isThirdWhitePkgs(pkgName) ? this.mExtThirdWhitePkgs.contains(pkgName) : true;
    }

    private boolean isTargetAndCallerSameUID(ApplicationInfo applicationInfo, int callerUid) {
        if (applicationInfo.uid == callerUid) {
            return true;
        }
        return false;
    }

    private boolean isSpecialCaller(int callerUid) {
        if (callerUid >= 10000 && !this.mSpecialUidSet.checkUidExist(callerUid)) {
            return false;
        }
        return true;
    }

    public void dumpToLog() {
    }

    public void dumpToDumpsys(PrintWriter pw) {
        pw.println(this.mXmlData.toString());
        pw.println(this.mCacheMgr.toString());
    }
}
