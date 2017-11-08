package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import com.android.gallery3d.common.Utils;

public class BitmapTexture extends UploadedTexture {
    private final Bitmap mContentBitmap;

    public BitmapTexture(Bitmap bitmap) {
        this(bitmap, false);
    }

    public BitmapTexture(Bitmap bitmap, boolean hasBorder) {
        boolean z = false;
        super(hasBorder);
        if (!(bitmap == null || bitmap.isRecycled())) {
            z = true;
        }
        Utils.assertTrue(z);
        this.mContentBitmap = bitmap;
    }

    protected void onFreeBitmap(Bitmap bitmap) {
    }

    protected Bitmap onGetBitmap() {
        return this.mContentBitmap;
    }

    public Bitmap getBitmap() {
        return this.mContentBitmap;
    }

    public boolean sourceBitmapInvalid() {
        return this.mContentBitmap != null ? this.mContentBitmap.isRecycled() : false;
    }
}
