package com.huawei.systemmanager.spacecleanner.engine.jpeg;

public final class JpegNative {
    public static final String TAG = "JpegNative";
    private static JpegNative mInstance;

    private native int checkBlur(byte[] bArr, int i, int i2);

    private native int initBlurEnv();

    private native int initTransCodeEnv();

    private native int transcode(String str, Object obj);

    private native int uninitBlurEnv();

    private native int uninitTransCodeEnv();

    public static synchronized JpegNative getInstance() {
        JpegNative jpegNative;
        synchronized (JpegNative.class) {
            if (mInstance == null) {
                mInstance = new JpegNative();
            }
            jpegNative = mInstance;
        }
        return jpegNative;
    }

    static {
        System.loadLibrary("HwJpeg");
    }

    public int transPhotocode(String filePath, JpegCode outParam) {
        return transcode(filePath, outParam);
    }

    public int checkPhotoBlur(byte[] buffer, int width, int height) {
        return checkBlur(buffer, width, height);
    }

    public int initPhotoBlurEnv() {
        return initBlurEnv();
    }

    public int uninitPhotoBlurEnv() {
        return uninitBlurEnv();
    }

    public void initPhotoTransCodeEnv() {
        initTransCodeEnv();
    }

    public void uninitPhotoTransCodeEnv() {
        uninitTransCodeEnv();
    }
}
