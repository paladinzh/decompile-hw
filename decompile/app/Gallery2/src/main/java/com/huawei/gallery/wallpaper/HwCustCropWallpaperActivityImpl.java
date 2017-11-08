package com.huawei.gallery.wallpaper;

import android.content.Context;
import android.provider.Settings.System;

public class HwCustCropWallpaperActivityImpl extends HwCustCropWallpaperActivity {
    public boolean isShowCustWallpaperFirst(Context context) {
        return "true".equals(System.getString(context.getContentResolver(), "hw_custwallpaper_firstshown"));
    }
}
