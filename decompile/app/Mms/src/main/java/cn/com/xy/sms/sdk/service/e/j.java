package cn.com.xy.sms.sdk.service.e;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.net.NetUtil;

/* compiled from: Unknown */
final class j implements Runnable {
    private final /* synthetic */ boolean a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String e;
    private final /* synthetic */ int f;
    private final /* synthetic */ XyCallBack g;
    private final /* synthetic */ boolean h;

    j(boolean z, String str, String str2, String str3, String str4, int i, XyCallBack xyCallBack, boolean z2) {
        this.a = z;
        this.b = str;
        this.c = str2;
        this.d = str3;
        this.e = str4;
        this.f = i;
        this.g = xyCallBack;
        this.h = z2;
    }

    public final void run() {
        try {
            a.a("xy_query_pubinfo_1", 10);
            if ((NetUtil.isEnhance() && this.a) || NetUtil.checkAccessNetWork(1)) {
                b.a(false, this.b, this.c, this.d, this.e, String.valueOf(this.f), this.g, 0, false, false, this.h);
            }
        } catch (Throwable th) {
        }
    }
}
