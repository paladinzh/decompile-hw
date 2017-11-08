package cn.com.xy.sms.sdk.db.entity.a;

import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.service.e.g;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
final class d implements Runnable {
    private final /* synthetic */ String a;

    d(String str) {
        this.a = str;
    }

    public final void run() {
        try {
            a.a("xy_query_pubinfo_1", 10);
            Map hashMap = new HashMap();
            hashMap.put("SUPPORT_NET_QUERY", "1");
            Constant.getContext();
            g.a(this.a, 1, null, hashMap, null);
        } catch (Throwable th) {
        }
    }
}
