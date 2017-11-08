package com.fyusion.sdk.common.ext.filter;

import com.fyusion.sdk.common.ext.filter.a.y;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public abstract class ToneCurveFilter extends FilterControl {
    public ToneCurveFilter(String str) {
        super(str);
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

    public y createFilterWithMaxValue() {
        y yVar = (y) createFilter(c());
        setValue(getValue());
        return yVar;
    }
}
