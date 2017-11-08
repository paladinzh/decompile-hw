package com.avast.android.shepherd.obfuscated;

import android.content.Context;

/* compiled from: Unknown */
public class ac {
    private static ac a;
    private final Context b;
    private final ab c;

    private ac(Context context) {
        this.b = context.getApplicationContext();
        this.c = ab.a(context);
    }

    public static synchronized ac a(Context context) {
        ac acVar;
        synchronized (ac.class) {
            if (a == null) {
                a = new ac(context);
            }
            acVar = a;
        }
        return acVar;
    }

    public void a() {
        if (!(this.c.a() >= System.currentTimeMillis())) {
            x.c("Informing config downloader to download config based on app activity");
            v.a(this.b).a(false);
        }
    }
}
