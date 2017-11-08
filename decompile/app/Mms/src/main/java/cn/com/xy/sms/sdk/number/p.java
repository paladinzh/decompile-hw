package cn.com.xy.sms.sdk.number;

import android.os.Process;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.n;
import cn.com.xy.sms.sdk.service.number.a;
import cn.com.xy.sms.sdk.util.b;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* compiled from: Unknown */
public class p extends b {
    private static final int b = 20;
    private static final Map<Integer, b> c = new ConcurrentHashMap();
    private int d = -1;

    private p(int i) {
        this.d = i;
        this.a = Constant.MINUTE;
    }

    public static b a(int i) {
        b bVar;
        synchronized (p.class) {
            bVar = (b) c.get(Integer.valueOf(i));
            if (bVar == null) {
                bVar = new p(i);
                c.put(Integer.valueOf(i), bVar);
            }
        }
        return bVar;
    }

    private static void a(Map<String, String> map) {
        a.a(map, null, new q());
    }

    public final void b() {
        a.a(n.a(this.d, 20), null, new q());
    }

    public final void c() {
        Object obj;
        if (this.d != 1) {
            obj = null;
        } else {
            int i = 1;
        }
        int i2 = obj == null ? 1 : 2;
        int i3 = obj == null ? 19 : 10;
        Thread.currentThread().setPriority(i2);
        Process.setThreadPriority(i3);
    }
}
