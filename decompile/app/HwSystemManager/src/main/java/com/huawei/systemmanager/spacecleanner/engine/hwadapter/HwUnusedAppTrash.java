package com.huawei.systemmanager.spacecleanner.engine.hwadapter;

import android.content.Context;
import android.content.pm.IPackageDeleteObserver.Stub;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.UnusedAppTrash;
import com.huawei.systemmanager.spacecleanner.utils.RarelyUsedAppBean;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class HwUnusedAppTrash extends UnusedAppTrash {
    private boolean isPreInstalled;
    private long mAppSize;
    private int mDayNotUsed;
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
                public void packageDeleted(boolean b) {
                }

                public void packageDeleted(String s, int i) throws RemoteException {
                    HwLog.d(Trash.TAG_CLEAN, "End  delete app cache trash, pkg:" + pkgName + ",result:" + i);
                    synchronized (UninstallPkgTask.this.mLocker) {
                        if (i == -2) {
                            UninstallPkgTask.this.mSuccess = false;
                        }
                        UninstallPkgTask.this.mReust = true;
                        UninstallPkgTask.this.mLocker.notifyAll();
                    }
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
                        this.mReust = true;
                        HwLog.e(Trash.TAG, "uninstall app time out! pkg:" + pkgName);
                    }
                }
                z = this.mSuccess;
            }
            return z;
        }
    }

    private HwUnusedAppTrash(HsmPkgInfo info) {
        this.mPkgInfo = info;
    }

    public String getPackageName() {
        return this.mPkgInfo.getPackageName();
    }

    public boolean isPreInstalled() {
        return this.isPreInstalled;
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

    public HsmPkgInfo getPkgInfo() {
        return this.mPkgInfo;
    }

    public int getPosition() {
        return 2;
    }

    public boolean clean(Context context) {
        String pkgName = getPackageName();
        HwLog.i(Trash.TAG_CLEAN, "begin to delete unused app trash, pkg:" + pkgName + ",type:" + getType());
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        boolean result = new UninstallPkgTask().execute(context.getPackageManager(), pkgName);
        if (result) {
            setCleaned();
        } else {
            HwLog.w(Trash.TAG, "HwUnusedAppTrash clean failed! pkg:" + pkgName);
        }
        return result;
    }

    public int getUnusedDay() {
        return this.mDayNotUsed;
    }

    public static HwUnusedAppTrash create(RarelyUsedAppBean bean) {
        if (bean == null) {
            return null;
        }
        String pkgName = bean.getPackgeName();
        HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkgName);
        if (info == null) {
            return null;
        }
        HwUnusedAppTrash trash = new HwUnusedAppTrash(info);
        trash.mAppSize = bean.getAppSize();
        trash.mDayNotUsed = bean.getDayNotUsed();
        trash.isPreInstalled = HsmPackageManager.getInstance().isPreInstalled(pkgName);
        return trash;
    }
}
