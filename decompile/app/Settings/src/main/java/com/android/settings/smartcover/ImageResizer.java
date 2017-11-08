package com.android.settings.smartcover;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

public class ImageResizer extends ImageWorker {
    public ImageResizer(Context context, int imageWidth, int imageHeight) {
        super(context);
        setImageSize(imageWidth, imageHeight);
    }

    public void setImageSize(int width, int height) {
        this.mImageWidth = width;
        this.mImageHeight = height;
    }

    private Bitmap processBitmap(int resId) {
        return decodeSampledBitmapFromResource(this.mResources, resId, this.mImageWidth, this.mImageHeight, 5);
    }

    protected Bitmap processBitmap(Object data) {
        if (data == null) {
            return null;
        }
        Bitmap map = null;
        try {
            map = processBitmap(Integer.parseInt(String.valueOf(data)));
        } catch (Exception e) {
        }
        return map;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight, int sampleSize) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
}
