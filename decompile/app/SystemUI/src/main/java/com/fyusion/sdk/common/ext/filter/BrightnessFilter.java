package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.d;

/* compiled from: Unknown */
public class BrightnessFilter extends AdjustmentFilter {
    public BrightnessFilter() {
        super("BRIGHTNESS");
    }

    float a() {
        return 0.5f;
    }

    public Class<? extends a> getImplementationClass() {
        return d.class;
    }
}
