package com.huawei.systemmanager.comm.module;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.antivirus.ui.AntiVirusActivity;
import com.huawei.systemmanager.comm.module.IHsmModule.AbsHsmModule;

public class ModuleVirusScanner extends AbsHsmModule {
    private static final String TAG = "ModuleVirusScanner";

    public boolean entryEnabled(Context ctx) {
        return true;
    }

    public Intent getMainEntry(Context ctx) {
        return new Intent(ctx, AntiVirusActivity.class);
    }
}
