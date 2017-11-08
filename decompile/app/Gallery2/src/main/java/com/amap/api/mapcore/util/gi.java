package com.amap.api.mapcore.util;

/* compiled from: DexDownloadItem */
public class gi {
    String a;
    String b;
    String c;
    String d;
    String e;
    int f;
    int g;
    private String h;
    private String i;

    public gi(String str, String str2, String str3) {
        this.h = str;
        this.i = str2;
        try {
            String[] split = str.split("/");
            int length = split.length;
            if (length > 1) {
                this.a = split[length - 1];
                split = this.a.split("_");
                this.b = split[0];
                this.c = split[2];
                this.d = split[1];
                this.f = Integer.parseInt(split[3]);
                this.g = Integer.parseInt(split[4].split("\\.")[0]);
            }
        } catch (Throwable th) {
            gs.a(th, "DexDownloadItem", "DexDownloadItem");
        }
    }

    String a() {
        return this.h;
    }

    String b() {
        return this.i;
    }

    String c() {
        return this.c;
    }
}
