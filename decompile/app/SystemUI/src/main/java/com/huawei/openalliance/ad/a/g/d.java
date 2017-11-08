package com.huawei.openalliance.ad.a.g;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.huawei.openalliance.ad.a.a.b.e;
import com.huawei.openalliance.ad.a.a.c;
import com.huawei.openalliance.ad.utils.h;

/* compiled from: Unknown */
public class d {
    public static int a(Context context, int i) {
        switch (i) {
            case 2:
                return h.a(context).i();
            case 4:
                return h.a(context).h();
            default:
                return 500;
        }
    }

    public static c a(String str, e eVar) {
        return eVar != null ? new c(str, eVar) : null;
    }

    public static boolean a(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static long c() {
        return System.currentTimeMillis();
    }
}
