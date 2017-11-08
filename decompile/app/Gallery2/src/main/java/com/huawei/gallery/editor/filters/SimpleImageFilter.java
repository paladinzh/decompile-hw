package com.huawei.gallery.editor.filters;

public class SimpleImageFilter extends ImageFilter {
    private FilterBasicRepresentation mParameters;

    public void useRepresentation(FilterRepresentation representation) {
        if (representation instanceof FilterBasicRepresentation) {
            this.mParameters = (FilterBasicRepresentation) representation;
        }
    }

    public FilterBasicRepresentation getParameters() {
        return this.mParameters;
    }
}
