package com.huawei.gallery.editor.step;

import android.graphics.Bitmap;
import com.huawei.gallery.editor.cache.BitmapCache;
import com.huawei.gallery.editor.filters.BaseFiltersManager;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.ImageFilter;
import com.huawei.gallery.editor.imageshow.GeometryMathUtils;
import com.huawei.gallery.editor.pipeline.FilterEnvironment;
import com.huawei.gallery.editor.tools.EditorUtils;
import java.util.ArrayList;
import java.util.Vector;

public class GeometryEditorStep extends EditorStep {
    ArrayList<FilterRepresentation> mGeometryFilterRepresentation = new ArrayList();

    public boolean add(FilterRepresentation representation) {
        if (representation == null || representation.getFilterType() != 4) {
            return false;
        }
        return EditorUtils.addOrUpdateFilterRepresentation(this.mGeometryFilterRepresentation, representation.copy());
    }

    public Bitmap process(Bitmap src, FilterEnvironment environment) {
        if (isNil()) {
            return src;
        }
        return GeometryMathUtils.applyGeometryRepresentations(this.mGeometryFilterRepresentation, src, environment.getBitmapCache());
    }

    public boolean isNil() {
        for (FilterRepresentation rep : this.mGeometryFilterRepresentation) {
            if (!rep.isNil()) {
                return false;
            }
        }
        return true;
    }

    public EditorStep copy() {
        GeometryEditorStep step = new GeometryEditorStep();
        for (FilterRepresentation rep : this.mGeometryFilterRepresentation) {
            step.mGeometryFilterRepresentation.add(rep.copy());
        }
        return step;
    }

    public Vector<ImageFilter> getUsedFilters(BaseFiltersManager filtersManager) {
        Vector<ImageFilter> usedFilters = new Vector();
        for (int i = 0; i < this.mGeometryFilterRepresentation.size(); i++) {
            usedFilters.add(filtersManager.getFilterForRepresentation((FilterRepresentation) this.mGeometryFilterRepresentation.get(i)));
        }
        return usedFilters;
    }

    public ArrayList<FilterRepresentation> getFilterRepresentationList() {
        ArrayList<FilterRepresentation> list = new ArrayList();
        list.addAll(this.mGeometryFilterRepresentation);
        return list;
    }

    public void reset(BitmapCache bitmapCache) {
        super.reset(bitmapCache);
        for (FilterRepresentation representation : this.mGeometryFilterRepresentation) {
            representation.reset();
        }
    }

    public boolean equals(Object step) {
        if (step instanceof GeometryEditorStep) {
            return GeometryMathUtils.unpackGeometry(((GeometryEditorStep) step).getFilterRepresentationList()).equals(GeometryMathUtils.unpackGeometry(getFilterRepresentationList()));
        }
        return false;
    }

    public int hashCode() {
        return super.hashCode();
    }
}
