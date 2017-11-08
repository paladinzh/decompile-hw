package com.huawei.systemmanager.util.app;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import com.huawei.systemmanager.util.HwLog;

public class HsmPkgInfo {
    private static final String TAG = null;
    private static int sRemoveableFlag;
    private static int sUpdateRemovableFlag;
    public final String mFileName;
    public final int mFlag;
    private Drawable mIcon;
    private String mLabel;
    public final String mPath;
    public final String mPkgName;
    private final PackageManager mPm;
    public final int[] mSignCodes;
    private final int mTargetSdkVersion;
    public final int mUid;
    private final int mVersionCode;
    private final String mVersionName;

    static {
        sRemoveableFlag = 0;
        sUpdateRemovableFlag = 0;
        sRemoveableFlag = getStaticIntFiled("com.huawei.android.content.pm.PackageParserEx", "PARSE_IS_REMOVABLE_PREINSTALLED_APK");
        sUpdateRemovableFlag = getStaticIntFiled("com.huawei.android.content.pm.PackageParserEx", "FLAG_UPDATED_REMOVEABLE_APP");
    }

    public HsmPkgInfo(PackageInfo pi, PackageManager pm, boolean loadLabel) {
        this.mPm = pm;
        this.mPkgName = pi.packageName;
        this.mPath = getFilePathFromDir(pi.applicationInfo.sourceDir);
        this.mFileName = getFileNameFromDir(pi.applicationInfo.sourceDir);
        this.mFlag = pi.applicationInfo.flags;
        this.mSignCodes = getSignaturesCode(pi);
        this.mUid = pi.applicationInfo.uid;
        this.mVersionCode = pi.versionCode;
        this.mVersionName = pi.versionName;
        this.mTargetSdkVersion = pi.applicationInfo.targetSdkVersion;
        if (loadLabel) {
            this.mLabel = getLableFromPm();
        }
    }

    public HsmPkgInfo(PackageInfo pi, PackageManager pm) {
        this.mPm = pm;
        this.mPkgName = pi.packageName;
        this.mPath = getFilePathFromDir(pi.applicationInfo.sourceDir);
        this.mFileName = getFileNameFromDir(pi.applicationInfo.sourceDir);
        this.mFlag = pi.applicationInfo.flags;
        this.mSignCodes = getSignaturesCode(pi);
        this.mUid = pi.applicationInfo.uid;
        this.mVersionCode = pi.versionCode;
        this.mVersionName = pi.versionName;
        this.mTargetSdkVersion = pi.applicationInfo.targetSdkVersion;
    }

    public HsmPkgInfo(HsmPkgInfo pkgInfo) {
        this.mPm = pkgInfo.mPm;
        this.mPkgName = pkgInfo.mPkgName;
        this.mPath = pkgInfo.mPath;
        this.mFileName = pkgInfo.mFileName;
        this.mFlag = pkgInfo.mFlag;
        this.mSignCodes = pkgInfo.mSignCodes;
        this.mUid = pkgInfo.mUid;
        this.mVersionCode = pkgInfo.mVersionCode;
        this.mVersionName = pkgInfo.mVersionName;
        this.mTargetSdkVersion = pkgInfo.mTargetSdkVersion;
        this.mLabel = pkgInfo.mLabel;
    }

    public String label() {
        if (this.mLabel == null) {
            this.mLabel = getLableFromPm();
        }
        return this.mLabel;
    }

    public void clearLabel() {
        this.mLabel = null;
    }

    public Drawable icon() {
        if (this.mIcon == null) {
            this.mIcon = getIconFromPm();
        }
        return this.mIcon;
    }

    public void clearIcon() {
        this.mIcon = null;
    }

    private Drawable getIconFromPm() {
        try {
            Drawable icon = this.mPm.getApplicationInfo(this.mPkgName, 8192).loadIcon(this.mPm);
            int userId = UserHandle.getUserId(this.mUid);
            if (userId != UserHandle.myUserId()) {
                icon = this.mPm.getUserBadgedIcon(icon, new UserHandle(userId));
            }
            return icon;
        } catch (NameNotFoundException e) {
            HwLog.w(TAG, "can't get application info:" + this.mPkgName);
            return HsmPackageManager.getInstance().getDefaultIcon();
        }
    }

    private String getLableFromPm() {
        if (this.mPm == null || this.mPkgName == null) {
            HwLog.e(TAG, "getLableFromPm,but pm or pkgname is null.");
            return "";
        }
        try {
            return this.mPm.getApplicationInfo(this.mPkgName, 8192).loadLabel(this.mPm).toString().trim();
        } catch (NameNotFoundException e) {
            HwLog.w(TAG, "can't get application info:" + this.mPkgName);
            return this.mPkgName;
        }
    }

    public static String getFilePathFromDir(String appSourceDir) {
        if (appSourceDir == null) {
            HwLog.w(TAG, "source dir is null?");
            return "";
        }
        int idx = appSourceDir.lastIndexOf(47);
        if (-1 != idx) {
            return appSourceDir.substring(0, idx);
        }
        HwLog.w(TAG, "no '/' in source dir?");
        return "";
    }

    public static String getFileNameFromDir(String appSourceDir) {
        if (appSourceDir != null) {
            return appSourceDir.substring(appSourceDir.lastIndexOf(47) + 1);
        }
        HwLog.w(TAG, "source dir is null?");
        return "";
    }

    public static int[] getSignaturesCode(PackageInfo packageInfo) {
        Signature[] signatures = packageInfo.signatures;
        if (signatures == null) {
            HwLog.w("getSignaturesCode", "package " + packageInfo.packageName + " has no signatures.");
            return new int[0];
        }
        int len = signatures.length;
        int[] signatureArray = new int[len];
        for (int i = 0; i < len; i++) {
            signatureArray[i] = signatures[i].hashCode();
        }
        return signatureArray;
    }

    public boolean isPersistent() {
        return (this.mFlag & 8) != 0;
    }

    public int getUid() {
        return this.mUid;
    }

    public String getPackageName() {
        return this.mPkgName;
    }

    public String getPath() {
        return this.mPath;
    }

    public boolean isSystem() {
        return (this.mFlag & 1) != 0;
    }

    public boolean isInstalled() {
        return (this.mFlag & 8388608) != 0;
    }

    public boolean isRemoveAblePreInstall() {
        try {
            int hwFlags = ((Integer) Class.forName("android.content.pm.ApplicationInfo").getField("hwFlags").get(this.mPm.getApplicationInfo(this.mPkgName, 8192))).intValue();
            if ((sRemoveableFlag & hwFlags) == 0 && (sUpdateRemovableFlag & hwFlags) == 0) {
                return false;
            }
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
            return false;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return false;
        } catch (NameNotFoundException e4) {
            e4.printStackTrace();
            return false;
        } catch (Exception e5) {
            e5.printStackTrace();
            return false;
        }
    }

    public void setLabel(String label) {
        this.mLabel = label;
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
    }

    private static int getStaticIntFiled(String clazzName, String fieldName) {
        try {
            return Class.forName(clazzName).getField(fieldName).getInt(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
            return 0;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return 0;
        } catch (Exception e4) {
            e4.printStackTrace();
            return 0;
        }
    }

    public boolean isRemovable() {
        if (isRemoveAblePreInstall() || !isSystem()) {
            return true;
        }
        return false;
    }

    public final int getVersionCode() {
        return this.mVersionCode;
    }

    public final String getVersionName() {
        return this.mVersionName;
    }

    public final int getTargetSdkVersion() {
        return this.mTargetSdkVersion;
    }

    public boolean isLegacy() {
        return this.mTargetSdkVersion <= 22;
    }
}
