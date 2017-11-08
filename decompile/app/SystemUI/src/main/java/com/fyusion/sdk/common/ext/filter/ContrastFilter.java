package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.g;

/* compiled from: Unknown */
public class ContrastFilter extends AdjustmentFilter {
    public ContrastFilter() {
        super("CONTRAST");
    }

    float a() {
        return 1.0f;
    }

    public Class<? extends a> getImplementationClass() {
        return g.class;
    }
}
