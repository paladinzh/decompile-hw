package com.google.android.gms.maps;

import android.os.RemoteException;
import com.google.android.gms.internal.er;
import com.google.android.gms.maps.internal.ICameraUpdateFactoryDelegate;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.RuntimeRemoteException;

/* compiled from: Unknown */
public final class CameraUpdateFactory {
    private static ICameraUpdateFactoryDelegate OJ;

    private CameraUpdateFactory() {
    }

    static void a(ICameraUpdateFactoryDelegate iCameraUpdateFactoryDelegate) {
        if (OJ == null) {
            OJ = (ICameraUpdateFactoryDelegate) er.f(iCameraUpdateFactoryDelegate);
        }
    }

    private static ICameraUpdateFactoryDelegate gL() {
        return (ICameraUpdateFactoryDelegate) er.b(OJ, (Object) "CameraUpdateFactory is not initialized");
    }

    public static CameraUpdate newCameraPosition(CameraPosition cameraPosition) {
        try {
            return new CameraUpdate(gL().newCameraPosition(cameraPosition));
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public static CameraUpdate newLatLng(LatLng latLng) {
        try {
            return new CameraUpdate(gL().newLatLng(latLng));
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public static CameraUpdate newLatLngBounds(LatLngBounds bounds, int width, int height, int padding) {
        try {
            return new CameraUpdate(gL().newLatLngBoundsWithSize(bounds, width, height, padding));
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }
}
