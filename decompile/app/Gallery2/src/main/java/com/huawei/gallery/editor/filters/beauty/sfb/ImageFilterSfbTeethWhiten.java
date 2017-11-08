package com.huawei.gallery.editor.filters.beauty.sfb;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterSfbTeethWhiten extends SimpleImageFilter {
    public ImageFilterSfbTeethWhiten() {
        this.mName = "TeethWhiten";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 11);
        representation.setSerializationName("TEETH_WHITEN");
        representation.setFilterClass(ImageFilterSfbTeethWhiten.class);
        representation.setTextId(R.string.teeth_edit_whiten);
        representation.setOverlayId(R.drawable.ic_gallery_edit_beauty_white_teeth);
        representation.setOverlayPressedId(R.drawable.ic_gallery_edit_beauty_white_teeth);
        return representation;
    }
}
