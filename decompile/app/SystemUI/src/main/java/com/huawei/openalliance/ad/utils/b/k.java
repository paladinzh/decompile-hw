package com.huawei.openalliance.ad.utils.b;

import fyusion.vislib.BuildConfig;

/* compiled from: Unknown */
public class k {
    private StringBuilder a = new StringBuilder(64);

    public static k a() {
        return new k();
    }

    public <T> k a(T t) {
        if (this.a != null) {
            this.a.append(t);
        }
        return this;
    }

    public k b() {
        return a(Character.valueOf('\n'));
    }

    public String c() {
        if (this.a == null) {
            return BuildConfig.FLAVOR;
        }
        String stringBuilder = this.a.toString();
        this.a = null;
        return stringBuilder;
    }

    public String toString() {
        return this.a != null ? this.a.toString() : BuildConfig.FLAVOR;
    }
}
