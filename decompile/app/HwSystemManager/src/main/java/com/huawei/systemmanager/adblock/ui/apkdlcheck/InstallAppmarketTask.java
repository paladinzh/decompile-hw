package com.huawei.systemmanager.adblock.ui.apkdlcheck;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver.Stub;
import android.net.Uri;
import android.os.RemoteException;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.adblock.ui.connect.request.DlAppmarketRequest;
import com.huawei.systemmanager.comm.concurrent.HsmSingleExecutor;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class InstallAppmarketTask implements Runnable {
    private static final String TAG = "AdBlock_InstallAppmarketTask";
    private static HsmSingleExecutor sHsmSingleExecutor = new HsmSingleExecutor();
    private final Context mAppContext;
    private final Callback mCallback;
    private final CountDownLatch mLatch = new CountDownLatch(1);

    public interface Callback {
        void onInstallAppmarketFinish(boolean z);
    }

    class PackageInstallObserver extends Stub {
        PackageInstallObserver() {
        }

        public void packageInstalled(String s, int i) throws RemoteException {
            HwLog.i(InstallAppmarketTask.TAG, "packageInstalled s=" + s + ", i=" + i);
            InstallAppmarketTask.this.mLatch.countDown();
        }
    }

    public InstallAppmarketTask(Context context, Callback callback) {
        this.mAppContext = context.getApplicationContext();
        this.mCallback = callback;
    }

    public synchronized void execute() {
        sHsmSingleExecutor.execute(this);
    }

    public void run() {
        ApplicationInfo info = AdUtils.getAppmarket(this.mAppContext);
        if (info != null) {
            HwLog.i(TAG, "run appmarket is already installed, enable=" + info.enabled);
            if (!info.enabled) {
                AdUtils.enableAppmarket(this.mAppContext);
            }
            this.mCallback.onInstallAppmarketFinish(true);
            return;
        }
        DlAppmarketRequest request = new DlAppmarketRequest();
        request.processRequest(this.mAppContext);
        boolean dlSuccess = request.isDownloadSuccess();
        HwLog.i(TAG, "run download appmarket success=" + dlSuccess);
        if (dlSuccess && installAppmarket()) {
            try {
                HwLog.i(TAG, "apk cache scan task do task flag is: " + this.mLatch.await(60, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                HwLog.w(TAG, "run latch exception");
            }
            deleteCache();
            ApplicationInfo result = AdUtils.getAppmarket(this.mAppContext);
            this.mCallback.onInstallAppmarketFinish(result != null ? result.enabled : false);
            return;
        }
        deleteCache();
        this.mCallback.onInstallAppmarketFinish(false);
    }

    private boolean installAppmarket() {
        if (AdUtils.checkAppmarketCacheFile(this.mAppContext)) {
            HwLog.i(TAG, "installAppmarket");
            this.mAppContext.getPackageManager().installPackage(Uri.fromFile(AdUtils.getAppmarketCacheFile(this.mAppContext)), new PackageInstallObserver(), 0, "com.huawei.appmarket");
            return true;
        }
        HwLog.w(TAG, "installAppmarket signature not match");
        return false;
    }

    private void deleteCache() {
        File file = AdUtils.getAppmarketCacheFile(this.mAppContext);
        if (file.exists()) {
            HwLog.i(TAG, "delete cache file result=" + file.delete());
        }
    }
}
