package com.dts.hpx.lite.sdk.thin;

public class HpxLiteSdkThin {
    public native int getHpxEnabled();

    public native int setHpxEnabled(int i);

    static {
        System.loadLibrary("dts_hpx_service_c");
        System.loadLibrary("hpx-lite-sdk-thin-jni");
    }
}
