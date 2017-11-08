package com.huawei.gallery.editor.filters.fx;

import com.huawei.gallery.editor.filters.FilterRepresentation;

public class FilterHuaweiMistFxRepresentation extends FilterFxRepresentation {
    public FilterHuaweiMistFxRepresentation(String name, int nameResource) {
        super(name, nameResource);
        setFilterClass(ImageFilterHuaweiMistFx.class);
    }

    public FilterRepresentation copy() {
        FilterHuaweiMistFxRepresentation representation = new FilterHuaweiMistFxRepresentation(getName(), 0);
        copyAllParameters(representation);
        return representation;
    }
}
