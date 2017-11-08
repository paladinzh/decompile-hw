package cn.com.xy.sms.sdk.service.e;

import cn.com.xy.sms.sdk.net.NetUtil;

/* compiled from: Unknown */
final class i implements Runnable {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;

    i(String str, String str2) {
        this.a = str;
        this.b = str2;
    }

    public final void run() {
        try {
            if (NetUtil.checkAccessNetWork(2)) {
                g.a(this.a, this.b);
            }
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
