package com.fyusion.sdk.share;

/* compiled from: Unknown */
public class FyuseUser {
    private String a;
    private String b;
    private String c;
    private String d;

    FyuseUser() {
    }

    protected void a(String str) {
        this.a = str;
    }

    protected void b(String str) {
        this.b = str;
    }

    protected void c(String str) {
        this.c = str;
    }

    protected void d(String str) {
        this.d = str;
    }

    public String getEmail() {
        return this.c;
    }

    public String getName() {
        return this.b;
    }

    public String getThumbnailUrl() {
        return this.d;
    }

    public String getUsername() {
        return this.a;
    }
}
