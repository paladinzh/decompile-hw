package com.huawei.gallery.editor.filters.fx;

import com.huawei.gallery.editor.filters.FilterRepresentation;

public class FilterGoogleFxRepresentation extends FilterFxRepresentation {
    private int mBitmapResource = 0;

    public FilterGoogleFxRepresentation(String name, int nameResource, int bitmapResource) {
        super(name, nameResource);
        setFilterClass(ImageFilterGoogleFx.class);
        this.mBitmapResource = bitmapResource;
    }

    public String toString() {
        return "FilterFx: " + hashCode() + " : " + getName() + " bitmap rsc: " + this.mBitmapResource;
    }

    public FilterRepresentation copy() {
        FilterGoogleFxRepresentation representation = new FilterGoogleFxRepresentation(getName(), 0, 0);
        copyAllParameters(representation);
        return representation;
    }

    public synchronized void useParametersFrom(FilterRepresentation a) {
        super.useParametersFrom(a);
        if (a instanceof FilterGoogleFxRepresentation) {
            setBitmapResource(((FilterGoogleFxRepresentation) a).getBitmapResource());
        }
    }

    public boolean equals(FilterRepresentation representation) {
        if (super.equals(representation) && (representation instanceof FilterGoogleFxRepresentation) && ((FilterGoogleFxRepresentation) representation).mBitmapResource == this.mBitmapResource) {
            return true;
        }
        return false;
    }

    public int getBitmapResource() {
        return this.mBitmapResource;
    }

    private void setBitmapResource(int bitmapResource) {
        this.mBitmapResource = bitmapResource;
    }
}
