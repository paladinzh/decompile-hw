package cn.com.xy.sms.sdk.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.entity.n;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.Map;

/* compiled from: Unknown */
final class m implements Runnable {
    private final /* synthetic */ boolean a;
    private final /* synthetic */ String b;
    private final /* synthetic */ XyCallBack c;
    private final /* synthetic */ Map d;

    m(boolean z, String str, XyCallBack xyCallBack, Map map) {
        this.a = z;
        this.b = str;
        this.c = xyCallBack;
        this.d = map;
    }

    public final void run() {
        if (!this.a) {
            Object[] b = l.g(this.b);
            if (b != null) {
                XyUtil.doXycallBackResult(this.c, b);
                return;
            }
        }
        if (NetUtil.checkAccessNetWork(2)) {
            l.b(this.b, this.d, this.c);
            return;
        }
        l.h(this.b);
        n.a(this.b, null, "", System.currentTimeMillis());
        XyUtil.doXycallBackResult(this.c, Integer.valueOf(-10), this.b, "no network");
    }
}
