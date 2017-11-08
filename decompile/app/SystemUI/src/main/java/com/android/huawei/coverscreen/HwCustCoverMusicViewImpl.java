package com.android.huawei.coverscreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.SystemProperties;
import com.huawei.keyguard.util.Typefaces;
import fyusion.vislib.BuildConfig;

public class HwCustCoverMusicViewImpl extends HwCustCoverMusicView {
    private static final String RESOURCE_SUFFIX = SystemProperties.get("ro.config.small_cover_size", BuildConfig.FLAVOR);
    private Context context;

    public HwCustCoverMusicViewImpl(Context context) {
        super(context);
        this.context = context;
    }

    public int getStateImageResId(int state, int imageResId) {
        switch (state) {
            case 3:
                return CoverResourceUtils.getResIdentifier(this.context, "btn_music_pause_selector", "drawable", "com.android.systemui", imageResId);
            default:
                return CoverResourceUtils.getResIdentifier(this.context, "btn_music_play_selector", "drawable", "com.android.systemui", imageResId);
        }
    }

    public Typeface getTypeface() {
        if (RESOURCE_SUFFIX.equals("_1047x1312")) {
            return Typefaces.get(this.context, "/system/fonts/Roboto-Light.ttf");
        }
        return null;
    }

    public int getBgBlurRadius(int defaultBackgroundBlurRadius) {
        if (RESOURCE_SUFFIX.equals("_1047x1312")) {
            return 1;
        }
        return defaultBackgroundBlurRadius;
    }

    public float getBgDarknessAlpha(int defaultBackgroundAlpha) {
        if (RESOURCE_SUFFIX.equals("_1047x1312")) {
            return 0.4f;
        }
        return (float) defaultBackgroundAlpha;
    }

    public Bitmap getAlbumCutBmp(Bitmap bitmap) {
        String str = RESOURCE_SUFFIX;
        int bWidth;
        if (str.equals("_401x1920")) {
            bWidth = bitmap.getWidth();
            return Bitmap.createBitmap(bitmap, (bWidth * 679) / 1080, 0, (bWidth * 401) / 1080, bitmap.getHeight());
        } else if (str.equals("_500x2560") || str.equals("_540x2560")) {
            bWidth = bitmap.getWidth();
            return Bitmap.createBitmap(bitmap, (bWidth * 900) / 1440, 0, (bWidth * 540) / 1440, bitmap.getHeight());
        } else if (!str.equals("_747x1920")) {
            return bitmap;
        } else {
            bWidth = bitmap.getWidth();
            return Bitmap.createBitmap(bitmap, (bWidth * 333) / 1080, 0, (bWidth * 747) / 1080, bitmap.getHeight());
        }
    }
}
