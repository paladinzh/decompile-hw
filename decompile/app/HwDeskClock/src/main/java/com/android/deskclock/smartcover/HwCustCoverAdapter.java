package com.android.deskclock.smartcover;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View.OnClickListener;
import com.android.deskclock.alarmclock.CoverView;

public class HwCustCoverAdapter {
    public static final String APP_PACKEGE = "com.android.deskclock";
    public static final String TYPE_ARRAY = "array";
    public static final String TYPE_COLOR = "color";
    public static final String TYPE_DIMEN = "dimen";
    public static final String TYPE_DRAWABLE = "drawable";
    public static final String TYPE_LAYOUT = "layout";
    public static final String TYPE_STRING = "string";

    public boolean isAdapterCoverEnable() {
        return false;
    }

    public Drawable getCoverBackground(Context context, int cover_full_lock_background) {
        return null;
    }

    public int getCoverBGColor(Context context, int deskclock_cover_background) {
        return 0;
    }

    public int getResIdentifier(Context context, String name, String defType, String defPackage, int defResIdentifier) {
        return 0;
    }

    public float getCoverCloseTextSize(Context context, int cover_close_textSize) {
        return 0.0f;
    }

    public boolean isLONPortCover() {
        return false;
    }

    public void initLONCover(Context context, CoverView mCoverScreen, Handler mHandler, OnClickListener mSnoozeListener) {
    }

    public void stopLONCoverAnim() {
    }

    public boolean isMTPortCover() {
        return false;
    }

    public boolean isEvaPortCover() {
        return false;
    }

    public boolean isNeedBoldText() {
        return false;
    }
}
