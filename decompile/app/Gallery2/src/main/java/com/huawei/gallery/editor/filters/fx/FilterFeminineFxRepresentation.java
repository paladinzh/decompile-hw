package com.huawei.gallery.editor.filters.fx;

import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.ImageFilterFxFeminine;

public class FilterFeminineFxRepresentation extends FilterFxRepresentation {
    private String mEffectName;

    public FilterFeminineFxRepresentation(String name, String effectName, int nameResource) {
        super(name, nameResource);
        setFilterClass(ImageFilterFxFeminine.class);
        this.mEffectName = effectName;
    }

    public String toString() {
        return "ImageFilterFxExtends: " + hashCode() + " : " + getName() + " effect name rsc: " + this.mEffectName;
    }

    public FilterRepresentation copy() {
        FilterFeminineFxRepresentation representation = new FilterFeminineFxRepresentation(getName(), null, 0);
        copyAllParameters(representation);
        return representation;
    }

    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    public synchronized void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterFeminineFxRepresentation) {
            FilterFeminineFxRepresentation representation = (FilterFeminineFxRepresentation) a;
            setName(representation.getName());
            setSerializationName(representation.getSerializationName());
            setEffectName(representation.getEffectName());
            setNameResource(representation.getNameResource());
        }
    }

    public boolean equals(FilterRepresentation representation) {
        if (super.equals(representation) && (representation instanceof FilterFeminineFxRepresentation)) {
            FilterFeminineFxRepresentation fx = (FilterFeminineFxRepresentation) representation;
            if (fx.getNameResource() == getNameResource() && this.mEffectName.equalsIgnoreCase(fx.mEffectName)) {
                return true;
            }
        }
        return false;
    }

    public String getEffectName() {
        return this.mEffectName;
    }

    public void setEffectName(String effectName) {
        this.mEffectName = effectName;
    }
}
