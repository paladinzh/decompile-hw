package com.fyusion.sdk.common.ext.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.ext.g;
import com.fyusion.sdk.common.ext.k;
import com.fyusion.sdk.common.ext.m;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface;
import com.fyusion.sdk.common.ext.util.exif.ExifTag;
import com.fyusion.sdk.common.h;
import com.fyusion.sdk.common.util.a;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/* compiled from: Unknown */
public class FyuseUtils {

    /* compiled from: Unknown */
    /* renamed from: com.fyusion.sdk.common.ext.util.FyuseUtils$1 */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] a = new int[FyuseContainerVersion.values().length];

        static {
            try {
                a[FyuseContainerVersion.VERSION_1.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                a[FyuseContainerVersion.VERSION_3.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    /* compiled from: Unknown */
    public enum FyuseContainerVersion {
        UNKNOWN(0),
        VERSION_1(1),
        VERSION_2(2),
        VERSION_3(3);
        
        private final int id;

        private FyuseContainerVersion(int i) {
            this.id = i;
        }
    }

    public static void delete(File file) {
        a.a(new m(file).c());
        a.a(file);
    }

    public static Bitmap getRectifiedPreview(File file) {
        Bitmap decodeFile = BitmapFactory.decodeFile(file.getAbsolutePath());
        ExifInterface exifInterface = new ExifInterface();
        try {
            Bitmap createBitmap;
            exifInterface.readExif(file.getAbsolutePath());
            int width = decodeFile.getWidth();
            int height = decodeFile.getHeight();
            Matrix matrix = new Matrix();
            matrix.preTranslate((float) ((-width) / 2), (float) ((-height) / 2));
            ExifTag tag = exifInterface.getTag(ExifInterface.TAG_ORIENTATION);
            int valueAsInt = tag == null ? 1 : tag.getValueAsInt(1);
            if (valueAsInt != 1) {
                matrix.postRotate(90.0f);
            }
            ExifTag tag2 = exifInterface.getTag(ExifInterface.TAG_MAKER_NOTE);
            if (tag2 != null) {
                String valueAsString = tag2.getValueAsString();
                if (valueAsString != null && Arrays.asList(valueAsString.split(",")).contains("tw=1")) {
                    matrix.postScale(1.059f, 1.059f);
                }
            }
            Rect rect = new Rect(0, 0, width, height);
            rect = new Rect(0, 0, width, height);
            if (valueAsInt == 1) {
                matrix.postTranslate((float) (width / 2), (float) (height / 2));
                createBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            } else {
                rect.right = height;
                rect.bottom = width;
                matrix.postTranslate((float) (height / 2), (float) (width / 2));
                createBitmap = Bitmap.createBitmap(height, width, Config.ARGB_8888);
            }
            Canvas canvas = new Canvas(createBitmap);
            canvas.setMatrix(matrix);
            canvas.drawBitmap(decodeFile, 0.0f, 0.0f, null);
            decodeFile.recycle();
            return createBitmap;
        } catch (IOException e) {
            h.c("FyuseUtils", "Failed reading Exif from " + file);
            return decodeFile;
        }
    }

    public static boolean isFyuseContainerVersion(@NonNull File file, @NonNull FyuseContainerVersion fyuseContainerVersion) {
        boolean z = true;
        if (file == null || !file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            file = new File(file.getAbsoluteFile() + File.separator + k.ak);
        }
        switch (AnonymousClass1.a[fyuseContainerVersion.ordinal()]) {
            case 1:
                boolean z2 = !g.d(file.getAbsolutePath()) && g.b(file);
                return z2;
            case 2:
                if (g.d(file.getAbsolutePath())) {
                    if (!g.b(file)) {
                    }
                    return z;
                }
                z = false;
                return z;
            default:
                return false;
        }
    }

    public static boolean isFyuseFile(@NonNull File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            file = new File(file.getAbsoluteFile() + File.separator + k.ak);
        }
        return g.b(file);
    }

    @Deprecated
    public static boolean isFyuseProcessed(File file) {
        return (file == null || !file.exists()) ? false : g.f(file);
    }
}
