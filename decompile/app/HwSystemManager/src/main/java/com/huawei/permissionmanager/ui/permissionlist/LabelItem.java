package com.huawei.permissionmanager.ui.permissionlist;

import android.content.Context;
import com.huawei.systemmanager.comm.component.ListItem;

public class LabelItem implements ListItem {
    private final int mLabelResId;

    public LabelItem(int resId) {
        this.mLabelResId = resId;
    }

    public String getTitle(Context ctx) {
        return ctx.getString(this.mLabelResId);
    }
}
