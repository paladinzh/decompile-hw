package com.android.gallery3d.gadget;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import java.lang.reflect.Array;

public class BitmapHelper {
    private static final String TAG = "BitmapHelper";
    private static int[][] arrayPixelsIndex = null;

    public static Bitmap GetCornerBitmap(Bitmap bitmap, Resources res, int templateId) throws Exception {
        Drawable cornerDrawable = res.getDrawable(templateId);
        Bitmap tempateBitmap = Bitmap.createBitmap(cornerDrawable.getIntrinsicWidth(), cornerDrawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(tempateBitmap);
        cornerDrawable.setBounds(0, 0, cornerDrawable.getIntrinsicWidth(), cornerDrawable.getIntrinsicHeight());
        cornerDrawable.draw(canvas);
        Bitmap cloneBitmap = CornerRightBottom(CornerLeftBottom(CornerRightTop(CornerLeftTop(bitmap.copy(Config.ARGB_8888, true), tempateBitmap), tempateBitmap), tempateBitmap), tempateBitmap);
        if (!tempateBitmap.isRecycled()) {
            tempateBitmap.recycle();
        }
        return cloneBitmap;
    }

    private static Bitmap CornerLeftTop(Bitmap cloneBitmap, Bitmap templateBitmap) throws Exception {
        arrayPixelsIndex = getAlphaLeftTop(templateBitmap);
        if (arrayPixelsIndex != null) {
            return setAlphaLeftTop(arrayPixelsIndex, cloneBitmap);
        }
        return cloneBitmap;
    }

    private static Bitmap CornerRightTop(Bitmap cloneBitmap, Bitmap templateBitmap) throws Exception {
        arrayPixelsIndex = getAlphaRightTop(templateBitmap);
        if (arrayPixelsIndex != null) {
            return setAlphaRightTop(arrayPixelsIndex, cloneBitmap);
        }
        return cloneBitmap;
    }

    private static Bitmap CornerLeftBottom(Bitmap cloneBitmap, Bitmap templateBitmap) throws Exception {
        arrayPixelsIndex = getAlphaLeftBottom(templateBitmap);
        if (arrayPixelsIndex != null) {
            return setAlphaLeftBottom(arrayPixelsIndex, cloneBitmap);
        }
        return cloneBitmap;
    }

    private static Bitmap CornerRightBottom(Bitmap cloneBitmap, Bitmap templateBitmap) throws Exception {
        arrayPixelsIndex = getAlphaRightBottom(templateBitmap);
        if (arrayPixelsIndex != null) {
            return setAlphaRightBottom(arrayPixelsIndex, cloneBitmap);
        }
        return cloneBitmap;
    }

    private static Bitmap setAlphaLeftTop(int[][] arrayPixelsIndex, Bitmap bitmap) {
        if (arrayPixelsIndex == null || bitmap == null) {
            throw new NullPointerException("arrayPixelsIndex = " + arrayPixelsIndex + ", bitmap = " + bitmap);
        }
        int width = arrayPixelsIndex.length;
        int height = arrayPixelsIndex[0].length;
        int i = 0;
        while (i < width) {
            for (int j = 0; j < height; j++) {
                if (i < bitmap.getWidth() && i < bitmap.getHeight()) {
                    int color = bitmap.getPixel(i, j);
                    if (arrayPixelsIndex[i][j] < 20) {
                        bitmap.setPixel(i, j, Color.argb(arrayPixelsIndex[i][j], Color.red(color), Color.green(color), Color.blue(color)));
                    }
                }
            }
            i++;
        }
        return bitmap;
    }

    private static int[][] getAlphaLeftTop(Bitmap tempateBitmap) throws Exception {
        if (tempateBitmap == null) {
            throw new NullPointerException("template bitmap is null");
        }
        int width = tempateBitmap.getWidth() >> 1;
        int height = tempateBitmap.getHeight() >> 1;
        int[][] arrayPixelsIndex = (int[][]) Array.newInstance(Integer.TYPE, new int[]{width, height});
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                arrayPixelsIndex[i][j] = Color.alpha(tempateBitmap.getPixel(i, j));
            }
        }
        return arrayPixelsIndex;
    }

    private static Bitmap setAlphaRightTop(int[][] arrayPixelsIndex, Bitmap bitmap) throws NullPointerException {
        if (arrayPixelsIndex == null || bitmap == null) {
            throw new NullPointerException("arrayPixelsIndex = " + arrayPixelsIndex + ", bitmap = " + bitmap);
        }
        int width = arrayPixelsIndex.length;
        int height = arrayPixelsIndex[0].length;
        int startLeft = bitmap.getWidth() - width;
        for (int i = 0; i < width; i++) {
            int j = 0;
            while (j < height) {
                if (startLeft + i < bitmap.getWidth() && j < bitmap.getHeight()) {
                    int color = bitmap.getPixel(startLeft + i, j);
                    if (arrayPixelsIndex[i][j] < 20) {
                        bitmap.setPixel(startLeft + i, j, Color.argb(arrayPixelsIndex[i][j], Color.red(color), Color.green(color), Color.blue(color)));
                    }
                }
                j++;
            }
        }
        return bitmap;
    }

    private static int[][] getAlphaRightTop(Bitmap tempateBitmap) throws Exception {
        if (tempateBitmap == null) {
            throw new NullPointerException("template bitmap is null");
        }
        int width = (tempateBitmap.getWidth() + 1) >> 1;
        int height = tempateBitmap.getHeight() >> 1;
        int maxWidth = tempateBitmap.getWidth();
        int[][] arrayPixelsIndex = (int[][]) Array.newInstance(Integer.TYPE, new int[]{width, height});
        int startPositionX = maxWidth - width;
        for (int i = startPositionX; i < maxWidth; i++) {
            for (int j = 0; j < height; j++) {
                arrayPixelsIndex[i - startPositionX][j] = Color.alpha(tempateBitmap.getPixel(i, j));
            }
        }
        return arrayPixelsIndex;
    }

    private static Bitmap setAlphaRightBottom(int[][] arrayPixelsIndex, Bitmap bitmap) throws NullPointerException {
        if (arrayPixelsIndex == null || bitmap == null) {
            throw new NullPointerException("arrayPixelsIndex = " + arrayPixelsIndex + ", bitmap = " + bitmap);
        }
        int width = arrayPixelsIndex.length;
        int height = arrayPixelsIndex[0].length;
        int startLeft = bitmap.getWidth() - width;
        int startBottom = bitmap.getHeight() - height;
        for (int i = 0; i < width; i++) {
            int j = 0;
            while (j < height) {
                if (startLeft + i < bitmap.getWidth() && startBottom + j < bitmap.getHeight()) {
                    int color = bitmap.getPixel(startLeft + i, startBottom + j);
                    if (arrayPixelsIndex[i][j] < 20) {
                        bitmap.setPixel(startLeft + i, startBottom + j, Color.argb(arrayPixelsIndex[i][j], Color.red(color), Color.green(color), Color.blue(color)));
                    }
                }
                j++;
            }
        }
        return bitmap;
    }

    private static int[][] getAlphaRightBottom(Bitmap tempateBitmap) throws Exception {
        if (tempateBitmap == null) {
            throw new NullPointerException("template bitmap is null");
        }
        int width = (tempateBitmap.getWidth() + 1) >> 1;
        int height = (tempateBitmap.getHeight() + 1) >> 1;
        int maxHeight = tempateBitmap.getHeight();
        int maxWidth = tempateBitmap.getWidth();
        int[][] arrayPixelsIndex = (int[][]) Array.newInstance(Integer.TYPE, new int[]{width, height});
        int startPositionX = maxWidth - width;
        int startPositionY = maxHeight - height;
        for (int i = startPositionX; i < maxWidth; i++) {
            for (int j = startPositionY; j < maxHeight; j++) {
                arrayPixelsIndex[i - startPositionX][j - startPositionY] = Color.alpha(tempateBitmap.getPixel(i, j));
            }
        }
        return arrayPixelsIndex;
    }

    private static Bitmap setAlphaLeftBottom(int[][] arrayPixelsIndex, Bitmap bitmap) throws NullPointerException {
        if (arrayPixelsIndex == null || bitmap == null) {
            throw new NullPointerException("arrayPixelsIndex = " + arrayPixelsIndex + ", bitmap = " + bitmap);
        }
        int width = arrayPixelsIndex.length;
        int height = arrayPixelsIndex[0].length;
        int startBottom = bitmap.getHeight() - height;
        for (int i = 0; i < width; i++) {
            int j = 0;
            while (j < height) {
                if (i < bitmap.getWidth() && startBottom + j < bitmap.getHeight()) {
                    int color = bitmap.getPixel(i, startBottom + j);
                    if (arrayPixelsIndex[i][j] < 20) {
                        bitmap.setPixel(i, startBottom + j, Color.argb(arrayPixelsIndex[i][j], Color.red(color), Color.green(color), Color.blue(color)));
                    }
                }
                j++;
            }
        }
        return bitmap;
    }

    private static int[][] getAlphaLeftBottom(Bitmap tempateBitmap) throws Exception {
        if (tempateBitmap == null) {
            throw new NullPointerException("template bitmap is null");
        }
        int width = tempateBitmap.getWidth() >> 1;
        int height = (tempateBitmap.getHeight() + 1) >> 1;
        int maxHeight = tempateBitmap.getHeight();
        int[][] arrayPixelsIndex = (int[][]) Array.newInstance(Integer.TYPE, new int[]{width, height});
        int startPositionY = maxHeight - height;
        for (int i = 0; i < width; i++) {
            for (int j = startPositionY; j < maxHeight; j++) {
                arrayPixelsIndex[i][j - startPositionY] = Color.alpha(tempateBitmap.getPixel(i, j));
            }
        }
        return arrayPixelsIndex;
    }
}
