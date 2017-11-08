package com.amap.api.maps;

import android.content.Context;
import android.os.RemoteException;
import com.amap.api.mapcore.util.fa;
import com.amap.api.mapcore.util.g;
import com.amap.api.mapcore.util.p;

public final class MapsInitializer {
    private static boolean a = true;
    public static String sdcardDir = "";

    public static void initialize(Context context) throws RemoteException {
        p.a = context.getApplicationContext();
    }

    public static void setNetWorkEnable(boolean z) {
        a = z;
    }

    public static boolean getNetWorkEnable() {
        return a;
    }

    public static void setApiKey(String str) {
        if (str != null && str.trim().length() > 0) {
            fa.a(str);
        }
    }

    public static String getVersion() {
        return "4.1.2";
    }

    public static void loadWorldGridMap(boolean z) {
        int i = 0;
        if (!z) {
            i = 1;
        }
        g.c = i;
    }
}
