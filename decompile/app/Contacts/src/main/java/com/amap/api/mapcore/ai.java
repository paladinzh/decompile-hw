package com.amap.api.mapcore;

import android.os.RemoteException;
import com.amap.api.maps.model.LatLng;
import java.util.List;

/* compiled from: INavigateArrowDelegate */
public interface ai extends aj {
    void a(int i) throws RemoteException;

    void a(List<LatLng> list) throws RemoteException;

    void b(float f) throws RemoteException;

    void b(int i) throws RemoteException;

    float h() throws RemoteException;

    int i() throws RemoteException;

    int l() throws RemoteException;

    List<LatLng> m() throws RemoteException;
}
