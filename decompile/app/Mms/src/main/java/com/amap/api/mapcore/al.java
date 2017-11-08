package com.amap.api.mapcore;

import android.os.RemoteException;
import com.amap.api.maps.model.LatLng;
import java.util.List;

/* compiled from: IPolylineDelegate */
public interface al extends aj {
    LatLng a(LatLng latLng);

    void a(int i) throws RemoteException;

    void a(List<LatLng> list) throws RemoteException;

    void b(float f) throws RemoteException;

    void b(boolean z) throws RemoteException;

    boolean b(LatLng latLng);

    void c(float f);

    void c(boolean z);

    float h() throws RemoteException;

    int i() throws RemoteException;

    List<LatLng> l() throws RemoteException;

    boolean m();

    boolean n();

    void o();
}
