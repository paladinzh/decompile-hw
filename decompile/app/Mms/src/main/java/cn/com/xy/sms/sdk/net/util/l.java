package cn.com.xy.sms.sdk.net.util;

import cn.com.xy.sms.sdk.db.entity.E;
import cn.com.xy.sms.sdk.db.entity.H;

/* compiled from: Unknown */
final class l implements Runnable {
    private final /* synthetic */ String a;
    private final /* synthetic */ H b;
    private final /* synthetic */ int c;

    l(String str, H h, int i) {
        this.a = str;
        this.b = h;
        this.c = i;
    }

    public final void run() {
        try {
            E.a(this.a, this.b, this.c);
        } catch (Throwable th) {
        }
    }
}
