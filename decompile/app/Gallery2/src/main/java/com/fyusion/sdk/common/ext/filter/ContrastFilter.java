package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.g;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class ContrastFilter extends AdjustmentFilter {
    public ContrastFilter() {
        super(ImageFilterAbstractFactory.CONTRAST);
    }

    float a() {
        return WMElement.CAMERASIZEVALUE1B1;
    }

    float b() {
        return 0.5f;
    }

    float c() {
        return 1.5f;
    }

    public Class<? extends a> getImplementationClass() {
        return g.class;
    }
}
