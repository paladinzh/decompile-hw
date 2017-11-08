package com.avast.android.shepherd.obfuscated;

import android.content.Context;

/* compiled from: Unknown */
public class v {
    private static v a = null;
    private final w b;

    /* compiled from: Unknown */
    public interface a {
        void onNewConfigDownloaded(Context context, byte[] bArr);
    }

    private v(Context context) {
        this.b = new w(context);
        this.b.start();
    }

    public static synchronized v a(Context context) {
        v vVar;
        synchronized (v.class) {
            if (a == null) {
                a = new v(context);
            }
            vVar = a;
        }
        return vVar;
    }

    public void a(a aVar) {
        this.b.a(aVar);
    }

    public void a(boolean z) {
        this.b.a(z);
    }
}
