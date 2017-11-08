package cn.com.xy.sms.sdk.net.util;

import android.content.Context;

/* compiled from: Unknown */
final class d extends Thread {
    private final /* synthetic */ Context a;

    d(Context context) {
        this.a = context;
    }

    public final void run() {
        try {
            c.b(this.a);
        } catch (Throwable th) {
        }
    }
}
