package com.android.mms.attachment.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import com.android.mms.attachment.Factory;
import com.android.mms.exif.ExifInterface;
import com.huawei.cspcommon.MLog;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ImageUtils {
    private static final byte[] GIF87_HEADER = "GIF87a".getBytes(Charset.forName("US-ASCII"));
    private static final byte[] GIF89_HEADER = "GIF89a".getBytes(Charset.forName("US-ASCII"));
    private static final String[] MEDIA_CONTENT_PROJECTION = new String[]{"mime_type"};
    private static volatile ImageUtils sInstance;

    public static ImageUtils get() {
        if (sInstance == null) {
            synchronized (ImageUtils.class) {
                if (sInstance == null) {
                    sInstance = new ImageUtils();
                }
            }
        }
        return sInstance;
    }

    public static byte[] bitmapToBytes(Bitmap bitmap, int quality) throws OutOfMemoryError {
        boolean done = false;
        byte[] bArr = null;
        while (!done) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bitmap.compress(CompressFormat.JPEG, quality, os);
                bArr = os.toByteArray();
                done = true;
            } catch (OutOfMemoryError e) {
                MLog.w("ImageUtils", "OutOfMemory converting bitmap to bytes.");
                if (1 <= 1) {
                    Factory.get().reclaimMemory();
                } else {
                    MLog.w("ImageUtils", "Failed to convert bitmap to bytes. Out of Memory.");
                }
                throw e;
            }
        }
        return bArr;
    }

    public static void drawBitmapWithCircleOnCanvas(Bitmap bitmap, Canvas canvas, RectF source, RectF dest, Paint bitmapPaint, boolean fillBackground, int backgroundColor, int strokeColor) {
        BitmapShader shader = new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP);
        Matrix matrix = new Matrix();
        matrix.setRectToRect(source, dest, ScaleToFit.CENTER);
        shader.setLocalMatrix(matrix);
        if (bitmapPaint == null) {
            bitmapPaint = new Paint();
        }
        bitmapPaint.setAntiAlias(true);
        if (fillBackground) {
            bitmapPaint.setColor(backgroundColor);
            canvas.drawCircle(dest.centerX(), dest.centerX(), dest.width() / 2.0f, bitmapPaint);
        }
        bitmapPaint.setShader(shader);
        canvas.drawCircle(dest.centerX(), dest.centerX(), dest.width() / 2.0f, bitmapPaint);
        bitmapPaint.setShader(null);
        if (strokeColor != 0) {
            Paint stroke = new Paint();
            stroke.setAntiAlias(true);
            stroke.setColor(strokeColor);
            stroke.setStyle(Style.STROKE);
            stroke.setStrokeWidth(6.0f);
            canvas.drawCircle(dest.centerX(), dest.centerX(), (dest.width() / 2.0f) - (stroke.getStrokeWidth() / 2.0f), stroke);
        }
    }

    public int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        boolean checkHeight = reqHeight != -1;
        boolean checkWidth = reqWidth != -1;
        if ((checkHeight && height > reqHeight) || (checkWidth && width > reqWidth)) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while (true) {
                if ((checkHeight && halfHeight / inSampleSize <= reqHeight) || (checkWidth && halfWidth / inSampleSize <= reqWidth)) {
                    break;
                }
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int getOrientation(InputStream inputStream) {
        int orientation = 0;
        if (inputStream != null) {
            try {
                ExifInterface exifInterface = new ExifInterface();
                exifInterface.readExif(inputStream);
                Integer orientationValue = exifInterface.getTagIntValue(ExifInterface.TAG_ORIENTATION);
                if (orientationValue != null) {
                    orientation = orientationValue.intValue();
                }
                try {
                    inputStream.close();
                } catch (IOException e) {
                    MLog.e("ImageUtils", "getOrientation error closing input stream", (Throwable) e);
                }
            } catch (IOException e2) {
                MLog.e("ImageUtils", "getOrientation: IOException");
            } catch (OutOfMemoryError e3) {
                MLog.e("ImageUtils", "get Orientation failed, because of OutOfMemoryError:" + e3.getMessage());
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    MLog.e("ImageUtils", "getOrientation error closing input stream", (Throwable) e4);
                }
            } catch (Throwable th) {
                try {
                    inputStream.close();
                } catch (IOException e42) {
                    MLog.e("ImageUtils", "getOrientation error closing input stream", (Throwable) e42);
                }
            }
        }
        return orientation;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isGif(InputStream inputStream) {
        if (inputStream != null) {
            try {
                byte[] gifHeaderBytes = new byte[6];
                if (inputStream.read(gifHeaderBytes, 0, 6) == 6) {
                    boolean z;
                    if (Arrays.equals(gifHeaderBytes, GIF87_HEADER)) {
                        z = true;
                    } else {
                        z = Arrays.equals(gifHeaderBytes, GIF89_HEADER);
                    }
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        MLog.e("ImageUtils", "isGif: inputStream.close IOException");
                    }
                    return z;
                }
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    MLog.e("ImageUtils", "isGif: inputStream.close IOException");
                }
            } catch (IOException e3) {
                return false;
            } catch (Throwable th) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    MLog.e("ImageUtils", "isGif: inputStream.close IOException");
                }
            }
        }
        return false;
    }
}
