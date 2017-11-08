package com.huawei.gallery.editor.filters;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class FilterRepresentation {
    private Class<?> mFilterClass;
    private boolean mIsBooleanFilter = false;
    private String mName;
    private int mOverlayId = 0;
    private int mOverlayPressedId = 0;
    private int mPriority = 3;
    protected String mReportMsg;
    private String mSerializationName;
    private int mTextId = 0;

    public FilterRepresentation(String name) {
        this.mName = name;
    }

    public FilterRepresentation copy() {
        FilterRepresentation representation = new FilterRepresentation(this.mName);
        representation.useParametersFrom(this);
        return representation;
    }

    protected void copyAllParameters(FilterRepresentation representation) {
        representation.setName(getName());
        representation.setFilterClass(getFilterClass());
        representation.setFilterType(getFilterType());
        representation.setTextId(getTextId());
        representation.setOverlayId(getOverlayId());
        representation.mSerializationName = this.mSerializationName;
        representation.setIsBooleanFilter(isBooleanFilter());
    }

    @SuppressWarnings({"HE_EQUALS_USE_HASHCODE", "EQ_SELF_USE_OBJECT"})
    public boolean equals(FilterRepresentation representation) {
        boolean z = false;
        if (representation == null) {
            return false;
        }
        if (representation.mFilterClass == this.mFilterClass && representation.mName.equalsIgnoreCase(this.mName) && representation.mPriority == this.mPriority && representation.mTextId == this.mTextId && representation.mOverlayId == this.mOverlayId && representation.mOverlayPressedId == this.mOverlayPressedId && representation.mIsBooleanFilter == this.mIsBooleanFilter) {
            z = true;
        }
        return z;
    }

    public boolean isBooleanFilter() {
        return this.mIsBooleanFilter;
    }

    public void setIsBooleanFilter(boolean value) {
        this.mIsBooleanFilter = value;
    }

    public String toString() {
        return this.mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getName() {
        return this.mName;
    }

    public void setSerializationName(String sname) {
        this.mSerializationName = sname;
    }

    public String getSerializationName() {
        return this.mSerializationName;
    }

    public String getReportMsg() {
        return this.mSerializationName;
    }

    public void setFilterType(int priority) {
        this.mPriority = priority;
    }

    public int getFilterType() {
        return this.mPriority;
    }

    public boolean isNil() {
        return false;
    }

    public void useParametersFrom(FilterRepresentation a) {
    }

    public Class<?> getFilterClass() {
        return this.mFilterClass;
    }

    public void setFilterClass(Class<?> filterClass) {
        this.mFilterClass = filterClass;
    }

    public int getTextId() {
        return this.mTextId;
    }

    public void setTextId(int textId) {
        this.mTextId = textId;
    }

    public int getOverlayId() {
        return this.mOverlayId;
    }

    public void setOverlayId(int overlayId) {
        this.mOverlayId = overlayId;
    }

    public int getOverlayPressedId() {
        return this.mOverlayPressedId;
    }

    public void setOverlayPressedId(int overlayPressedId) {
        this.mOverlayPressedId = overlayPressedId;
    }

    public void reset() {
    }
}
