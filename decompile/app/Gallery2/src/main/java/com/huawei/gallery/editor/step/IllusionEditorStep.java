package com.huawei.gallery.editor.step;

import android.graphics.Bitmap;
import com.huawei.gallery.editor.app.IllusionState;
import com.huawei.gallery.editor.cache.BitmapCache;
import com.huawei.gallery.editor.filters.BaseFiltersManager;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.ImageFilter;
import com.huawei.gallery.editor.pipeline.FilterEnvironment;
import java.util.ArrayList;
import java.util.Vector;

public class IllusionEditorStep extends EditorStep {
    private FilterRepresentation mFilterIllusionRepresentation;
    private IllusionState mState;

    public boolean add(FilterRepresentation representation) {
        if (representation == null || representation.getFilterType() != 11) {
            return false;
        }
        this.mFilterIllusionRepresentation = representation.copy();
        return true;
    }

    public Bitmap process(Bitmap src, FilterEnvironment environment) {
        if (isNil()) {
            return src;
        }
        return environment.applyRepresentation(src, this.mFilterIllusionRepresentation, this.mState);
    }

    public boolean isNil() {
        return this.mFilterIllusionRepresentation == null;
    }

    public EditorStep copy() {
        FilterRepresentation filterRepresentation = null;
        IllusionEditorStep step = new IllusionEditorStep();
        step.mState = this.mState;
        if (this.mFilterIllusionRepresentation != null) {
            filterRepresentation = this.mFilterIllusionRepresentation.copy();
        }
        step.mFilterIllusionRepresentation = filterRepresentation;
        return step;
    }

    public Vector<ImageFilter> getUsedFilters(BaseFiltersManager filtersManager) {
        Vector<ImageFilter> usedFilters = new Vector();
        if (this.mFilterIllusionRepresentation != null) {
            usedFilters.add(filtersManager.getFilterForRepresentation(this.mFilterIllusionRepresentation));
        }
        return usedFilters;
    }

    public ArrayList<FilterRepresentation> getFilterRepresentationList() {
        ArrayList<FilterRepresentation> list = new ArrayList();
        if (this.mFilterIllusionRepresentation != null) {
            list.add(this.mFilterIllusionRepresentation);
        }
        return list;
    }

    public boolean equals(Object step) {
        if (this.mFilterIllusionRepresentation == null || !(step instanceof IllusionEditorStep)) {
            return false;
        }
        return this.mFilterIllusionRepresentation.equals(((IllusionEditorStep) step).mFilterIllusionRepresentation);
    }

    public void reset(BitmapCache bitmapCache) {
        super.reset(bitmapCache);
        this.mState = null;
        if (this.mFilterIllusionRepresentation != null) {
            this.mFilterIllusionRepresentation.reset();
        }
    }

    public void setEditorState(IllusionState state) {
        this.mState = state;
    }

    public int hashCode() {
        return super.hashCode();
    }
}
