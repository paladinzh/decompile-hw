package com.huawei.gallery.editor.filters;

public class FilterBasicRepresentation extends FilterRepresentation {
    private int mDefaultValue;
    private int mMaximum;
    private int mMinimum;
    private int mValue;

    public FilterBasicRepresentation(String name, int minimum, int defaultValue, int maximum) {
        super(name);
        this.mMinimum = minimum;
        this.mMaximum = maximum;
        setValue(defaultValue);
        setDefaultValue(defaultValue);
    }

    public void reset() {
        setValue(getDefaultValue());
    }

    public String toString() {
        return getName() + " : " + this.mMinimum + " < " + this.mValue + " < " + this.mMaximum;
    }

    public FilterRepresentation copy() {
        FilterBasicRepresentation representation = new FilterBasicRepresentation(getName(), 0, 0, 0);
        copyAllParameters(representation);
        return representation;
    }

    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    public boolean isNil() {
        return getValue() == getDefaultValue();
    }

    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterBasicRepresentation) {
            FilterBasicRepresentation representation = (FilterBasicRepresentation) a;
            setMinimum(representation.getMinimum());
            setMaximum(representation.getMaximum());
            setValue(representation.getValue());
            setDefaultValue(representation.getDefaultValue());
        }
    }

    public boolean equals(FilterRepresentation representation) {
        if (super.equals(representation) && (representation instanceof FilterBasicRepresentation)) {
            FilterBasicRepresentation basic = (FilterBasicRepresentation) representation;
            if (basic.mMinimum == this.mMinimum && basic.mMaximum == this.mMaximum && basic.mValue == this.mValue && basic.mDefaultValue == this.mDefaultValue) {
                return true;
            }
        }
        return false;
    }

    public int getMinimum() {
        return this.mMinimum;
    }

    public void setMinimum(int minimum) {
        this.mMinimum = minimum;
    }

    public int getValue() {
        return this.mValue;
    }

    public void setValue(int value) {
        this.mValue = value;
        if (this.mValue < this.mMinimum) {
            this.mValue = this.mMinimum;
        }
        if (this.mValue > this.mMaximum) {
            this.mValue = this.mMaximum;
        }
    }

    public int getMaximum() {
        return this.mMaximum;
    }

    public void setMaximum(int maximum) {
        this.mMaximum = maximum;
    }

    public void setDefaultValue(int defaultValue) {
        this.mDefaultValue = defaultValue;
    }

    public int getDefaultValue() {
        return this.mDefaultValue;
    }
}
