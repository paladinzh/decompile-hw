package com.fyusion.sdk.camera.util;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/* compiled from: Unknown */
public class d implements LocationListener {
    private static String d = "GpsListener";
    private Location a;
    private boolean b = false;
    private String c;

    public d(String str) {
        this.c = str;
        this.a = new Location(this.c);
    }

    public Location a() {
        return !this.b ? null : this.a;
    }

    public void onLocationChanged(Location location) {
        if (location.getLatitude() != 0.0d || location.getLongitude() != 0.0d) {
            if (!this.b) {
                Log.d(d, "Got first location.");
            }
            this.a.set(location);
            this.b = true;
        }
    }

    public void onProviderDisabled(String str) {
        Log.d(d, str + "disabled");
        this.b = false;
    }

    public void onProviderEnabled(String str) {
        Log.d(d, str + "enabled");
    }

    public void onStatusChanged(String str, int i, Bundle bundle) {
        Log.d(d, str + "onStatusChanged: " + i);
        switch (i) {
            case 0:
            case 1:
                this.b = false;
                return;
            default:
                return;
        }
    }
}
