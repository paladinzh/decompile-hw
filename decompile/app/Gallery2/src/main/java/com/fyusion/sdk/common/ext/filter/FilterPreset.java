package com.fyusion.sdk.common.ext.filter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/* compiled from: Unknown */
public class FilterPreset {
    private EditFilterCollection a = new EditFilterCollection();
    @Nullable
    private String b = null;

    public FilterPreset(String str) {
        this.b = str;
    }

    public void addAdjustment(@NonNull FilterControl filterControl) {
        this.a.addFilter(filterControl);
    }

    public EditFilterCollection getFilters() {
        return this.a;
    }

    public void reset() {
        this.a.reset();
    }

    public void setToneFilter(@NonNull ToneCurveFilter toneCurveFilter) {
        this.a.addFilter(toneCurveFilter);
    }
}
