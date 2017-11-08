package com.huawei.gallery.map.google;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.Marker;
import com.huawei.gallery.map.app.MapFragmentBase.MapCenter;
import com.huawei.gallery.map.app.MapMarkBase;
import com.huawei.gallery.map.app.MapMarkerManagerFragment;
import com.huawei.gallery.map.app.MapUtils;
import com.huawei.gallery.map.app.MapZoomButton;
import com.huawei.gallery.map.app.MapZoomButton.MapCameraHandler;
import com.huawei.gallery.map.data.MapLatLng;
import com.huawei.gallery.util.UIUtils;
import java.util.List;

public class GoogleMapFragment extends MapMarkerManagerFragment implements OnMarkerClickListener, OnCameraChangeListener, OnMapLoadedCallback, MapCameraHandler {
    private LatLngBounds mGloableBounds;
    private GlobalCenter mGlobalCenter;
    private GoogleMap mMap;
    private MapView mMapView;
    private MapZoomButton mZoomBtn;

    protected boolean isMapFragmentValid() {
        return this.mMap != null;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_frag_google, container, false);
        requestFeature(258);
        super.init(view);
        this.mZoomBtn = (MapZoomButton) view.findViewById(R.id.map_zoom_btn);
        this.mZoomBtn.updateIcon(this.mClusterStatus);
        this.mMapView = (MapView) view.findViewById(R.id.map);
        this.mMapView.onCreate(savedInstanceState);
        MapsInitializer.initialize(getActivity());
        this.mMap = this.mMapView.getMap();
        if (this.mMap != null) {
            this.mMap.setOnCameraChangeListener(this);
            this.mMap.setOnMapLoadedCallback(this);
            this.mMap.setOnMarkerClickListener(this);
            this.mMap.getUiSettings().setZoomControlsEnabled(false);
            moveCameraTo(this.mMapCenter, this.mClusterStatus);
            this.mZoomBtn.setMapCameraHandler(this);
        } else {
            this.mZoomBtn.setVisibility(8);
        }
        if (!isZoomButtonVisible()) {
            this.mZoomBtn.setVisibility(8);
        }
        return view;
    }

    public void onResume() {
        super.onResume();
        this.mMapView.onResume();
        if (this.mMap != null) {
            this.mMap.getUiSettings().setCompassEnabled(true);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mMapView.onSaveInstanceState(outState);
    }

    public void onPause() {
        if (this.mMap != null) {
            this.mMap.getUiSettings().setCompassEnabled(false);
        }
        super.onPause();
        this.mMapView.onPause();
    }

    public void onDestroyView() {
        super.onDestroyView();
        clearMark();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mMapView.onDestroy();
    }

    public void onLowMemory() {
        super.onLowMemory();
        this.mMapView.onLowMemory();
    }

    protected void clearMark() {
        super.clearMark();
        if (this.mMap != null) {
            this.mMap.clear();
        }
    }

    protected MapMarkBase createMapMark(MapLatLng position, Bitmap icon) {
        GoogleMapMark mark = new GoogleMapMark(this);
        mark.create(this.mMap, position, icon);
        return mark;
    }

    protected MapLatLng getFallingStartPosition(MapMarkBase mark) {
        Projection proj = this.mMap.getProjection();
        Point startPoint = proj.toScreenLocation(new LatLng(mark.getPosition().latitude, mark.getPosition().longitude));
        startPoint.set(startPoint.x, 0);
        LatLng startLatLng = proj.fromScreenLocation(startPoint);
        return new MapLatLng(startLatLng.latitude, startLatLng.longitude);
    }

    protected double[] getMeterPerPxArray() {
        return GoogleMapUtils.METER_PER_PX_ARRAY_GOOGLE;
    }

    protected int getAcceptableZoomLevel() {
        return 17;
    }

    protected int getAcceptableMinLevel() {
        return 2;
    }

    protected int clampZoomLevel(int level) {
        return Utils.clamp(level, 2, 21);
    }

    private CameraPosition wrapMapCenterToCameraPosition(MapCenter point) {
        return new CameraPosition(new LatLng(point.centerLat, point.centerLng), point.zoomLevel, point.tilt, point.bearing);
    }

    private MapCenter wrapCameraPositionToMapCenter(CameraPosition position) {
        return new MapCenter(position.target.latitude, position.target.longitude, position.zoom, position.tilt, position.bearing);
    }

    public boolean onMarkerClick(Marker mark) {
        onMarkClicked(getMarkerInList(this.mMarkerList, new MapLatLng(mark.getPosition().latitude, mark.getPosition().longitude)));
        return true;
    }

    public void onCameraChange(CameraPosition position) {
        onMapCenterChanged(wrapCameraPositionToMapCenter(position));
    }

    public void onMapLoaded() {
    }

    public MapCenter getCurrentCameraPosition() {
        return wrapCameraPositionToMapCenter(this.mMap.getCameraPosition());
    }

    public void moveCameraTo(MapCenter position, int status) {
        this.mClusterStatus = status;
        this.mMap.moveCamera(CameraUpdateFactory.newCameraPosition(wrapMapCenterToCameraPosition(position)));
    }

    public void sendItemRange(List<double[]> location) {
        if (location != null && location.size() != 0) {
            double[] center = null;
            Builder builder = LatLngBounds.builder();
            for (double[] latlng : location) {
                if (center == null) {
                    center = latlng;
                }
                builder.include(new LatLng(latlng[0], latlng[1]));
            }
            this.mGloableBounds = builder.build();
            LatLng southwest = this.mGloableBounds.southwest;
            LatLng northeast = this.mGloableBounds.northeast;
            if (MapUtils.isMapOverRanged(Math.abs(southwest.latitude - northeast.latitude), Math.abs(southwest.longitude - northeast.longitude))) {
                this.mGlobalCenter = new GlobalCenter(center);
            }
        }
    }

    public void zoomGroup(int status) {
        moveCameraTo(this.mGroupBound, status);
    }

    protected void moveCamera(MapCenter center) {
        moveCameraTo(center, this.mClusterStatus);
    }

    protected void moveToGlobalCenter(double lat, double lng) {
        this.mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
        this.mGlobalCenter = null;
    }

    public void zoomGlobal(int status) {
        if (this.mGlobalBound != null) {
            moveCameraTo(this.mGlobalBound, status);
        } else if (this.mGloableBounds != null) {
            this.mClusterStatus = status;
            Point displaySize = UIUtils.getDisplaySizeWithoutStatusBar(getActivity());
            this.mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(this.mGloableBounds, displaySize.x - (this.mMarkSize * 2), displaySize.y - (this.mMarkSize * 4), 0));
            if (this.mGlobalCenter != null) {
                runOnUiThread(this.mGlobalCenter);
            }
        }
    }
}
