package com.huawei.gallery.editor.filters;

import com.huawei.gallery.editor.pipeline.ImagePreset;

public class FilterUserPresetRepresentation extends FilterRepresentation {
    private int mId;
    private ImagePreset mPreset;

    public FilterUserPresetRepresentation(String name, ImagePreset preset, int id) {
        super(name);
        setFilterType(2);
        this.mPreset = preset;
        this.mId = id;
    }

    public FilterRepresentation copy() {
        FilterRepresentation rep = new FilterUserPresetRepresentation(getName(), new ImagePreset(this.mPreset), this.mId);
        rep.setSerializationName(getSerializationName());
        return rep;
    }
}
