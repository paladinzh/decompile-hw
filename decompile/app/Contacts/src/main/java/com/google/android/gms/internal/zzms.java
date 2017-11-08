package com.google.android.gms.internal;

import android.database.CharArrayBuffer;
import android.text.TextUtils;

/* compiled from: Unknown */
public final class zzms {
    public static void zzb(String str, CharArrayBuffer charArrayBuffer) {
        if (TextUtils.isEmpty(str)) {
            charArrayBuffer.sizeCopied = 0;
        } else if (charArrayBuffer.data != null && charArrayBuffer.data.length >= str.length()) {
            str.getChars(0, str.length(), charArrayBuffer.data, 0);
        } else {
            charArrayBuffer.data = str.toCharArray();
        }
        charArrayBuffer.sizeCopied = str.length();
    }
}
