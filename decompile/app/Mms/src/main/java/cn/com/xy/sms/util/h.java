package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.iccid.IccidLocationUtil;
import cn.com.xy.sms.sdk.net.a;
import cn.com.xy.sms.sdk.net.util.n;
import cn.com.xy.sms.sdk.queue.i;
import cn.com.xy.sms.sdk.queue.k;
import cn.com.xy.sms.sdk.util.DateUtils;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.g;
import java.util.Map;

/* compiled from: Unknown */
final class h implements Runnable {
    private final /* synthetic */ Context a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ boolean d;
    private final /* synthetic */ boolean e;
    private final /* synthetic */ Map f;

    h(Context context, String str, String str2, boolean z, boolean z2, Map map) {
        this.a = context;
        this.b = str;
        this.c = str2;
        this.d = z;
        this.e = z2;
        this.f = map;
    }

    public final void run() {
        try {
            Context applicationContext = this.a.getApplicationContext();
            if (applicationContext == null) {
                applicationContext = this.a;
            }
            SysParamEntityManager.initParams(applicationContext, this.b, this.c, this.d, this.e, this.f);
            String str = this.b;
            n.b();
            i.a();
            g.c();
            DexUtil.beforeInitBigJar();
            if ("GwIDAQABZTE".equals(KeyManager.channel) || "1w36SBLwVNEW_ZTE".equals(KeyManager.channel)) {
                ParseManager.a(DateUtils.getCurrentTimeString("yyyyMMdd"), DexUtil.getBubbleViewVersion(null));
            }
            i.a(new k(2, new String[0]));
            i.a(new k(7, new String[0]));
            IccidLocationUtil.changeIccidAreaCode(true);
            a.getDeviceId(false);
            if (!SysParamEntityManager.getBooleanParam(applicationContext, Constant.ISCLEANOLDTOKEN, false)) {
                ParseManager.cleanToken(this.a);
                SysParamEntityManager.setParam(Constant.ISCLEANOLDTOKEN, "true");
            }
        } catch (Throwable th) {
        }
    }
}
