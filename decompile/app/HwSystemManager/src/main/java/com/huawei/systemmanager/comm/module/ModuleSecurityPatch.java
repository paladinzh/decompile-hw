package com.huawei.systemmanager.comm.module;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.comm.module.IHsmModule.AbsHsmModule;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.secpatch.ui.SecurityPatchActivity;

public class ModuleSecurityPatch extends AbsHsmModule {
    public boolean entryEnabled(Context ctx) {
        return !AbroadUtils.isAbroad();
    }

    public Intent getMainEntry(Context ctx) {
        return new Intent(ctx, SecurityPatchActivity.class);
    }
}
