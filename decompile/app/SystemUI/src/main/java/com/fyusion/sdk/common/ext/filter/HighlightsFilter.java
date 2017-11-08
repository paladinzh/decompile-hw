package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.q;

/* compiled from: Unknown */
public class HighlightsFilter extends AdjustmentFilter {
    public HighlightsFilter() {
        super("HIGHLIGHTS");
    }

    float a() {
        return 0.0f;
    }

    public Class<? extends a> getImplementationClass() {
        return q.class;
    }
}
