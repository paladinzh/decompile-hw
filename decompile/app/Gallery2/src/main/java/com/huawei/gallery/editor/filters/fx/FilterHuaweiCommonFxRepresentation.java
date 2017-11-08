package com.huawei.gallery.editor.filters.fx;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;

public class FilterHuaweiCommonFxRepresentation extends FilterChangableFxRepresentation {
    private int mFilterId;

    public FilterHuaweiCommonFxRepresentation(String name, int nameResource, int filterId) {
        super(name, nameResource);
        setFilterClass(ImageFilterHuaweiCommonFx.class);
        this.mFilterId = filterId;
        initSeekBarItems();
    }

    public String toString() {
        return "FilterFx: " + hashCode() + " : " + getName() + " filter id: " + this.mFilterId;
    }

    public FilterRepresentation copy() {
        FilterHuaweiCommonFxRepresentation representation = new FilterHuaweiCommonFxRepresentation(getName(), 0, 0);
        copyAllParameters(representation);
        return representation;
    }

    public synchronized void useParametersFrom(FilterRepresentation a) {
        super.useParametersFrom(a);
        if (a instanceof FilterHuaweiCommonFxRepresentation) {
            setFilterId(((FilterHuaweiCommonFxRepresentation) a).getFilterId());
        }
    }

    public boolean equals(FilterRepresentation representation) {
        if (super.equals(representation) && (representation instanceof FilterHuaweiCommonFxRepresentation) && ((FilterHuaweiCommonFxRepresentation) representation).mFilterId == this.mFilterId) {
            return true;
        }
        return false;
    }

    public int getFilterId() {
        return this.mFilterId;
    }

    private void setFilterId(int filterId) {
        this.mFilterId = filterId;
    }

    private void initSeekBarItems() {
        if (this.mSeekBarItems != null) {
            this.mSeekBarItems.clear();
            this.mSeekBarItems.put(0, R.string.strength);
        }
    }
}
