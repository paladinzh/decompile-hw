package com.avast.android.sdk.engine.obfuscated;

import java.util.HashMap;

/* compiled from: Unknown */
public class x {
    private HashMap<String, byte[]> a = new HashMap();

    public void a(String str, byte[] bArr) {
        this.a.put(str, bArr);
    }

    public byte[] a(String str) {
        return (byte[]) this.a.get(str);
    }

    public boolean b(String str, byte[] bArr) {
        byte[] bArr2 = (byte[]) this.a.get(str);
        if (bArr2 == null || bArr == null || bArr2.length != bArr.length) {
            return false;
        }
        for (int i = 0; i < bArr2.length; i++) {
            if (bArr2[i] != bArr[i]) {
                return false;
            }
        }
        return true;
    }
}
