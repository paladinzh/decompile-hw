package com.fyusion.sdk.processor.mjpegutils;

import android.graphics.Bitmap;
import java.io.FileDescriptor;
import java.util.concurrent.locks.ReentrantLock;

/* compiled from: Unknown */
public class JPEGTranscoder {
    private static final boolean LOCK = false;
    private static final int LOCK_WAIT_TIME = 40;
    private static final boolean VERBOSE = false;
    private static final ReentrantLock lock = new ReentrantLock(true);
    private long jni_file_pointer_;
    private long jni_jpd_pointer_;
    private long jni_jpe_pointer_;

    static {
        System.loadLibrary("vislib_jni");
    }

    private native int Compress(Bitmap bitmap, int i, int i2, int i3, String str, boolean z);

    private native int CompressToBuffer(Bitmap bitmap, int i, int i2, int i3, boolean z);

    private native int CompressToFile(Bitmap bitmap, int i, int i2, int i3, String str, boolean z, boolean z2, boolean z3);

    private native boolean Decompress(byte[] bArr, Bitmap bitmap);

    private native void DecompressToYUV(byte[] bArr, byte[] bArr2);

    private native void FinishCompress();

    private native void FinishDecompress();

    private native FileDescriptor PrepareFd(String str);

    private native void WriteJPEGToFile(String str);

    private native void WriteJPEGUsingFD(FileDescriptor fileDescriptor);

    public static native void glReadPixelsPBO(int i, int i2, int i3, int i4, int i5, int i6, int i7);

    public native void CreateCompressor();

    public native void CreateDecompressor();

    public FileDescriptor GetFD(String str) {
        return PrepareFd(str);
    }

    public native void closeFilestream(long j);

    public void decodeYUV(byte[] bArr, byte[] bArr2) {
        DecompressToYUV(bArr, bArr2);
    }

    public int encodeJPEG(Bitmap bitmap, int i, String str, boolean z) {
        return Compress(bitmap, bitmap.getWidth(), bitmap.getHeight(), i, str, z);
    }

    public int encodeJPEGtoBuffer(Bitmap bitmap, byte[] bArr, int i, boolean z) {
        return CompressToBuffer(bitmap, bitmap.getWidth(), bitmap.getHeight(), i, z);
    }

    public int encodeJPEGtoFile(Bitmap bitmap, String str, int i, boolean z, boolean z2, boolean z3) {
        return CompressToFile(bitmap, bitmap.getWidth(), bitmap.getHeight(), i, str, z, z2, z3);
    }

    public void finishDecode() {
        FinishDecompress();
    }

    public void finishEncode() {
        FinishCompress();
    }

    public long getFP() {
        return this.jni_file_pointer_;
    }

    public native void getFilePointer(String str);

    public void writeJpeg(String str) {
        WriteJPEGToFile(str);
    }

    public void writeJpegUsingFD(FileDescriptor fileDescriptor) {
        WriteJPEGUsingFD(fileDescriptor);
    }

    public native void writeUsingFilestream(long j);
}
