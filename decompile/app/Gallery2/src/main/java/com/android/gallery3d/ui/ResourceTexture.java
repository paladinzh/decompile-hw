package com.android.gallery3d.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;

public class ResourceTexture extends UploadedTexture {
    protected final Context mContext;
    protected final int mResId;

    public ResourceTexture(Context context, int resId) {
        this.mContext = (Context) Utils.checkNotNull(context);
        this.mResId = resId;
        setOpaque(false);
    }

    protected Bitmap onGetBitmap() {
        Drawable drawable = this.mContext.getResources().getDrawable(this.mResId);
        try {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            canvas.setBitmap(null);
            return bitmap;
        } catch (Exception ex) {
            GalleryLog.e("ResourceTexture", "failed to create the Bitmap " + ex);
            Options options = new Options();
            options.inPreferredConfig = Config.ARGB_8888;
            return BitmapFactory.decodeResource(this.mContext.getResources(), this.mResId, options);
        }
    }

    protected void onFreeBitmap(Bitmap bitmap) {
        if (!BasicTexture.inFinalizer()) {
            GalleryLog.w("ResourceTexture", String.format("bitmap(%s) will be recycled[mWidth=%d, mHeight=%d ]", new Object[]{bitmap, Integer.valueOf(bitmap.getWidth()), Integer.valueOf(bitmap.getHeight())}));
            bitmap.recycle();
        }
    }
}
