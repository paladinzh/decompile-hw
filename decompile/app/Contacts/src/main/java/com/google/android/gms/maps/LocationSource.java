package com.google.android.gms.maps;

import android.location.Location;

/* compiled from: Unknown */
public interface LocationSource {

    /* compiled from: Unknown */
    public interface OnLocationChangedListener {
        void onLocationChanged(Location location);
    }

    void activate(OnLocationChangedListener onLocationChangedListener);

    void deactivate();
}
