package com.huawei.gallery.editor.step;

import android.graphics.Bitmap;
import com.huawei.gallery.editor.cache.BitmapCache;
import com.huawei.gallery.editor.filters.BaseFiltersManager;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.FilterWaterMarkRepresentation;
import com.huawei.gallery.editor.filters.ImageFilter;
import com.huawei.gallery.editor.pipeline.FilterEnvironment;
import java.util.ArrayList;
import java.util.Vector;

public class WaterMarkEditorStep extends EditorStep {
    ArrayList<FilterRepresentation> mFilterWaterMarkRepresentation = new ArrayList();

    public boolean add(FilterRepresentation representation) {
        if (!(representation instanceof FilterWaterMarkRepresentation)) {
            return false;
        }
        this.mFilterWaterMarkRepresentation.add(representation.copy());
        return true;
    }

    public Bitmap process(Bitmap src, FilterEnvironment environment) {
        if (isNil()) {
            return src;
        }
        Bitmap target = src;
        for (FilterRepresentation representation : this.mFilterWaterMarkRepresentation) {
            if (!representation.isNil()) {
                target = environment.applyRepresentation(target, representation);
            }
        }
        return target;
    }

    public boolean isNil() {
        return this.mFilterWaterMarkRepresentation.size() == 0;
    }

    public EditorStep copy() {
        WaterMarkEditorStep step = new WaterMarkEditorStep();
        for (FilterRepresentation representation : this.mFilterWaterMarkRepresentation) {
            step.mFilterWaterMarkRepresentation.add(representation.copy());
        }
        return step;
    }

    public Vector<ImageFilter> getUsedFilters(BaseFiltersManager filtersManager) {
        Vector<ImageFilter> usedFilters = new Vector();
        if (!isNil()) {
            for (FilterRepresentation representation : this.mFilterWaterMarkRepresentation) {
                if (representation != null) {
                    usedFilters.add(filtersManager.getFilterForRepresentation(representation));
                }
            }
        }
        return usedFilters;
    }

    public ArrayList<FilterRepresentation> getFilterRepresentationList() {
        ArrayList<FilterRepresentation> list = new ArrayList();
        if (!isNil()) {
            list.addAll(this.mFilterWaterMarkRepresentation);
        }
        return list;
    }

    public void reset(BitmapCache bitmapCache) {
        super.reset(bitmapCache);
        if (!isNil()) {
            for (FilterRepresentation representation : this.mFilterWaterMarkRepresentation) {
                representation.reset();
            }
        }
    }

    public boolean equals(Object step) {
        if (this.mFilterWaterMarkRepresentation == null || !(step instanceof WaterMarkEditorStep)) {
            return false;
        }
        WaterMarkEditorStep s = (WaterMarkEditorStep) step;
        if (this.mFilterWaterMarkRepresentation.size() != s.mFilterWaterMarkRepresentation.size()) {
            return false;
        }
        for (int index = 0; index < this.mFilterWaterMarkRepresentation.size(); index++) {
            if (!((FilterRepresentation) this.mFilterWaterMarkRepresentation.get(index)).equals((FilterRepresentation) s.mFilterWaterMarkRepresentation.get(index))) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return super.hashCode();
    }
}
