package com.android.contacts.hap.rcs.map;

import android.graphics.Bitmap;

public interface RcsPrecallLocationListener {
    void onLocatinonSnapShot(Bitmap bitmap);

    void onLocationAddressResult(String str);

    void onLocationLatLngResult(double d, double d2);
}
