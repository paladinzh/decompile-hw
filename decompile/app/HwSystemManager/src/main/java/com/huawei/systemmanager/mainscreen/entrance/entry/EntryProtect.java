package com.huawei.systemmanager.mainscreen.entrance.entry;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.mainscreen.entrance.entry.AbsEntrance.SimpleEntrace;
import com.huawei.systemmanager.optimize.process.ProtectActivity;

public class EntryProtect extends SimpleEntrace {
    public static final String NAME = "EntryProtect";

    public boolean isEnable(Context ctx) {
        return true;
    }

    public String getEntryName() {
        return NAME;
    }

    public Intent getEntryIntent(Context ctx) {
        return new Intent(ctx, ProtectActivity.class);
    }

    protected int getTitleStringId() {
        return R.string.systemmanager_module_title_lockscreencleanup;
    }

    protected int getIconResId() {
        return R.drawable.ic_unlock_clean_mainpage;
    }
}
