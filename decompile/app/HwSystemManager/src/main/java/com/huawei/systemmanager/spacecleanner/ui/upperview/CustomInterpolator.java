package com.huawei.systemmanager.spacecleanner.ui.upperview;

import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.widget.slideview.DurationInterpolator;

public class CustomInterpolator implements DurationInterpolator {
    public static final long sMainDuration = 627;
    private Interpolator mInnerInterpolator;

    public CustomInterpolator() {
        initInerpolator();
    }

    public int getDuration() {
        return 627;
    }

    public float getInterpolation(float input) {
        return this.mInnerInterpolator.getInterpolation(input);
    }

    private void initInerpolator() {
        try {
            this.mInnerInterpolator = AnimationUtils.loadInterpolator(GlobalContext.getContext(), R.anim.cubic_bezier_interpolator_type_a);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.mInnerInterpolator == null) {
            this.mInnerInterpolator = new DecelerateInterpolator();
        }
    }
}
