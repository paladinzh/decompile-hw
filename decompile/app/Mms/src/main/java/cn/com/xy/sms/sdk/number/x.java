package cn.com.xy.sms.sdk.number;

import cn.com.xy.sms.sdk.db.entity.n;
import cn.com.xy.sms.sdk.service.number.m;
import cn.com.xy.sms.sdk.util.b;
import java.util.Map;

/* compiled from: Unknown */
public class x extends b {
    private static b b = null;
    private static final int c = 20;

    private x() {
        this.a = 3600000;
    }

    public static b a() {
        synchronized (x.class) {
            if (b == null) {
                b = new x();
            }
        }
        return b;
    }

    private static void a(Map<String, String[]> map) {
        m.a(map, null, new y());
    }

    public final void b() {
        m.a(n.a(20), null, new y());
    }
}
