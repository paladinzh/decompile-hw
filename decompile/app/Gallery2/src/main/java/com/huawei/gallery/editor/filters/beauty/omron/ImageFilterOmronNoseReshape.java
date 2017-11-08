package com.huawei.gallery.editor.filters.beauty.omron;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterOmronNoseReshape extends SimpleImageFilter {
    public ImageFilterOmronNoseReshape() {
        this.mName = "NoseReshape";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 3);
        representation.setSerializationName("NOSE_RESHAPE");
        representation.setFilterClass(ImageFilterOmronNoseReshape.class);
        representation.setTextId(R.string.nose_edit_reshape);
        representation.setOverlayId(R.drawable.beauty_nose);
        return representation;
    }
}
