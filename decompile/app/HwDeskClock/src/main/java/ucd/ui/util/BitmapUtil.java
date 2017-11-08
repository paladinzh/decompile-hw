package ucd.ui.util;

import android.graphics.Bitmap;

public class BitmapUtil {
    public static Bitmap createFitBitmap(Bitmap bitmap, int widthTarget, int heightTarget) {
        return getFitSizeBitmap(bitmap, widthTarget, heightTarget);
    }

    private static Bitmap getFitSizeBitmap(Bitmap bitmap, int containerW, int containerH) {
        if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0 || containerW == 0 || containerH == 0) {
            return bitmap;
        }
        float bw = (float) bitmap.getWidth();
        float bh = (float) bitmap.getHeight();
        if ((bw * 1.0f) / bh >= (((float) containerW) * 1.0f) / ((float) containerH)) {
            if (bw <= ((float) containerW)) {
                return bitmap;
            }
            return Bitmap.createScaledBitmap(bitmap, (int) ((float) containerW), (int) (bh * ((((float) containerW) * 1.0f) / bw)), false);
        } else if (bh <= ((float) containerH)) {
            return bitmap;
        } else {
            return Bitmap.createScaledBitmap(bitmap, (int) (bw * ((((float) containerH) * 1.0f) / bh)), (int) ((float) containerH), false);
        }
    }
}
