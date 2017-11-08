package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.b;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class BlurFilter extends AdjustmentFilter {
    public BlurFilter() {
        super(ImageFilterAbstractFactory.BLUR);
    }

    float a() {
        return 0.0f;
    }

    float b() {
        return 0.0f;
    }

    float c() {
        return WMElement.CAMERASIZEVALUE1B1;
    }

    public Class<? extends a> getImplementationClass() {
        return b.class;
    }
}
