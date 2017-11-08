package com.amap.api.services.core;

import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;

/* compiled from: LogUpdateRequest */
public class bh extends bt {
    private byte[] a;

    public bh(byte[] bArr) {
        this.a = (byte[]) bArr.clone();
    }

    private String f() {
        Object bytes = au.a.getBytes();
        byte[] bArr = new byte[(bytes.length + 50)];
        System.arraycopy(this.a, 0, bArr, 0, 50);
        System.arraycopy(bytes, 0, bArr, 50, bytes.length);
        return ab.a(bArr);
    }

    public Map<String, String> d_() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("Content-Type", "application/zip");
        hashMap.put("Content-Length", String.valueOf(this.a.length));
        return hashMap;
    }

    public Map<String, String> c_() {
        return null;
    }

    public String b() {
        return String.format(au.b, new Object[]{"1", "1", "1", "open", f()});
    }

    public byte[] e_() {
        return this.a;
    }

    public HttpEntity e() {
        return null;
    }
}
