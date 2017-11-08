package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.u;

/* compiled from: Unknown */
public class SharpenFilter extends AdjustmentFilter {
    public SharpenFilter() {
        super("SHARPEN");
    }

    float a() {
        return 0.0f;
    }

    public Class<? extends a> getImplementationClass() {
        return u.class;
    }
}
