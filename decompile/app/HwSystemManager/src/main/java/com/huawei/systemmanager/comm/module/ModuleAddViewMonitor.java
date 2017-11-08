package com.huawei.systemmanager.comm.module;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity;
import com.huawei.systemmanager.comm.module.IHsmModule.AbsHsmModule;

public class ModuleAddViewMonitor extends AbsHsmModule {
    public Intent getMainEntry(Context ctx) {
        return new Intent(ctx, AddViewMonitorActivity.class);
    }
}
