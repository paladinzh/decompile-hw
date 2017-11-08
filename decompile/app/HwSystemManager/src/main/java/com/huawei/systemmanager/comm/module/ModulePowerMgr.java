package com.huawei.systemmanager.comm.module;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.comm.module.IHsmModule.AbsHsmModule;
import com.huawei.systemmanager.power.ui.HwPowerManagerActivity;

public class ModulePowerMgr extends AbsHsmModule {
    public Intent getMainEntry(Context ctx) {
        return new Intent(ctx, HwPowerManagerActivity.class);
    }
}
