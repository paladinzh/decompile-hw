package com.huawei.gallery.app;

import android.content.Intent;
import com.huawei.android.quickaction.ActionIcon;

public class AlbumContact {
    private ActionIcon mIcon;
    private Intent mIntent;
    private final int mResId;

    public AlbumContact(int resId, Intent intent, ActionIcon icon) {
        this.mResId = resId;
        this.mIntent = intent;
        this.mIcon = icon;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public int getResId() {
        return this.mResId;
    }
}
