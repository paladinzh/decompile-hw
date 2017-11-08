package com.huawei.gallery.editor.filters.beauty.omron;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterOmronFaceColor extends SimpleImageFilter {
    public ImageFilterOmronFaceColor() {
        this.mName = "FaceColor";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 1);
        representation.setSerializationName("FACE_COLOR");
        representation.setFilterClass(ImageFilterOmronFaceColor.class);
        representation.setTextId(R.string.face_edit_color);
        representation.setOverlayId(R.drawable.beauty_white);
        return representation;
    }
}
