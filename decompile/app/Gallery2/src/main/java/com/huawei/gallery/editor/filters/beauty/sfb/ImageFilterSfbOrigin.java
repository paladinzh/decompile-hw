package com.huawei.gallery.editor.filters.beauty.sfb;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterSfbOrigin extends SimpleImageFilter {
    public ImageFilterSfbOrigin() {
        this.mName = "ORIGIN";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 0);
        representation.setSerializationName("ORIGIN");
        representation.setFilterClass(ImageFilterSfbOrigin.class);
        representation.setTextId(R.string.original);
        representation.setOverlayId(R.drawable.ic_gallery_edit_beauty_origin);
        representation.setOverlayPressedId(R.drawable.ic_gallery_edit_beauty_origin);
        return representation;
    }
}
