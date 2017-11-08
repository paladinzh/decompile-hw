package com.fyusion.sdk.camera.util;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.media.Image;
import android.media.Image.Plane;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

/* compiled from: Unknown */
public class ColorHelper {
    public static boolean nativeOK;

    static {
        nativeOK = false;
        try {
            System.loadLibrary("colorhelper");
            nativeOK = true;
        } catch (Throwable e) {
            e.printStackTrace();
        } catch (Throwable e2) {
            e2.printStackTrace();
        }
    }

    private static native void convertYUV420ImageToNV21(Image image, byte[] bArr);

    public static void convertYUV420SPToRGBA(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2) {
        if (!nativeOK) {
            throw new IllegalStateException("Native code has not been succesfully initialized");
        } else if (byteBuffer == null || !byteBuffer.isDirect()) {
            throw new InvalidParameterException("Illegal (type of) input buffer supplied");
        } else if (byteBuffer2 != null && byteBuffer2.isDirect()) {
            yuv420SPToRGBA(byteBuffer, byteBuffer2, i, i2);
        } else {
            throw new InvalidParameterException("Illegal (type of) output buffer supplied");
        }
    }

    public static void cropAndScaleImageCentered(Image image, ByteBuffer byteBuffer, int i, int i2) {
        int i3 = 0;
        if (!nativeOK) {
            throw new IllegalStateException("Native code has not been succesfully initialized");
        } else if (byteBuffer != null && byteBuffer.isDirect()) {
            int width;
            int width2;
            int height;
            float width3 = ((float) i) / ((float) image.getWidth());
            float height2 = ((float) i2) / ((float) image.getHeight());
            if (width3 < height2) {
                width = ((image.getWidth() - ((int) (((float) i) / height2))) / 2) & -2;
                width2 = image.getWidth() - (width * 2);
                height = image.getHeight();
            } else {
                int height3 = ((image.getHeight() - ((int) (((float) i2) / width3))) / 2) & -2;
                width2 = image.getWidth();
                height = image.getHeight() - (height3 * 2);
                width = 0;
                i3 = height3;
            }
            scaleYUV420ImageToSPNN(image, byteBuffer, width2, height, width, i3, i, i2);
        } else {
            throw new InvalidParameterException("Illegal (type of) target buffer supplied");
        }
    }

    private static native void scaleYUV420ImageToSPNN(Image image, ByteBuffer byteBuffer, int i, int i2, int i3, int i4, int i5, int i6);

    @TargetApi(21)
    public static byte[] toNV21(Image image) {
        int i = 0;
        if (nativeOK) {
            byte[] bArr = new byte[(((image.getWidth() * image.getHeight()) * 3) / 2)];
            convertYUV420ImageToNV21(image, bArr);
            return bArr;
        }
        Rect cropRect = image.getCropRect();
        int width = cropRect.width();
        int height = cropRect.height();
        byte[] bArr2 = new byte[(((width * height) * 3) / 2)];
        Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        buffer.rewind();
        buffer.get(bArr2, 0, buffer.remaining());
        buffer = planes[1].getBuffer();
        buffer.rewind();
        byte[] bArr3 = new byte[buffer.remaining()];
        buffer.get(bArr3, 0, buffer.remaining());
        buffer = planes[2].getBuffer();
        buffer.rewind();
        byte[] bArr4 = new byte[buffer.remaining()];
        buffer.get(bArr4, 0, buffer.remaining());
        int pixelStride = planes[1].getPixelStride();
        int i2 = 0;
        while (i < (height * width) / 2) {
            bArr2[(width * height) + i2] = (byte) bArr4[i];
            bArr2[((width * height) + i2) + 1] = (byte) bArr3[i];
            i2 += 2;
            i += pixelStride;
        }
        return bArr2;
    }

    private static native void yuv420SPToRGBA(ByteBuffer byteBuffer, ByteBuffer byteBuffer2, int i, int i2);
}
