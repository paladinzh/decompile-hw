package com.android.huawei.coverscreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;

public class HwCustCoverMusicView {
    public HwCustCoverMusicView(Context context) {
    }

    public int getStateImageResId(int state, int imageResId) {
        return imageResId;
    }

    public Typeface getTypeface() {
        return null;
    }

    public int getBgBlurRadius(int defaultBackgroundBlurRadius) {
        return defaultBackgroundBlurRadius;
    }

    public float getBgDarknessAlpha(float defaultBackgroundAlpha) {
        return defaultBackgroundAlpha;
    }

    public Bitmap getAlbumCutBmp(Bitmap bitmap) {
        return bitmap;
    }
}
