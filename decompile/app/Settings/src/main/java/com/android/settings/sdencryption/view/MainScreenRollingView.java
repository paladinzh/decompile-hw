package com.android.settings.sdencryption.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

public class MainScreenRollingView extends RollingView {
    private Interpolator mTimerInterpolator = new AccelerateInterpolator();

    public MainScreenRollingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainScreenRollingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setNumberQuick(int target) {
        setNumberByDuration(target, 200);
    }
}
