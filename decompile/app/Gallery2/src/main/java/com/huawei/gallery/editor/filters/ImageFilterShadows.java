package com.huawei.gallery.editor.filters;

import android.graphics.Bitmap;
import com.android.gallery3d.R;
import com.fyusion.sdk.common.ext.filter.ImageFilterAbstractFactory;

public class ImageFilterShadows extends SimpleImageFilter {
    protected native void nativeApplyFilter(Bitmap bitmap, int i, int i2, float f);

    public ImageFilterShadows() {
        this.mName = "Shadows";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterBasicRepresentation representation = new FilterBasicRepresentation(this.mName, -100, 0, 100);
        representation.setSerializationName(ImageFilterAbstractFactory.SHADOWS);
        representation.setFilterClass(ImageFilterShadows.class);
        representation.setTextId(R.string.shadow_recovery);
        representation.setOverlayId(R.drawable.ic_gallery_adjust_shadows);
        representation.setOverlayPressedId(R.drawable.ic_gallery_adjust_shadows);
        representation.setFilterType(8);
        return representation;
    }

    public Bitmap apply(Bitmap bitmap) {
        if (getParameters() == null) {
            return bitmap;
        }
        nativeApplyFilter(bitmap, bitmap.getWidth(), bitmap.getHeight(), (float) getParameters().getValue());
        return bitmap;
    }
}
