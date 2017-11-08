package com.android.gallery3d.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.gallery.util.ImmersionUtils;

public class HwExtResourceTexture extends UploadedTexture {
    private final Context mContext;
    private final int mDefaultRes;
    private final String mResName;

    public HwExtResourceTexture(Context context, String resName, int res) {
        this.mContext = (Context) Utils.checkNotNull(context);
        this.mResName = resName;
        this.mDefaultRes = res;
        setOpaque(false);
    }

    protected Bitmap onGetBitmap() {
        Drawable drawable = null;
        Resources res = this.mContext.getResources();
        if (ImmersionUtils.getControlColor(this.mContext) != 0) {
            int drawableID = ColorfulUtils.getHwExtDrawable(res, this.mResName);
            if (drawableID != 0) {
                drawable = res.getDrawable(drawableID);
            }
        }
        if (drawable == null) {
            drawable = res.getDrawable(this.mDefaultRes);
        }
        try {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            canvas.setBitmap(null);
            return bitmap;
        } catch (Exception ex) {
            GalleryLog.e("HwExtResourceTexture", "failed to create the Bitmap " + ex);
            Options options = new Options();
            options.inPreferredConfig = Config.ARGB_8888;
            return BitmapFactory.decodeResource(this.mContext.getResources(), this.mDefaultRes, options);
        }
    }

    protected void onFreeBitmap(Bitmap bitmap) {
        if (!BasicTexture.inFinalizer() && bitmap != null) {
            GalleryLog.d("HwExtResourceTexture", String.format("bitmap(%s) will be recycled[mWidth=%d, mHeight=%d ]", new Object[]{bitmap, Integer.valueOf(bitmap.getWidth()), Integer.valueOf(bitmap.getHeight())}));
            bitmap.recycle();
        }
    }
}
