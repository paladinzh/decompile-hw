package cn.com.xy.sms.sdk.service.a;

import android.content.Context;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.a.a;
import java.util.Map;

/* compiled from: Unknown */
final class c implements Runnable {
    private final /* synthetic */ Context a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ long e;
    private final /* synthetic */ Map f;
    private final /* synthetic */ XyCallBack g;

    c(Context context, String str, String str2, String str3, long j, Map map, XyCallBack xyCallBack) {
        this.a = context;
        this.b = str;
        this.c = str2;
        this.d = str3;
        this.e = j;
        this.f = map;
        this.g = xyCallBack;
    }

    public final void run() {
        try {
            a.a("xy_feature_parse_1", 10);
            if (b.b(this.a, this.b, this.c, this.d, this.e, this.f) != null) {
                this.g.execute(r0);
            }
        } catch (Throwable th) {
        }
    }
}
