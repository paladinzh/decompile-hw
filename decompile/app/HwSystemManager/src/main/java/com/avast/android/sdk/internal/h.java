package com.avast.android.sdk.internal;

import android.content.Context;
import android.content.SharedPreferences;

/* compiled from: Unknown */
public class h {
    private static h a;
    private final SharedPreferences b;

    private h(Context context) {
        this.b = context.getSharedPreferences("sdk_engine_settings", 0);
    }

    public static synchronized h a(Context context) {
        h hVar;
        synchronized (h.class) {
            if (a == null) {
                a = new h(context);
            }
            hVar = a;
        }
        return hVar;
    }

    public long a() {
        return this.b.getLong("last_vps_update_time", -1);
    }

    public boolean a(long j) {
        return this.b.edit().putLong("last_vps_update_time", j).commit();
    }

    public boolean a(String str) {
        return this.b.edit().putString("guid", str).commit();
    }

    public String b() {
        return this.b.getString("guid", "");
    }
}
