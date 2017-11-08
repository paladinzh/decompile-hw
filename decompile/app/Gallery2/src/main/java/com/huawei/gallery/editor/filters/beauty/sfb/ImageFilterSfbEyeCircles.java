package com.huawei.gallery.editor.filters.beauty.sfb;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.SimpleImageFilter;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;

public class ImageFilterSfbEyeCircles extends SimpleImageFilter {
    public ImageFilterSfbEyeCircles() {
        this.mName = "EYECIRCLES";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterFaceRepresentation representation = new FilterFaceRepresentation(this.mName, 0, 0, 100, 8);
        representation.setSerializationName("EYECIRCLES");
        representation.setFilterClass(ImageFilterSfbEyeCircles.class);
        representation.setTextId(R.string.eye_circles);
        representation.setOverlayId(R.drawable.ic_gallery_edit_beauty_eye_circles);
        representation.setOverlayPressedId(R.drawable.ic_gallery_edit_beauty_eye_circles);
        return representation;
    }
}
