package com.loc;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Message;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.DPoint;
import com.amap.api.services.geocoder.GeocodeSearch;

/* compiled from: GPSLocation */
class h implements LocationListener {
    final /* synthetic */ g a;

    h(g gVar) {
        this.a = gVar;
    }

    public void onLocationChanged(Location location) {
        Object obj = 1;
        if (location != null) {
            try {
                Bundle extras = location.getExtras();
                int i = extras == null ? 0 : extras.getInt("satellites");
                if (i > 0 || this.a.d.isMockEnable()) {
                    if (this.a.a != null) {
                        this.a.a.sendEmptyMessage(5);
                    }
                    if (cw.b() - this.a.f > this.a.e) {
                        obj = null;
                    }
                    if (obj == null) {
                        AMapLocation aMapLocation;
                        Message message;
                        if (e.a(location.getLatitude(), location.getLongitude())) {
                            if (this.a.d.isOffset()) {
                                aMapLocation = new AMapLocation(location);
                                aMapLocation.setLocationType(1);
                                DPoint a = j.a(this.a.b, location.getLongitude(), location.getLatitude());
                                aMapLocation.setLatitude(a.getLatitude());
                                aMapLocation.setLongitude(a.getLongitude());
                                aMapLocation.setSatellites(i);
                                message = new Message();
                                message.obj = aMapLocation;
                                message.what = 2;
                                if (this.a.a != null) {
                                    this.a.a.sendMessage(message);
                                }
                                this.a.f = cw.b();
                            }
                        }
                        aMapLocation = new AMapLocation(location);
                        aMapLocation.setLatitude(location.getLatitude());
                        aMapLocation.setLongitude(location.getLongitude());
                        aMapLocation.setLocationType(1);
                        aMapLocation.setSatellites(i);
                        message = new Message();
                        message.obj = aMapLocation;
                        message.what = 2;
                        if (this.a.a != null) {
                            this.a.a.sendMessage(message);
                        }
                        this.a.f = cw.b();
                    }
                }
            } catch (Throwable th) {
                e.a(th, "GPSLocation", "onLocationChanged");
            }
        }
    }

    public void onProviderDisabled(String str) {
        try {
            if (GeocodeSearch.GPS.equals(str)) {
                this.a.a.sendEmptyMessage(3);
            }
        } catch (Throwable th) {
            e.a(th, "GPSLocation", "onProviderDisabled");
        }
    }

    public void onProviderEnabled(String str) {
    }

    public void onStatusChanged(String str, int i, Bundle bundle) {
        if (i == 0 || i == 1) {
            try {
                this.a.a.sendEmptyMessage(3);
            } catch (Throwable th) {
                e.a(th, "GPSLocation", "onStatusChanged");
            }
        }
    }
}
