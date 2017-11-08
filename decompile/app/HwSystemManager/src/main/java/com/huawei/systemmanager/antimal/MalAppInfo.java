package com.huawei.systemmanager.antimal;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import com.huawei.systemmanager.util.HwLog;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class MalAppInfo {
    private static final String TAG = "MalAppInfo";
    public String mAppName;
    public int mAppStatus;
    public String mAppVersion;
    private Context mContext;
    public long mInstallBeginTime;
    public long mInstallEndTime;
    public int mInstaller;
    public String mPackageName;
    public String mSignHash;
    public long mSpaceTime;

    public MalAppInfo(Context context) {
        this.mContext = context;
    }

    public String toString() {
        return "MalAppInfo: mPackageName=" + this.mPackageName + "\nmInstaller=" + this.mInstaller + "\nmInstallBeginTime=" + this.mInstallBeginTime + "\nmInstallEndTime=" + this.mInstallEndTime + "\nmSpaceTime=" + this.mSpaceTime + "\nmAppVersion=" + this.mAppVersion + "\nmAppName=" + this.mAppName + "\nmAppStatus=" + this.mAppStatus + "\nmSignHash=" + this.mSignHash;
    }

    public JSONObject toJson(boolean sendToBD) {
        JSONObject appJson = new JSONObject();
        try {
            appJson.put("pkg", this.mPackageName);
            if (!sendToBD) {
                appJson.put("src", this.mInstaller);
                appJson.put(MalwareConst.INSTALL_BEGIN_TIME, this.mInstallBeginTime);
            }
            appJson.put(MalwareConst.INSTALL_END_TIME, this.mInstallEndTime);
            appJson.put(MalwareConst.INSTALL_SPACE_TIME, this.mSpaceTime);
            appJson.put("ver", this.mAppVersion);
            appJson.put("name", this.mAppName);
            appJson.put(MalwareConst.APP_STATUS, this.mAppStatus);
            appJson.put("hash", this.mSignHash);
        } catch (Exception e) {
            HwLog.e(TAG, "toJson Exception:" + e);
        }
        return appJson;
    }

    private boolean hasDevMgrPermission(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            HwLog.e(TAG, "hasDevMgrPermission pkgName is null.");
            return false;
        }
        List<ResolveInfo> enabledForProfile = this.mContext.getPackageManager().queryBroadcastReceivers(new Intent("android.app.action.DEVICE_ADMIN_ENABLED"), 32896);
        if (enabledForProfile == null) {
            return false;
        }
        int n = enabledForProfile.size();
        for (int j = 0; j < n; j++) {
            ResolveInfo resolveInfo = (ResolveInfo) enabledForProfile.get(j);
            if (resolveInfo != null && pkgName.equals(resolveInfo.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isActiveDevMgrApp(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            HwLog.e(TAG, "isActiveDevMgrApp pkgName is null.");
            return false;
        }
        List<ComponentName> compList = ((DevicePolicyManager) this.mContext.getSystemService("device_policy")).getActiveAdmins();
        if (!(compList == null || compList.size() == 0)) {
            for (int i = 0; i < compList.size(); i++) {
                if (pkgName.equals(((ComponentName) compList.get(i)).getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isLauncherApp(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            HwLog.e(TAG, "isLauncherApp input param is null.");
            return false;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        List<ResolveInfo> resList = this.mContext.getPackageManager().queryIntentActivities(intent, 0);
        if (resList != null) {
            for (ResolveInfo res : resList) {
                if (pkgName.equals(res.activityInfo.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getSignByPkgName(String pkgName) {
        String sign = "";
        try {
            return getSignSha256(this.mContext.getPackageManager().getPackageInfo(pkgName, 64), MessageDigest.getInstance("SHA-256"));
        } catch (NameNotFoundException nnfe) {
            HwLog.e(TAG, "NameNotFoundException:" + nnfe.getMessage());
            return "";
        } catch (Exception e) {
            HwLog.e(TAG, "getSignByPkgName Exception:" + e);
            return sign;
        }
    }

    private String getSignSha256(PackageInfo packageInfo, MessageDigest digest) {
        if (packageInfo == null || digest == null) {
            HwLog.e(TAG, "getSignSha256 input param is null");
            return "";
        }
        String apkSigSha = null;
        byte[] sigArray = null;
        try {
            digest.reset();
            if (packageInfo.signatures[0] != null) {
                digest.update(packageInfo.signatures[0].toByteArray());
                sigArray = digest.digest();
            }
        } catch (Exception e) {
            HwLog.e(TAG, "getSignSha256 Exception:" + e);
        }
        if (sigArray != null) {
            apkSigSha = convertHashToString(sigArray);
        }
        return apkSigSha;
    }

    private String convertHashToString(byte[] hashBytes) {
        StringBuffer strBuf = new StringBuffer();
        for (byte b : hashBytes) {
            strBuf.append(Integer.toString((b & 255) + 256, 16).substring(1));
        }
        return strBuf.toString().toLowerCase(Locale.getDefault());
    }

    public static List<MalAppInfo> getSortAppList(List<MalAppInfo> appList) {
        Collections.sort(appList, new Comparator<MalAppInfo>() {
            public int compare(MalAppInfo app1, MalAppInfo app2) {
                return (int) (app1.mInstallEndTime - app2.mInstallEndTime);
            }
        });
        return appList;
    }

    public static void writeMalAppInfo(XmlSerializer serializer, MalAppInfo appInfo) {
        try {
            serializer.startTag(null, "package");
            serializer.attribute(null, "pkg", appInfo.mPackageName);
            serializer.attribute(null, "name", appInfo.mAppName);
            serializer.attribute(null, "src", String.valueOf(appInfo.mInstaller));
            serializer.attribute(null, MalwareConst.INSTALL_BEGIN_TIME, String.valueOf(appInfo.mInstallBeginTime));
            serializer.attribute(null, MalwareConst.INSTALL_END_TIME, String.valueOf(appInfo.mInstallEndTime));
            serializer.attribute(null, MalwareConst.INSTALL_SPACE_TIME, String.valueOf(appInfo.mSpaceTime));
            serializer.attribute(null, "ver", appInfo.mAppVersion);
            serializer.attribute(null, MalwareConst.APP_STATUS, String.valueOf(appInfo.mAppStatus));
            serializer.attribute(null, "hash", appInfo.mSignHash);
            serializer.endTag(null, "package");
        } catch (IOException e) {
            HwLog.e(TAG, "writeMalAppInfo IOException:" + e);
        }
    }

    public static MalAppInfo readMalAppInfo(Context context, XmlPullParser xmlParser) {
        MalAppInfo appInfo = new MalAppInfo(context);
        if ("pkg".equals(xmlParser.getAttributeName(0))) {
            appInfo.mPackageName = xmlParser.getAttributeValue(null, "pkg");
        }
        if ("name".equals(xmlParser.getAttributeName(1))) {
            appInfo.mAppName = xmlParser.getAttributeValue(null, "name");
        }
        if ("src".equals(xmlParser.getAttributeName(2))) {
            appInfo.mInstaller = Integer.parseInt(xmlParser.getAttributeValue(null, "src"));
        }
        if (MalwareConst.INSTALL_BEGIN_TIME.equals(xmlParser.getAttributeName(3))) {
            appInfo.mInstallBeginTime = Long.parseLong(xmlParser.getAttributeValue(null, MalwareConst.INSTALL_BEGIN_TIME));
        }
        if (MalwareConst.INSTALL_END_TIME.equals(xmlParser.getAttributeName(4))) {
            appInfo.mInstallEndTime = Long.parseLong(xmlParser.getAttributeValue(null, MalwareConst.INSTALL_END_TIME));
        }
        if (MalwareConst.INSTALL_SPACE_TIME.equals(xmlParser.getAttributeName(5))) {
            appInfo.mSpaceTime = Long.parseLong(xmlParser.getAttributeValue(null, MalwareConst.INSTALL_SPACE_TIME));
        }
        if ("ver".equals(xmlParser.getAttributeName(6))) {
            appInfo.mAppVersion = xmlParser.getAttributeValue(null, "ver");
        }
        if (MalwareConst.APP_STATUS.equals(xmlParser.getAttributeName(7))) {
            appInfo.mAppStatus = Integer.parseInt(xmlParser.getAttributeValue(null, MalwareConst.APP_STATUS));
        }
        if ("hash".equals(xmlParser.getAttributeName(8))) {
            appInfo.mSignHash = xmlParser.getAttributeValue(null, "hash");
        }
        return appInfo;
    }

    public void analyze() {
        if (TextUtils.isEmpty(this.mPackageName)) {
            HwLog.e(TAG, "analyzeMalApp input param is null.");
            return;
        }
        if (hasDevMgrPermission(this.mPackageName)) {
            this.mAppStatus |= 2;
            if (isActiveDevMgrApp(this.mPackageName)) {
                this.mAppStatus |= 4;
            }
        }
        if (isLauncherApp(this.mPackageName)) {
            this.mAppStatus |= 8;
        }
        this.mSignHash = getSignByPkgName(this.mPackageName);
        PackageManager pkgManager = this.mContext.getPackageManager();
        try {
            PackageInfo pkgInfo = pkgManager.getPackageInfo(this.mPackageName, 0);
            this.mAppName = pkgInfo.applicationInfo.loadLabel(pkgManager).toString();
            this.mAppVersion = pkgInfo.versionName;
            this.mInstallEndTime = pkgInfo.lastUpdateTime;
        } catch (Exception e) {
            HwLog.e(TAG, "analyze Exception:" + e);
        }
        HwLog.i(TAG, "analyzeMalApp malApp:" + toString());
    }
}
