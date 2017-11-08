package com.huawei.systemmanager.mainscreen.entrance.entry;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.module.IHsmModule;
import com.huawei.systemmanager.comm.module.ModuleMgr;
import com.huawei.systemmanager.mainscreen.entrance.entry.AbsEntrance.SimpleEntrace;

public class EntryAddView extends SimpleEntrace {
    public static final String NAME = "AddViewEntry";

    protected int getIconResId() {
        return R.drawable.ic_window_mainpage;
    }

    protected int getTitleStringId() {
        return R.string.main_screen_entry_addview_activity_title;
    }

    protected IHsmModule getModule() {
        return ModuleMgr.MODULE_ADDVIEW;
    }

    public String getEntryName() {
        return NAME;
    }
}
