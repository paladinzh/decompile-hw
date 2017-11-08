package com.intsig.scanner;

import android.content.Context;
import java.util.HashMap;

/* compiled from: Unknown */
class ScannerEngine {
    static HashMap mCallbacks = new HashMap();
    private static boolean sFinishInit = true;
    static int step = 10;

    static {
        System.loadLibrary("scanner");
    }

    ScannerEngine() {
    }

    public static int decodeImageS(String str) {
        return decodeImageS(str, 2);
    }

    public static native int decodeImageS(String str, int i);

    public static native int destroyThreadContext(int i);

    public static native int detectImageS(int i, int i2, int[] iArr, int i3);

    public static native int encodeImageS(int i, String str, int i2, boolean z);

    public static native int initEngine(Context context, String str);

    public static int initSDKEngine(Context context, String str) {
        return initEngine(context, str);
    }

    public static native int initThreadContext();

    public static native int releaseImageS(int i);

    public static native int trimImageS(int i, int i2, int[] iArr, int i3, int i4);
}
