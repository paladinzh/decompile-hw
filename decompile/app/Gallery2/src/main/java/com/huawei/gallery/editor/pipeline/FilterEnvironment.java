package com.huawei.gallery.editor.pipeline;

import android.graphics.Bitmap;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.app.EditorState;
import com.huawei.gallery.editor.cache.BitmapCache;
import com.huawei.gallery.editor.cache.BubbleCache;
import com.huawei.gallery.editor.cache.CustDrawBrushCache;
import com.huawei.gallery.editor.cache.DrawCache;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.FilterUserPresetRepresentation;
import com.huawei.gallery.editor.filters.FiltersManagerInterface;
import com.huawei.gallery.editor.filters.ImageFilter;

public class FilterEnvironment {
    private BitmapCache mBitmapCache;
    private BubbleCache mBubbleCache;
    private CustDrawBrushCache mCustDrawBrushCache;
    private DrawCache mDrawCache;
    private FiltersManagerInterface mFiltersManager;
    private PipelineInterface mPipeline;
    private int mQuality;
    private volatile boolean mStop = false;

    public synchronized boolean needsStop() {
        return this.mStop;
    }

    public synchronized void setStop(boolean stop) {
        this.mStop = stop;
    }

    public void setBitmapCache(BitmapCache cache) {
        this.mBitmapCache = cache;
    }

    public void cache(Bitmap bitmap) {
        if (this.mBitmapCache != null) {
            this.mBitmapCache.cache(bitmap);
        }
    }

    public Bitmap getBitmap(int w, int h) {
        if (this.mBitmapCache != null) {
            return this.mBitmapCache.getBitmap(w, h);
        }
        return null;
    }

    public Bitmap getBitmapCopy(Bitmap source) {
        if (this.mBitmapCache != null) {
            return this.mBitmapCache.getBitmapCopy(source);
        }
        return null;
    }

    public int getQuality() {
        return this.mQuality;
    }

    public void setQuality(int quality) {
        this.mQuality = quality;
    }

    public void setFiltersManager(FiltersManagerInterface filtersManager) {
        this.mFiltersManager = filtersManager;
    }

    public Bitmap applyRepresentation(Bitmap bitmap, FilterRepresentation representation) {
        return applyRepresentation(bitmap, representation, null);
    }

    public Bitmap applyRepresentation(Bitmap bitmap, FilterRepresentation representation, EditorState state) {
        if ((representation instanceof FilterUserPresetRepresentation) || this.mFiltersManager == null) {
            return bitmap;
        }
        ImageFilter filter = this.mFiltersManager.getFilterForRepresentation(representation);
        if (filter == null) {
            GalleryLog.e("FilterEnvironment", "No ImageFilter for " + representation.getSerializationName());
            return bitmap;
        }
        filter.useRepresentation(representation);
        filter.useEditorState(state);
        filter.setEnvironment(this);
        Bitmap ret = filter.apply(bitmap);
        if (bitmap != ret) {
            cache(bitmap);
        }
        filter.setEnvironment(null);
        return ret;
    }

    public void setPipeline(PipelineInterface cachingPipeline) {
        this.mPipeline = cachingPipeline;
    }

    public BitmapCache getBitmapCache() {
        return this.mBitmapCache;
    }

    public void setDrawCache(DrawCache drawCache) {
        this.mDrawCache = drawCache;
    }

    public DrawCache getDrawCache() {
        return this.mDrawCache;
    }

    public void setCustDrawBrushCache(CustDrawBrushCache brushCache) {
        this.mCustDrawBrushCache = brushCache;
    }

    public CustDrawBrushCache getCustDrawBrushCache() {
        return this.mCustDrawBrushCache;
    }

    public void setBubbleCache(BubbleCache bubbleCache) {
        this.mBubbleCache = bubbleCache;
    }

    public BubbleCache getBubbleCache() {
        return this.mBubbleCache;
    }
}
