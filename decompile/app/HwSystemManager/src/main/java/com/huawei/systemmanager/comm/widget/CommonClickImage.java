package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.huawei.systemmanager.util.HwLog;

public class CommonClickImage extends ImageView {
    public static final int ALPHA_DISABLE = 76;
    public static final int ALPHA_MAX = 255;
    public static final int ALPHA_PRESSED = 127;
    private static final String TAG = "CommonClickImage";

    public CommonClickImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void dispatchSetPressed(boolean pressed) {
        Drawable d = getDrawable();
        if (d == null) {
            HwLog.e(TAG, "no drawable");
            return;
        }
        int i;
        if (pressed) {
            i = 127;
        } else {
            i = 255;
        }
        d.setAlpha(i);
    }
}
