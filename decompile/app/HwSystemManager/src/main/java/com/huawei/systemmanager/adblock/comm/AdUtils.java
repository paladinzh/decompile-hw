package com.huawei.systemmanager.adblock.comm;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.text.TextUtils;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.adblock.ui.apkdlcheck.DlUrlCheckService;
import com.huawei.systemmanager.adblock.ui.model.AdIntentService;
import com.huawei.systemmanager.adblock.ui.view.AdBlockAppListActivity;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.rainbow.CloudClientOperation;
import com.huawei.systemmanager.service.MainService;
import com.huawei.systemmanager.useragreement.UserAgreementHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class AdUtils {
    private static final String APPMARKET_ACTION_APPDETAIL = "com.huawei.appmarket.appmarket.intent.action.AppDetail.withdetailId";
    private static final String APPMARKET_ACTION_SEARCH = "com.huawei.appmarket.appmarket.intent.action.SearchActivity";
    private static final String APPMARKET_BUNDLE_DETAIL_ID = "appDetailId";
    private static final String APPMARKET_BUNDLE_SEARCH_KEY = "keyWord";
    private static final String APPMARKET_CACHE_FILE = "appmarket.apk";
    public static final String APPMARKET_PKG_NAME = "com.huawei.appmarket";
    private static final String APPMARKET_SIGNATURE = "FFE391E0EA186D0734ED601E4E70E3224B7309D48E2075BAC46D8C667EAE7212";
    private static final String APPMARKET_THIRDID = "thirdId";
    private static final String APPMARKET_THIRDID_VALUE = "4026638";
    private static final String SHA_256 = "SHA-256";
    private static final String TAG = "AdBlock_AdUtils";

    public static void update(Context context, int updateType) {
        Intent intent = new Intent(context, AdIntentService.class);
        intent.setAction(AdConst.ACTION_AD_UPDATE);
        intent.putExtra(AdConst.BUNDLE_KEY_UPDATE_TYPE, updateType);
        context.startServiceAsUser(intent, UserHandle.OWNER);
    }

    public static void sendUpdateResult(Context context, int updateType, boolean result) {
        Intent intent = new Intent(context, MainService.class);
        intent.setAction(AdConst.ACTION_AD_UPDATE_RESULT);
        intent.putExtra(AdConst.BUNDLE_KEY_UPDATE_TYPE, updateType);
        intent.putExtra(AdConst.BUNDLE_KEY_UPDATE_RESULT, result);
        context.startServiceAsUser(intent, UserHandle.OWNER);
    }

    public static void dispatchAllAsync(final Context context) {
        HsmExecutor.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            public void run() {
                AdUtils.dispatchAll(context);
            }
        });
    }

    public static void dispatchAll(Context context) {
        List<AdBlock> adBlocks = AdBlock.getAllAdBlocks(context);
        HwLog.i(TAG, "dispatchAll adBlocks.size=" + adBlocks.size());
        AdDispatcher.setAdStrategy(context, adBlocks, true);
    }

    public static void dispatchPart(Context context, List<String> packages) {
        if (packages == null || packages.isEmpty()) {
            HwLog.i(TAG, "dispatchPart packages is empty");
            return;
        }
        StringBuilder selection = new StringBuilder();
        selection.append("pkg_name").append(" in ('").append((String) packages.get(0));
        for (int i = 1; i < packages.size(); i++) {
            selection.append("','").append((String) packages.get(i));
        }
        selection.append("')");
        List<AdBlock> adBlocks = AdBlock.getAdBlocks(context, selection.toString(), null, null);
        HwLog.i(TAG, "dispatchPart adBlocks.size=" + adBlocks.size());
        AdDispatcher.setAdStrategy(context, adBlocks, false);
    }

    public static void checkUrl(Context context, Bundle bundle) {
        Intent intent = new Intent(context, DlUrlCheckService.class);
        intent.setAction(AdConst.ACTION_AD_CHECK_APK_URL);
        intent.putExtras(bundle);
        context.startServiceAsUser(intent, UserHandle.CURRENT);
    }

    public static boolean checkAppmarketCacheFile(Context context) {
        boolean z = false;
        PackageInfo info = context.getPackageManager().getPackageArchiveInfo(getAppmarketCacheFile(context).getPath(), 64);
        if (info == null) {
            HwLog.w(TAG, "checkAppmarketCacheFile info is null");
            return false;
        }
        if (checkAppmarketPkgName(info)) {
            z = checkAppmarketSignature(info);
        }
        return z;
    }

    public static boolean checkAppmarket(Context context, String packageName) {
        if ("com.huawei.appmarket".equals(packageName)) {
            try {
                PackageInfo info = PackageManagerWrapper.getPackageInfo(context.getPackageManager(), packageName, 64);
                if (info == null) {
                    HwLog.w(TAG, "checkAppmarket info is null");
                    return false;
                }
                boolean signatureMatch = checkAppmarketSignature(info);
                HwLog.i(TAG, "checkAppmarket signatureMatch=" + signatureMatch);
                return signatureMatch;
            } catch (NameNotFoundException e) {
                HwLog.w(TAG, "checkAppmarket NameNotFoundException");
                return false;
            }
        }
        HwLog.w(TAG, "checkAppmarket packageName not match");
        return false;
    }

    private static boolean checkAppmarketPkgName(PackageInfo info) {
        return "com.huawei.appmarket".equals(info.packageName);
    }

    private static boolean checkAppmarketSignature(PackageInfo info) {
        return APPMARKET_SIGNATURE.equalsIgnoreCase(getFirstSignatureSha2Hex(info));
    }

    public static File getAppmarketCacheFile(Context context) {
        return new File(context.getExternalCacheDir(), APPMARKET_CACHE_FILE);
    }

    private static String getFirstSignatureSha2Hex(PackageInfo info) {
        return byte2hex(sha256(getFirstSignature(info)));
    }

    private static byte[] getFirstSignature(PackageInfo info) {
        if (!(info == null || info.signatures == null || info.signatures.length <= 0)) {
            Signature signature = info.signatures[0];
            if (signature != null) {
                return signature.toByteArray();
            }
        }
        return new byte[0];
    }

    private static byte[] sha256(byte[] data) {
        if (data == null) {
            return new byte[0];
        }
        try {
            return MessageDigest.getInstance(SHA_256).digest(data);
        } catch (NoSuchAlgorithmException e) {
            HwLog.w(TAG, "sha256 NoSuchAlgorithmException", e);
            return data;
        }
    }

    private static String byte2hex(byte[] data) {
        if (data == null) {
            return null;
        }
        StringBuilder hex = new StringBuilder();
        int length = data.length;
        for (int i = 0; i < length; i++) {
            hex.append(String.format("%02X", new Object[]{Byte.valueOf(data[i])}));
        }
        return hex.toString();
    }

    public static ApplicationInfo getAppmarket(Context context) {
        if (context == null) {
            return null;
        }
        try {
            return context.getPackageManager().getApplicationInfo("com.huawei.appmarket", 0);
        } catch (NameNotFoundException e) {
            HwLog.i(TAG, "appmarket is not installed in current user");
            return null;
        }
    }

    public static void enableAppmarket(Context context) {
        if (context != null) {
            try {
                context.getPackageManager().setApplicationEnabledSetting("com.huawei.appmarket", 1, 0);
            } catch (RuntimeException e) {
                HwLog.w(TAG, "enableAppmarket fail", e);
            }
        }
    }

    public static boolean shouldShowAppmarkedDialog(Context context) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo("com.huawei.appmarket", 0);
            if (info == null) {
                HwLog.i(TAG, "shouldShowAppmarkedDialog appmarket is not installed");
                return true;
            }
            boolean isSystem;
            if ((info.flags & 1) != 0) {
                isSystem = true;
            } else {
                isSystem = false;
            }
            if (isSystem || checkAppmarket(context, "com.huawei.appmarket")) {
                return true;
            }
            HwLog.i(TAG, "shouldShowAppmarkedDialog appmarket is not legal");
            return false;
        } catch (NameNotFoundException e) {
            HwLog.i(TAG, "appmarket is not installed in current user");
            return true;
        }
    }

    public static void downloadApkByAppmarket(Context context, String detailId, String appName) {
        HwLog.i(TAG, "downloadApkByAppmarket detailId=" + detailId + ",appName=" + appName);
        Intent intent = new Intent();
        if (TextUtils.isEmpty(detailId)) {
            intent.setAction(APPMARKET_ACTION_SEARCH);
            intent.putExtra(APPMARKET_BUNDLE_SEARCH_KEY, appName);
        } else {
            intent.setAction(APPMARKET_ACTION_APPDETAIL);
            intent.putExtra(APPMARKET_BUNDLE_DETAIL_ID, detailId);
        }
        intent.putExtra(APPMARKET_THIRDID, APPMARKET_THIRDID_VALUE);
        intent.setPackage("com.huawei.appmarket");
        intent.addFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            HwLog.w(TAG, "downloadApkByAppmarket Exception", e);
        }
    }

    public static boolean isPackageMatchUid(Context context, int uid, String pkgName) {
        String[] pkgNames = context.getPackageManager().getPackagesForUid(uid);
        if (!(pkgNames == null || pkgNames.length == 0)) {
            for (String pkg : pkgNames) {
                if (TextUtils.equals(pkg, pkgName)) {
                    HwLog.i(TAG, "package match uid pkgName=" + pkgName);
                    return true;
                }
            }
        }
        return false;
    }

    public static String getFistPackageLabel(Context context, int uid) {
        String pkgName = getFistPackageName(context, uid);
        if (TextUtils.isEmpty(pkgName)) {
            return "";
        }
        return getAppName(context, pkgName);
    }

    public static String getAppName(Context context, String packageName) {
        String appName = "";
        if (TextUtils.isEmpty(packageName)) {
            return appName;
        }
        HsmPkgInfo hsmPkgInfo = null;
        try {
            hsmPkgInfo = HsmPackageManager.getInstance().getPkgInfo(packageName, 8192);
        } catch (NameNotFoundException e) {
            try {
                PackageManager pm = context.getPackageManager();
                hsmPkgInfo = new HsmPkgInfo(PackageManagerWrapper.getPackageInfo(pm, packageName, 8192), pm, true);
            } catch (NameNotFoundException e1) {
                HwLog.w(TAG, "getFistPackageLabel NameNotFoundException", e1);
            }
        }
        if (hsmPkgInfo != null) {
            return hsmPkgInfo.label();
        }
        return packageName;
    }

    public static String getFistPackageName(Context context, int uid) {
        String[] pkgNames = context.getPackageManager().getPackagesForUid(uid);
        String name = "";
        if (pkgNames != null && pkgNames.length > 0) {
            name = pkgNames[0];
        }
        return name != null ? name : "";
    }

    public static boolean isSystem(PackageInfo info) {
        boolean z = false;
        if (info == null) {
            return false;
        }
        if ("com.android.providers.downloads".equals(info.packageName)) {
            HwLog.i(TAG, "isSystem set downloads false");
            return false;
        }
        ApplicationInfo applicationInfo = info.applicationInfo;
        if (applicationInfo == null) {
            return false;
        }
        if ((applicationInfo.flags & 1) != 0) {
            z = true;
        }
        return z;
    }

    public static boolean isDlCheckEnable(Context context) {
        if (Global.getInt(context.getContentResolver(), AdConst.DOWNLOAD_APPS_KEY, 0) == 0) {
            return true;
        }
        return false;
    }

    public static boolean isCloudEnable(Context context) {
        if (!UserAgreementHelper.getUserAgreementState(context)) {
            HwLog.i(TAG, "isCloudEnable User agreement is not agreed");
            return false;
        } else if (CloudClientOperation.getSystemManageCloudsStatus(context)) {
            return true;
        } else {
            HwLog.i(TAG, "isCloudEnable SYSTEM_MANAGER_CLOUD close");
            return false;
        }
    }

    public static void startAppListActivityForResult(Activity activity, int requestCode) {
        try {
            activity.startActivityForResult(new Intent(activity, AdBlockAppListActivity.class), requestCode);
        } catch (Exception e) {
            HwLog.e(TAG, "startAppListActivity", e);
        }
    }
}
