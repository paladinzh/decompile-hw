package com.huawei.gallery.editor.filters.fx;

import android.util.SparseIntArray;
import com.huawei.gallery.editor.filters.FilterRepresentation;

public class FilterChangableFxRepresentation extends FilterFxRepresentation {
    protected FilterChangableParameter mParameter = new FilterChangableParameter();
    protected SparseIntArray mSeekBarItems = new SparseIntArray();

    public FilterChangableFxRepresentation(String name, int nameResource) {
        super(name, nameResource);
    }

    public String toString() {
        return "ImageFilterFxExtends: " + hashCode() + " : " + getName();
    }

    public FilterRepresentation copy() {
        FilterChangableFxRepresentation representation = new FilterChangableFxRepresentation(getName(), 0);
        copyAllParameters(representation);
        return representation;
    }

    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    public synchronized void useParametersFrom(FilterRepresentation a) {
        super.useParametersFrom(a);
        if (a instanceof FilterChangableFxRepresentation) {
            setParameter(((FilterChangableFxRepresentation) a).getParameter());
        }
    }

    public boolean equals(FilterRepresentation representation) {
        if (!super.equals(representation) || !(representation instanceof FilterChangableFxRepresentation)) {
            return false;
        }
        FilterChangableFxRepresentation fx = (FilterChangableFxRepresentation) representation;
        if (!this.mParameter.equals(fx.getParameter()) || this.mSeekBarItems.size() != fx.mSeekBarItems.size()) {
            return false;
        }
        int i = 0;
        while (i < this.mSeekBarItems.size()) {
            if (this.mSeekBarItems.keyAt(i) != fx.mSeekBarItems.keyAt(i) || this.mSeekBarItems.get(this.mSeekBarItems.keyAt(i)) != fx.mSeekBarItems.get(fx.mSeekBarItems.keyAt(i))) {
                return false;
            }
            i++;
        }
        return true;
    }

    public void reset() {
        this.mParameter.reset();
        super.reset();
    }

    public FilterChangableParameter getParameter() {
        return this.mParameter;
    }

    public void setParameter(FilterChangableParameter parameter) {
        this.mParameter.setParameter(parameter);
    }

    public SparseIntArray getSeekBarItems() {
        return this.mSeekBarItems;
    }
}
