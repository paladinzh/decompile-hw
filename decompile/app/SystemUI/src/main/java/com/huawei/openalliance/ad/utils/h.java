package com.huawei.openalliance.ad.utils;

import android.content.Context;
import android.content.SharedPreferences;
import fyusion.vislib.BuildConfig;

/* compiled from: Unknown */
public class h {
    private static h b;
    private SharedPreferences a;

    private h(Context context) {
        this.a = context.getSharedPreferences("HiAdSharedPreferences", 0);
    }

    public static synchronized h a(Context context) {
        h hVar;
        synchronized (h.class) {
            Context applicationContext = context.getApplicationContext();
            if (b == null) {
                b = new h(applicationContext);
            }
            hVar = b;
        }
        return hVar;
    }

    public int a() {
        return this.a.getInt("splash_cache_num", 10);
    }

    public void a(String str) {
        this.a.edit().putString("maglock_show_id", str).commit();
    }

    public int e() {
        return this.a.getInt("splash_real_pre_num", 10);
    }

    public int h() {
        return this.a.getInt("gif_size_upper_limit", 2048);
    }

    public int i() {
        return this.a.getInt("img_size_upper_limit", 500);
    }

    public String k() {
        return this.a.getString("maglock_show_id", BuildConfig.FLAVOR);
    }
}
