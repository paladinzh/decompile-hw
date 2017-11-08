package com.fyusion.sdk.common.ext.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.ext.f;
import com.fyusion.sdk.common.ext.j;
import com.fyusion.sdk.common.ext.l;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface;
import com.fyusion.sdk.common.ext.util.exif.ExifTag;
import com.fyusion.sdk.common.util.a;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

        public int getValue() {
            return this.id;
        }
    }

    public static boolean convertV1ToV3(File file) {
        if (!isFyuseContainerVersion(file, FyuseContainerVersion.VERSION_3)) {
            l lVar = new l(file);
            try {
                lVar.h();
                lVar.a(isFyuseProcessed(file), true);
            } catch (IOException e) {
                DLog.e("FyuseUtils", e.getMessage());
                return false;
            }
        }
        return true;
    }

    public static void delete(File file) {
        a.a(new l(file).c());
        a.a(file);
    }

    public static Bitmap getOriginalThumbnail(File file) throws Exception {
        return a.a(file, null);
    }

    public static Bitmap getPreview(File file) {
        Bitmap decodeFile = BitmapFactory.decodeFile(file.getAbsolutePath());
        ExifInterface exifInterface = new ExifInterface();
        try {
            exifInterface.readExif(file.getAbsolutePath());
            ExifTag tag = exifInterface.getTag(ExifInterface.TAG_MAKER_NOTE);
            if (tag != null) {
                String valueAsString = tag.getValueAsString();
                if (valueAsString != null && Arrays.asList(valueAsString.split(",")).contains("tw=1")) {
                    int width = decodeFile.getWidth();
                    int height = decodeFile.getHeight();
                    Bitmap createBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
                    Canvas canvas = new Canvas(createBitmap);
                    Matrix matrix = new Matrix();
                    matrix.preTranslate((float) ((-width) / 2), (float) ((-height) / 2));
                    matrix.postScale(1.059f, 1.059f);
                    matrix.postTranslate((float) (width / 2), (float) (height / 2));
                    Rect rect = new Rect(0, 0, width, height);
                    canvas.setMatrix(matrix);
                    canvas.drawBitmap(decodeFile, rect, rect, null);
                    return createBitmap;
                }
            }
        } catch (IOException e) {
            DLog.w("FyuseUtils", "Failed reading Exif from " + file);
        }
        return decodeFile;
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
            DLog.w("FyuseUtils", "Failed reading Exif from " + file);
            return decodeFile;
        }
    }

    public static Bitmap getSmallThumb(File file) throws Exception {
        return a.a(file);
    }

    public static Bitmap getThumbnail(File file) {
        if (file.isDirectory()) {
            File file2 = new File(file.getPath(), j.ak);
            if (!file2.exists()) {
                file2 = new File(file.getPath(), j.ad);
            }
            file = file2;
        }
        Options options = new Options();
        options.inPreferredConfig = Config.RGB_565;
        options.inSampleSize = 4;
        return BitmapFactory.decodeFile(file.getPath(), options);
    }

    public static boolean isFyuseContainerVersion(@NonNull File file, @NonNull FyuseContainerVersion fyuseContainerVersion) {
        boolean z = true;
        if (file == null || !file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            file = new File(file.getAbsoluteFile() + File.separator + j.ak);
        }
        switch (AnonymousClass1.a[fyuseContainerVersion.ordinal()]) {
            case 1:
                boolean z2 = !f.d(file.getAbsolutePath()) && f.b(file);
                return z2;
            case 2:
                if (f.d(file.getAbsolutePath())) {
                    if (!f.b(file)) {
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
            file = new File(file.getAbsoluteFile() + File.separator + j.ak);
        }
        return f.b(file);
    }

    @Deprecated
    public static boolean isFyuseProcessed(File file) {
        return (file == null || !file.exists()) ? false : f.f(file);
    }

    public static boolean isLegacyFyuse(@NonNull File file) {
        if (file == null) {
            return false;
        }
        if (file.isDirectory()) {
            file = new File(file.getAbsoluteFile() + File.separator + j.ak);
        }
        if (!file.exists()) {
            return false;
        }
        boolean z = !f.d(file.getAbsolutePath()) && f.b(file);
        return z;
    }

    @Deprecated
    public static boolean isLegacyFyuse(@NonNull String str) {
        return str != null ? isLegacyFyuse(new File(str)) : false;
    }

    @Deprecated
    public static boolean isNewFyuse(@NonNull File file) {
        boolean z = false;
        if (file == null || !file.exists()) {
            return false;
        }
        if (f.d(file.getAbsolutePath()) && f.b(file)) {
            z = true;
        }
        return z;
    }

    @Deprecated
    public static boolean isNewFyuse(@NonNull String str) {
        boolean z = false;
        if (str == null) {
            return false;
        }
        if (f.d(str) && f.b(new File(str))) {
            z = true;
        }
        return z;
    }

    public static boolean isTaggedAsFyuse(@NonNull File file) {
        return (file != null && file.exists()) ? f.d(file.getAbsolutePath()) : false;
    }

    public static List<ExifTag> readExifData(File file) {
        return f.e(file);
    }

    public static boolean setExifTag(File file, int i, Object obj) {
        return f.a(file, i, obj);
    }

    public static boolean setExifTags(File file, List<ExifTag> list) {
        return f.a(file, (List) list);
    }
}
