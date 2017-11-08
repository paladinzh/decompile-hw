package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.r;

/* compiled from: Unknown */
public class HorensteinToneCurve extends ToneCurveFilter {
    public HorensteinToneCurve() {
        super("Horenstein");
    }

    public Class getImplementationClass() {
        return r.class;
    }
}
