package com.huawei.systemmanager.applock.fingerprint;

import android.content.Context;

public class FingerprintAdapter {
    public static IFingerprintAuth create(Context ctx) {
        return FingerprintGoogleAPI.create(ctx);
    }
}
