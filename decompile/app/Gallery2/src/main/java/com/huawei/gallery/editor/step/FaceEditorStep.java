package com.huawei.gallery.editor.step;

import com.huawei.gallery.editor.cache.BitmapCache;
import com.huawei.gallery.editor.filters.BaseFiltersManager;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.ImageFilter;
import com.huawei.gallery.editor.tools.EditorUtils;
import java.util.ArrayList;
import java.util.Vector;

public abstract class FaceEditorStep extends EditorStep {
    ArrayList<FilterRepresentation> mFaceFilterRepresentation = new ArrayList();

    public boolean add(FilterRepresentation representation) {
        if (representation == null || representation.getFilterType() != 5) {
            return false;
        }
        return EditorUtils.addOrUpdateFilterRepresentation(this.mFaceFilterRepresentation, representation.copy());
    }

    public boolean isNil() {
        return this.mFaceFilterRepresentation.size() == 0;
    }

    public Vector<ImageFilter> getUsedFilters(BaseFiltersManager filtersManager) {
        Vector<ImageFilter> usedFilters = new Vector();
        for (int i = 0; i < this.mFaceFilterRepresentation.size(); i++) {
            usedFilters.add(filtersManager.getFilterForRepresentation((FilterRepresentation) this.mFaceFilterRepresentation.get(i)));
        }
        return usedFilters;
    }

    public ArrayList<FilterRepresentation> getFilterRepresentationList() {
        ArrayList<FilterRepresentation> list = new ArrayList();
        list.addAll(this.mFaceFilterRepresentation);
        return list;
    }

    public void reset(BitmapCache bitmapCache) {
        super.reset(bitmapCache);
        for (FilterRepresentation representation : this.mFaceFilterRepresentation) {
            representation.reset();
        }
    }

    public boolean equals(Object step) {
        if (!(step instanceof FaceEditorStep)) {
            return false;
        }
        FaceEditorStep s = (FaceEditorStep) step;
        if (this.mFaceFilterRepresentation.size() != s.mFaceFilterRepresentation.size()) {
            return false;
        }
        for (int index = 0; index < this.mFaceFilterRepresentation.size(); index++) {
            if (!((FilterRepresentation) this.mFaceFilterRepresentation.get(index)).equals((FilterRepresentation) s.mFaceFilterRepresentation.get(index))) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return super.hashCode();
    }
}
