package com.huawei.systemmanager.mainscreen.view;

import android.content.Context;
import android.util.AttributeSet;
import com.huawei.systemmanager.comm.widget.RollingView;

public class DetectScoreRollingView extends RollingView {
    private static final long MAX_ROLLING_TIME = 1800;
    private static final long MIN_ROLLING_TIME = 200;

    public DetectScoreRollingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DetectScoreRollingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void updateScore(int score, boolean anima) {
        if (anima) {
            setNumberByDuration(score, (int) Math.max(((long) (Math.abs(score - getCurrentNumber()) / 100)) * MAX_ROLLING_TIME, 200));
        } else {
            setNumberImmediately(score);
        }
    }
}
