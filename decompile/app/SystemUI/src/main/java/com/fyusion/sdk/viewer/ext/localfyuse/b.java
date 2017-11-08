package com.fyusion.sdk.viewer.ext.localfyuse;

import android.content.Context;
import android.util.AttributeSet;
import com.fyusion.sdk.common.ext.filter.FilterRenderer;
import com.fyusion.sdk.common.ext.filter.ImageFilter;
import com.fyusion.sdk.common.k;
import com.fyusion.sdk.common.k.a;
import com.fyusion.sdk.viewer.view.l;
import com.fyusion.sdk.viewer.view.m;
import java.util.Collection;
import java.util.List;

/* compiled from: Unknown */
public class b extends l implements a {
    protected FilterRenderer a;
    protected com.fyusion.sdk.common.ext.filter.a.l b;
    private m d = null;

    public b(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void a(int i, int i2) {
    }

    public void a(List<ImageFilter> list) {
        this.b.a((Collection) list);
    }

    protected k c() throws IllegalStateException {
        k filterRenderer = new FilterRenderer(new com.fyusion.sdk.viewer.view.k());
        this.a = (FilterRenderer) filterRenderer;
        this.a.setOverlayCompositor(new com.fyusion.sdk.common.a.a());
        this.b = new com.fyusion.sdk.common.ext.filter.a.l();
        this.a.setFilters(this.b);
        return filterRenderer;
    }
}
