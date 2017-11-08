package com.huawei.systemmanager.shortcut;

import android.content.Intent;

/* compiled from: ShortCutFragmentAdapter */
class ShortCutInfoItem {
    Intent mDestinationIntent;
    boolean mIsInLauncher;
    int mShortCutDeskIconResId;
    int mShortCutIconResId;
    int mShortCutNameResId;
    int mShortCutStatusDescriptionResId;

    public ShortCutInfoItem(int shortCutNameResId, int shortCutStatusDescriptionResId, boolean isInLauncher, int shortCutIconResId, int shotcutDeskIconResId, Intent destinationIntent) {
        this.mShortCutNameResId = shortCutNameResId;
        this.mShortCutStatusDescriptionResId = shortCutStatusDescriptionResId;
        this.mIsInLauncher = isInLauncher;
        this.mShortCutIconResId = shortCutIconResId;
        this.mShortCutDeskIconResId = shotcutDeskIconResId;
        this.mDestinationIntent = destinationIntent;
    }
}
