package org.mtnwrw.pdqimg;

import java.nio.ByteBuffer;

/* compiled from: Unknown */
public class DecompressionService {
    private static boolean NativeOK;

    /* compiled from: Unknown */
    public static class DecompressError extends Exception {
        DecompressError(String str) {
            super(str);
        }
    }

    /* compiled from: Unknown */
    public static class ImageInformation {
        public int Height;
        public int ImageType;
        public int Quality;
        public int StreamSize;
        public int UsedBits;
        public int Width;
    }

    static {
        NativeOK = false;
        try {
            System.loadLibrary("pdqimg");
            NativeOK = true;
        } catch (Throwable e) {
            e.printStackTrace();
        } catch (Throwable e2) {
            e2.printStackTrace();
        }
    }

    public static PDQImage decompressImage(ByteBuffer byteBuffer) throws DecompressError {
        return decompressImage(byteBuffer, 0, byteBuffer.limit());
    }

    public static PDQImage decompressImage(ByteBuffer byteBuffer, int i, int i2) throws DecompressError {
        PDQImage decompressImageNativeOffsetSize = decompressImageNativeOffsetSize(byteBuffer, i, i2);
        if (decompressImageNativeOffsetSize != null) {
            return decompressImageNativeOffsetSize;
        }
        throw new DecompressError("Unable to decompress PDQ image from bitstream");
    }

    public static PDQImage decompressImage(ByteBuffer byteBuffer, int i, int i2, int i3, int i4) throws DecompressError {
        PDQImage decompressImageNativeOffsetSizeHint = decompressImageNativeOffsetSizeHint(byteBuffer, i, i2, i3, i4);
        if (decompressImageNativeOffsetSizeHint != null) {
            return decompressImageNativeOffsetSizeHint;
        }
        throw new DecompressError("Unable to decompress PDQ image from bitstream");
    }

    public static void decompressImage(ByteBuffer byteBuffer, PDQImage pDQImage) throws DecompressError {
        decompressImage(byteBuffer, pDQImage, 0, byteBuffer.limit());
    }

    public static void decompressImage(ByteBuffer byteBuffer, PDQImage pDQImage, int i, int i2) throws DecompressError {
        if (!decompressToImageNativeOffsetSize(byteBuffer, pDQImage, i, i2)) {
            throw new DecompressError("Unable to decompress PDQ image from bitstream");
        }
    }

    private static native PDQImage decompressImageNativeOffsetSize(ByteBuffer byteBuffer, int i, int i2);

    private static native PDQImage decompressImageNativeOffsetSizeHint(ByteBuffer byteBuffer, int i, int i2, int i3, int i4);

    public static PDQImage decompressPreviewImage(ByteBuffer byteBuffer) throws DecompressError {
        PDQImage decompressPreviewNative = decompressPreviewNative(byteBuffer);
        if (decompressPreviewNative != null) {
            return decompressPreviewNative;
        }
        throw new DecompressError("Unable to decompress PDQ image from bitstream");
    }

    private static native PDQImage decompressPreviewNative(ByteBuffer byteBuffer);

    public static PDQImage decompressThumbnailImage(ByteBuffer byteBuffer) throws DecompressError {
        PDQImage decompressThumbnailNative = decompressThumbnailNative(byteBuffer);
        if (decompressThumbnailNative != null) {
            return decompressThumbnailNative;
        }
        throw new DecompressError("Unable to decompress PDQ image from bitstream");
    }

    private static native PDQImage decompressThumbnailNative(ByteBuffer byteBuffer);

    private static native boolean decompressToImageNativeOffsetSize(ByteBuffer byteBuffer, PDQImage pDQImage, int i, int i2);

    public static ImageInformation getImageInformation(ByteBuffer byteBuffer) throws DecompressError {
        if (!NativeOK) {
            return null;
        }
        ImageInformation imageInformationNative = getImageInformationNative(byteBuffer);
        if (imageInformationNative != null) {
            return imageInformationNative;
        }
        throw new DecompressError("Unable to obtain image information for bitstream");
    }

    public static ImageInformation getImageInformation(byte[] bArr) throws DecompressError {
        if (!NativeOK) {
            return null;
        }
        ImageInformation imageInformationNativeBytes = getImageInformationNativeBytes(bArr);
        if (imageInformationNativeBytes != null) {
            return imageInformationNativeBytes;
        }
        throw new DecompressError("Unable to obtain image information for bitstream");
    }

    private static native ImageInformation getImageInformationNative(ByteBuffer byteBuffer);

    private static native ImageInformation getImageInformationNativeBytes(byte[] bArr);

    public static boolean initialize(int i) {
        return NativeOK ? setupDecompression(i) : false;
    }

    private static native boolean setupDecompression(int i);
}
