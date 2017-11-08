package com.huawei.gallery.editor.filters.fx;

import com.huawei.gallery.editor.filters.FilterRepresentation;

public class FilterFxRepresentation extends FilterRepresentation {
    private int mNameResource = 0;

    public FilterFxRepresentation(String name, int nameResource) {
        super(name);
        this.mNameResource = nameResource;
        setFilterType(2);
        setTextId(nameResource);
    }

    public FilterRepresentation copy() {
        FilterFxRepresentation representation = new FilterFxRepresentation(getName(), 0);
        copyAllParameters(representation);
        return representation;
    }

    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    public synchronized void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterFxRepresentation) {
            FilterFxRepresentation representation = (FilterFxRepresentation) a;
            setName(representation.getName());
            setSerializationName(representation.getSerializationName());
            setNameResource(representation.getNameResource());
        }
    }

    public boolean equals(FilterRepresentation representation) {
        if (super.equals(representation) && (representation instanceof FilterFxRepresentation) && ((FilterFxRepresentation) representation).mNameResource == this.mNameResource) {
            return true;
        }
        return false;
    }

    public int getNameResource() {
        return this.mNameResource;
    }

    public void setNameResource(int nameResource) {
        this.mNameResource = nameResource;
    }
}
