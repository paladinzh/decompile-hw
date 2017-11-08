package com.huawei.systemmanager.adblock.background;

import android.content.Context;
import com.huawei.systemmanager.adblock.comm.AdBlock;
import com.huawei.systemmanager.adblock.comm.AdDispatcher;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.util.HwLog;

class DlAppChangedRunnable implements Runnable {
    private static final String TAG = "AdBlock_DlAppChangedRunnable";
    private final Context mContext;

    DlAppChangedRunnable(Context context) {
        this.mContext = context;
    }

    public void run() {
        boolean isDlCheckEnable = AdUtils.isDlCheckEnable(this.mContext);
        HwLog.i(TAG, "isDlCheckEnable=" + isDlCheckEnable);
        if (isDlCheckEnable) {
            AdDispatcher.setApkDownloadBlackList(this.mContext, AdBlock.getAdBlocks(this.mContext, "dl_check=1", null, null), true);
            return;
        }
        AdDispatcher.clearApkDownloadBlackList();
    }
}
