package cn.com.xy.sms.sdk.net;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.a.a;

/* compiled from: Unknown */
final class j implements Runnable {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ XyCallBack c;

    j(String str, String str2, XyCallBack xyCallBack) {
        this.a = str;
        this.b = str2;
        this.c = xyCallBack;
    }

    public final void run() {
        try {
            a.a("xy-netWebPool", 10);
            NetWebUtil.b(this.a, this.b, this.c);
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
