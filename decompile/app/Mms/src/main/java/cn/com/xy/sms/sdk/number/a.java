package cn.com.xy.sms.sdk.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.service.number.m;
import java.util.Map;

/* compiled from: Unknown */
final class A implements Runnable {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ int c;
    private final /* synthetic */ Map d;
    private final /* synthetic */ XyCallBack e;

    A(String str, String str2, int i, Map map, XyCallBack xyCallBack) {
        this.a = str;
        this.b = str2;
        this.c = i;
        this.d = map;
        this.e = xyCallBack;
    }

    public final void run() {
        m.a(z.a(this.a, this.b, this.c), this.d, new B(this, this.e, this.a));
    }
}
