package com.huawei.systemmanager.mainscreen.entrance.entry;

import android.view.View;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.module.IHsmModule;
import com.huawei.systemmanager.comm.module.ModuleMgr;
import com.huawei.systemmanager.mainscreen.entrance.entry.AbsEntrance.SimpleEntrace;

public class EntryStartupMgr extends SimpleEntrace {
    public static final String NAME = "EntryStartupMgr";

    protected int getIconResId() {
        return R.drawable.ic_app_launch_mainpage;
    }

    protected int getTitleStringId() {
        return R.string.systemmanager_module_title_autolaunch;
    }

    protected IHsmModule getModule() {
        return ModuleMgr.MODULE_BOOTUP;
    }

    public String getEntryName() {
        return NAME;
    }

    protected void onCreateView(View container) {
        Utility.setViewEnabled(container, Utility.isOwnerUser(false));
    }
}
