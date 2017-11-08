package cn.com.xy.sms.sdk.number;

import android.content.Context;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.util.b;

/* compiled from: Unknown */
final class c implements Runnable {
    private final /* synthetic */ Context a;

    c(Context context) {
        this.a = context;
    }

    public final void run() {
        try {
            if (b.b(this.a)) {
                b.e = false;
                b.f = true;
                return;
            }
            b.a(this.a);
            SysParamEntityManager.setParam("init_embed_number", "0");
            b.a(b.d());
            SysParamEntityManager.setParam("init_embed_number", "1");
        } catch (Throwable th) {
        } finally {
            b.e = false;
            b.f = true;
        }
    }
}
