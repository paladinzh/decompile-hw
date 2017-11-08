package com.huawei.gallery.editor.filters.beauty.omron;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterOmronTeethWhiten extends SimpleImageFilter {
    public ImageFilterOmronTeethWhiten() {
        this.mName = "TeethWhiten";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 6);
        representation.setSerializationName("TEETH_WHITEN");
        representation.setFilterClass(ImageFilterOmronTeethWhiten.class);
        representation.setTextId(R.string.teeth_edit_whiten);
        representation.setOverlayId(R.drawable.beauty_teeth);
        return representation;
    }
}
