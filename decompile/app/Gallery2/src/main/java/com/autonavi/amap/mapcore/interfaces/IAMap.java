package com.autonavi.amap.mapcore.interfaces;

import android.location.Location;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Pair;
import android.view.View;
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
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CustomRenderer;
import com.amap.api.maps.InfoWindowAnimationManager;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.Projection;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.Arc;
import com.amap.api.maps.model.ArcOptions;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.IndoorBuildingInfo;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.MyTrafficStyle;
import com.amap.api.maps.model.NavigateArrow;
import com.amap.api.maps.model.NavigateArrowOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.amap.api.maps.model.TileOverlay;
import com.amap.api.maps.model.TileOverlayOptions;
import com.autonavi.amap.mapcore.MapConfig;
import java.util.ArrayList;
import java.util.List;

public interface IAMap {
    Arc addArc(ArcOptions arcOptions) throws RemoteException;

    Circle addCircle(CircleOptions circleOptions) throws RemoteException;

    GroundOverlay addGroundOverlay(GroundOverlayOptions groundOverlayOptions) throws RemoteException;

    Marker addMarker(MarkerOptions markerOptions) throws RemoteException;

    ArrayList<Marker> addMarkers(ArrayList<MarkerOptions> arrayList, boolean z) throws RemoteException;

    NavigateArrow addNavigateArrow(NavigateArrowOptions navigateArrowOptions) throws RemoteException;

    Polygon addPolygon(PolygonOptions polygonOptions) throws RemoteException;

    Polyline addPolyline(PolylineOptions polylineOptions) throws RemoteException;

    Text addText(TextOptions textOptions) throws RemoteException;

    TileOverlay addTileOverlay(TileOverlayOptions tileOverlayOptions) throws RemoteException;

    void animateCamera(CameraUpdate cameraUpdate) throws RemoteException;

    void animateCameraWithCallback(CameraUpdate cameraUpdate, CancelableCallback cancelableCallback) throws RemoteException;

    void animateCameraWithDurationAndCallback(CameraUpdate cameraUpdate, long j, CancelableCallback cancelableCallback) throws RemoteException;

    Pair<Float, LatLng> calculateZoomToSpanLevel(int i, int i2, int i3, int i4, LatLng latLng, LatLng latLng2);

    void clear() throws RemoteException;

    void clear(boolean z) throws RemoteException;

    void destroy();

    Projection getAMapProjection() throws RemoteException;

    UiSettings getAMapUiSettings() throws RemoteException;

    float getCameraAngle();

    CameraPosition getCameraPosition() throws RemoteException;

    InfoWindowAnimationManager getInfoWindowAnimationManager();

    Handler getMainHandler();

    MapConfig getMapConfig();

    int getMapHeight();

    void getMapPrintScreen(onMapPrintScreenListener onmapprintscreenlistener);

    List<Marker> getMapScreenMarkers() throws RemoteException;

    void getMapScreenShot(OnMapScreenShotListener onMapScreenShotListener);

    int getMapTextZIndex() throws RemoteException;

    int getMapType() throws RemoteException;

    int getMapWidth();

    float getMaxZoomLevel();

    float getMinZoomLevel();

    Location getMyLocation() throws RemoteException;

    float getScalePerPixel() throws RemoteException;

    View getView() throws RemoteException;

    float getZoomToSpanLevel(LatLng latLng, LatLng latLng2);

    boolean isIndoorEnabled() throws RemoteException;

    boolean isMaploaded();

    boolean isMyLocationEnabled() throws RemoteException;

    boolean isTrafficEnabled() throws RemoteException;

    void moveCamera(CameraUpdate cameraUpdate) throws RemoteException;

    void onActivityPause();

    void onActivityResume();

    void onChangeFinish();

    void onFling();

    void reloadMap();

    void removecache() throws RemoteException;

    void removecache(OnCacheRemoveListener onCacheRemoveListener) throws RemoteException;

    void resetMinMaxZoomPreference();

    void set3DBuildingEnabled(boolean z) throws RemoteException;

    void setAMapGestureListener(AMapGestureListener aMapGestureListener);

    void setCenterToPixel(int i, int i2) throws RemoteException;

    void setCustomRenderer(CustomRenderer customRenderer) throws RemoteException;

    void setIndoorBuildingInfo(IndoorBuildingInfo indoorBuildingInfo) throws RemoteException;

    void setIndoorEnabled(boolean z) throws RemoteException;

    void setInfoWindowAdapter(InfoWindowAdapter infoWindowAdapter) throws RemoteException;

    void setLoadOfflineData(boolean z) throws RemoteException;

    void setLocationSource(LocationSource locationSource) throws RemoteException;

    void setMapStatusLimits(LatLngBounds latLngBounds);

    void setMapTextEnable(boolean z) throws RemoteException;

    void setMapTextZIndex(int i) throws RemoteException;

    void setMapType(int i) throws RemoteException;

    void setMaskLayerParams(int i, int i2, int i3, int i4, int i5, long j);

    void setMaxZoomLevel(float f);

    void setMinZoomLevel(float f);

    void setMyLocationEnabled(boolean z) throws RemoteException;

    void setMyLocationRotateAngle(float f) throws RemoteException;

    void setMyLocationStyle(MyLocationStyle myLocationStyle) throws RemoteException;

    void setMyLocationType(int i) throws RemoteException;

    void setMyTrafficStyle(MyTrafficStyle myTrafficStyle) throws RemoteException;

    void setOnCameraChangeListener(OnCameraChangeListener onCameraChangeListener) throws RemoteException;

    void setOnIndoorBuildingActiveListener(OnIndoorBuildingActiveListener onIndoorBuildingActiveListener) throws RemoteException;

    void setOnInfoWindowClickListener(OnInfoWindowClickListener onInfoWindowClickListener) throws RemoteException;

    void setOnMapClickListener(OnMapClickListener onMapClickListener) throws RemoteException;

    void setOnMapLongClickListener(OnMapLongClickListener onMapLongClickListener) throws RemoteException;

    void setOnMapTouchListener(OnMapTouchListener onMapTouchListener) throws RemoteException;

    void setOnMaploadedListener(OnMapLoadedListener onMapLoadedListener) throws RemoteException;

    void setOnMarkerClickListener(OnMarkerClickListener onMarkerClickListener) throws RemoteException;

    void setOnMarkerDragListener(OnMarkerDragListener onMarkerDragListener) throws RemoteException;

    void setOnMyLocationChangeListener(OnMyLocationChangeListener onMyLocationChangeListener) throws RemoteException;

    void setOnPOIClickListener(OnPOIClickListener onPOIClickListener) throws RemoteException;

    void setOnPolylineClickListener(OnPolylineClickListener onPolylineClickListener) throws RemoteException;

    void setRenderFps(int i);

    void setRunLowFrame(boolean z);

    void setTrafficEnabled(boolean z) throws RemoteException;

    void setVisibilityEx(int i);

    void setZOrderOnTop(boolean z) throws RemoteException;

    void setZoomScaleParam(float f);

    void stopAnimation() throws RemoteException;
}
