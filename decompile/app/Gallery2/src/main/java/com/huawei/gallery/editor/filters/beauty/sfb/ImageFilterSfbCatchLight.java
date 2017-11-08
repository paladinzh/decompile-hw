package com.huawei.gallery.editor.filters.beauty.sfb;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterSfbCatchLight extends SimpleImageFilter {
    public ImageFilterSfbCatchLight() {
        this.mName = "CatchLight";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 10);
        representation.setSerializationName("CatchLight");
        representation.setFilterClass(ImageFilterSfbCatchLight.class);
        representation.setTextId(R.string.catchlight);
        representation.setOverlayId(R.drawable.ic_gallery_edit_beauty_catchlight);
        representation.setOverlayPressedId(R.drawable.ic_gallery_edit_beauty_catchlight);
        return representation;
    }
}
