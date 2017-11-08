package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class ik {
    private static ik rY = null;

    private ik() {
    }

    public static synchronized ik bM() {
        ik ikVar;
        synchronized (ik.class) {
            if (rY == null) {
                rY = new ik();
            }
            ikVar = rY;
        }
        return ikVar;
    }

    public void b(Context context) {
        fu u = fu.u();
        u.setContext(context);
        u.b(d.isEnable(), "TMSLog");
        u.c(d.isEnable());
        u.d(false);
        u.e(false);
        u.f(true);
        u.g(true);
        u.h(false);
        u.i(false);
        u.j(false);
        u.af("tms.pService");
        u.ag("_tms");
        u.k(true);
        u.a(null);
        u.l(true);
        if (VERSION.SDK_INT < 21) {
            u.m(true);
        } else {
            u.m(false);
        }
        u.a(new Intent("com.tencent.tmsecure.ACTION_PKG_MONITOR"));
    }
}
