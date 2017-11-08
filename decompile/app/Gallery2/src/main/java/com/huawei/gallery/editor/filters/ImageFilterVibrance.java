package com.huawei.gallery.editor.filters;

import android.graphics.Bitmap;
import com.android.gallery3d.R;

public class ImageFilterVibrance extends SimpleImageFilter {
    protected native void nativeApplyFilter(Bitmap bitmap, int i, int i2, float f);

    public ImageFilterVibrance() {
        this.mName = "Vibrance";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterBasicRepresentation representation = new FilterBasicRepresentation(this.mName, -100, 0, 100);
        representation.setSerializationName("VIBRANCE");
        representation.setFilterClass(ImageFilterVibrance.class);
        representation.setTextId(R.string.saturation);
        representation.setOverlayId(R.drawable.ic_gallery_adjust_vibrance);
        representation.setOverlayPressedId(R.drawable.ic_gallery_adjust_vibrance);
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
