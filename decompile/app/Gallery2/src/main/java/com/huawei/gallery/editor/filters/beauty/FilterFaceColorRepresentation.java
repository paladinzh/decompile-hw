package com.huawei.gallery.editor.filters.beauty;

import com.huawei.gallery.editor.filters.FilterRepresentation;

public class FilterFaceColorRepresentation extends FilterFaceRepresentation {
    private int[] mFaceColorValue = new int[2];
    private int mFaceType = 14;

    public FilterFaceColorRepresentation(String name, int minimum, int value, int maximum, int type) {
        super(name, minimum, value, maximum, type);
        setFilterType(5);
        this.mFaceType = type;
    }

    public int getFaceType() {
        return this.mFaceType;
    }

    public int getValue(int i) {
        return this.mFaceColorValue[i];
    }

    public int[] getValues() {
        return (int[]) this.mFaceColorValue.clone();
    }

    public void setValue(int index, int value) {
        if (index >= 0 && index <= 2) {
            this.mFaceColorValue[index] = value;
            if (this.mFaceColorValue[index] < getMinimum()) {
                this.mFaceColorValue[index] = getMinimum();
            }
            if (this.mFaceColorValue[index] > getMaximum()) {
                this.mFaceColorValue[index] = getMaximum();
            }
        }
    }

    public FilterRepresentation copy() {
        FilterFaceColorRepresentation representation = new FilterFaceColorRepresentation(getName(), 0, 0, 0, this.mFaceType);
        copyAllParameters(representation);
        return representation;
    }

    public void useParametersFrom(FilterRepresentation a) {
        super.useParametersFrom(a);
        if (a instanceof FilterFaceColorRepresentation) {
            FilterFaceColorRepresentation representation = (FilterFaceColorRepresentation) a;
            setMinimum(representation.getMinimum());
            setMaximum(representation.getMaximum());
            setValue(0, representation.getValue(0));
            setValue(1, representation.getValue(1));
            setDefaultValue(representation.getDefaultValue());
            this.mFaceType = representation.mFaceType;
        }
    }

    public boolean isNil() {
        return getValue(0) == getDefaultValue() && getValue(1) == getDefaultValue();
    }

    public void reset() {
        setValue(0, getDefaultValue());
        setValue(1, getDefaultValue());
    }
}
