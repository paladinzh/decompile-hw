package com.fyusion.sdk.camera.util;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import com.amap.api.services.geocoder.GeocodeSearch;

/* compiled from: Unknown */
public class c {
    private static c d = null;
    public d[] a;
    private boolean b = false;
    private Context c = null;
    private boolean e = false;

    private c(Context context) {
        this.c = context;
        if (this.a == null) {
            this.a = new d[]{new d(GeocodeSearch.GPS), new d("network")};
        }
    }

    public static c a(Context context) {
        if (d == null) {
            d = new c(context);
        }
        return d;
    }

    public Location a() {
        if (this.a == null) {
            return null;
        }
        for (d a : this.a) {
            Location a2 = a.a();
            if (a2 != null) {
                return a2;
            }
        }
        Log.d("FLS", "No location received yet.");
        return null;
    }
}
