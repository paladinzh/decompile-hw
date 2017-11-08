package com.huawei.gallery.editor.filters.beauty.omron;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterOmronFaceSmooth extends SimpleImageFilter {
    public ImageFilterOmronFaceSmooth() {
        this.mName = "FaceSmooth";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 0);
        representation.setSerializationName("FACE_SMOOTH");
        representation.setFilterClass(ImageFilterOmronFaceSmooth.class);
        representation.setTextId(R.string.face_edit_smooth);
        representation.setOverlayId(R.drawable.beauty_polish);
        return representation;
    }
}
