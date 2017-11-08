package com.huawei.systemmanager.spacecleanner.engine.trash;

import android.content.Context;
import android.content.pm.IPackageDeleteObserver.Stub;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.FileUtil;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash.SimpleTrash;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.io.IOException;

public class PreInstalledAppTrash extends SimpleTrash implements IAppTrashInfo {
    private long mAppSize;
    private final HsmPkgInfo mPkgInfo;

    private static class UninstallPkgTask {
        private static final long DELETE_MAX_WAIT_TIME = 10000;
        private Object mLocker;
        private volatile boolean mReust;
        private volatile boolean mSuccess;

        private UninstallPkgTask() {
            this.mReust = false;
            this.mSuccess = true;
            this.mLocker = new Object();
        }

        public boolean execute(PackageManager pm, final String pkgName) {
            boolean z;
            pm.deletePackage(pkgName, new Stub() {
                public void packageDeleted(String s, int i) throws RemoteException {
                    HwLog.i(Trash.TAG_CLEAN, "End  delete PreInstalledAppTrash, pkg:" + pkgName + ",result:" + i);
                    synchronized (UninstallPkgTask.this.mLocker) {
                        if (i == -2) {
                            UninstallPkgTask.this.mSuccess = false;
                        }
                        UninstallPkgTask.this.mReust = true;
                        UninstallPkgTask.this.mLocker.notifyAll();
                    }
                }

                public void packageDeleted(boolean b) {
                }
            }, 0);
            synchronized (this.mLocker) {
                long startTime = SystemClock.elapsedRealtime();
                while (!this.mReust) {
                    try {
                        this.mLocker.wait(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (SystemClock.elapsedRealtime() - startTime >= 10000) {
                        HwLog.e(Trash.TAG, "uninstall app time out! pkg:" + pkgName);
                        this.mReust = true;
                    }
                }
                z = this.mSuccess;
            }
            return z;
        }
    }

    private PreInstalledAppTrash(HsmPkgInfo info) {
        this.mPkgInfo = info;
    }

    public String getName() {
        return getAppLabel();
    }

    public String getPackageName() {
        return this.mPkgInfo.getPackageName();
    }

    public String getAppLabel() {
        return this.mPkgInfo.label();
    }

    public Drawable getAppIcon() {
        return this.mPkgInfo.icon();
    }

    public long getTrashSize() {
        return this.mAppSize;
    }

    public int getPosition() {
        return 2;
    }

    public HsmPkgInfo getPkgInfo() {
        return this.mPkgInfo;
    }

    public String getVersionName() {
        if (this.mPkgInfo == null) {
            return "";
        }
        return this.mPkgInfo.getVersionName();
    }

    public int getType() {
        return 524288;
    }

    public boolean isSuggestClean() {
        return false;
    }

    public boolean clean(Context context) {
        String pkgName = getPackageName();
        HwLog.i(Trash.TAG_CLEAN, "begin to delete preinstall app, pkg:" + pkgName + ",type:" + getType());
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        boolean result = new UninstallPkgTask().execute(context.getPackageManager(), pkgName);
        if (result) {
            setCleaned();
        } else {
            HwLog.w(Trash.TAG, "PreInstalledAppTrash clean failed! pkg:" + pkgName);
        }
        return result;
    }

    public static PreInstalledAppTrash create(long size, HsmPkgInfo mPkgInfo) {
        if (mPkgInfo == null) {
            return null;
        }
        PreInstalledAppTrash trash = new PreInstalledAppTrash(mPkgInfo);
        trash.mAppSize = size;
        return trash;
    }

    public void printf(Appendable appendable) throws IOException {
        appendable.append("     ").append("apkName:").append(getAppLabel()).append(", pkgName:").append(getPackageName()).append(",size").append(FileUtil.getFileSize(getTrashSize()));
    }
}
