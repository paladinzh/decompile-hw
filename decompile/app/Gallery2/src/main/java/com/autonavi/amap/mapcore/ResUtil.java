package com.autonavi.amap.mapcore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.TypedValue;
import com.amap.api.maps.model.WeightedLatLng;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResUtil {
    public static Bitmap decodeBitmapWithAdaptiveSize(Context context, int i) {
        Options options = new Options();
        options.inScaled = false;
        options.inDensity = 0;
        options.inTargetDensity = 0;
        Bitmap decodeResource = BitmapFactory.decodeResource(context.getResources(), i, options);
        decodeResource.setDensity(0);
        float f = 2.0f / context.getResources().getDisplayMetrics().density;
        if (((double) f) == WeightedLatLng.DEFAULT_INTENSITY) {
            return decodeResource;
        }
        Bitmap createScaledBitmap = Bitmap.createScaledBitmap(decodeResource, (int) (((float) decodeResource.getWidth()) / f), (int) (((float) decodeResource.getHeight()) / f), false);
        decodeResource.recycle();
        return createScaledBitmap;
    }

    public static Bitmap decodeAssetBitmap(Context context, String str) {
        Bitmap bitmap = null;
        try {
            InputStream open = context.getAssets().open(str);
            bitmap = BitmapFactory.decodeStream(open);
            bitmap.setDensity(0);
            open.close();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    public static Bitmap decodeAssetBitmapWithSize(Context context, String str, int i, int i2) {
        try {
            InputStream open = context.getAssets().open(str);
            Bitmap decodeStream = BitmapFactory.decodeStream(open);
            open.close();
            Bitmap createBitmap = Bitmap.createBitmap(i, i2, Config.ARGB_8888);
            Canvas canvas = new Canvas(createBitmap);
            Rect rect = new Rect();
            Rect rect2 = new Rect();
            rect.set(0, 0, decodeStream.getWidth(), decodeStream.getHeight());
            rect2.set(0, 0, i, i2);
            canvas.drawBitmap(decodeStream, rect, rect2, null);
            decodeStream.recycle();
            return createBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decodeAssetResData(Context context, String str) {
        try {
            InputStream open = context.getAssets().open(str);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr = new byte[1024];
            while (true) {
                int read = open.read(bArr);
                if (read <= -1) {
                    bArr = byteArrayOutputStream.toByteArray();
                    byteArrayOutputStream.close();
                    open.close();
                    return bArr;
                }
                byteArrayOutputStream.write(bArr, 0, read);
            }
        } catch (Throwable th) {
            return null;
        }
    }

    public static int dipToPixel(Context context, int i) {
        if (context == null) {
            return i;
        }
        try {
            return (int) TypedValue.applyDimension(1, (float) i, context.getResources().getDisplayMetrics());
        } catch (Exception e) {
            e.printStackTrace();
            return i;
        }
    }

    public static int spToPixel(Context context, int i) {
        return (int) TypedValue.applyDimension(2, (float) i, context.getResources().getDisplayMetrics());
    }

    public static String getString(Context context, int i) {
        return context.getResources().getString(i);
    }
}
