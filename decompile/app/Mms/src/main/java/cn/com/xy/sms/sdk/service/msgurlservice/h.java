package cn.com.xy.sms.sdk.service.msgurlservice;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.util.D;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
final class h implements Runnable {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ Map d;
    private final /* synthetic */ SdkCallBack e;
    private final /* synthetic */ D f;
    private final /* synthetic */ long g;

    h(String str, String str2, String str3, Map map, SdkCallBack sdkCallBack, D d, long j) {
        this.a = str;
        this.b = str2;
        this.c = str3;
        this.d = map;
        this.e = sdkCallBack;
        this.f = d;
        this.g = j;
    }

    public final void run() {
        try {
            a.a("xy_msgUrlPool_1", 10);
            JSONObject a = MsgUrlService.b(this.a, this.b, this.c);
            if (MsgUrlService.b(a)) {
                XyUtil.doXycallBackResult(this.e, Integer.valueOf(1), a, this.a, Integer.valueOf(16));
                this.f.q.remove(this.a);
                this.f.p.put(this.a, a);
                return;
            }
            MsgUrlService.a(this.a, this.b, this.c, this.d, a, (XyCallBack) new i(this, this.f, this.a, this.b, this.c, this.g, this.e));
        } catch (Throwable th) {
            this.f.q.remove(this.a);
        }
    }
}
