package com.huawei.gallery.editor.filters;

import android.graphics.Bitmap;
import com.android.gallery3d.R;
import com.fyusion.sdk.common.ext.filter.ImageFilterAbstractFactory;

public class ImageFilterExposure extends SimpleImageFilter {
    protected native void nativeApplyFilter(Bitmap bitmap, int i, int i2, int i3);

    public ImageFilterExposure() {
        this.mName = "Exposure";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterBasicRepresentation representation = new FilterBasicRepresentation(this.mName, -100, 0, 100);
        representation.setSerializationName(ImageFilterAbstractFactory.EXPOSURE);
        representation.setFilterClass(ImageFilterExposure.class);
        representation.setTextId(R.string.editor_grad_brightness);
        representation.setOverlayId(R.drawable.ic_gallery_adjust_exposure);
        representation.setOverlayPressedId(R.drawable.ic_gallery_adjust_exposure);
        representation.setFilterType(8);
        return representation;
    }

    public Bitmap apply(Bitmap bitmap) {
        if (getParameters() == null) {
            return bitmap;
        }
        nativeApplyFilter(bitmap, bitmap.getWidth(), bitmap.getHeight(), getParameters().getValue());
        return bitmap;
    }
}
