package com.huawei.gallery.editor.step;

import android.graphics.Bitmap;
import com.huawei.gallery.editor.cache.BitmapCache;
import com.huawei.gallery.editor.filters.BaseFiltersManager;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.ImageFilter;
import com.huawei.gallery.editor.pipeline.FilterEnvironment;
import java.util.ArrayList;
import java.util.Vector;

public abstract class EditorStep {
    private Bitmap mCachedBitmap;

    public abstract boolean add(FilterRepresentation filterRepresentation);

    public abstract EditorStep copy();

    public abstract boolean equals(Object obj);

    public abstract ArrayList<FilterRepresentation> getFilterRepresentationList();

    public abstract Vector<ImageFilter> getUsedFilters(BaseFiltersManager baseFiltersManager);

    public abstract boolean isNil();

    public abstract Bitmap process(Bitmap bitmap, FilterEnvironment filterEnvironment);

    public void cache(Bitmap bmp) {
        this.mCachedBitmap = bmp;
    }

    public Bitmap getCachedBitmap() {
        return this.mCachedBitmap;
    }

    public void reset(BitmapCache bitmapCache) {
        resetCachedBitmap(bitmapCache);
    }

    public void resetCachedBitmap(BitmapCache bitmapCache) {
        bitmapCache.cache(this.mCachedBitmap);
        this.mCachedBitmap = null;
    }
}
