package com.huawei.systemmanager.spacecleanner.ui.commonitem;

import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;

public class CommonEnterDeepItem {
    public static final int TYPE_RESTORE = 2;
    public static final int TYPE_SAVE_MORE = 1;
    public static final int TYPE_WECHAT = 3;
    protected String mDescription;
    protected Drawable mIcon;
    OnClickListener mItemClicker;
    protected String mName;
    protected int mType;

    public CommonEnterDeepItem(Drawable icon, String name, String des, OnClickListener listerner, int type) {
        this.mIcon = icon;
        this.mName = name;
        this.mDescription = des;
        this.mItemClicker = listerner;
        this.mType = type;
    }

    public Drawable getItemIcon() {
        return this.mIcon;
    }

    public String getName() {
        return this.mName;
    }

    public int getType() {
        return this.mType;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public OnClickListener getClickListener() {
        return this.mItemClicker;
    }
}
