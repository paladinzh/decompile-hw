package com.huawei.powergenie.integration.adapter;

import android.util.Log;

public class NativeAdapter {
    private static final int CABC_MOVING = 3;
    private static final int CABC_OFF = 0;
    private static final int CABC_STILL = 2;
    private static final int CABC_UI = 1;
    public static final int PLATFORM_HI = 2;
    public static final int PLATFORM_K3V3 = 3;
    public static final int PLATFORM_MTK = 1;
    public static final int PLATFORM_QCOM = 0;
    public static final int PLATFORM_UNKNOWN = -1;
    private static final String TAG = "NativeAdapter";
    private static boolean mNativeAvailabe;

    private static native boolean nativeClearLog();

    private static native int nativeGetCPUCoreCount();

    private static native int nativeGetPlatformType();

    private static native int nativeReadEvent(RawEvent rawEvent, int i);

    private static native int nativeReadPowerLog(byte[] bArr);

    private static native boolean nativeSetCABCMode(int i);

    private static native boolean nativeSetChargeHotLimit(int i, int i2);

    private static native boolean nativeSetFlashLimit(boolean z, boolean z2);

    private static native boolean nativeSetIspLimit(int i);

    private static native boolean nativeSetPAFallback(boolean z);

    private static native long[] nativeUpdateTrafficStats();

    private static native boolean nativeWriteGpuFreq(long j);

    static {
        mNativeAvailabe = true;
        try {
            System.loadLibrary("powergenie_native3");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("WARNING: Could not load libpowergenie_native3.so");
            mNativeAvailabe = false;
        }
    }

    public static int getPlatformType() {
        if (mNativeAvailabe) {
            int type = nativeGetPlatformType();
            if (-1 == type) {
                Log.e(TAG, "Platform Unknown", new Exception("Platform Unknown"));
            }
            return type;
        }
        Log.e(TAG, "native lib is error, unknown platform!");
        return -1;
    }

    public static boolean clearLog() {
        if (mNativeAvailabe) {
            return nativeClearLog();
        }
        return false;
    }

    public static int readPowerLog(byte[] mLogEntry) {
        if (mNativeAvailabe) {
            return nativeReadPowerLog(mLogEntry);
        }
        return -1;
    }

    public static int readHookEvent(RawEvent eventEntry) {
        if (mNativeAvailabe) {
            return nativeReadEvent(eventEntry, 1);
        }
        return -1;
    }

    public static int getCpuCores() {
        if (mNativeAvailabe) {
            return nativeGetCPUCoreCount();
        }
        Log.e(TAG, "native lib is error, default cores 8!");
        return -1;
    }

    public static boolean setCABC(boolean savePower) {
        if (mNativeAvailabe) {
            return nativeSetCABCMode(savePower ? 3 : 1);
        }
        Log.e(TAG, "native lib is error, cabc!");
        return false;
    }

    public static boolean setChargeHotLimit(int mode, int value) {
        if (mNativeAvailabe) {
            return nativeSetChargeHotLimit(mode, value);
        }
        Log.e(TAG, "native lib is error, charge limit!");
        return false;
    }

    public static boolean setFlashLimit(boolean isFront, boolean limit) {
        if (mNativeAvailabe) {
            return nativeSetFlashLimit(isFront, limit);
        }
        Log.e(TAG, "native lib is error, flash limit!");
        return false;
    }

    public static boolean setPAFallback(boolean fallback) {
        if (mNativeAvailabe) {
            return nativeSetPAFallback(fallback);
        }
        Log.e(TAG, "native lib is error, PA fall back!");
        return false;
    }

    public static boolean setIspLimit(int value) {
        if (mNativeAvailabe) {
            return nativeSetIspLimit(value);
        }
        Log.e(TAG, "native lib is error, Isp limit!");
        return false;
    }

    public static boolean writeGpuFreq(int value) {
        if (mNativeAvailabe) {
            return nativeWriteGpuFreq((long) value);
        }
        Log.e(TAG, "native lib is error, write gpu freq");
        return false;
    }

    public static long[] updateTrafficStats() {
        if (mNativeAvailabe) {
            return nativeUpdateTrafficStats();
        }
        return null;
    }
}
