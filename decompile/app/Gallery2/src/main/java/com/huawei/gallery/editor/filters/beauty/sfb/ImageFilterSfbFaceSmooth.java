package com.huawei.gallery.editor.filters.beauty.sfb;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterSfbFaceSmooth extends SimpleImageFilter {
    public ImageFilterSfbFaceSmooth() {
        this.mName = "FaceSmooth";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 2);
        representation.setSerializationName("FACE_SMOOTH");
        representation.setFilterClass(ImageFilterSfbFaceSmooth.class);
        representation.setTextId(R.string.face_edit_smooth);
        representation.setOverlayId(R.drawable.ic_gallery_edit_beauty_smooth);
        representation.setOverlayPressedId(R.drawable.ic_gallery_edit_beauty_smooth);
        return representation;
    }
}
