package com.huawei.systemmanager.mainscreen.entrance.entry;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.module.IHsmModule;
import com.huawei.systemmanager.comm.module.ModuleMgr;
import com.huawei.systemmanager.mainscreen.entrance.entry.AbsEntrance.SimpleEntrace;

public class EntryNotification extends SimpleEntrace {
    public static final String NAME = "EntryNotification";

    protected int getIconResId() {
        return R.drawable.ic_notification_mainpage;
    }

    protected int getTitleStringId() {
        return R.string.systemmanager_module_title_notifications;
    }

    protected IHsmModule getModule() {
        return ModuleMgr.MODULE_NOTIFICATION;
    }

    public String getEntryName() {
        return NAME;
    }
}
