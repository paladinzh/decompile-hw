package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.t;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class SaturationFilter extends AdjustmentFilter {
    public SaturationFilter() {
        super(ImageFilterAbstractFactory.SATURATION);
    }

    float a() {
        return 0.5f;
    }

    float b() {
        return 0.0f;
    }

    float c() {
        return WMElement.CAMERASIZEVALUE1B1;
    }

    public Class<? extends a> getImplementationClass() {
        return t.class;
    }
}
