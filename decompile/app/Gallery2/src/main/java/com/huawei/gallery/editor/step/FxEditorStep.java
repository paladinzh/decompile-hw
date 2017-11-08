package com.huawei.gallery.editor.step;

import android.graphics.Bitmap;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.cache.BitmapCache;
import com.huawei.gallery.editor.filters.BaseFiltersManager;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.ImageFilter;
import com.huawei.gallery.editor.filters.fx.FilterFxRepresentation;
import com.huawei.gallery.editor.pipeline.FilterEnvironment;
import java.util.ArrayList;
import java.util.Vector;

public class FxEditorStep extends EditorStep {
    FilterRepresentation mFilterFxRepresentation;

    public boolean add(FilterRepresentation representation) {
        if (representation == null || representation.getFilterType() != 2) {
            return false;
        }
        this.mFilterFxRepresentation = representation.copy();
        return true;
    }

    public Bitmap process(Bitmap src, FilterEnvironment environment) {
        if (isNil()) {
            return src;
        }
        return environment.applyRepresentation(src, this.mFilterFxRepresentation);
    }

    public boolean isNil() {
        if (this.mFilterFxRepresentation != null) {
            return isNoneFxFilter(this.mFilterFxRepresentation);
        }
        return true;
    }

    public EditorStep copy() {
        FilterRepresentation filterRepresentation = null;
        FxEditorStep step = new FxEditorStep();
        if (this.mFilterFxRepresentation != null) {
            filterRepresentation = this.mFilterFxRepresentation.copy();
        }
        step.mFilterFxRepresentation = filterRepresentation;
        return step;
    }

    private boolean isNoneFxFilter(FilterRepresentation representation) {
        if ((representation instanceof FilterFxRepresentation) && ((FilterFxRepresentation) representation).getNameResource() == R.string.pref_camera_coloreffect_entry_original) {
            return true;
        }
        return false;
    }

    public Vector<ImageFilter> getUsedFilters(BaseFiltersManager filtersManager) {
        Vector<ImageFilter> usedFilters = new Vector();
        if (this.mFilterFxRepresentation != null) {
            usedFilters.add(filtersManager.getFilterForRepresentation(this.mFilterFxRepresentation));
        }
        return usedFilters;
    }

    public ArrayList<FilterRepresentation> getFilterRepresentationList() {
        ArrayList<FilterRepresentation> list = new ArrayList();
        if (this.mFilterFxRepresentation != null) {
            list.add(this.mFilterFxRepresentation);
        }
        return list;
    }

    public void reset(BitmapCache bitmapCache) {
        super.reset(bitmapCache);
        if (this.mFilterFxRepresentation != null) {
            this.mFilterFxRepresentation.reset();
        }
    }

    public boolean equals(Object step) {
        if (this.mFilterFxRepresentation == null || !(step instanceof FxEditorStep)) {
            return false;
        }
        return this.mFilterFxRepresentation.equals(((FxEditorStep) step).mFilterFxRepresentation);
    }

    public int hashCode() {
        return super.hashCode();
    }
}
