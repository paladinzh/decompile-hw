package com.huawei.gallery.editor.filters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import com.android.gallery3d.common.BitmapUtils;
import com.huawei.gallery.editor.filters.FilterWaterMarkRepresentation.WaterMarkHolder;
import com.huawei.watermark.manager.parse.WMElement;

public class ImageFilterWaterMark extends ImageFilter {
    private FilterWaterMarkRepresentation mParameters;

    public void useRepresentation(FilterRepresentation representation) {
        if (representation instanceof FilterWaterMarkRepresentation) {
            this.mParameters = (FilterWaterMarkRepresentation) representation;
        }
    }

    public Bitmap apply(Bitmap bitmap) {
        FilterWaterMarkRepresentation representation = this.mParameters;
        if (representation == null) {
            return bitmap;
        }
        WaterMarkHolder waterMarkHolder = representation.getWaterMarkHolder();
        if (waterMarkHolder.isNil()) {
            return bitmap;
        }
        float scale = Math.min(waterMarkHolder.width / ((float) bitmap.getWidth()), waterMarkHolder.height / ((float) bitmap.getHeight()));
        if (scale > WMElement.CAMERASIZEVALUE1B1) {
            bitmap = BitmapUtils.resizeBitmapByScale(bitmap, scale, true);
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float left = waterMarkHolder.left * ((float) width);
        float top = waterMarkHolder.top * ((float) height);
        Matrix matrix = new Matrix();
        matrix.setTranslate(left, top);
        matrix.preScale(((float) width) / waterMarkHolder.width, ((float) height) / waterMarkHolder.height);
        new Canvas(bitmap).drawBitmap(waterMarkHolder.waterMarkBitmap, matrix, null);
        return bitmap;
    }
}
