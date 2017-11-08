package android.support.v4.view.animation;

import android.view.animation.Interpolator;
import com.huawei.systemmanager.comm.misc.Utility;

abstract class LookupTableInterpolator implements Interpolator {
    private final float mStepSize = (Utility.ALPHA_MAX / ((float) (this.mValues.length - 1)));
    private final float[] mValues;

    public LookupTableInterpolator(float[] values) {
        this.mValues = values;
    }

    public float getInterpolation(float input) {
        if (input >= Utility.ALPHA_MAX) {
            return Utility.ALPHA_MAX;
        }
        if (input <= 0.0f) {
            return 0.0f;
        }
        int position = Math.min((int) (((float) (this.mValues.length - 1)) * input), this.mValues.length - 2);
        return this.mValues[position] + ((this.mValues[position + 1] - this.mValues[position]) * ((input - (((float) position) * this.mStepSize)) / this.mStepSize));
    }
}
