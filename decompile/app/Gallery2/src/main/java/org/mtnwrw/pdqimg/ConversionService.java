package org.mtnwrw.pdqimg;

import android.graphics.Bitmap;
import android.graphics.YuvImage;
import java.nio.ByteBuffer;

/* compiled from: Unknown */
public class ConversionService {
    private static final String LOGTAG = "Conv";

    /* compiled from: Unknown */
    public static class ConversionError extends Exception {
        public ConversionError(String str) {
            super(str);
        }
    }

    static {
        try {
            System.loadLibrary("pdqimg");
        } catch (Throwable e) {
            e.printStackTrace();
        } catch (Throwable e2) {
            e2.printStackTrace();
        }
    }

    public static Bitmap convertPDQImageToBitmap(PDQImage pDQImage, boolean z, boolean z2) throws ConversionError {
        Bitmap convertPDQImageToBitmapNative = convertPDQImageToBitmapNative(pDQImage, z, z2);
        if (convertPDQImageToBitmapNative != null) {
            return convertPDQImageToBitmapNative;
        }
        throw new ConversionError("Cannot convert PDQ image to bitmap");
    }

    public static void convertPDQImageToBitmap(PDQImage pDQImage, Bitmap bitmap, boolean z, boolean z2) throws ConversionError {
        if (convertPDQImageToExistingBitmapNative(pDQImage, bitmap, z, z2) != 0) {
            throw new ConversionError("Cannot convert PDQImage to Bitmap");
        }
    }

    private static native Bitmap convertPDQImageToBitmapNative(PDQImage pDQImage, boolean z, boolean z2);

    public static ByteBuffer convertPDQImageToDNG(PDQImage pDQImage) throws IllegalArgumentException, ConversionError {
        if (pDQImage.getFormat() != 32) {
            throw new IllegalArgumentException("Only RAW input is allowed for DNG conversion");
        }
        ByteBuffer convertPDQImageToDNGNative = convertPDQImageToDNGNative(pDQImage);
        if (convertPDQImageToDNGNative != null) {
            return convertPDQImageToDNGNative;
        }
        throw new ConversionError("Unable to convert PDQ image to DNG");
    }

    private static native ByteBuffer convertPDQImageToDNGNative(PDQImage pDQImage);

    private static native int convertPDQImageToExistingBitmapNative(PDQImage pDQImage, Bitmap bitmap, boolean z, boolean z2);

    public static YuvImage convertPDQImageToYuvImage(PDQImage pDQImage) throws ConversionError {
        YuvImage convertPDQImageToYuvImageNative = convertPDQImageToYuvImageNative(pDQImage);
        if (convertPDQImageToYuvImageNative != null) {
            return convertPDQImageToYuvImageNative;
        }
        throw new ConversionError("Cannot convert PDQ image to YuvImage");
    }

    private static native YuvImage convertPDQImageToYuvImageNative(PDQImage pDQImage);
}
