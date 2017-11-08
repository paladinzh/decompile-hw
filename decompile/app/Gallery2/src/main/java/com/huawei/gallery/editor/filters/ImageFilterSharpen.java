package com.huawei.gallery.editor.filters;

import android.graphics.Bitmap;
import com.android.gallery3d.R;
import com.fyusion.sdk.common.ext.filter.ImageFilterAbstractFactory;

public class ImageFilterSharpen extends SimpleImageFilter {
    protected native void nativeApplyFilter(Bitmap bitmap, int i, int i2, float f);

    public ImageFilterSharpen() {
        this.mName = "Sharpen";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterBasicRepresentation representation = new FilterBasicRepresentation(this.mName, 0, 0, 100);
        representation.setSerializationName(ImageFilterAbstractFactory.SHARPEN);
        representation.setFilterClass(ImageFilterSharpen.class);
        representation.setTextId(R.string.sharpness);
        representation.setOverlayId(R.drawable.ic_gallery_adjust_sharpness);
        representation.setOverlayPressedId(R.drawable.ic_gallery_adjust_sharpness);
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
