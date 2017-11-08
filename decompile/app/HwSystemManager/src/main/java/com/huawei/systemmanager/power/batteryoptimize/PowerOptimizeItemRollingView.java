package com.huawei.systemmanager.power.batteryoptimize;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.PowerRollingView;
import com.huawei.systemmanager.util.HwLog;

public class PowerOptimizeItemRollingView extends PowerRollingView {
    private static final int PROGRESS_MAX = 100;
    private static final int QUICK_DURING_TIME = 200;
    private static final int SPEED_PER_NUM = 100;
    private Interpolator mTimerInterpolator = new AccelerateInterpolator();

    public PowerOptimizeItemRollingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PowerOptimizeItemRollingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setNumberQuick(int target) {
        setNumberByDuration(target, 200);
    }

    public void setNumberByProgress(int target, int progress) {
        if (progress < 0) {
            HwLog.e(PowerRollingView.TAG, "setNumberByProgress wrong ! progress:" + progress);
            progress = 0;
        }
        if (progress > 100) {
            HwLog.e(PowerRollingView.TAG, "setNumberByProgress wrong ! progress:" + progress);
            progress = 100;
        }
        float factor = ((float) progress) / 100.0f;
        setNumberByDuration(target, (int) ((Utility.ALPHA_MAX - this.mTimerInterpolator.getInterpolation(factor)) * ((float) getDuration(target))));
    }

    private int getDuration(int target) {
        return Math.abs(getCurrentNumber() - target) * 100;
    }

    public int getPowerRollType() {
        return 1;
    }
}
