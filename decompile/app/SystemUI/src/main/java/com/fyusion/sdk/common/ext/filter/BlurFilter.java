package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.b;

/* compiled from: Unknown */
public class BlurFilter extends AdjustmentFilter {
    public BlurFilter() {
        super("BLUR");
    }

    float a() {
        return 0.0f;
    }

    public Class<? extends a> getImplementationClass() {
        return b.class;
    }
}
