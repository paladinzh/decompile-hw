package com.huawei.systemmanager.comm.module;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.comm.module.IHsmModule.AbsHsmModule;
import com.huawei.systemmanager.spacecleanner.SpaceCleanActivity;

public class ModuleStorageCleaner extends AbsHsmModule {
    public Intent getMainEntry(Context ctx) {
        return new Intent(ctx, SpaceCleanActivity.class);
    }
}
