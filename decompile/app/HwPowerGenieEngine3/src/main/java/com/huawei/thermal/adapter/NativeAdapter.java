package com.huawei.thermal.adapter;

import android.util.Log;

public class NativeAdapter {
    private static boolean mNativeAvailabe;

    private static native int nativeGetPlatformType();

    private static native boolean nativeSetFlashLimit(boolean z, boolean z2);

    static {
        mNativeAvailabe = true;
        try {
            System.loadLibrary("powergenie_native3");
        } catch (UnsatisfiedLinkError ule) {
            System.err.println("WARNING: Could not load powergenie_native3 " + ule);
            mNativeAvailabe = false;
        }
    }

    public static int getPlatformType() {
        if (mNativeAvailabe) {
            int type = nativeGetPlatformType();
            if (-1 == type) {
                Log.e("NativeAdapter", "Platform Unknown", new Exception("Platform Unknown"));
            }
            return type;
        }
        Log.e("NativeAdapter", "native lib is error, unknown platform!");
        return -1;
    }

    public static boolean setFlashLimit(boolean isFront, boolean limit) {
        if (mNativeAvailabe) {
            return nativeSetFlashLimit(isFront, limit);
        }
        Log.e("NativeAdapter", "native lib is error, flash limit!");
        return false;
    }
}
