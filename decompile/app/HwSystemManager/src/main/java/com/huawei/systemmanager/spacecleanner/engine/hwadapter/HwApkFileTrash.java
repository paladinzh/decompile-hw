package com.huawei.systemmanager.spacecleanner.engine.hwadapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.trash.ApkFileTrash;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class HwApkFileTrash extends ApkFileTrash {
    private static final String TAG = "HwApkFileTrashI";
    private Context mContext;
    private HsmPkgInfo mExsistPkg;
    private Drawable mIcon;
    private String mLabel;
    private PackageInfo mResovledPkg;

    private HwApkFileTrash(Context ctx, String file, PathEntry pathEntry) {
        super(file, pathEntry);
        this.mContext = ctx;
    }

    public String getPackageName() {
        if (this.mResovledPkg == null) {
            return "";
        }
        return this.mResovledPkg.packageName;
    }

    public String getAppLabel() {
        return this.mLabel;
    }

    public Drawable getAppIcon() {
        if (this.mIcon != null) {
            return this.mIcon;
        }
        if (this.mExsistPkg == null) {
            this.mIcon = getUninstalledApkIcon(this.mPath);
        } else {
            this.mIcon = this.mExsistPkg.icon();
        }
        return this.mIcon;
    }

    private Drawable getUninstalledApkIcon(String apkPath) {
        PackageManager pm = this.mContext.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, 1);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                HwLog.e(TAG, "OutOfMemoryError : " + e.getMessage());
            }
        }
        return null;
    }

    public boolean isNormal() {
        return true;
    }

    public void setRepeat(boolean repeat) {
        this.mRepeat = repeat;
    }

    public int getVersionCode() {
        if (this.mResovledPkg == null) {
            return Integer.MIN_VALUE;
        }
        return this.mResovledPkg.versionCode;
    }

    public int getInstalledVersionCode() {
        if (this.mExsistPkg == null) {
            return Integer.MIN_VALUE;
        }
        return this.mExsistPkg.getVersionCode();
    }

    public String getVersionName() {
        if (this.mResovledPkg == null) {
            return "";
        }
        return this.mResovledPkg.versionName;
    }

    public boolean isBroken() {
        return this.mResovledPkg == null;
    }

    public boolean isInstalled() {
        if (this.mExsistPkg == null) {
            return false;
        }
        int versionCode = getVersionCode();
        if (versionCode != Integer.MIN_VALUE && versionCode == this.mExsistPkg.getVersionCode()) {
            return true;
        }
        return false;
    }

    private void resolveLabel() {
        Exception e;
        Throwable th;
        if (this.mResovledPkg != null) {
            AssetManager assetManager = null;
            try {
                AssetManager mgr = new AssetManager();
                try {
                    mgr.addAssetPath(getPath());
                    Resources res = this.mContext.getResources();
                    Resources resolveApkRes = new Resources(mgr, res.getDisplayMetrics(), res.getConfiguration());
                    int labelRes = this.mResovledPkg.applicationInfo.labelRes;
                    if (labelRes == 0) {
                        if (mgr != null) {
                            try {
                                mgr.close();
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        }
                        return;
                    }
                    this.mLabel = resolveApkRes.getText(labelRes, "").toString();
                    if (mgr != null) {
                        try {
                            mgr.close();
                        } catch (Exception e22) {
                            e22.printStackTrace();
                        }
                    }
                    assetManager = mgr;
                } catch (Exception e3) {
                    e22 = e3;
                    assetManager = mgr;
                    try {
                        HwLog.e(TAG, "resolveLabel failed! pkg:" + this.mResovledPkg.packageName);
                        e22.printStackTrace();
                        if (assetManager != null) {
                            try {
                                assetManager.close();
                            } catch (Exception e222) {
                                e222.printStackTrace();
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (assetManager != null) {
                            try {
                                assetManager.close();
                            } catch (Exception e2222) {
                                e2222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    assetManager = mgr;
                    if (assetManager != null) {
                        assetManager.close();
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e2222 = e4;
                HwLog.e(TAG, "resolveLabel failed! pkg:" + this.mResovledPkg.packageName);
                e2222.printStackTrace();
                if (assetManager != null) {
                    assetManager.close();
                }
            }
        }
    }

    public static HwApkFileTrash createApkFileTrash(Context ctx, String path, PathEntry pathEntry) {
        String filePath = path;
        PackageInfo resovledPkgInfo = null;
        if (FileUtil.isZipFile(path)) {
            try {
                resovledPkgInfo = ctx.getPackageManager().getPackageArchiveInfo(path, 0);
            } catch (Exception e) {
                HwLog.e(TAG, "resovleApk file failed!");
                e.printStackTrace();
            }
        }
        HsmPkgInfo pkgInfo = null;
        if (resovledPkgInfo != null) {
            pkgInfo = HsmPackageManager.getInstance().getPkgInfo(resovledPkgInfo.packageName);
        }
        HwApkFileTrash trash = new HwApkFileTrash(ctx, path, pathEntry);
        trash.mExsistPkg = pkgInfo;
        trash.mResovledPkg = resovledPkgInfo;
        trash.resolveLabel();
        return trash;
    }
}
