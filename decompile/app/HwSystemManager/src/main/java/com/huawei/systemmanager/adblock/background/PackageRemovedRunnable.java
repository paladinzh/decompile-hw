package com.huawei.systemmanager.adblock.background;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.systemmanager.adblock.comm.AdBlock;
import com.huawei.systemmanager.adblock.comm.AdDispatcher;
import com.huawei.systemmanager.util.HwLog;

class PackageRemovedRunnable implements Runnable {
    private static final String TAG = "AdBlock_PackageRemovedRunnable";
    private final Context mContext;
    private final String mPkg;

    PackageRemovedRunnable(Context context, String pkg) {
        this.mContext = context;
        this.mPkg = pkg;
    }

    public void run() {
        Context context = this.mContext;
        if (TextUtils.isEmpty(this.mPkg)) {
            HwLog.i(TAG, "pkg is empty");
            return;
        }
        try {
            AdBlock adBlock = AdBlock.restoreAdBlockWithPkg(context, this.mPkg);
            if (adBlock == null) {
                HwLog.i(TAG, "pkg is not blocked: " + this.mPkg);
            } else if (adBlock.isPackageInstalled(context)) {
                HwLog.i(TAG, "pkg is still installed: " + this.mPkg);
                AdDispatcher.setAdStrategy(context, adBlock);
            } else {
                HwLog.i(TAG, "pkg is " + this.mPkg);
                adBlock.delete(context);
                AdDispatcher.clearPackage(adBlock);
            }
        } catch (Exception e) {
            HwLog.w(TAG, "Exception pkg=" + this.mPkg, e);
        }
    }
}
