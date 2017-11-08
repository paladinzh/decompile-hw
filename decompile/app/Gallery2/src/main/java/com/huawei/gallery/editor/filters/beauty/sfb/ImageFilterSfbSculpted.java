package com.huawei.gallery.editor.filters.beauty.sfb;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterSfbSculpted extends SimpleImageFilter {
    public ImageFilterSfbSculpted() {
        this.mName = "SCULPTED";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 13);
        representation.setSerializationName("SCULPTED");
        representation.setFilterClass(ImageFilterSfbSculpted.class);
        representation.setTextId(R.string.dimensional);
        representation.setOverlayId(R.drawable.ic_gallery_edit_beauty_contour);
        representation.setOverlayPressedId(R.drawable.ic_gallery_edit_beauty_contour);
        return representation;
    }
}
