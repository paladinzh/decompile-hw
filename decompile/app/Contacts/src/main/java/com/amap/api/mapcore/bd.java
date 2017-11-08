package com.amap.api.mapcore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import com.amap.api.mapcore.util.bh;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

/* compiled from: NinePatchTool */
class bd {
    private bd() {
    }

    public static Drawable a(Context context, String str) throws Exception {
        Bitmap b = b(context, str);
        if (b.getNinePatchChunk() == null) {
            return new BitmapDrawable(b);
        }
        Rect rect = new Rect();
        a(b.getNinePatchChunk(), rect);
        return new NinePatchDrawable(context.getResources(), b, b.getNinePatchChunk(), rect, null);
    }

    private static Bitmap a(InputStream inputStream) throws Exception {
        Bitmap decodeStream = BitmapFactory.decodeStream(inputStream);
        Object a = a(decodeStream);
        if (!NinePatch.isNinePatchChunk(a)) {
            return decodeStream;
        }
        Bitmap createBitmap = Bitmap.createBitmap(decodeStream, 1, 1, decodeStream.getWidth() - 2, decodeStream.getHeight() - 2);
        decodeStream.recycle();
        Field declaredField = createBitmap.getClass().getDeclaredField("mNinePatchChunk");
        declaredField.setAccessible(true);
        declaredField.set(createBitmap, a);
        return createBitmap;
    }

    private static Bitmap b(Context context, String str) throws Exception {
        InputStream open = bh.a(context).open(str);
        Bitmap a = a(open);
        open.close();
        return a;
    }

    private static void a(byte[] bArr, Rect rect) {
        rect.left = a(bArr, 12);
        rect.right = a(bArr, 16);
        rect.top = a(bArr, 20);
        rect.bottom = a(bArr, 24);
    }

    private static byte[] a(Bitmap bitmap) throws IOException {
        int i;
        int i2;
        int i3;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (i = 0; i < 32; i++) {
            byteArrayOutputStream.write(0);
        }
        int[] iArr = new int[(width - 2)];
        bitmap.getPixels(iArr, 0, width, 1, 0, width - 2, 1);
        if (iArr[0] != -16777216) {
            i = 0;
        } else {
            byte b = (byte) 1;
        }
        if (iArr[iArr.length - 1] != -16777216) {
            width = 0;
        } else {
            byte b2 = (byte) 1;
        }
        int length = iArr.length;
        int i4 = 0;
        int i5 = 0;
        for (i2 = 0; i2 < length; i2++) {
            if (i4 != iArr[i2]) {
                i5++;
                a(byteArrayOutputStream, i2);
                i4 = iArr[i2];
            }
        }
        if (width == 0) {
            i3 = i5;
        } else {
            i5++;
            a(byteArrayOutputStream, iArr.length);
            i3 = i5;
        }
        i4 = i3 + 1;
        if (i == 0) {
            i = i4;
        } else {
            i = i4 - 1;
        }
        if (width != 0) {
            i--;
        }
        int[] iArr2 = new int[(height - 2)];
        bitmap.getPixels(iArr2, 0, 1, 0, 1, 1, height - 2);
        if (iArr2[0] != -16777216) {
            width = 0;
        } else {
            b2 = (byte) 1;
        }
        if (iArr2[iArr2.length - 1] != -16777216) {
            i4 = 0;
        } else {
            byte b3 = (byte) 1;
        }
        int length2 = iArr2.length;
        i2 = 0;
        height = 0;
        for (length = 0; length < length2; length++) {
            if (i2 != iArr2[length]) {
                height++;
                a(byteArrayOutputStream, length);
                i2 = iArr2[length];
            }
        }
        if (i4 != 0) {
            height++;
            a(byteArrayOutputStream, iArr2.length);
        }
        i5 = height + 1;
        if (width == 0) {
            width = i5;
        } else {
            width = i5 - 1;
        }
        if (i4 != 0) {
            width--;
        }
        for (i4 = 0; i4 < i * width; i4++) {
            a(byteArrayOutputStream, 1);
        }
        byte[] toByteArray = byteArrayOutputStream.toByteArray();
        toByteArray[0] = (byte) 1;
        toByteArray[1] = (byte) ((byte) i3);
        toByteArray[2] = (byte) ((byte) height);
        toByteArray[3] = (byte) ((byte) (i * width));
        a(bitmap, toByteArray);
        return toByteArray;
    }

    private static void a(Bitmap bitmap, byte[] bArr) {
        int i;
        int i2 = 0;
        int[] iArr = new int[(bitmap.getWidth() - 2)];
        bitmap.getPixels(iArr, 0, iArr.length, 1, bitmap.getHeight() - 1, iArr.length, 1);
        for (i = 0; i < iArr.length; i++) {
            if (-16777216 == iArr[i]) {
                a(bArr, 12, i);
                break;
            }
        }
        i = iArr.length;
        do {
            i--;
            if (i < 0) {
                break;
            }
        } while (-16777216 != iArr[i]);
        a(bArr, 16, (iArr.length - i) - 2);
        int[] iArr2 = new int[(bitmap.getHeight() - 2)];
        bitmap.getPixels(iArr2, 0, 1, bitmap.getWidth() - 1, 0, 1, iArr2.length);
        while (i2 < iArr2.length) {
            if (-16777216 == iArr2[i2]) {
                a(bArr, 20, i2);
                break;
            }
            i2++;
        }
        i = iArr2.length;
        do {
            i--;
            if (i < 0) {
                return;
            }
        } while (-16777216 != iArr2[i]);
        a(bArr, 24, (iArr2.length - i) - 2);
    }

    private static void a(OutputStream outputStream, int i) throws IOException {
        outputStream.write((i >> 0) & 255);
        outputStream.write((i >> 8) & 255);
        outputStream.write((i >> 16) & 255);
        outputStream.write((i >> 24) & 255);
    }

    private static void a(byte[] bArr, int i, int i2) {
        bArr[i + 0] = (byte) ((byte) (i2 >> 0));
        bArr[i + 1] = (byte) ((byte) (i2 >> 8));
        bArr[i + 2] = (byte) ((byte) (i2 >> 16));
        bArr[i + 3] = (byte) ((byte) (i2 >> 24));
    }

    private static int a(byte[] bArr, int i) {
        return (((bArr[i + 0] & 255) | (bArr[i + 1] << 8)) | (bArr[i + 2] << 16)) | (bArr[i + 3] << 24);
    }
}
