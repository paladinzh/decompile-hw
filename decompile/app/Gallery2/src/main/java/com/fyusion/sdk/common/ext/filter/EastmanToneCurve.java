package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.i;

/* compiled from: Unknown */
public class EastmanToneCurve extends ToneCurveFilter {
    public EastmanToneCurve() {
        super(ImageFilterAbstractFactory.EASTMAN);
    }

    public Class getImplementationClass() {
        return i.class;
    }
}
