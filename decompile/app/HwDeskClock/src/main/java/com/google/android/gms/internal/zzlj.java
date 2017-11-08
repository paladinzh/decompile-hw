package com.google.android.gms.internal;

import android.util.Base64;

/* compiled from: Unknown */
public final class zzlj {
    public static String zzi(byte[] bArr) {
        return bArr != null ? Base64.encodeToString(bArr, 0) : null;
    }

    public static String zzj(byte[] bArr) {
        return bArr != null ? Base64.encodeToString(bArr, 10) : null;
    }
}
