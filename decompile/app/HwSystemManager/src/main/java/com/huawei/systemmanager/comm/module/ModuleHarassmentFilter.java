package com.huawei.systemmanager.comm.module;

import android.content.Context;
import android.content.Intent;
import com.huawei.harassmentinterception.ui.InterceptionActivity;
import com.huawei.systemmanager.comm.module.IHsmModule.AbsHsmModule;

public class ModuleHarassmentFilter extends AbsHsmModule {
    public Intent getMainEntry(Context ctx) {
        return new Intent(ctx, InterceptionActivity.class);
    }
}
