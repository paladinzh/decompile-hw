package com.huawei.systemmanager.util.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfoEx;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HsmPackageManager {
    public static final int ALL_APP_WITHOUT_KEEP_DATA = 0;
    private static final String TAG = "HsmPackageManager";
    private static HsmPackageManager mInstance = null;
    private static List<IPackageChangeListener> mListener = new ArrayList();
    private Context mContext;
    private InfoCreator mCreator;
    private Drawable mDefaultIcon;
    private Configuration mLastConfig;
    private final Map<String, HsmPkgInfo> mPackages;
    private AppChangeReceiver mReceiver;
    private String mSavedLocale;
    private final Map<String, HsmPkgInfo> mUninstalledPackages;

    private HsmPackageManager() {
        this.mContext = null;
        this.mReceiver = new AppChangeReceiver();
        this.mCreator = InfoCreator.DEFAULT_CREATE;
        this.mPackages = new HashMap();
        this.mUninstalledPackages = new HashMap();
        this.mDefaultIcon = null;
        this.mLastConfig = new Configuration();
        this.mContext = GlobalContext.getContext();
        scanApks();
        registerReceiver();
        initLocaleConfig();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addDataScheme("package");
        this.mContext.registerReceiver(this.mReceiver, filter);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(IntentCompat.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filter2.addAction(IntentCompat.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        this.mContext.registerReceiver(this.mReceiver, filter2);
    }

    public static synchronized HsmPackageManager getInstance() {
        HsmPackageManager hsmPackageManager;
        synchronized (HsmPackageManager.class) {
            if (mInstance == null) {
                mInstance = new HsmPackageManager();
            }
            hsmPackageManager = mInstance;
        }
        return hsmPackageManager;
    }

    public List<HsmPkgInfo> getInstalledPackages(int flag) {
        List<HsmPkgInfo> result = new ArrayList();
        synchronized (this) {
            result.addAll(this.mPackages.values());
        }
        if (needUninstalledPkgs(flag)) {
            synchronized (this) {
                result.addAll(this.mUninstalledPackages.values());
            }
        }
        HwLog.i(TAG, "get all installed packages, flag:" + flag + ", size:" + result.size());
        return result;
    }

    public List<HsmPkgInfo> getAllPackages() {
        return getInstalledPackages(8192);
    }

    public List<HsmPkgInfo> getPartUninstalledPackages() {
        List<HsmPkgInfo> result = new ArrayList();
        synchronized (this) {
            result.addAll(this.mUninstalledPackages.values());
        }
        HwLog.i(TAG, "get uninstalled packages, size:" + result.size());
        return result;
    }

    public HsmPkgInfo getPkgInfo(String pkgName, int flag) throws NameNotFoundException {
        HsmPkgInfo info = getPkgInfoInner(pkgName, flag);
        if (info != null) {
            return info;
        }
        throw new NameNotFoundException("pacakge not exist :" + pkgName);
    }

    public HsmPkgInfo getPkgInfo(String pkgName) {
        return getPkgInfoInner(pkgName, 8192);
    }

    public HsmPkgInfo getPkgInfoInstalled(String pkgName) {
        return getPkgInfoInner(pkgName, 0);
    }

    private boolean needUninstalledPkgs(int flag) {
        return (flag & 8192) != 0;
    }

    private void scanApks() {
        HwLog.v(TAG, "begin to scan apks.");
        synchronized (this) {
            this.mPackages.clear();
            this.mUninstalledPackages.clear();
        }
        PackageManager pm = this.mContext.getPackageManager();
        List<PackageInfo> installed = PackageManagerWrapper.getInstalledPackages(pm, 64);
        List<PackageInfo> includeUninstalled = PackageManagerWrapper.getInstalledPackages(pm, 8256);
        Map<String, HsmPkgInfo> tempMap = new HashMap();
        for (PackageInfo info : installed) {
            tempMap.put(info.packageName, new HsmPkgInfo(info, pm));
        }
        synchronized (this) {
            this.mPackages.putAll(tempMap);
        }
        tempMap.clear();
        Map<String, HsmPkgInfo> tempMap2 = new HashMap();
        synchronized (this) {
            for (PackageInfo info2 : includeUninstalled) {
                if (!this.mPackages.containsKey(info2.packageName)) {
                    tempMap2.put(info2.packageName, new HsmPkgInfo(info2, pm, true));
                }
            }
            this.mUninstalledPackages.putAll(tempMap2);
        }
        tempMap2.clear();
        installed.clear();
        includeUninstalled.clear();
        synchronized (this) {
            if (Log.HWINFO) {
                HwLog.i(TAG, "end to scan apks. size:" + this.mPackages.size() + " + " + this.mUninstalledPackages.size());
            }
        }
    }

    public boolean isSystem(String pkgName) {
        try {
            return getPkgInfo(pkgName, 8192).isSystem();
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public boolean isRemovable(String pkgName) {
        try {
            return getPkgInfo(pkgName, 8192).isRemovable();
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public boolean isPreInstalled(String pkgName) {
        try {
            return getPkgInfo(pkgName, 8192).isRemoveAblePreInstall();
        } catch (NameNotFoundException e) {
            HwLog.i(TAG, "isPreInstalled,can not find pkg:" + pkgName);
            return false;
        }
    }

    public boolean packageExists(String pkgName, int flag) {
        try {
            getPkgInfo(pkgName, flag);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public Drawable getIcon(String pkgName) {
        try {
            return getPkgInfo(pkgName, 0).icon();
        } catch (NameNotFoundException e) {
            HwLog.w(TAG, "getIcon, not found app, return default icon.");
            return getDefaultIcon();
        }
    }

    public String getLabel(String pkgName) {
        try {
            return getPkgInfo(pkgName, 0).label();
        } catch (NameNotFoundException e) {
            return pkgName;
        }
    }

    private HsmPkgInfo getPkgInfoInner(String pkgName, int flag) {
        HsmPkgInfo info;
        synchronized (this) {
            info = (HsmPkgInfo) this.mPackages.get(pkgName);
            if (info == null && (flag & 8192) != 0) {
                info = (HsmPkgInfo) this.mUninstalledPackages.get(pkgName);
            }
        }
        return info;
    }

    public void setInfoCreator(InfoCreator creator) {
        if (creator == null) {
            creator = InfoCreator.DEFAULT_CREATE;
        }
        this.mCreator = creator;
    }

    public static synchronized void destroyInstance() {
        synchronized (HsmPackageManager.class) {
            HwLog.i(TAG, "destroy HsmPackageManager.");
            mInstance = null;
        }
    }

    public static void registerListener(IPackageChangeListener listener) {
        synchronized (mListener) {
            mListener.add(listener);
        }
    }

    public static void unregisterListener(IPackageChangeListener listener) {
        synchronized (mListener) {
            mListener.remove(listener);
        }
    }

    public void onReceive(Context context, Intent intent) {
        HwLog.i(TAG, "Receive intent:" + intent);
        if (intent == null) {
            HwLog.w(TAG, "intent is null?");
            return;
        }
        boolean replacing = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
        HwLog.i(TAG, "replacing:" + replacing);
        String action = intent.getAction();
        if (IntentCompat.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
            doExternalAppChanged(intent, true);
        } else if (IntentCompat.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
            doExternalAppChanged(intent, false);
        } else if ("android.intent.action.PACKAGE_CHANGED".equals(action) || "android.intent.action.PACKAGE_REPLACED".equals(action)) {
            pkgName = getPkgNameFromIntent(intent);
            HwLog.i(TAG, "pkgName:" + pkgName);
            doAppChanged(pkgName);
        } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
            pkgName = getPkgNameFromIntent(intent);
            HwLog.i(TAG, "pkgName:" + pkgName);
            doAppInstalled(pkgName, replacing);
        } else if ("android.intent.action.PACKAGE_REMOVED".equals(action) && !replacing) {
            pkgName = getPkgNameFromIntent(intent);
            HwLog.i(TAG, "pkgName:" + pkgName);
            doAppRemoved(pkgName);
        }
    }

    private void doExternalAppChanged(Intent intent, boolean avail) {
        String[] pkgArray = intent.getStringArrayExtra(IntentCompat.EXTRA_CHANGED_PACKAGE_LIST);
        if (pkgArray == null) {
            HwLog.w(TAG, "null array, ignore.");
            return;
        }
        synchronized (this) {
            for (String pkg : pkgArray) {
                HsmPkgInfo info = this.mCreator.createByPkgName(this.mContext, pkg);
                if (avail) {
                    this.mUninstalledPackages.remove(pkg);
                    if (info != null) {
                        this.mPackages.put(pkg, info);
                    }
                    HwLog.i(TAG, "sdcard mounted, add " + pkg);
                } else {
                    this.mPackages.remove(pkg);
                    this.mUninstalledPackages.remove(pkg);
                    if (info != null) {
                        this.mUninstalledPackages.put(pkg, info);
                    }
                    HwLog.i(TAG, "sdcard unmounted, remove " + pkg);
                }
            }
        }
        synchronized (mListener) {
            List<IPackageChangeListener> cpList = new ArrayList();
            cpList.addAll(mListener);
            for (IPackageChangeListener listener : cpList) {
                listener.onExternalChanged(pkgArray, avail);
            }
        }
    }

    private void doAppChanged(String pkgName) {
        HsmPkgInfo info = this.mCreator.createByPkgName(this.mContext, pkgName);
        if (info != null) {
            synchronized (this) {
                if (this.mUninstalledPackages.containsKey(pkgName)) {
                    this.mUninstalledPackages.remove(pkgName);
                }
                HwLog.i(TAG, "doAppChanged, put " + pkgName + ", path:" + info.mPath);
                this.mPackages.put(pkgName, info);
            }
        }
        synchronized (mListener) {
            List<IPackageChangeListener> cpList = new ArrayList();
            cpList.addAll(mListener);
            for (IPackageChangeListener listener : cpList) {
                listener.onPackageChanged(pkgName);
            }
        }
    }

    private void doAppRemoved(String pkgName) {
        synchronized (this) {
            if (cantFindLocked(pkgName)) {
                HwLog.w(TAG, "doAppRemoved, can't find apk :" + pkgName + ", resan all apk.");
                scanApks();
            } else {
                HwLog.i(TAG, "doAppRemoved, remove:" + pkgName);
                this.mPackages.remove(pkgName);
                this.mUninstalledPackages.remove(pkgName);
            }
        }
        synchronized (mListener) {
            List<IPackageChangeListener> cpList = new ArrayList();
            cpList.addAll(mListener);
            for (IPackageChangeListener listener : cpList) {
                listener.onPackageRemoved(pkgName);
            }
        }
    }

    private boolean cantFindLocked(String pkgName) {
        return (this.mPackages.containsKey(pkgName) || this.mUninstalledPackages.containsKey(pkgName)) ? false : true;
    }

    private void doAppInstalled(String pkgName, boolean replacing) {
        HsmPkgInfo info = this.mCreator.createByPkgName(this.mContext, pkgName);
        if (info != null) {
            synchronized (this) {
                if (this.mUninstalledPackages.containsKey(pkgName)) {
                    this.mUninstalledPackages.remove(pkgName);
                }
                boolean prePkgExist = ((HsmPkgInfo) this.mPackages.put(pkgName, info)) != null;
                HwLog.i(TAG, "doAppInstalled, installed: " + pkgName + ", prePkgExist:" + prePkgExist + ", replacing:" + replacing);
                if (prePkgExist) {
                    return;
                }
            }
        }
        HwLog.w(TAG, "installed apk but can't get info." + pkgName);
        synchronized (mListener) {
            List<IPackageChangeListener> cpList = new ArrayList();
            cpList.addAll(mListener);
            for (IPackageChangeListener listener : cpList) {
                listener.onPackagedAdded(pkgName);
            }
        }
    }

    private String getPkgNameFromIntent(Intent intent) {
        return intent.getData().getSchemeSpecificPart();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        HwLog.i(TAG, "onConfigureChanged: " + newConfig.locale);
        String curLocale = this.mContext.getResources().getConfiguration().locale.toString();
        if (this.mSavedLocale == null) {
            HwLog.w(TAG, "locale is null, init failed?");
            return;
        }
        if (!this.mSavedLocale.equals(curLocale)) {
            HwLog.i(TAG, "locale changed, oldLocale: " + this.mSavedLocale + ", newLocale: " + curLocale);
            this.mSavedLocale = curLocale;
            onLocaleChange();
        } else if (isThemeChange(newConfig)) {
            HwLog.i(TAG, "onConfigurationChanged: Theme is changed");
            onThemeChange();
        }
    }

    private void initLocaleConfig() {
        Configuration config = this.mContext.getResources().getConfiguration();
        if (config == null) {
            HwLog.w(TAG, "can't get config.");
            return;
        }
        this.mSavedLocale = config.locale.toString();
        HwLog.i(TAG, "init locale:" + this.mSavedLocale);
    }

    private boolean isThemeChange(Configuration newConfig) {
        if ((32768 & ActivityInfoEx.activityInfoConfigToNative(this.mLastConfig.updateFrom(newConfig))) != 0) {
            return true;
        }
        return false;
    }

    private void onLocaleChange() {
        synchronized (this) {
            HwLog.i(TAG, "locale changed, clear all label.");
            clearLabelCache(this.mPackages);
            clearLabelCache(this.mUninstalledPackages);
        }
    }

    private void onThemeChange() {
        synchronized (this) {
            HwLog.i(TAG, "onThemeChange: Theme changed, clear all icons.");
            clearIconCache(this.mPackages);
            clearIconCache(this.mUninstalledPackages);
        }
    }

    private void clearLabelCache(Map<String, HsmPkgInfo> pkgs) {
        for (HsmPkgInfo info : pkgs.values()) {
            info.clearLabel();
        }
    }

    private void clearIconCache(Map<String, HsmPkgInfo> pkgs) {
        for (HsmPkgInfo info : pkgs.values()) {
            info.clearIcon();
        }
    }

    public synchronized Drawable getDefaultIcon() {
        if (this.mDefaultIcon == null) {
            this.mDefaultIcon = this.mContext.getResources().getDrawable(17301651);
        }
        return this.mDefaultIcon;
    }

    public boolean checkIsPackageLegacy(String pkg) {
        HsmPkgInfo pkgInfo = getPkgInfo(pkg);
        if (pkgInfo != null) {
            return pkgInfo.isLegacy();
        }
        return false;
    }
}
