package com.android.setupwizardlib.util;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build.VERSION;
import android.view.View;

public class DrawableLayoutDirectionHelper {
    public static InsetDrawable createRelativeInsetDrawable(Drawable drawable, int insetStart, int insetTop, int insetEnd, int insetBottom, View view) {
        boolean isRtl = true;
        if (VERSION.SDK_INT < 17) {
            isRtl = false;
        } else if (view.getLayoutDirection() != 1) {
            isRtl = false;
        }
        return createRelativeInsetDrawable(drawable, insetStart, insetTop, insetEnd, insetBottom, isRtl);
    }

    private static InsetDrawable createRelativeInsetDrawable(Drawable drawable, int insetStart, int insetTop, int insetEnd, int insetBottom, boolean isRtl) {
        if (isRtl) {
            return new InsetDrawable(drawable, insetEnd, insetTop, insetStart, insetBottom);
        }
        return new InsetDrawable(drawable, insetStart, insetTop, insetEnd, insetBottom);
    }
}
