package com.amap.api.services.core;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/* compiled from: LogUpdateRequest */
public class bf extends cj {
    private byte[] a;

    public bf(byte[] bArr) {
        this.a = (byte[]) bArr.clone();
    }

    private String a() {
        Object bytes;
        try {
            bytes = au.a.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            bytes = au.a.getBytes();
        }
        byte[] bArr = new byte[(bytes.length + 50)];
        System.arraycopy(this.a, 0, bArr, 0, 50);
        System.arraycopy(bytes, 0, bArr, 50, bytes.length);
        return ap.a(bArr);
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

    public String g() {
        return String.format(au.b, new Object[]{"1", "1", "1", "open", a()});
    }

    public byte[] f() {
        return this.a;
    }
}
