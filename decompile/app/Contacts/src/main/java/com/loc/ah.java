package com.loc;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/* compiled from: LogUpdateRequest */
public class ah extends bs {
    private byte[] d;

    public ah(byte[] bArr) {
        this.d = (byte[]) bArr.clone();
    }

    private String g() {
        Object bytes;
        try {
            bytes = y.a.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            bytes = y.a.getBytes();
        }
        byte[] bArr = new byte[(bytes.length + 50)];
        System.arraycopy(this.d, 0, bArr, 0, 50);
        System.arraycopy(bytes, 0, bArr, 50, bytes.length);
        return s.a(bArr);
    }

    public Map<String, String> a() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("Content-Type", "application/zip");
        hashMap.put("Content-Length", String.valueOf(this.d.length));
        return hashMap;
    }

    public Map<String, String> b() {
        return null;
    }

    public String c() {
        return String.format(y.b, new Object[]{CallInterceptDetails.BRANDED_STATE, CallInterceptDetails.BRANDED_STATE, CallInterceptDetails.BRANDED_STATE, "open", g()});
    }

    public byte[] d() {
        return this.d;
    }
}
