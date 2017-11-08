package com.amap.api.mapcore.util;

import java.util.HashMap;
import java.util.Map;

/* compiled from: LogUpdateRequest */
public class cd extends dj {
    private byte[] a;
    private String b = "1";

    public cd(byte[] bArr) {
        this.a = (byte[]) bArr.clone();
    }

    public cd(byte[] bArr, String str) {
        this.a = (byte[]) bArr.clone();
        this.b = str;
    }

    private String e() {
        Object a = bx.a(ca.a);
        byte[] bArr = new byte[(a.length + 50)];
        System.arraycopy(this.a, 0, bArr, 0, 50);
        System.arraycopy(a, 0, bArr, 50, a.length);
        return bs.a(bArr);
    }

    public Map<String, String> c() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("Content-Type", "application/zip");
        hashMap.put("Content-Length", String.valueOf(this.a.length));
        return hashMap;
    }

    public Map<String, String> b() {
        return null;
    }

    public String a() {
        return String.format(ca.b, new Object[]{"1", this.b, "1", "open", e()});
    }

    public byte[] a_() {
        return this.a;
    }
}
