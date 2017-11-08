package com.amap.api.maps;

import android.content.Context;
import android.os.RemoteException;
import com.amap.api.mapcore.at;
import com.amap.api.mapcore.util.bm;

public final class MapsInitializer {
    private static boolean a = true;
    public static String sdcardDir = "";

    public static void initialize(Context context) throws RemoteException {
        at.a = context.getApplicationContext();
    }

    public static void setNetWorkEnable(boolean z) {
        a = z;
    }

    public static boolean getNetWorkEnable() {
        return a;
    }

    public static void setApiKey(String str) {
        if (str != null && str.trim().length() > 0) {
            bm.a(str);
        }
    }

    public static String getVersion() {
        return "3.3.0";
    }
}
