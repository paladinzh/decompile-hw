package com.android.systemui.screenshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

/* compiled from: GlobalScreenshot */
class SaveImageInBackgroundData {
    Context context;
    int errorMsgResId;
    Runnable finisher;
    int iconSize;
    Bitmap image;
    Uri imageUri;
    int previewWidth;
    int previewheight;

    SaveImageInBackgroundData() {
    }

    void clearImage() {
        this.image = null;
        this.imageUri = null;
        this.iconSize = 0;
    }

    void clearContext() {
        this.context = null;
    }
}
