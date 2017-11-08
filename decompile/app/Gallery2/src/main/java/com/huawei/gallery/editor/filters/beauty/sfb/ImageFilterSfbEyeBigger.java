package com.huawei.gallery.editor.filters.beauty.sfb;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterSfbEyeBigger extends SimpleImageFilter {
    public ImageFilterSfbEyeBigger() {
        this.mName = "EyeBigger";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 9);
        representation.setSerializationName("EYE_BIGGER");
        representation.setFilterClass(ImageFilterSfbEyeBigger.class);
        representation.setTextId(R.string.eye_edit_bigger);
        representation.setOverlayId(R.drawable.ic_gallery_edit_beauty_enhance_eyes);
        representation.setOverlayPressedId(R.drawable.ic_gallery_edit_beauty_enhance_eyes);
        return representation;
    }
}
