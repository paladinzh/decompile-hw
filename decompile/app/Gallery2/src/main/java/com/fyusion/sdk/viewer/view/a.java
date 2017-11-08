package com.fyusion.sdk.viewer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import com.amap.api.maps.model.WeightedLatLng;

/* compiled from: Unknown */
public class a extends FrameLayout {
    private double a = -1.0d;

    public a(Context context) {
        super(context);
    }

    public a(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    protected void onMeasure(int i, int i2) {
        if (this.a > 0.0d) {
            int size = MeasureSpec.getSize(i);
            int size2 = MeasureSpec.getSize(i2);
            int paddingRight = getPaddingRight() + getPaddingLeft();
            int paddingBottom = getPaddingBottom() + getPaddingTop();
            int i3 = size - paddingRight;
            size = size2 - paddingBottom;
            double d = (this.a / (((double) i3) / ((double) size))) - WeightedLatLng.DEFAULT_INTENSITY;
            if (Math.abs(d) >= 0.01d) {
                if (d > 0.0d) {
                    size = (int) (((double) i3) / this.a);
                } else {
                    i3 = (int) (((double) size) * this.a);
                }
                size += paddingBottom;
                i = MeasureSpec.makeMeasureSpec(i3 + paddingRight, 1073741824);
                i2 = MeasureSpec.makeMeasureSpec(size, 1073741824);
            }
        }
        super.onMeasure(i, i2);
    }

    public void setAspectRatio(double d) {
        if (d < 0.0d) {
            throw new IllegalArgumentException();
        }
        Log.d("AFL", "Setting aspect ratio to " + d + " (was " + this.a + ")");
        if (this.a != d) {
            this.a = d;
            requestLayout();
        }
    }
}
