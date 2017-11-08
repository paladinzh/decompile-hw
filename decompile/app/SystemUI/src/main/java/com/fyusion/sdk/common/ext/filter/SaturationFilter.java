package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.t;

/* compiled from: Unknown */
public class SaturationFilter extends AdjustmentFilter {
    public SaturationFilter() {
        super("SATURATION");
    }

    float a() {
        return 0.5f;
    }

    public Class<? extends a> getImplementationClass() {
        return t.class;
    }
}
