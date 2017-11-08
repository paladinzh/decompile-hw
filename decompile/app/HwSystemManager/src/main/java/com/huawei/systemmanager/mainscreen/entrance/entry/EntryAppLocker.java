package com.huawei.systemmanager.mainscreen.entrance.entry;

import android.view.View;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.module.IHsmModule;
import com.huawei.systemmanager.comm.module.ModuleMgr;
import com.huawei.systemmanager.mainscreen.entrance.entry.AbsEntrance.SimpleEntrace;

public class EntryAppLocker extends SimpleEntrace {
    public static final String NAME = "EntryAppLocker";

    protected int getTitleStringId() {
        return R.string.main_screen_entry_title_applock;
    }

    protected int getIconResId() {
        return R.drawable.ic_privace_mainpage;
    }

    protected IHsmModule getModule() {
        return ModuleMgr.MODULE_APPLOCK;
    }

    public String getEntryName() {
        return NAME;
    }

    protected void onCreateView(View container) {
        Utility.setViewEnabled(container, Utility.isOwnerUser(false));
    }
}
