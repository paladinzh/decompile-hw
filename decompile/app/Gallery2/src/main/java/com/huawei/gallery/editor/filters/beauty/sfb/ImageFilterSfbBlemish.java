package com.huawei.gallery.editor.filters.beauty.sfb;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterSfbBlemish extends SimpleImageFilter {
    public ImageFilterSfbBlemish() {
        this.mName = "BLEMISH";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 4);
        representation.setSerializationName("BLEMISH");
        representation.setFilterClass(ImageFilterSfbBlemish.class);
        representation.setTextId(R.string.blemish);
        representation.setOverlayId(R.drawable.ic_gallery_edit_beauty_blemish);
        representation.setOverlayPressedId(R.drawable.ic_gallery_edit_beauty_blemish);
        return representation;
    }
}
