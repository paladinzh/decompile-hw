package com.avast.android.sdk.engine.obfuscated;

/* compiled from: Unknown */
public class at {
    private final String a;
    private String b;
    private String c;
    private boolean d = true;

    private at(String str) {
        this.a = str;
    }

    public static at a(String str, boolean z) {
        at atVar = new at(str);
        atVar.d = z;
        return atVar;
    }

    public String a() {
        return this.a;
    }

    public void a(String str, String str2) {
        this.b = str;
        this.c = str2;
    }

    public void a(boolean z) {
        this.d = z;
    }

    public String b() {
        return this.b;
    }
}
