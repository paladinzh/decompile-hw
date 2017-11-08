package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.e;

/* compiled from: Unknown */
public class BukowskiToneCurve extends ToneCurveFilter {
    public BukowskiToneCurve() {
        super(ImageFilterAbstractFactory.BUKOWSKI);
    }

    public Class getImplementationClass() {
        return e.class;
    }
}
