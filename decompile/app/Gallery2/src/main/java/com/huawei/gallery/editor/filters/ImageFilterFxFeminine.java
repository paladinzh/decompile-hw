package com.huawei.gallery.editor.filters;

import android.graphics.Bitmap;
import com.huawei.gallery.editor.filters.fx.FilterFeminineFxRepresentation;

public class ImageFilterFxFeminine extends ImageFilter {
    private FilterFeminineFxRepresentation mParameters = null;

    protected native void nativeApplyFilter(Bitmap bitmap, String str, int i, int i2, int i3);

    public void freeResources() {
    }

    public void useRepresentation(FilterRepresentation representation) {
        if (representation instanceof FilterFeminineFxRepresentation) {
            this.mParameters = (FilterFeminineFxRepresentation) representation;
        }
    }

    public FilterFeminineFxRepresentation getParameters() {
        return this.mParameters;
    }

    public Bitmap apply(Bitmap bitmap) {
        if (getParameters() == null) {
            return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int stride = w * 4;
        if (!(this.mParameters.getEffectName() == null || getEnvironment().needsStop())) {
            nativeApplyFilter(bitmap, this.mParameters.getEffectName(), w, h, stride);
        }
        return bitmap;
    }
}
