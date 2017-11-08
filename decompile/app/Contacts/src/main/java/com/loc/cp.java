package com.loc;

import java.util.Map;

/* compiled from: LocationRequest */
public class cp extends bs {
    Map<String, String> d = null;
    Map<String, String> e = null;
    String f = "";
    byte[] g = null;

    public Map<String, String> a() {
        return this.d;
    }

    public void a(String str) {
        this.f = str;
    }

    public void a(Map<String, String> map) {
        this.d = map;
    }

    public void a(byte[] bArr) {
        this.g = bArr;
    }

    public Map<String, String> b() {
        return this.e;
    }

    public String c() {
        return this.f;
    }

    public byte[] d() {
        return this.g;
    }
}
