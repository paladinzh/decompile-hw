package com.android.gallery3d.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.util.ColorfulUtils;

public class ThemeableNinePatchTexture extends NinePatchTexture {
    private boolean mNeedColorFul = false;

    public ThemeableNinePatchTexture(Context context, int resId) {
        super(context, resId);
    }

    protected Bitmap onGetBitmap() {
        NinePatchChunk ninePatchChunk = null;
        if (this.mBitmap != null) {
            return this.mBitmap;
        }
        Options options = new Options();
        options.inPreferredConfig = Config.ARGB_8888;
        Bitmap bitmap1 = BitmapFactory.decodeResource(this.mContext.getResources(), this.mResId, options);
        byte[] ninePatchChunk2 = bitmap1.getNinePatchChunk();
        bitmap1.recycle();
        if (ninePatchChunk2 == null) {
            throw new RuntimeException("invalid nine-patch image: " + this.mResId);
        }
        Drawable drawable = this.mContext.getResources().getDrawable(this.mResId);
        if (this.mNeedColorFul && ColorfulUtils.mappingColorfulColor(this.mContext, 0) != 0) {
            drawable = ColorfulUtils.mappingColorfulDrawableForce(this.mContext, this.mResId);
        }
        try {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            canvas.setBitmap(null);
            bitmap.setNinePatchChunk(ninePatchChunk2);
            this.mBitmap = bitmap;
            setSize(bitmap.getWidth(), bitmap.getHeight());
            if (bitmap.getNinePatchChunk() != null) {
                ninePatchChunk = NinePatchChunk.deserialize(bitmap.getNinePatchChunk());
            }
            this.mChunk = ninePatchChunk;
            if (this.mChunk != null) {
                return bitmap;
            }
            throw new RuntimeException("invalid nine-patch image: " + this.mResId);
        } catch (Exception ex) {
            GalleryLog.e("ResourceTexture", "failed to create the Bitmap " + ex);
            return null;
        }
    }
}
