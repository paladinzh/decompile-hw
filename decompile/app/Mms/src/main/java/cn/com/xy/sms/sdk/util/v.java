package cn.com.xy.sms.sdk.util;

import cn.com.xy.sms.sdk.number.d;
import cn.com.xy.sms.sdk.number.p;
import cn.com.xy.sms.sdk.number.x;
import java.util.concurrent.ExecutorService;

/* compiled from: Unknown */
public final class v {
    public static final b a = p.a(1);
    public static final b b = p.a(2);
    public static final b c = p.a(3);
    public static final b d = x.a();
    public static final b e = d.a();
    public static final b f = cn.com.xy.sms.sdk.number.v.a();

    public static void a(ExecutorService executorService, b bVar) {
        if (executorService != null && bVar != null && !bVar.f()) {
            executorService.execute(bVar);
        }
    }
}
