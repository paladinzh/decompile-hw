package com.huawei.gallery.editor.filters.beauty.sfb;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterSfbBeauty extends SimpleImageFilter {
    public ImageFilterSfbBeauty() {
        this.mName = "BEAUTY";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 1);
        representation.setSerializationName("BEAUTY");
        representation.setFilterClass(ImageFilterSfbBeauty.class);
        representation.setTextId(R.string.beauty);
        representation.setOverlayId(R.drawable.ic_gallery_edit_beauty_onekey);
        representation.setOverlayPressedId(R.drawable.ic_gallery_edit_beauty_onekey);
        return representation;
    }
}
