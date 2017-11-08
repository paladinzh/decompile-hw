package com.huawei.systemmanager.comm.module;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.comm.module.IHsmModule.AbsHsmModule;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity;

public class ModuleStartupMgr extends AbsHsmModule {
    public boolean entryEnabled(Context ctx) {
        return CustomizeWrapper.isBootstartupEnabled();
    }

    public Intent getMainEntry(Context ctx) {
        return new Intent(ctx, StartupNormalAppListActivity.class);
    }
}
