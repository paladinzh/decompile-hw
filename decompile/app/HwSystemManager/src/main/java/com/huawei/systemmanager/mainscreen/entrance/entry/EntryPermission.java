package com.huawei.systemmanager.mainscreen.entrance.entry;

import android.content.Context;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.module.IHsmModule;
import com.huawei.systemmanager.comm.module.ModuleMgr;
import com.huawei.systemmanager.mainscreen.entrance.entry.AbsEntrance.SimpleEntrace;

public class EntryPermission extends SimpleEntrace {
    public static final String NAME = "EntryPermission";

    protected int getIconResId() {
        return R.drawable.ic_permission_mainpage;
    }

    protected int getTitleStringId() {
        return R.string.systemmanager_module_title_permissions;
    }

    protected IHsmModule getModule() {
        return ModuleMgr.MODULE_PERMISSION;
    }

    public String getEntryName() {
        return NAME;
    }

    public boolean isEnable(Context ctx) {
        return true;
    }
}
