package com.fyusion.sdk.viewer.ext.localfyuse;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import com.fyusion.sdk.common.ext.filter.EditFilterCollection;
import com.fyusion.sdk.common.ext.filter.EditFilterCollection.FilterUpdateListener;
import com.fyusion.sdk.common.ext.filter.FilterRenderer;
import com.fyusion.sdk.common.ext.filter.ImageFilter;
import com.fyusion.sdk.common.j;
import com.fyusion.sdk.common.j.a;
import com.fyusion.sdk.common.o;
import com.fyusion.sdk.viewer.view.k;
import com.fyusion.sdk.viewer.view.l;
import com.fyusion.sdk.viewer.view.m;
import java.util.Collection;
import java.util.List;

/* compiled from: Unknown */
public class b extends l implements FilterUpdateListener, a {
    protected FilterRenderer a;
    protected com.fyusion.sdk.common.ext.filter.a.l b;
    private m d = null;

    public b(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void b(@NonNull EditFilterCollection editFilterCollection) {
        if (this.b != null) {
            this.b.a(editFilterCollection);
            d();
            return;
        }
        Log.e("FilteredTweeningView", "Null filters, this should not happen. Bailing out gracefully");
    }

    public void a(int i, int i2) {
    }

    public void a(EditFilterCollection editFilterCollection) {
        if (editFilterCollection != null) {
            editFilterCollection.addListener(this);
            b(editFilterCollection);
        }
    }

    public void a(o oVar) {
        this.a.addOverlay(oVar);
    }

    public void a(List<ImageFilter> list) {
        this.b.a((Collection) list);
    }

    protected j c() throws IllegalStateException {
        j filterRenderer = new FilterRenderer(new k());
        this.a = (FilterRenderer) filterRenderer;
        this.a.setOverlayCompositor(new com.fyusion.sdk.common.internal.a());
        this.b = new com.fyusion.sdk.common.ext.filter.a.l();
        this.a.setFilters(this.b);
        return filterRenderer;
    }

    public Collection<ImageFilter> getActiveFilters() {
        return this.b.d();
    }

    public void onFilterChainUpdated(EditFilterCollection editFilterCollection) {
        b(editFilterCollection);
        this.a.filtersUpdated();
    }
}
