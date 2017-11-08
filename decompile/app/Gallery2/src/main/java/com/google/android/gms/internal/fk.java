package com.google.android.gms.internal;

import android.util.Base64;

/* compiled from: Unknown */
public final class fk {
    public static String d(byte[] bArr) {
        return bArr != null ? Base64.encodeToString(bArr, 0) : null;
    }

    public static String e(byte[] bArr) {
        return bArr != null ? Base64.encodeToString(bArr, 10) : null;
    }
}
