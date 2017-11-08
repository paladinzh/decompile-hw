package com.huawei.gallery.editor.filters.beauty.sfb;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterSfbDeFlash extends SimpleImageFilter {
    public ImageFilterSfbDeFlash() {
        this.mName = "DEFlASH";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 7);
        representation.setSerializationName("DEFlASH");
        representation.setFilterClass(ImageFilterSfbDeFlash.class);
        representation.setTextId(R.string.deflash);
        representation.setOverlayId(R.drawable.ic_gallery_edit_beauty_deflash);
        representation.setOverlayPressedId(R.drawable.ic_gallery_edit_beauty_deflash);
        return representation;
    }
}
