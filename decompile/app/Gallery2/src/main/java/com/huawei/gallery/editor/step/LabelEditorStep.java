package com.huawei.gallery.editor.step;

import android.graphics.Bitmap;
import com.huawei.gallery.editor.app.LabelState;
import com.huawei.gallery.editor.cache.BitmapCache;
import com.huawei.gallery.editor.filters.BaseFiltersManager;
import com.huawei.gallery.editor.filters.FilterLabelRepresentation;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.ImageFilter;
import com.huawei.gallery.editor.pipeline.FilterEnvironment;
import java.util.ArrayList;
import java.util.Vector;

public class LabelEditorStep extends EditorStep {
    FilterRepresentation mFilterLabelRepresentation;
    private LabelState mState;

    public boolean add(FilterRepresentation representation) {
        if (!(representation instanceof FilterLabelRepresentation)) {
            return false;
        }
        this.mFilterLabelRepresentation = representation.copy();
        return true;
    }

    public Bitmap process(Bitmap src, FilterEnvironment environment) {
        if (isNil()) {
            return src;
        }
        return environment.applyRepresentation(src, this.mFilterLabelRepresentation, this.mState);
    }

    public boolean isNil() {
        if (this.mFilterLabelRepresentation != null) {
            return this.mFilterLabelRepresentation.isNil();
        }
        return true;
    }

    public EditorStep copy() {
        FilterRepresentation filterRepresentation = null;
        LabelEditorStep step = new LabelEditorStep();
        if (this.mFilterLabelRepresentation != null) {
            filterRepresentation = this.mFilterLabelRepresentation.copy();
        }
        step.mFilterLabelRepresentation = filterRepresentation;
        step.mState = this.mState;
        return step;
    }

    public Vector<ImageFilter> getUsedFilters(BaseFiltersManager filtersManager) {
        Vector<ImageFilter> usedFilters = new Vector();
        if (this.mFilterLabelRepresentation != null) {
            usedFilters.add(filtersManager.getFilterForRepresentation(this.mFilterLabelRepresentation));
        }
        return usedFilters;
    }

    public ArrayList<FilterRepresentation> getFilterRepresentationList() {
        ArrayList<FilterRepresentation> list = new ArrayList();
        if (this.mFilterLabelRepresentation != null) {
            list.add(this.mFilterLabelRepresentation);
        }
        return list;
    }

    public void reset(BitmapCache bitmapCache) {
        super.reset(bitmapCache);
        this.mState = null;
        if (this.mFilterLabelRepresentation != null) {
            this.mFilterLabelRepresentation.reset();
        }
    }

    public boolean equals(Object step) {
        if (this.mFilterLabelRepresentation == null || !(step instanceof LabelEditorStep)) {
            return false;
        }
        return this.mFilterLabelRepresentation.equals(((LabelEditorStep) step).mFilterLabelRepresentation);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public void setEditorState(LabelState state) {
        this.mState = state;
    }
}
