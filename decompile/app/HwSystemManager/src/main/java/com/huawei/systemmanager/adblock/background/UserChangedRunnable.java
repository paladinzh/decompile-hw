package com.huawei.systemmanager.adblock.background;

import android.content.Context;
import com.huawei.systemmanager.adblock.comm.AdBlock;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

class UserChangedRunnable implements Runnable {
    private static final String TAG = "AdBlock_UserChangedRunnable";
    private final Context mContext;
    private final boolean mRemoved;

    UserChangedRunnable(Context context, boolean removed) {
        this.mContext = context;
        this.mRemoved = removed;
    }

    public void run() {
        if (this.mRemoved) {
            deleteUninstalled();
        }
        AdUtils.dispatchAll(this.mContext);
    }

    private void deleteUninstalled() {
        List<AdBlock> adBlocks = AdBlock.getAllAdBlocks(this.mContext);
        List<String> deleteList = new ArrayList();
        for (AdBlock adBlock : adBlocks) {
            if (!adBlock.isPackageInstalled(this.mContext)) {
                deleteList.add(adBlock.getPkgName());
            }
        }
        if (!deleteList.isEmpty()) {
            try {
                AdBlock.deleteByPackages(this.mContext, deleteList);
            } catch (RuntimeException e) {
                HwLog.w(TAG, "deleteUninstalled RuntimeException", e);
            }
        }
    }
}
