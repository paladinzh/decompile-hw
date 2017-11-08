package com.fyusion.sdk.common.ext.filter;

import com.amap.api.maps.model.GroundOverlayOptions;
import com.fyusion.sdk.common.ext.filter.a.a;
import com.fyusion.sdk.common.ext.filter.a.q;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class HighlightsFilter extends AdjustmentFilter {
    public HighlightsFilter() {
        super(ImageFilterAbstractFactory.HIGHLIGHTS);
    }

    float a() {
        return 0.0f;
    }

    float b() {
        return GroundOverlayOptions.NO_DIMENSION;
    }

    float c() {
        return WMElement.CAMERASIZEVALUE1B1;
    }

    public Class<? extends a> getImplementationClass() {
        return q.class;
    }
}
