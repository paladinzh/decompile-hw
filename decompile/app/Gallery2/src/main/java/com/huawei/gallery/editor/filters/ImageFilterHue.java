package com.huawei.gallery.editor.filters;

import android.graphics.Bitmap;
import com.android.gallery3d.R;
import com.huawei.watermark.manager.parse.WMElement;

public class ImageFilterHue extends SimpleImageFilter {
    private static final float[] sMatrixLabToRgb = new float[]{WMElement.CAMERASIZEVALUE1B1, 2.09337f, 0.8695f, 0.0f, WMElement.CAMERASIZEVALUE1B1, -0.62592f, -0.07239f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.036092f, -1.84355f, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1};
    private static final float[] sMatrixRgbToLab = new float[]{0.2126f, 0.71519f, 0.0722f, 0.0f, 0.3259f, -0.49926f, 0.17334f, 0.0f, 0.12181f, 0.37856f, -0.50037f, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1};
    private static final float[] sMatrixTranslate = new float[]{WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1};

    protected native void nativeApplyFilter(Bitmap bitmap, int i, int i2, float[] fArr);

    public ImageFilterHue() {
        this.mName = "Hue";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterBasicRepresentation representation = new FilterBasicRepresentation(this.mName, -180, 0, 180);
        representation.setSerializationName("HUE");
        representation.setFilterClass(ImageFilterHue.class);
        representation.setTextId(R.string.hue);
        representation.setOverlayId(R.drawable.ic_gallery_adjust_temperature);
        representation.setOverlayPressedId(R.drawable.ic_gallery_adjust_temperature);
        representation.setFilterType(8);
        return representation;
    }

    private float[] multiplyMM(float[] leftMatrix, float[] rightMatrix) {
        float[] temp = (float[]) sMatrixTranslate.clone();
        if (temp.length != leftMatrix.length || leftMatrix.length != rightMatrix.length || temp.length != 16) {
            return temp;
        }
        for (int i = 0; i < 16; i++) {
            int row = i / 4;
            int colume = i % 4;
            temp[i] = (((leftMatrix[row * 4] * rightMatrix[colume]) + (leftMatrix[(row * 4) + 1] * rightMatrix[colume + 4])) + (leftMatrix[(row * 4) + 2] * rightMatrix[colume + 8])) + (leftMatrix[(row * 4) + 3] * rightMatrix[colume + 12]);
        }
        return temp;
    }

    public Bitmap apply(Bitmap bitmap) {
        if (getParameters() == null) {
            return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float value = (float) getParameters().getValue();
        float[] matrix = (float[]) sMatrixTranslate.clone();
        matrix[7] = (-value) / 15.0f;
        matrix[11] = (-value) / 15.0f;
        nativeApplyFilter(bitmap, w, h, multiplyMM(multiplyMM(sMatrixLabToRgb, matrix), sMatrixRgbToLab));
        return bitmap;
    }
}
