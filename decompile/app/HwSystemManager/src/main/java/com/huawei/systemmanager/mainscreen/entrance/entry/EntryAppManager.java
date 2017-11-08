package com.huawei.systemmanager.mainscreen.entrance.entry;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.module.IHsmModule;
import com.huawei.systemmanager.comm.module.ModuleMgr;
import com.huawei.systemmanager.mainscreen.entrance.entry.AbsEntrance.SimpleEntrace;

public class EntryAppManager extends SimpleEntrace {
    public static final String NAME = "EntryAppManager";

    protected int getIconResId() {
        return R.drawable.ic_app_mainpage;
    }

    protected int getTitleStringId() {
        return R.string.app_manager_title_str;
    }

    protected IHsmModule getModule() {
        return ModuleMgr.MODULE_APPMANAGER;
    }

    public String getEntryName() {
        return NAME;
    }
}
