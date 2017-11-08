package com.huawei.gallery.editor.step;

import android.graphics.Bitmap;
import com.huawei.gallery.editor.app.BaseMosaicState;
import com.huawei.gallery.editor.cache.BitmapCache;
import com.huawei.gallery.editor.cache.DrawCache;
import com.huawei.gallery.editor.filters.BaseFiltersManager;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.ImageFilter;
import com.huawei.gallery.editor.pipeline.FilterEnvironment;
import java.util.ArrayList;
import java.util.Vector;

public class MosaicEditorStep extends EditorStep {
    private FilterRepresentation mFilterMosaicRepresentation;
    private BaseMosaicState mState;
    private boolean mUseCache;

    public boolean add(FilterRepresentation representation) {
        if (representation == null || representation.getFilterType() != 9) {
            return false;
        }
        this.mFilterMosaicRepresentation = representation.copy();
        return true;
    }

    public Bitmap process(Bitmap src, FilterEnvironment environment) {
        if (isNil()) {
            DrawCache drawCache = environment.getDrawCache();
            if (drawCache != null) {
                drawCache.setCachedStrokesCount(0, drawCache.getMagicId());
                drawCache.setOverlayBitmap(null, environment.getBitmapCache(), drawCache.getMagicId());
            }
            return src;
        }
        if (this.mFilterMosaicRepresentation instanceof FilterMosaicRepresentation) {
            ((FilterMosaicRepresentation) this.mFilterMosaicRepresentation).setUseCache(this.mUseCache);
        }
        return environment.applyRepresentation(src, this.mFilterMosaicRepresentation, this.mState);
    }

    public boolean isNil() {
        if (this.mFilterMosaicRepresentation != null) {
            return this.mFilterMosaicRepresentation.isNil();
        }
        return true;
    }

    public EditorStep copy() {
        FilterRepresentation filterRepresentation = null;
        MosaicEditorStep step = new MosaicEditorStep();
        if (this.mFilterMosaicRepresentation != null) {
            filterRepresentation = this.mFilterMosaicRepresentation.copy();
        }
        step.mFilterMosaicRepresentation = filterRepresentation;
        step.mState = this.mState;
        step.mUseCache = this.mUseCache;
        return step;
    }

    public Vector<ImageFilter> getUsedFilters(BaseFiltersManager filtersManager) {
        Vector<ImageFilter> usedFilters = new Vector();
        if (this.mFilterMosaicRepresentation != null) {
            usedFilters.add(filtersManager.getFilterForRepresentation(this.mFilterMosaicRepresentation));
        }
        return usedFilters;
    }

    public ArrayList<FilterRepresentation> getFilterRepresentationList() {
        ArrayList<FilterRepresentation> list = new ArrayList();
        if (this.mFilterMosaicRepresentation != null) {
            list.add(this.mFilterMosaicRepresentation);
        }
        return list;
    }

    public void reset(BitmapCache bitmapCache) {
        super.reset(bitmapCache);
        this.mState = null;
        this.mUseCache = false;
        if (this.mFilterMosaicRepresentation != null) {
            this.mFilterMosaicRepresentation.reset();
        }
    }

    public boolean equals(Object step) {
        if (this.mFilterMosaicRepresentation == null || !(step instanceof MosaicEditorStep)) {
            return false;
        }
        return this.mFilterMosaicRepresentation.equals(((MosaicEditorStep) step).mFilterMosaicRepresentation);
    }

    public void setEditorState(BaseMosaicState state) {
        this.mState = state;
    }

    public void setUseCache(boolean useCache) {
        this.mUseCache = useCache;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public void mergeMosaicStep(MosaicEditorStep target) {
        if (!(target == null || target.isNil())) {
            FilterRepresentation filterRepresentation = target.mFilterMosaicRepresentation;
            FilterMosaicRepresentation filterMosaicRepresentation = null;
            FilterMosaicRepresentation b = null;
            if (this.mFilterMosaicRepresentation != null && (this.mFilterMosaicRepresentation instanceof FilterMosaicRepresentation)) {
                filterMosaicRepresentation = this.mFilterMosaicRepresentation;
            }
            if (filterRepresentation != null && (filterRepresentation instanceof FilterMosaicRepresentation)) {
                b = (FilterMosaicRepresentation) filterRepresentation;
            }
            if (filterMosaicRepresentation != null && b != null) {
                filterMosaicRepresentation.getAppliedMosaic().addAll(b.getAppliedMosaic());
            }
        }
    }
}
