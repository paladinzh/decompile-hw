package cn.com.xy.sms.sdk.net.util;

import cn.com.xy.sms.sdk.db.entity.f;
import java.util.Map;

/* compiled from: Unknown */
final class p implements Runnable {
    p() {
    }

    public final void run() {
        Map b = f.b();
        if (b != null) {
            n.a.putAll(b);
        }
    }
}
