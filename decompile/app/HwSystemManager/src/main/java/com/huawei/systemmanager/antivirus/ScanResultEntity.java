package com.huawei.systemmanager.antivirus;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.optimizer.utils.PackageUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tmsdk.common.module.qscanner.QScanAdPluginEntity;
import tmsdk.common.module.qscanner.QScanResultEntity;

public class ScanResultEntity implements Serializable {
    private static final String TAG = "ScanResultEntity";
    private static final long serialVersionUID = -1;
    public String apkFilePath;
    public String appName;
    public boolean isUninstalledApk;
    public List<String> mBanUrls;
    public String mDescribtion;
    public String mHash;
    public List<String> mPlugNames;
    public String mVersion;
    public String packageName;
    public int type;
    public String virusInfo;
    public String virusName;

    public ScanResultEntity() {
        this.mBanUrls = new ArrayList();
    }

    public ScanResultEntity(Context context, PackageInfo packageInfo) {
        this.mBanUrls = new ArrayList();
        this.mDescribtion = "";
        this.mPlugNames = new ArrayList();
        this.virusInfo = "";
        if (packageInfo != null) {
            this.appName = context != null ? context.getPackageManager().getApplicationLabel(packageInfo.applicationInfo).toString() : "";
            this.mVersion = packageInfo.versionName;
            this.packageName = packageInfo.packageName;
            return;
        }
        this.appName = "";
        this.mVersion = "";
        this.packageName = "";
    }

    public ScanResultEntity(QScanResultEntity result, boolean isUninstalledApkFile) {
        this.mBanUrls = new ArrayList();
        this.appName = result.softName;
        this.packageName = result.packageName;
        this.virusName = result.name;
        this.virusInfo = result.discription;
        this.isUninstalledApk = isUninstalledApkFile;
        this.mVersion = result.version;
        this.mHash = result.dexSha1;
        switch (result.type) {
            case 0:
                this.type = 302;
                break;
            case 1:
                this.type = 301;
                break;
            case 2:
                this.type = 303;
                break;
            case 3:
                this.type = AntiVirusTools.TYPE_VIRUS;
                break;
            case 7:
            case 13:
            case 14:
                this.type = AntiVirusTools.TYPE_AD_BLOCK;
                break;
            case 8:
                this.type = 304;
                break;
        }
        this.apkFilePath = result.path;
        initAdData(result);
    }

    protected void initAdData(QScanResultEntity result) {
        this.mDescribtion = result.discription;
        this.mPlugNames = new ArrayList();
        if (result.plugins.size() != 0) {
            if (!(3 == result.type || 2 == result.type)) {
                this.type = AntiVirusTools.TYPE_ADVERTISE;
            }
            for (QScanAdPluginEntity entity : result.plugins) {
                this.mBanUrls.addAll(entity.banUrls);
                this.mPlugNames.add(entity.name);
            }
        }
    }

    public Map<String, Object> getResultInfoMap(PackageManager pm, Context context, String defaultType) {
        Drawable icon;
        Map<String, Object> map = new HashMap();
        try {
            icon = pm.getApplicationIcon(this.packageName);
        } catch (NameNotFoundException e) {
            if (this.isUninstalledApk) {
                icon = HsmPackageManager.getInstance().getIcon(this.appName);
            } else {
                HwLog.d(TAG, "getResultInfoMap: This package is uninstalled , apkName = " + this.appName);
                return null;
            }
        }
        map.put(Const.APP_ICON, icon);
        map.put(Const.APP_NAME, this.appName);
        if (this.isUninstalledApk) {
            map.put("type", context.getResources().getString(R.string.app_is_file));
        } else {
            map.put("type", defaultType);
        }
        if (this.type == AntiVirusTools.TYPE_ADVERTISE || this.type == AntiVirusTools.TYPE_RISKPERM) {
            map.put("type", this.mVersion);
        }
        map.put(Const.APP_PAKAGE_NAME, this.packageName);
        map.put(AntiVirusTools.IS_IN_SDCARD_FILE, Boolean.valueOf(this.isUninstalledApk));
        return map;
    }

    public String getVirusName(Context context) {
        if (TextUtils.isEmpty(this.virusName)) {
            return context.getResources().getString(R.string.antivirus_none);
        }
        return this.virusName;
    }

    public String getAppTypeText(Context context, int resultType) {
        String appTypeText = "";
        Resources resource = context.getResources();
        switch (resultType) {
            case 303:
                appTypeText = resource.getString(R.string.app_contains_risk);
                break;
            case 304:
                appTypeText = resource.getString(R.string.app_contains_not_official);
                break;
            case AntiVirusTools.TYPE_VIRUS /*305*/:
                appTypeText = resource.getString(R.string.app_contains_virus);
                break;
        }
        if (this.isUninstalledApk) {
            return resource.getString(R.string.app_is_file);
        }
        return appTypeText;
    }

    public String getDangerLevelText(Context context, int resultType) {
        String dangerLeverText = "";
        Resources resource = context.getResources();
        switch (resultType) {
            case 303:
                return resource.getString(R.string.lever_middle);
            case 304:
                return resource.getString(R.string.lever_low);
            case AntiVirusTools.TYPE_VIRUS /*305*/:
                return resource.getString(R.string.lever_high);
            default:
                return dangerLeverText;
        }
    }

    public Drawable getAppIcon(Context context) {
        Drawable icon = null;
        try {
            return context.getPackageManager().getApplicationIcon(this.packageName);
        } catch (NameNotFoundException e) {
            if (this.isUninstalledApk) {
                return HsmPackageManager.getInstance().getIcon(this.packageName);
            }
            e.printStackTrace();
            return icon;
        }
    }

    public String getVirusDetail(Context context) {
        if (TextUtils.isEmpty(this.virusInfo)) {
            return context.getResources().getString(R.string.antivirus_none);
        }
        return this.virusInfo;
    }

    public int getOperationDescripId() {
        if (this.isUninstalledApk) {
            return R.string.clean;
        }
        return R.string.common_uninstall;
    }

    public boolean isDeleted(Context context) {
        try {
            context.getPackageManager().getApplicationIcon(this.packageName);
        } catch (NameNotFoundException e) {
            if (!this.isUninstalledApk) {
                HwLog.d(TAG, "isDeleted: Uninstalled, packageName = " + this.packageName);
                return true;
            } else if (new File(this.apkFilePath).exists()) {
                HwLog.d(TAG, "isDeleted: Uninstalled but not deleted, packageName = " + this.packageName);
            } else {
                HwLog.d(TAG, "isDeleted: Deleted, packageName = " + this.packageName);
                return true;
            }
        }
        return false;
    }

    public boolean delete(Context context) {
        return delete(context, false);
    }

    public boolean delete(Context context, boolean isDelDirectly) {
        if (this.packageName.equals("com.huawei.systemmanager")) {
            HwLog.w(TAG, "delete: Can not delete self");
            return false;
        } else if (this.isUninstalledApk) {
            HwLog.i(TAG, "delete: try to delete apk file, packageName = " + this.packageName);
            return AntiVirusTools.deleteApkFile(this.apkFilePath);
        } else if (isDelDirectly) {
            HwLog.i(TAG, "delete: try to delete directly, packageName = " + this.packageName);
            context.getPackageManager().deletePackage(this.packageName, null, 0);
            return true;
        } else {
            HwLog.i(TAG, "delete: try to uninstall, packageName = " + this.packageName);
            return PackageUtils.uninstallApp(context, this.packageName, true);
        }
    }

    public String getPackageName() {
        return this.packageName;
    }

    public static ScanResultEntity createRiskPermItem(HsmPkgInfo info) {
        ScanResultEntity entity = new ScanResultEntity();
        if (info == null) {
            return null;
        }
        entity.appName = info.label();
        entity.packageName = info.getPackageName();
        entity.virusName = "";
        entity.virusInfo = "";
        entity.isUninstalledApk = false;
        entity.mVersion = info.getVersionName();
        entity.type = AntiVirusTools.TYPE_RISKPERM;
        entity.apkFilePath = "";
        entity.mDescribtion = "";
        entity.mPlugNames = Lists.newArrayList();
        return entity;
    }

    public static List<String> convertToStringArray(List<ScanResultEntity> input) {
        if (HsmCollections.isEmpty(input)) {
            return Lists.newArrayList();
        }
        List<String> result = Lists.newArrayListWithCapacity(input.size());
        for (ScanResultEntity entity : input) {
            result.add(entity.getPackageName());
        }
        return result;
    }

    public static boolean isDangerType(ScanResultEntity entity) {
        if (entity.type == AntiVirusTools.TYPE_AD_BLOCK || entity.type == AntiVirusTools.TYPE_ADVERTISE || entity.type == 304 || entity.type == 303 || entity.type == AntiVirusTools.TYPE_RISKPERM || entity.type == AntiVirusTools.TYPE_VIRUS) {
            return true;
        }
        return false;
    }

    public static boolean isRiskORVirus(ScanResultEntity entity) {
        if (entity.type == 303 || entity.type == AntiVirusTools.TYPE_VIRUS) {
            return true;
        }
        return false;
    }
}
