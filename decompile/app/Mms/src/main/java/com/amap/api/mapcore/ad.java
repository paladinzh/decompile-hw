package com.amap.api.mapcore;

import android.os.RemoteException;
import com.amap.api.maps.model.LatLng;

/* compiled from: ICircleDelegate */
public interface ad extends aj {
    void a(double d) throws RemoteException;

    void a(int i) throws RemoteException;

    void a(LatLng latLng) throws RemoteException;

    void b(float f) throws RemoteException;

    void b(int i) throws RemoteException;

    boolean b(LatLng latLng) throws RemoteException;

    LatLng i() throws RemoteException;

    double l() throws RemoteException;

    float m() throws RemoteException;

    int n() throws RemoteException;

    int o() throws RemoteException;
}
