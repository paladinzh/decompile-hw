package com.huawei.gallery.editor.filters.beauty.omron;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterOmronFaceReshape extends SimpleImageFilter {
    public ImageFilterOmronFaceReshape() {
        this.mName = "FaceReshape";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 2);
        representation.setSerializationName("FACE_RESHAPE");
        representation.setFilterClass(ImageFilterOmronFaceReshape.class);
        representation.setTextId(R.string.face_edit_reshape);
        representation.setOverlayId(R.drawable.beauty_lean);
        return representation;
    }
}
