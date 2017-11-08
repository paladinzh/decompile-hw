package cn.com.xy.sms.util;

import android.os.Process;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.entity.k;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.service.e.g;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.Map;

/* compiled from: Unknown */
final class i implements Runnable {
    private final /* synthetic */ Map a;
    private final /* synthetic */ SdkCallBack b;

    i(Map map, SdkCallBack sdkCallBack) {
        this.a = map;
        this.b = sdkCallBack;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void run() {
        try {
            Thread.currentThread().setName("xiaoyuan_pool_netutil");
            Process.setThreadPriority(cn.com.xy.sms.sdk.queue.i.b);
            if (!NetUtil.checkAccessNetWork(this.a)) {
                XyUtil.doXycallBack(this.b, ThemeUtil.SET_NULL_STR);
            } else if (ParseItemManager.isInitData()) {
                g.a(this.a, null);
                k c = cn.com.xy.sms.sdk.db.i.c();
                if (!cn.com.xy.sms.sdk.db.i.c(c)) {
                    Map map = this.a;
                    if (!cn.com.xy.sms.sdk.util.k.b(false, false)) {
                        XyUtil.doXycallBack(this.b, "2");
                    }
                }
                if (cn.com.xy.sms.sdk.db.i.c(c)) {
                    cn.com.xy.sms.sdk.db.i.a(c);
                }
                Map map2 = this.a;
                if (cn.com.xy.sms.sdk.util.k.c()) {
                    cn.com.xy.sms.sdk.util.k.a(this.a, this.b);
                } else if (cn.com.xy.sms.sdk.db.i.c(cn.com.xy.sms.sdk.db.i.c())) {
                    XyUtil.doXycallBack(this.b, "0");
                } else {
                    XyUtil.doXycallBack(this.b, "1");
                }
            } else {
                XyUtil.doXycallBack(this.b, "2");
            }
            ParseManager.isupdateData = false;
        } catch (Throwable th) {
            ParseManager.isupdateData = false;
        }
    }
}
