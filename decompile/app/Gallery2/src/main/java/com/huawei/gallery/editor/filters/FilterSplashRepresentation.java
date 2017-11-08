package com.huawei.gallery.editor.filters;

import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;

public class FilterSplashRepresentation extends FilterMosaicRepresentation {
    private int mColorValue = 0;
    private boolean mNeedChange = true;

    public FilterSplashRepresentation() {
        setSerializationName("SPLASH");
        setFilterClass(ImageFilterSplash.class);
        setFilterType(9);
        setTextId(R.string.simple_editor_splash);
    }

    public void setColor(int color) {
        this.mColorValue = color;
    }

    public boolean needChange() {
        return this.mNeedChange;
    }

    public void setChangable(boolean needChange) {
        this.mNeedChange = needChange;
    }

    public int getColor() {
        return this.mColorValue;
    }

    public FilterRepresentation copy() {
        FilterSplashRepresentation representation = new FilterSplashRepresentation();
        copyAllParameters(representation);
        return representation;
    }

    public boolean equals(FilterRepresentation representation) {
        if (!super.equals(representation) || !(representation instanceof FilterSplashRepresentation)) {
            return false;
        }
        FilterSplashRepresentation fdRep = (FilterSplashRepresentation) representation;
        if (fdRep.mColorValue == this.mColorValue && fdRep.mNeedChange == this.mNeedChange) {
            return true;
        }
        return false;
    }

    public boolean isNil() {
        return super.isNil() && this.mColorValue == 0;
    }

    public void useParametersFrom(FilterRepresentation a) {
        super.useParametersFrom(a);
        if (a instanceof FilterSplashRepresentation) {
            FilterSplashRepresentation representation = (FilterSplashRepresentation) a;
            try {
                this.mColorValue = representation.mColorValue;
                this.mNeedChange = representation.mNeedChange;
                return;
            } catch (RuntimeException e) {
                GalleryLog.i("FilterSplashRepresentation", "catch a RuntimeException." + e.getMessage());
                return;
            }
        }
        GalleryLog.v("FilterSplashRepresentation", "cannot use parameters from " + a);
    }

    public void reset() {
        super.reset();
        this.mNeedChange = true;
        this.mColorValue = 0;
    }
}
