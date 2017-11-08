package org.mtnwrw.pdqimg;

import android.graphics.Bitmap;
import android.graphics.YuvImage;
import android.media.Image;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

/* compiled from: Unknown */
public class PDQImage {
    private int Format;
    private int Height;
    private long InternalData = 0;
    private int InternalFormat;
    private Plane[] Planes;
    int RawBitsUsed;
    private int Width;
    int bitDepth = 1;

    /* compiled from: Unknown */
    public class Plane {
        private ByteBuffer Buffer = null;
        private int PixelStride;
        private int RowStride;

        public ByteBuffer getBuffer() {
            return this.Buffer;
        }

        public int getPixelStride() {
            return this.PixelStride;
        }

        public int getRowStride() {
            return this.RowStride;
        }
    }

    /* compiled from: Unknown */
    public enum cfapattern {
        UNKNOWN,
        RGGB,
        BGGR,
        GBRG,
        GRBG
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

    public PDQImage(int i, int i2, int i3) {
        int i4 = 1;
        switch (i3) {
            case 17:
            case 35:
                i4 = 3;
                break;
            case 32:
                break;
            default:
                throw new InvalidParameterException("Image format " + i3 + " is not supported by PDQ");
        }
        this.Planes = new Plane[i4];
        this.Format = i3;
        this.Width = i;
        this.Height = i2;
        this.RawBitsUsed = 8;
        if (!setupPDQImageNative(this, i, i2, i3)) {
            throw new RuntimeException("Unable to create PDQ image instance");
        }
    }

    private PDQImage(int i, int i2, int i3, int i4, int i5) {
        this.Planes = new Plane[i];
        this.Width = i2;
        this.Height = i3;
        this.Format = i4;
        this.RawBitsUsed = 8;
        this.InternalFormat = i5;
    }

    public static PDQImage copy(PDQImage pDQImage) {
        PDQImage pDQImage2 = new PDQImage(pDQImage.Width, pDQImage.Height, pDQImage.Format);
        if (pDQImage2 != null && pDQImage2.Planes.length >= 1 && pDQImage2.Planes.length == pDQImage.Planes.length) {
            for (int i = 0; i < pDQImage2.Planes.length; i++) {
                pDQImage2.Planes[i].Buffer.put(pDQImage.Planes[i].Buffer);
            }
            pDQImage2.RawBitsUsed = pDQImage.RawBitsUsed;
            return pDQImage2;
        }
        if (pDQImage2 != null) {
            pDQImage2.close();
        }
        return null;
    }

    public static PDQImage createFromBitmap(Bitmap bitmap, int i, PDQImage pDQImage) {
        if (pDQImage != null) {
            if (bitmap.getWidth() != pDQImage.Width || bitmap.getHeight() != pDQImage.Height) {
                return null;
            }
        }
        return createFromBitmapNative(bitmap, i, pDQImage);
    }

    private static native PDQImage createFromBitmapNative(Bitmap bitmap, int i, PDQImage pDQImage);

    public static PDQImage createFromByteBuffer(ByteBuffer byteBuffer, int i, int i2, int i3, PDQImage pDQImage) {
        if (pDQImage != null) {
            if (i != pDQImage.Width || i2 != pDQImage.Height) {
                return null;
            }
        }
        return createFromByteBufferNative(byteBuffer, i, i2, i3, pDQImage);
    }

    private static native PDQImage createFromByteBufferNative(ByteBuffer byteBuffer, int i, int i2, int i3, PDQImage pDQImage);

    public static PDQImage createFromImage(Image image) {
        return createFromImageNative(image);
    }

    private static native PDQImage createFromImageNative(Image image);

    private static native PDQImage createFromYUVImageNative(YuvImage yuvImage);

    public static PDQImage createFromYuvImage(YuvImage yuvImage) {
        return createFromYUVImageNative(yuvImage);
    }

    private native int getSizeNative();

    private native void internalClose();

    private static native boolean setupPDQImageNative(PDQImage pDQImage, int i, int i2, int i3);

    private native void swapUVChannelsNative(boolean z);

    public synchronized void close() {
        synchronized (this) {
            if (this.InternalData != 0) {
                internalClose();
                for (int i = 0; i < this.Planes.length; i++) {
                    this.Planes[i] = null;
                }
                this.Width = 0;
                this.Height = 0;
            }
        }
    }

    protected void finalize() {
        close();
    }

    public int getBitDepth() {
        return this.bitDepth;
    }

    public int getFormat() {
        return this.Format;
    }

    public int getHeight() {
        return this.Height;
    }

    public Plane[] getPlanes() {
        return this.Planes;
    }

    public int getSize() {
        return getSizeNative();
    }

    public int getWidth() {
        return this.Width;
    }

    public void swapUVChannels(boolean z) {
        switch (this.Format) {
            case 17:
            case 35:
                swapUVChannelsNative(z);
                if (!z) {
                    return;
                }
                if (this.Format != 35) {
                    this.Format = 35;
                    return;
                } else {
                    this.Format = 17;
                    return;
                }
            default:
                return;
        }
    }
}
