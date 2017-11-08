package com.android.systemui;

import android.view.View;
import android.widget.TextView;

public class FontSizeUtils {
    public static void updateFontSize(View parent, int viewId, int dimensId) {
        updateFontSize((TextView) parent.findViewById(viewId), dimensId);
    }

    public static void updateFontSize(TextView v, int dimensId) {
        if (v != null) {
            v.setTextSize(0, (float) v.getResources().getDimensionPixelSize(dimensId));
        }
    }
}
