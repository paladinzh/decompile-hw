package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.k;

/* compiled from: Unknown */
public class ExposureFilter extends AdjustmentFilter {
    public ExposureFilter() {
        super("EXPOSURE");
    }

    float a() {
        return 0.5f;
    }

    public Class<? extends a> getImplementationClass() {
        return k.class;
    }
}
