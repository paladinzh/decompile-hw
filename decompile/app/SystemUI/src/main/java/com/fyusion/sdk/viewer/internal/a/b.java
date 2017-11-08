package com.fyusion.sdk.viewer.internal.a;

import android.util.Log;
import com.fyusion.sdk.common.a.a.f;
import com.fyusion.sdk.common.a.a.k;
import com.fyusion.sdk.common.a.a.l;
import com.fyusion.sdk.common.p;
import com.fyusion.sdk.viewer.internal.b.c.a;
import fyusion.vislib.BuildConfig;

/* compiled from: Unknown */
public class b {
    public static void a(a aVar) {
        if (aVar != null) {
            f.a().a(l.a("VIEW", aVar.d(), aVar.j()), new f.a<l>() {
                public void a(l lVar) {
                    lVar.e = "VIEW_END";
                    lVar.f = f.b();
                }
            });
        } else {
            Log.w("Fyulytics", "Unable to track View End Event. FyuseData is null");
        }
    }

    public static void a(a aVar, int i, int i2) {
        if (aVar != null) {
            int j = aVar.j();
            d lVar = new l(aVar.d());
            lVar.a = f.a(i, i2);
            lVar.b = j;
            p magic = aVar.n().getMagic();
            String uniqueDeviceId = magic.getUniqueDeviceId();
            if (!(BuildConfig.FLAVOR.equals(uniqueDeviceId) || BuildConfig.FLAVOR.equals(com.fyusion.sdk.common.a.a(uniqueDeviceId)))) {
                lVar.g = com.fyusion.sdk.common.a.a(uniqueDeviceId);
            }
            lVar.c = com.fyusion.sdk.common.a.a().a(magic.getDeviceId(), uniqueDeviceId);
            if (!(lVar.c == 1 || lVar.c == 2)) {
                com.fyusion.sdk.common.a.a();
                lVar.d = com.fyusion.sdk.common.a.b(uniqueDeviceId);
                lVar.k = magic.getDeviceId();
            }
            if (f.a().b(lVar)) {
                f.a().a(l.b(lVar));
            }
            return;
        }
        Log.w("Fyulytics", "Unable to track View Start Event. FyuseData is null");
    }

    public static void a(String str, int i) {
        f.a().a(new k(str, i));
    }
}
