package com.fyusion.sdk.viewer.internal.a;

import android.util.Log;
import com.fyusion.sdk.common.internal.analytics.Fyulytics;
import com.fyusion.sdk.common.internal.analytics.h;
import com.fyusion.sdk.common.internal.analytics.i;
import com.fyusion.sdk.common.n;
import com.fyusion.sdk.viewer.internal.b.c.a;

/* compiled from: Unknown */
public class b {
    public static void a(a aVar) {
        if (aVar != null) {
            Fyulytics.sharedInstance().endEvent(i.a("VIEW", aVar.d(), aVar.j()), new Fyulytics.a<i>() {
                public void a(i iVar) {
                    iVar.key = "VIEW_END";
                    iVar.timestamp = Fyulytics.currentTimestampMs();
                }
            });
        } else {
            Log.w(Fyulytics.TAG, "Unable to track View End Event. FyuseData is null");
        }
    }

    public static void a(a aVar, int i, int i2) {
        if (aVar != null) {
            int j = aVar.j();
            Event iVar = new i(aVar.d());
            iVar.a = Fyulytics.makeSizeString(i, i2);
            iVar.b = j;
            n magic = aVar.n().getMagic();
            String uniqueDeviceId = magic.getUniqueDeviceId();
            if (!("".equals(uniqueDeviceId) || "".equals(com.fyusion.sdk.common.a.a(uniqueDeviceId)))) {
                iVar.uid = com.fyusion.sdk.common.a.a(uniqueDeviceId);
            }
            iVar.c = com.fyusion.sdk.common.a.a().a(magic.getDeviceId(), uniqueDeviceId);
            if (!(iVar.c == 1 || iVar.c == 2)) {
                com.fyusion.sdk.common.a.a();
                iVar.d = com.fyusion.sdk.common.a.b(uniqueDeviceId);
                iVar.e = magic.getDeviceId();
            }
            if (Fyulytics.sharedInstance().startEvent(iVar)) {
                Fyulytics.sharedInstance().recordEvent(i.a(iVar));
            }
            return;
        }
        Log.w(Fyulytics.TAG, "Unable to track View Start Event. FyuseData is null");
    }

    public static void a(String str, int i) {
        Fyulytics.sharedInstance().recordEvent(new h(str, i));
    }
}
