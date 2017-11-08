package com.amap.api.mapcore;

import android.location.Location;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.LocationSource.OnLocationChangedListener;

/* compiled from: AMapOnLocationChangedListener */
class l implements OnLocationChangedListener {
    Location a;
    private ab b;

    l(ab abVar) {
        this.b = abVar;
    }

    public void onLocationChanged(Location location) {
        this.a = location;
        try {
            if (this.b.y()) {
                this.b.a(location);
            }
        } catch (Throwable e) {
            ce.a(e, "AMapOnLocationChangedListener", "onLocationChanged");
            e.printStackTrace();
        }
    }
}
