package com.amap.api.mapcore;

import android.os.RemoteException;
import com.amap.api.maps.model.LatLng;
import java.util.List;

/* compiled from: IPolygonDelegate */
public interface ak extends aj {
    void a(int i) throws RemoteException;

    void a(List<LatLng> list) throws RemoteException;

    boolean a(LatLng latLng) throws RemoteException;

    void b(float f) throws RemoteException;

    void b(int i) throws RemoteException;

    float h() throws RemoteException;

    int i() throws RemoteException;

    List<LatLng> l() throws RemoteException;

    int m() throws RemoteException;
}
