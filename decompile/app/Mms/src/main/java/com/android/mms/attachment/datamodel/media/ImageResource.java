package com.android.mms.attachment.datamodel.media;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public abstract class ImageResource extends RefCountedMediaResource {
    protected final int mOrientation;

    public abstract Bitmap getBitmap();

    public abstract Drawable getDrawable(Resources resources);

    public abstract Bitmap reuseBitmap();

    public abstract boolean supportsBitmapReuse();

    public ImageResource(String key, int orientation) {
        super(key);
        this.mOrientation = orientation;
    }

    public int getOrientation() {
        return this.mOrientation;
    }
}
