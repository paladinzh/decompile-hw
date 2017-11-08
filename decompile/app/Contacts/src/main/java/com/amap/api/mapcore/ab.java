package com.amap.api.mapcore;

import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.view.View;
import com.amap.api.mapcore.util.f;
import com.amap.api.maps.AMap.CancelableCallback;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnCacheRemoveListener;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnIndoorBuildingActiveListener;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMapLongClickListener;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMap.OnMarkerDragListener;
import com.amap.api.maps.AMap.OnMyLocationChangeListener;
import com.amap.api.maps.AMap.OnPOIClickListener;
import com.amap.api.maps.AMap.OnPolylineClickListener;
import com.amap.api.maps.AMap.onMapPrintScreenListener;
import com.amap.api.maps.CustomRenderer;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.ArcOptions;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.MyTrafficStyle;
import com.amap.api.maps.model.NavigateArrowOptions;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.amap.api.maps.model.TileOverlay;
import com.amap.api.maps.model.TileOverlayOptions;
import com.autonavi.amap.mapcore.DPoint;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;
import java.util.ArrayList;
import java.util.List;

/* compiled from: IAMapDelegate */
public interface ab {
    aq A() throws RemoteException;

    am B() throws RemoteException;

    View D() throws RemoteException;

    void E();

    float F();

    LatLngBounds H();

    Point I();

    float J() throws RemoteException;

    int K();

    List<Marker> L() throws RemoteException;

    void M();

    void N();

    int Q() throws RemoteException;

    boolean R();

    o S();

    void T() throws RemoteException;

    float W();

    void X();

    ac a(ArcOptions arcOptions) throws RemoteException;

    ad a(CircleOptions circleOptions) throws RemoteException;

    af a(GroundOverlayOptions groundOverlayOptions) throws RemoteException;

    ai a(NavigateArrowOptions navigateArrowOptions) throws RemoteException;

    ak a(PolygonOptions polygonOptions) throws RemoteException;

    al a(PolylineOptions polylineOptions) throws RemoteException;

    LatLngBounds a(LatLng latLng, float f);

    Marker a(MarkerOptions markerOptions) throws RemoteException;

    Text a(TextOptions textOptions) throws RemoteException;

    TileOverlay a(TileOverlayOptions tileOverlayOptions) throws RemoteException;

    ArrayList<Marker> a(ArrayList<MarkerOptions> arrayList, boolean z) throws RemoteException;

    void a(double d, double d2, FPoint fPoint);

    void a(double d, double d2, IPoint iPoint);

    void a(float f) throws RemoteException;

    void a(float f, float f2, IPoint iPoint);

    void a(int i) throws RemoteException;

    void a(int i, int i2) throws RemoteException;

    void a(int i, int i2, DPoint dPoint);

    void a(int i, int i2, FPoint fPoint);

    void a(int i, int i2, IPoint iPoint);

    void a(Location location) throws RemoteException;

    void a(ah ahVar) throws RemoteException;

    void a(p pVar) throws RemoteException;

    void a(p pVar, long j, CancelableCallback cancelableCallback) throws RemoteException;

    void a(p pVar, CancelableCallback cancelableCallback) throws RemoteException;

    void a(v vVar);

    void a(InfoWindowAdapter infoWindowAdapter) throws RemoteException;

    void a(OnCacheRemoveListener onCacheRemoveListener) throws RemoteException;

    void a(OnCameraChangeListener onCameraChangeListener) throws RemoteException;

    void a(OnIndoorBuildingActiveListener onIndoorBuildingActiveListener) throws RemoteException;

    void a(OnInfoWindowClickListener onInfoWindowClickListener) throws RemoteException;

    void a(OnMapClickListener onMapClickListener) throws RemoteException;

    void a(OnMapLoadedListener onMapLoadedListener) throws RemoteException;

    void a(OnMapLongClickListener onMapLongClickListener) throws RemoteException;

    void a(OnMapScreenShotListener onMapScreenShotListener);

    void a(OnMapTouchListener onMapTouchListener) throws RemoteException;

    void a(OnMarkerClickListener onMarkerClickListener) throws RemoteException;

    void a(OnMarkerDragListener onMarkerDragListener) throws RemoteException;

    void a(OnMyLocationChangeListener onMyLocationChangeListener) throws RemoteException;

    void a(OnPOIClickListener onPOIClickListener) throws RemoteException;

    void a(OnPolylineClickListener onPolylineClickListener) throws RemoteException;

    void a(onMapPrintScreenListener onmapprintscreenlistener);

    void a(CustomRenderer customRenderer) throws RemoteException;

    void a(LocationSource locationSource) throws RemoteException;

    void a(MyLocationStyle myLocationStyle) throws RemoteException;

    void a(MyTrafficStyle myTrafficStyle) throws RemoteException;

    void a(boolean z);

    boolean a(MotionEvent motionEvent);

    boolean a(String str) throws RemoteException;

    int b();

    void b(double d, double d2, IPoint iPoint);

    void b(int i) throws RemoteException;

    void b(int i, int i2, DPoint dPoint);

    void b(int i, int i2, FPoint fPoint);

    void b(p pVar) throws RemoteException;

    void b(f fVar) throws RemoteException;

    void b(boolean z);

    float c(int i);

    MapProjection c();

    void c(boolean z);

    void d();

    void d(int i);

    void d(boolean z);

    void e();

    void e(int i);

    void e(boolean z);

    void f();

    void f(int i);

    void f(boolean z);

    void g();

    void g(int i) throws RemoteException;

    void g(boolean z) throws RemoteException;

    void h();

    void h(int i);

    void h(boolean z) throws RemoteException;

    void i(int i);

    void i(boolean z) throws RemoteException;

    void j(boolean z) throws RemoteException;

    Rect k();

    void k(boolean z) throws RemoteException;

    int l();

    void l(boolean z) throws RemoteException;

    int m();

    void m(boolean z) throws RemoteException;

    CameraPosition n(boolean z);

    int p();

    void p(boolean z) throws RemoteException;

    void q();

    CameraPosition r() throws RemoteException;

    float s();

    float t();

    void u() throws RemoteException;

    void v() throws RemoteException;

    int w() throws RemoteException;

    boolean x() throws RemoteException;

    boolean y() throws RemoteException;

    Location z() throws RemoteException;
}
