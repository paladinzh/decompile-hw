package com.huawei.systemmanager.spacecleanner.engine.hwadapter;

import android.content.Context;
import android.content.pm.IPackageDataObserver.Stub;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppCacheTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class HwAppCacheTrash extends AppCacheTrash {
    private HsmPkgInfo mPkgInfo;

    private static class DeleteCacheFilesTask {
        private static final long DELETE_MAX_WAIT_TIME = 10000;
        private Object mLocker;
        private boolean mReust;

        private DeleteCacheFilesTask() {
            this.mReust = false;
            this.mLocker = new Object();
        }

        public boolean execute(PackageManager p, String pkgName) {
            boolean z;
            p.deleteApplicationCacheFiles(pkgName, new Stub() {
                public void onRemoveCompleted(String s, boolean b) throws RemoteException {
                    HwLog.d(Trash.TAG_CLEAN, "End  delete app cache trash, pkg:" + s + ",result:" + b);
                    synchronized (DeleteCacheFilesTask.this.mLocker) {
                        DeleteCacheFilesTask.this.mReust = true;
                        DeleteCacheFilesTask.this.mLocker.notifyAll();
                    }
                }
            });
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
                    }
                }
                z = this.mReust;
            }
            return z;
        }
    }

    private HwAppCacheTrash(HsmPkgInfo info, long cacheSize) {
        super(cacheSize);
        this.mPkgInfo = info;
    }

    public String getPackageName() {
        if (this.mPkgInfo == null) {
            return "";
        }
        return this.mPkgInfo.getPackageName();
    }

    public Drawable getAppIcon() {
        if (this.mPkgInfo == null) {
            return null;
        }
        return this.mPkgInfo.icon();
    }

    public String getAppLabel() {
        if (this.mPkgInfo == null) {
            return "";
        }
        return this.mPkgInfo.label();
    }

    public boolean clean(Context context) {
        String pkgName = getPackageName();
        HwLog.d(Trash.TAG_CLEAN, "begin to delete app cache trash, pkg:" + pkgName + ",type:" + getType());
        setCleaned();
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        return new DeleteCacheFilesTask().execute(context.getPackageManager(), pkgName);
    }

    public static final HwAppCacheTrash create(String pkgName, long cache) {
        HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkgName);
        if (info == null) {
            return null;
        }
        return new HwAppCacheTrash(info, cache);
    }
}
