package com.huawei.systemmanager.comm.module;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.applock.utils.ActivityIntentUtils;
import com.huawei.systemmanager.comm.module.IHsmModule.AbsHsmModule;

public class ModuleAppLockMgr extends AbsHsmModule {
    public Intent getMainEntry(Context ctx) {
        return ActivityIntentUtils.getStartAppLockMainIntent(ctx);
    }
}
