package com.huawei.systemmanager.spacecleanner.engine.hwadapter;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.IPackageDataObserver.Stub;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppDataTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class HwAppDataTrash extends AppDataTrash {
    private HsmPkgInfo mPkgInfo;

    private static class DeleteAppDataTask {
        private static final long DELETE_MAX_WAIT_TIME = 10000;
        private Object mLocker;
        private boolean mReust;

        private DeleteAppDataTask() {
            this.mReust = false;
            this.mLocker = new Object();
        }

        public boolean execute(ActivityManager am, String pkgName) {
            boolean z;
            am.clearApplicationUserData(pkgName, new Stub() {
                public void onRemoveCompleted(String s, boolean b) throws RemoteException {
                    HwLog.i(Trash.TAG_CLEAN, "End  delete app data trash, pkg:" + s + ",result:" + b);
                    synchronized (DeleteAppDataTask.this.mLocker) {
                        DeleteAppDataTask.this.mReust = true;
                        DeleteAppDataTask.this.mLocker.notifyAll();
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

    private HwAppDataTrash(HsmPkgInfo info, long dataSize) {
        super(dataSize);
        this.mPkgInfo = info;
    }

    public HsmPkgInfo getPkgInfo() {
        return this.mPkgInfo;
    }

    public int getType() {
        return 262144;
    }

    public String getPackageName() {
        if (this.mPkgInfo == null) {
            return "";
        }
        return this.mPkgInfo.getPackageName();
    }

    public String getAppLabel() {
        if (this.mPkgInfo == null) {
            return "";
        }
        return this.mPkgInfo.label();
    }

    public Drawable getAppIcon() {
        if (this.mPkgInfo == null) {
            return null;
        }
        return this.mPkgInfo.icon();
    }

    public boolean clean(Context context) {
        String pkgName = getPackageName();
        HwLog.i(Trash.TAG_CLEAN, "begin to delete app data trash, pkg:" + pkgName + ",type:" + getType());
        setCleaned();
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        return new DeleteAppDataTask().execute((ActivityManager) context.getSystemService("activity"), pkgName);
    }

    public static final HwAppDataTrash create(String pkgName, long data) {
        HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkgName);
        if (info == null) {
            return null;
        }
        return new HwAppDataTrash(info, data);
    }
}
