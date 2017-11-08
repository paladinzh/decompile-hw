package com.amap.api.mapcore.util;

import java.util.HashMap;
import java.util.Map;

/* compiled from: LogUpdateRequest */
public class fn extends hd {
    private byte[] a;
    private String b = "1";

    public fn(byte[] bArr) {
        this.a = (byte[]) bArr.clone();
    }

    public fn(byte[] bArr, String str) {
        this.a = (byte[]) bArr.clone();
        this.b = str;
    }

    private String d() {
        Object a = fi.a(fk.a);
        byte[] bArr = new byte[(a.length + 50)];
        System.arraycopy(this.a, 0, bArr, 0, 50);
        System.arraycopy(a, 0, bArr, 50, a.length);
        return fe.a(bArr);
    }

    public Map<String, String> a() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("Content-Type", "application/zip");
        hashMap.put("Content-Length", String.valueOf(this.a.length));
        return hashMap;
    }

    public Map<String, String> b() {
        return null;
    }

    public String c() {
        return String.format(fk.b, new Object[]{"1", this.b, "1", "open", d()});
    }

    public byte[] g() {
        return this.a;
    }
}
