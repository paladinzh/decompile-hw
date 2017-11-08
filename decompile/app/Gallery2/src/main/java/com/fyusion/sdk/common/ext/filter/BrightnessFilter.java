package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.d;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class BrightnessFilter extends AdjustmentFilter {
    public BrightnessFilter() {
        super(ImageFilterAbstractFactory.BRIGHTNESS);
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
        return d.class;
    }
}
