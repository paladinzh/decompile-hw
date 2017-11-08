package com.android.gallery3d.util;

import android.graphics.Bitmap;

public class WallpaperUtils {
    private static byte getB(int color) {
        return (byte) ((color >> 16) & 255);
    }

    private static byte getG(int color) {
        return (byte) ((color >> 8) & 255);
    }

    private static byte getR(int color) {
        return (byte) ((color >> 0) & 255);
    }

    private static boolean matched(int mathCount, int totalCount) {
        return ((float) mathCount) >= ((float) totalCount) * 0.9f;
    }

    private static boolean equal(byte value1, byte value2) {
        return Math.abs(value1 - value2) <= 30;
    }

    public static boolean checkBitmapLine(Bitmap bitmap, int width, int height) {
        GalleryLog.d("WallpaperUtils", String.format(" bitmap size(%s,%s) ", new Object[]{Integer.valueOf(width), Integer.valueOf(height)}));
        int[] pixels = new int[(width * height)];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int pointDistance = width / 2;
        int stepDistance = height / 20;
        int total = pointDistance;
        for (int i = 0; i < 20; i++) {
            int stepStart = (i * stepDistance) * width;
            int stepEnd = stepStart + pointDistance;
            int match = 0;
            for (int j = stepStart; j < stepEnd; j++) {
                byte r1 = getR(pixels[j]);
                byte g1 = getG(pixels[j]);
                byte b1 = getB(pixels[j]);
                byte r2 = getR(pixels[j + pointDistance]);
                byte g2 = getG(pixels[j + pointDistance]);
                byte b2 = getB(pixels[j + pointDistance]);
                if (equal(r1, r2) && equal(g1, g2) && equal(b1, b2)) {
                    match++;
                }
            }
            if (!matched(match, pointDistance)) {
                return false;
            }
        }
        return true;
    }
}
