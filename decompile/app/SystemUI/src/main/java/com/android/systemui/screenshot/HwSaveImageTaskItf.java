package com.android.systemui.screenshot;

import android.net.Uri;

interface HwSaveImageTaskItf {
    void onButtonClicked(int i);

    void onFileSaved(Uri uri);

    void onScreenshotAnimationEnd();

    void onScrollButtonClicked();
}
