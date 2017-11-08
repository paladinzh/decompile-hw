package cn.com.xy.sms.sdk.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.util.b;

/* compiled from: Unknown */
final class t implements Runnable {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ XyCallBack c;

    t(String str, String str2, XyCallBack xyCallBack) {
        this.a = str;
        this.b = str2;
        this.c = xyCallBack;
    }

    public final void run() {
        try {
            String b = b.b();
            String a = r.b(b);
            if (a == null || !this.a.equals(a)) {
                b.b(this.b);
                if (!this.a.equals(r.b(b))) {
                    r.a(this.c, -6, "digest mimatch");
                    return;
                }
            }
            r.a(b.c());
            b.a(r.k);
            r.a(this.c, 2, r.k);
            k.a();
        } catch (Throwable th) {
            r.a(this.c, -10, "fetchAndUpdate unknown");
        }
    }
}
