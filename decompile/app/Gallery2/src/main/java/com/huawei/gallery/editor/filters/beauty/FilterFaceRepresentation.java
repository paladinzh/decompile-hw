package com.huawei.gallery.editor.filters.beauty;

import com.huawei.gallery.editor.filters.FilterBasicRepresentation;
import com.huawei.gallery.editor.filters.FilterRepresentation;

public class FilterFaceRepresentation extends FilterBasicRepresentation {
    private int mFaceType = 2;

    public FilterFaceRepresentation(String name, int minimum, int value, int maximum, int type) {
        super(name, minimum, value, maximum);
        setFilterType(5);
        this.mFaceType = type;
    }

    public int getFaceType() {
        return this.mFaceType;
    }

    public FilterRepresentation copy() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(getName(), 0, 0, 0, this.mFaceType);
        copyAllParameters(representation);
        return representation;
    }

    public void useParametersFrom(FilterRepresentation a) {
        super.useParametersFrom(a);
        if (a instanceof FilterFaceRepresentation) {
            this.mFaceType = ((FilterFaceRepresentation) a).mFaceType;
        }
    }

    public boolean equals(FilterRepresentation representation) {
        boolean z = false;
        if (!super.equals(representation) || !(representation instanceof FilterFaceRepresentation)) {
            return false;
        }
        if (this.mFaceType == ((FilterFaceRepresentation) representation).mFaceType) {
            z = true;
        }
        return z;
    }
}
