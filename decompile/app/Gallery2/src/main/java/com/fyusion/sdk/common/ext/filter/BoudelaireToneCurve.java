package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.c;

/* compiled from: Unknown */
public class BoudelaireToneCurve extends ToneCurveFilter {
    public BoudelaireToneCurve() {
        super(ImageFilterAbstractFactory.BOUDELAIRE);
    }

    public Class<? extends a> getImplementationClass() {
        return c.class;
    }
}
