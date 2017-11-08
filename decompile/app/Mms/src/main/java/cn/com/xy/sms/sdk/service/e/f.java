package cn.com.xy.sms.sdk.service.e;

import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.db.entity.E;
import cn.com.xy.sms.sdk.db.entity.H;

/* compiled from: Unknown */
final class f implements Runnable {
    private final /* synthetic */ H a;

    f(H h) {
        this.a = h;
    }

    public final void run() {
        try {
            a.a("xy_update_pubinfo_1", 10);
            Thread.sleep(2000);
            E.a(this.a);
            synchronized (g.a) {
                b.a = false;
            }
        } catch (Throwable th) {
            synchronized (g.a) {
                b.a = false;
            }
        }
    }
}
