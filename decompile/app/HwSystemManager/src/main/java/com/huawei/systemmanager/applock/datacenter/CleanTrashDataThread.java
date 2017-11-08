package com.huawei.systemmanager.applock.datacenter;

import android.content.Context;
import com.huawei.systemmanager.applock.datacenter.AppLockProvider.AuthSuccessPackageAllProvider;
import com.huawei.systemmanager.comm.misc.ProviderUtils;

public class CleanTrashDataThread extends Thread {
    private Context mContext = null;

    public CleanTrashDataThread(Context context) {
        super("applock_CleanTrashDataThread");
        this.mContext = context;
    }

    public void run() {
        if (!MultiUserUtils.isInMultiUserMode()) {
            ProviderUtils.deleteAll(this.mContext, AuthSuccessPackageAllProvider.CONTENT_URI);
        }
    }
}
