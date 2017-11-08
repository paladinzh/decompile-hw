package com.android.gallery3d.gadget;

import android.content.res.Resources;
import android.graphics.Bitmap;

public class CutBitmapUtils {
    public static Bitmap cutBitmap(Bitmap source, int x, int y, int width, int height) throws Exception {
        if (source == null) {
            throw new Exception("source Bitmap is null");
        } else if (x < 0 || y < 0) {
            throw new Exception("x must between 0 and source.getWidth(), y must between 0 and source.getHeight()");
        } else {
            int mHeight = source.getHeight();
            int mWidth = source.getWidth();
            if (height + y <= mHeight && width + x <= mWidth) {
                return Bitmap.createBitmap(source, x, y, width, height);
            }
            throw new Exception("width must between 0 and source.getWidth()-x, height must between 0 and source.getHeight()-y");
        }
    }

    public static Bitmap getCornerBitmap(Bitmap mBitmap, Resources res, int templateId) throws Exception {
        return BitmapHelper.GetCornerBitmap(mBitmap, res, templateId);
    }
}
