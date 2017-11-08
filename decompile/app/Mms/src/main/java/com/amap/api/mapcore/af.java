package com.amap.api.mapcore;

import android.os.RemoteException;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;

/* compiled from: IGroundOverlayDelegate */
public interface af extends aj {
    void a(float f, float f2) throws RemoteException;

    void a(BitmapDescriptor bitmapDescriptor) throws RemoteException;

    void a(LatLng latLng) throws RemoteException;

    void a(LatLngBounds latLngBounds) throws RemoteException;

    void b(float f) throws RemoteException;

    void c(float f) throws RemoteException;

    void d(float f) throws RemoteException;

    LatLng h() throws RemoteException;

    float i() throws RemoteException;

    float l() throws RemoteException;

    LatLngBounds m() throws RemoteException;

    float n() throws RemoteException;

    float o() throws RemoteException;

    void p();
}
