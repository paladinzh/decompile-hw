package com.huawei.gallery.editor.filters.beauty.sfb;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceColorRepresentation;

public class ImageFilterSfbFaceColor extends SimpleImageFilter {
    public ImageFilterSfbFaceColor() {
        this.mName = "FaceColor";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceColorRepresentation representation = new FilterFaceColorRepresentation(this.mName, 0, 0, 100, 14);
        representation.setSerializationName("FACE_COLOR_ADJUST");
        representation.setFilterClass(ImageFilterSfbFaceColor.class);
        representation.setTextId(R.string.complexion);
        representation.setOverlayId(R.drawable.ic_gallery_edit_beauty_skintone);
        representation.setOverlayPressedId(R.drawable.ic_gallery_edit_beauty_skintone);
        return representation;
    }
}
