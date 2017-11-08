package com.huawei.keyguard.view.effect.bokeh;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;

public class BokehHelper {
    private static final int[] EMPTY_INTS = new int[0];
    private static float brightnessThreshold = 0.6f;

    public static int[] getBokehPositionList(Bitmap bitmap) {
        ArrayList<Integer> list = new ArrayList();
        if (bitmap == null) {
            return EMPTY_INTS;
        }
        int i;
        int bmpWidth = bitmap.getWidth();
        int bmpHeight = bitmap.getHeight();
        int[] pixels = new int[(bmpWidth * bmpHeight)];
        bitmap.getPixels(pixels, 0, bmpWidth, 0, 0, bmpWidth, bmpHeight);
        float[] hsv = new float[3];
        int count = 0;
        int countOfSamples = 0;
        for (i = 0; i < bmpHeight; i += 15) {
            int index = i * bmpWidth;
            for (int j = 0; j < bmpWidth; j += 15) {
                Color.colorToHSV(pixels[index], hsv);
                if (hsv[2] > brightnessThreshold) {
                    list.add(Integer.valueOf(index));
                    count++;
                }
                countOfSamples++;
                index += 15;
            }
        }
        Collections.shuffle(list);
        if (((float) count) / ((float) countOfSamples) > 0.75f) {
            Log.i("No Bokeh", "getBokehPositionList: no bokeh");
            return EMPTY_INTS;
        }
        int[] rst;
        if (list.size() >= 10) {
            rst = new int[10];
            for (i = 0; i < 10; i++) {
                rst[i] = ((Integer) list.get(i)).intValue();
            }
        } else {
            rst = new int[list.size()];
            for (i = 0; i < list.size(); i++) {
                rst[i] = ((Integer) list.get(i)).intValue();
            }
        }
        return rst;
    }

    public static ColorMatrixColorFilter createColorMatrix(int color) {
        float red = ((float) Color.red(color)) / 255.0f;
        float green = ((float) Color.green(color)) / 255.0f;
        float blue = ((float) Color.blue(color)) / 255.0f;
        float[] colorTransform = new float[]{red, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, green, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, blue, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(colorTransform);
        return new ColorMatrixColorFilter(colorMatrix);
    }
}
