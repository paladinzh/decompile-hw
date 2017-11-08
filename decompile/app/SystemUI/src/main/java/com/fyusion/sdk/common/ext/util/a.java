package com.fyusion.sdk.common.ext.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import com.fyusion.sdk.common.ext.f;
import java.io.FileNotFoundException;

/* compiled from: Unknown */
public class a {
    public static Bitmap b(Bitmap bitmap, f fVar) throws FileNotFoundException {
        int i = 180;
        if (bitmap == null) {
            return null;
        }
        int i2;
        Matrix matrix = new Matrix();
        if (fVar.getCameraOrientation() != 270) {
            i2 = 0;
        } else {
            boolean z = true;
        }
        float gravityX = fVar.getGravityX();
        if (Math.abs(gravityX) > Math.abs(fVar.getGravityY())) {
            if (gravityX > 0.0f) {
                if (i2 != 0) {
                    i2 = 0;
                }
            } else if (i2 == 0) {
                i = 0;
            }
            i2 = i;
        } else {
            i2 = 90;
        }
        matrix.postRotate((float) i2);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
