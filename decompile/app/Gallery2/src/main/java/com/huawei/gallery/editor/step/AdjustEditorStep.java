package com.huawei.gallery.editor.step;

import android.graphics.Bitmap;
import com.huawei.gallery.editor.cache.BitmapCache;
import com.huawei.gallery.editor.filters.BaseFiltersManager;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.ImageFilter;
import com.huawei.gallery.editor.pipeline.FilterEnvironment;
import com.huawei.gallery.editor.tools.EditorUtils;
import java.util.ArrayList;
import java.util.Vector;

public class AdjustEditorStep extends EditorStep {
    ArrayList<FilterRepresentation> mFilterRepresentation = new ArrayList();

    public boolean add(FilterRepresentation representation) {
        if (representation == null) {
            return false;
        }
        return EditorUtils.addOrUpdateFilterRepresentation(this.mFilterRepresentation, representation.copy());
    }

    public Bitmap process(Bitmap src, FilterEnvironment environment) {
        if (isNil()) {
            return src;
        }
        Bitmap target = src;
        for (FilterRepresentation representation : this.mFilterRepresentation) {
            if (!representation.isNil()) {
                target = environment.applyRepresentation(target, representation);
            }
        }
        return target;
    }

    public boolean isNil() {
        return this.mFilterRepresentation.size() == 0;
    }

    public EditorStep copy() {
        AdjustEditorStep step = new AdjustEditorStep();
        for (FilterRepresentation representation : this.mFilterRepresentation) {
            step.mFilterRepresentation.add(representation.copy());
        }
        return step;
    }

    public Vector<ImageFilter> getUsedFilters(BaseFiltersManager filtersManager) {
        Vector<ImageFilter> usedFilters = new Vector();
        for (int i = 0; i < this.mFilterRepresentation.size(); i++) {
            usedFilters.add(filtersManager.getFilterForRepresentation((FilterRepresentation) this.mFilterRepresentation.get(i)));
        }
        return usedFilters;
    }

    public ArrayList<FilterRepresentation> getFilterRepresentationList() {
        ArrayList<FilterRepresentation> list = new ArrayList();
        list.addAll(this.mFilterRepresentation);
        return list;
    }

    public void reset(BitmapCache bitmapCache) {
        super.reset(bitmapCache);
        for (FilterRepresentation representation : this.mFilterRepresentation) {
            representation.reset();
        }
    }

    public boolean equals(Object step) {
        if (!(step instanceof AdjustEditorStep)) {
            return false;
        }
        AdjustEditorStep s = (AdjustEditorStep) step;
        if (this.mFilterRepresentation.size() != s.mFilterRepresentation.size()) {
            return false;
        }
        for (int index = 0; index < this.mFilterRepresentation.size(); index++) {
            if (!((FilterRepresentation) this.mFilterRepresentation.get(index)).equals((FilterRepresentation) s.mFilterRepresentation.get(index))) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return super.hashCode();
    }
}
