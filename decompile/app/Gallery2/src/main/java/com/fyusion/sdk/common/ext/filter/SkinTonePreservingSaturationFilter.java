package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.v;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class SkinTonePreservingSaturationFilter extends FilterControl {
    public SkinTonePreservingSaturationFilter() {
        super("SkinToneSaturation");
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
        return v.class;
    }
}
