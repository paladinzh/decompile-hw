package com.huawei.gallery.editor.filters;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.android.gallery3d.R;
import com.huawei.watermark.manager.parse.WMElement;

public class ImageFilterBwFilter extends SimpleImageFilter {
    protected native void nativeApplyFilter(Bitmap bitmap, int i, int i2, int i3, int i4, int i5);

    public ImageFilterBwFilter() {
        this.mName = "BW Filter";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterBasicRepresentation representation = new FilterBasicRepresentation(this.mName, -180, 0, 180);
        representation.setSerializationName("BWFILTER");
        representation.setFilterClass(ImageFilterBwFilter.class);
        representation.setOverlayId(R.drawable.ic_gallery_adjust_bw);
        representation.setOverlayPressedId(R.drawable.ic_gallery_adjust_bw);
        representation.setTextId(R.string.bwfilter);
        representation.setFilterType(8);
        return representation;
    }

    public Bitmap apply(Bitmap bitmap) {
        if (getParameters() == null) {
            return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int rgb = Color.HSVToColor(new float[]{(float) (getParameters().getValue() + 180), WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1});
        nativeApplyFilter(bitmap, w, h, (rgb >> 16) & 255, (rgb >> 8) & 255, (rgb >> 0) & 255);
        return bitmap;
    }
}
