package com.android.contacts.hap.rcs;

import android.graphics.Outline;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;

public class RoundOutlineProvider extends ViewOutlineProvider {
    public void getOutline(View view, Outline outline) {
        Outline outline2 = outline;
        int i = 0;
        outline2.setRoundRect(0, i, view.getWidth(), view.getHeight(), (float) ((int) TypedValue.applyDimension(1, 8.0f, view.getResources().getDisplayMetrics())));
    }
}
