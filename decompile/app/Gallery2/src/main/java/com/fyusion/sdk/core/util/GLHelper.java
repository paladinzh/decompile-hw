package com.fyusion.sdk.core.util;

/* compiled from: Unknown */
public class GLHelper {
    static {
        System.loadLibrary("opengl-jni");
    }

    public static native void readBufferToPBO(int i, int i2, int i3);
}
