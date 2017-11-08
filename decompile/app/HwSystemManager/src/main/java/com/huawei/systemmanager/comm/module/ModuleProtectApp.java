package com.huawei.systemmanager.comm.module;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.comm.module.IHsmModule.AbsHsmModule;
import com.huawei.systemmanager.optimize.process.ProtectActivity;

public class ModuleProtectApp extends AbsHsmModule {
    public Intent getMainEntry(Context ctx) {
        return new Intent(ctx, ProtectActivity.class);
    }
}
