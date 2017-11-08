package cn.com.xy.sms.sdk.db.entity.a;

import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.service.e.b;
import cn.com.xy.sms.sdk.service.e.g;
import cn.com.xy.sms.util.SdkCallBack;

/* compiled from: Unknown */
final class h implements Runnable {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String e;
    private final /* synthetic */ SdkCallBack f;
    private final /* synthetic */ boolean g;

    h(String str, String str2, String str3, String str4, String str5, SdkCallBack sdkCallBack, boolean z) {
        this.a = str;
        this.b = str2;
        this.c = str3;
        this.d = str4;
        this.e = str5;
        this.f = sdkCallBack;
        this.g = z;
    }

    public final void run() {
        try {
            a.a("xy_query_pubinfo_1", 10);
            if (NetUtil.isEnhance()) {
                b.a(false, this.a, this.b, this.c, this.d, this.e, this.f, 1, false, this.g, false);
            } else if (this.f != null) {
                this.f.execute(new Object[0]);
            }
            if (NetUtil.checkAccessNetWork(2)) {
                g.a(this.c, this.d);
            }
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
