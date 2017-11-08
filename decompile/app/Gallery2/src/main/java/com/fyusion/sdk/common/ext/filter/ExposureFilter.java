package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.k;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class ExposureFilter extends AdjustmentFilter {
    public ExposureFilter() {
        super(ImageFilterAbstractFactory.EXPOSURE);
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
        return k.class;
    }
}
