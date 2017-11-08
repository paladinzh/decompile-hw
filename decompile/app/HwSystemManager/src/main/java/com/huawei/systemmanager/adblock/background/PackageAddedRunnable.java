package com.huawei.systemmanager.adblock.background;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.text.TextUtils;
import com.huawei.systemmanager.adblock.comm.AdBlock;
import com.huawei.systemmanager.adblock.comm.AdDispatcher;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant.AdBlockColumns;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;

class PackageAddedRunnable implements Runnable {
    private static final String TAG = "AdBlock_PackageAddedRunnable";
    private final Context mContext;
    private final Handler mInnerHandler;
    private final String mPkg;

    PackageAddedRunnable(Context context, String pkg, Handler handler) {
        this.mContext = context;
        this.mPkg = pkg;
        this.mInnerHandler = handler;
    }

    public void run() {
        Context context = this.mContext;
        if (TextUtils.isEmpty(this.mPkg)) {
            HwLog.i(TAG, "pkg is empty");
            return;
        }
        try {
            PackageInfo info = PackageManagerWrapper.getPackageInfo(context.getPackageManager(), this.mPkg, 8192);
            if (info == null || AdUtils.isSystem(info)) {
                HwLog.i(TAG, "PackageInfo is null or system app");
                return;
            }
            AdBlock adBlock = AdBlock.restoreAdBlockWithPkg(context, this.mPkg);
            if (adBlock == null) {
                HwLog.i(TAG, "insert pkg " + this.mPkg);
                new AdBlock(this.mPkg, info.versionCode, info.versionName).save(context);
            } else if (adBlock.getVersionCode() == info.versionCode) {
                HwLog.i(TAG, "versionCode is not changed");
                AdDispatcher.setAdStrategy(context, adBlock);
                return;
            } else {
                HwLog.i(TAG, "update pkg to dirty: " + this.mPkg);
                ContentValues values = new ContentValues();
                values.put(AdBlockColumns.COLUMN_DIRTY, Integer.valueOf(1));
                values.put(AdBlockColumns.COLUMN_VERSION_CODE, Integer.valueOf(info.versionCode));
                values.put(AdBlockColumns.COLUMN_VERSION_NAME, info.versionName);
                adBlock.update(context, values);
            }
            this.mInnerHandler.removeMessages(3);
            this.mInnerHandler.sendEmptyMessageDelayed(3, 15000);
        } catch (Exception e) {
            HwLog.w(TAG, "Exception pkg=" + this.mPkg, e);
        }
    }
}
