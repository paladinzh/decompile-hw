package com.loc;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.loc.a.c;

/* compiled from: GPSLocation */
public class g {
    Handler a;
    Context b;
    LocationManager c;
    AMapLocationClientOption d;
    long e = 1000;
    long f = 0;
    LocationListener g = new h(this);

    public g(Context context, c cVar) {
        this.b = context;
        this.a = cVar;
        this.c = (LocationManager) this.b.getSystemService("location");
    }

    public void a() {
        if (!(this.c == null || this.g == null)) {
            this.c.removeUpdates(this.g);
        }
    }

    void a(long j, float f) {
        try {
            Looper myLooper = Looper.myLooper();
            if (myLooper == null) {
                myLooper = this.b.getMainLooper();
            }
            this.e = j;
            this.c.requestLocationUpdates(GeocodeSearch.GPS, 1000, f, this.g, myLooper);
        } catch (Throwable e) {
            e.a(e, "GPSLocation", "requestLocationUpdates part1");
            Message obtain = Message.obtain();
            AMapLocation aMapLocation = new AMapLocation("");
            aMapLocation.setProvider(GeocodeSearch.GPS);
            aMapLocation.setErrorCode(12);
            aMapLocation.setLocationType(1);
            obtain.what = 2;
            obtain.obj = aMapLocation;
            if (this.a != null) {
                this.a.sendMessage(obtain);
            }
        } catch (Throwable e2) {
            e.a(e2, "GPSLocation", "requestLocationUpdates part2");
        }
    }

    public void a(AMapLocationClientOption aMapLocationClientOption) {
        this.d = aMapLocationClientOption;
        a(this.d.getInterval(), 0.0f);
    }
}
