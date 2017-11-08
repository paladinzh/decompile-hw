package com.loc;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import com.amap.api.services.geocoder.GeocodeSearch;
import java.text.SimpleDateFormat;

/* compiled from: Unknown */
final class dr implements LocationListener {
    private /* synthetic */ db a;

    dr(db dbVar) {
        this.a = dbVar;
    }

    private static boolean a(Location location) {
        return location != null && GeocodeSearch.GPS.equalsIgnoreCase(location.getProvider()) && location.getLatitude() > -90.0d && location.getLatitude() < 90.0d && location.getLongitude() > -180.0d && location.getLongitude() < 180.0d;
    }

    public final void onLocationChanged(Location location) {
        Object obj = null;
        try {
            long time = location.getTime();
            long currentTimeMillis = System.currentTimeMillis();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            simpleDateFormat.format(Long.valueOf(time));
            simpleDateFormat.format(Long.valueOf(currentTimeMillis));
            if (time > 0) {
                obj = 1;
            }
            if (obj != null) {
                currentTimeMillis = time;
            }
            if (location != null && a(location)) {
                if (location.getSpeed() > ((float) db.f)) {
                    dz.a(db.i);
                    dz.b(db.i * 10);
                } else if (location.getSpeed() > ((float) db.e)) {
                    dz.a(db.h);
                    dz.b(db.h * 10);
                } else {
                    dz.a(db.g);
                    dz.b(db.g * 10);
                }
                this.a.w.a();
                a(location);
                if (this.a.w.a() && a(location)) {
                    location.setTime(System.currentTimeMillis());
                    db.a(this.a, location, 0, currentTimeMillis);
                }
            }
        } catch (Exception e) {
        }
    }

    public final void onProviderDisabled(String str) {
    }

    public final void onProviderEnabled(String str) {
    }

    public final void onStatusChanged(String str, int i, Bundle bundle) {
    }
}
