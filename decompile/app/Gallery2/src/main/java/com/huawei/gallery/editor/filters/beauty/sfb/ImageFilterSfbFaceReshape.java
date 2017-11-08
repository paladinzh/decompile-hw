package com.huawei.gallery.editor.filters.beauty.sfb;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterSfbFaceReshape extends SimpleImageFilter {
    public ImageFilterSfbFaceReshape() {
        this.mName = "FaceReshape";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 5);
        representation.setSerializationName("FACE_RESHAPE");
        representation.setFilterClass(ImageFilterSfbFaceReshape.class);
        representation.setTextId(R.string.face_edit_reshape);
        representation.setOverlayId(R.drawable.ic_gallery_edit_beauty_thin_face);
        representation.setOverlayPressedId(R.drawable.ic_gallery_edit_beauty_thin_face);
        return representation;
    }
}
