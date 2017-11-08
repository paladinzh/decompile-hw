package com.amap.api.mapcore;

import android.graphics.Point;
import android.graphics.PointF;
import android.os.RemoteException;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.TileProjection;
import com.amap.api.maps.model.VisibleRegion;

/* compiled from: IProjectionDelegate */
public interface am {
    float a(int i) throws RemoteException;

    Point a(LatLng latLng) throws RemoteException;

    LatLng a(Point point) throws RemoteException;

    LatLngBounds a(LatLng latLng, float f) throws RemoteException;

    TileProjection a(LatLngBounds latLngBounds, int i, int i2) throws RemoteException;

    VisibleRegion a() throws RemoteException;

    PointF b(LatLng latLng) throws RemoteException;
}
