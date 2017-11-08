package com.huawei.gallery.editor.filters.beauty.omron;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterOmronEyeBigger extends SimpleImageFilter {
    public ImageFilterOmronEyeBigger() {
        this.mName = "EyeBigger";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 5);
        representation.setSerializationName("EYE_BIGGER");
        representation.setFilterClass(ImageFilterOmronEyeBigger.class);
        representation.setTextId(R.string.eye_edit_bigger);
        representation.setOverlayId(R.drawable.beauty_eye);
        return representation;
    }
}
