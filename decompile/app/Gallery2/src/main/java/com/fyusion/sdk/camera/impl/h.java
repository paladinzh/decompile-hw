package com.fyusion.sdk.camera.impl;

import android.annotation.TargetApi;
import android.util.Size;

@TargetApi(21)
/* compiled from: Unknown */
public class h {
    private static Size[] a = new Size[]{new Size(1920, 1080), new Size(1280, 720)};

    public static Size a(int i, int i2) {
        for (Size size : a) {
            if (i >= size.getWidth() || i2 >= size.getHeight()) {
                return size;
            }
        }
        return a[1];
    }

    public static Size a(Size size) {
        return size != null ? a(size.getWidth(), size.getHeight()) : a[1];
    }
}
