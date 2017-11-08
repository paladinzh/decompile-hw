package com.google.android.gms.internal;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/* compiled from: Unknown */
public final class zzks {
    public static Bitmap zza(Bitmap bitmap) {
        int i = 0;
        if (bitmap == null) {
            return null;
        }
        int i2;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width < height) {
            i2 = (width - height) / 2;
        } else {
            width = height;
            i2 = 0;
            i = (height - width) / 2;
        }
        Bitmap createBitmap = Bitmap.createBitmap(width, width, Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Paint paint = new Paint(1);
        paint.setColor(-16777216);
        canvas.drawCircle((float) (width / 2), (float) (width / 2), (float) (width / 2), paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, (float) i, (float) i2, paint);
        return createBitmap;
    }

    private static Bitmap zza(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap createBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return createBitmap;
    }

    public static Drawable zza(Resources resources, Drawable drawable) {
        return new BitmapDrawable(resources, zza(zza(drawable)));
    }
}
