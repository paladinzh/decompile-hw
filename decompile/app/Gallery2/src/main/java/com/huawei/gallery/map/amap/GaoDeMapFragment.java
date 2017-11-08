package com.huawei.gallery.map.amap;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.amap.api.maps.model.Marker;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.map.app.MapFragmentBase.MapCenter;
import com.huawei.gallery.map.app.MapMarkBase;
import com.huawei.gallery.map.app.MapMarkerManagerFragment;
import com.huawei.gallery.map.app.MapUtils;
import com.huawei.gallery.map.app.MapZoomButton;
import com.huawei.gallery.map.app.MapZoomButton.MapCameraHandler;
import com.huawei.gallery.map.data.MapLatLng;
import com.huawei.gallery.util.UIUtils;
import java.util.List;

public class GaoDeMapFragment extends MapMarkerManagerFragment implements OnCameraChangeListener, OnMarkerClickListener, OnMapLoadedListener, MapCameraHandler {
    private AMap mAmap;
    private LatLngBounds mGlobalBounds;
    private GlobalCenter mGlobalCenter;
    private MapView mMapView = null;
    private MapZoomButton mZoomBtn;

    protected double[] getMeterPerPxArray() {
        return GaoDeMapUtils.METER_PER_PX_ARRAY_GAODE;
    }

    protected int getAcceptableZoomLevel() {
        return 16;
    }

    protected int getAcceptableMinLevel() {
        return 3;
    }

    protected int clampZoomLevel(int level) {
        return Utils.clamp(level, 3, 20);
    }

    protected boolean isMapFragmentValid() {
        return this.mAmap != null;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.map_frag_gaode, container, false);
        requestFeature(258);
        super.init(fragView);
        this.mMapView = (MapView) fragView.findViewById(R.id.map);
        this.mMapView.onCreate(savedInstanceState);
        this.mAmap = this.mMapView.getMap();
        this.mAmap.setOnCameraChangeListener(this);
        this.mAmap.setOnMapLoadedListener(this);
        this.mAmap.setOnMarkerClickListener(this);
        this.mAmap.getUiSettings().setZoomControlsEnabled(false);
        moveCameraTo(this.mMapCenter, this.mClusterStatus);
        this.mZoomBtn = (MapZoomButton) fragView.findViewById(R.id.map_zoom_btn);
        if (isZoomButtonVisible()) {
            this.mZoomBtn.setMapCameraHandler(this);
            this.mZoomBtn.updateIcon(this.mClusterStatus);
        } else {
            this.mZoomBtn.setVisibility(8);
        }
        return fragView;
    }

    public void onResume() {
        this.mAmap.getUiSettings().setCompassEnabled(true);
        super.onResume();
        this.mMapView.onResume();
    }

    public void onPause() {
        this.mAmap.getUiSettings().setCompassEnabled(false);
        super.onPause();
        this.mMapView.onPause();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mMapView.onSaveInstanceState(outState);
    }

    public void onDestroyView() {
        super.onDestroyView();
        clearMark();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mMapView.onDestroy();
    }

    protected void clearMark() {
        super.clearMark();
        if (this.mAmap != null) {
            this.mAmap.clear();
        }
    }

    protected MapMarkBase createMapMark(MapLatLng position, Bitmap icon) {
        GaoDeMapMark mark = new GaoDeMapMark(this);
        mark.create(this.mAmap, position, icon);
        return mark;
    }

    protected MapLatLng getFallingStartPosition(MapMarkBase mark) {
        Projection proj = this.mAmap.getProjection();
        Point startPoint = proj.toScreenLocation(new LatLng(mark.getPosition().latitude, mark.getPosition().longitude));
        startPoint.set(startPoint.x, 0);
        LatLng startLatLng = proj.fromScreenLocation(startPoint);
        return new MapLatLng(startLatLng.latitude, startLatLng.longitude);
    }

    public void onCameraChange(CameraPosition newPosition) {
    }

    public void onCameraChangeFinish(CameraPosition newPosition) {
        onMapCenterChanged(wrapCameraPositionToMapCenter(newPosition));
    }

    public boolean onMarkerClick(Marker mark) {
        onMarkClicked(getMarkerInList(this.mMarkerList, new MapLatLng(mark.getPosition().latitude, mark.getPosition().longitude)));
        return true;
    }

    public void onMapLoaded() {
    }

    public void sendItemRange(List<double[]> location) {
        if (location != null && location.size() != 0) {
            double[] center = null;
            GalleryLog.d("GaoDeMapFragment", "gaode api sendItemRange");
            Builder builder = LatLngBounds.builder();
            for (double[] latlng : location) {
                if (center == null) {
                    center = latlng;
                }
                builder.include(new LatLng(latlng[0], latlng[1]));
            }
            this.mGlobalBounds = builder.build();
            if (this.mGlobalBounds != null) {
                LatLng southwest = this.mGlobalBounds.southwest;
                LatLng northeast = this.mGlobalBounds.northeast;
                if (MapUtils.isMapOverRanged(Math.abs(southwest.latitude - northeast.latitude), Math.abs(southwest.longitude - northeast.longitude))) {
                    this.mGlobalCenter = new GlobalCenter(center);
                }
            }
        }
    }

    private MapCenter wrapCameraPositionToMapCenter(CameraPosition position) {
        return new MapCenter(position.target.latitude, position.target.longitude, position.zoom, position.tilt, position.bearing);
    }

    private CameraPosition wrapMapCenterToCameraPosition(MapCenter point) {
        return new CameraPosition(new LatLng(point.centerLat, point.centerLng), point.zoomLevel, point.tilt, point.bearing);
    }

    protected void moveCamera(MapCenter center) {
        moveCameraTo(center, this.mClusterStatus);
    }

    protected void moveToGlobalCenter(double lat, double lng) {
        this.mAmap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
        this.mGlobalCenter = null;
    }

    public void zoomGlobal(int status) {
        if (this.mGlobalBound != null) {
            moveCameraTo(this.mGlobalBound, status);
        } else if (this.mGlobalBounds != null) {
            this.mClusterStatus = status;
            Point displaySize = UIUtils.getDisplaySizeWithoutStatusBar(getActivity());
            this.mAmap.moveCamera(CameraUpdateFactory.newLatLngBounds(this.mGlobalBounds, displaySize.x - (this.mMarkSize * 2), displaySize.y - (this.mMarkSize * 4), 0));
            if (this.mGlobalCenter != null) {
                runOnUiThread(this.mGlobalCenter);
            }
        }
    }

    public void zoomGroup(int status) {
        moveCameraTo(this.mGroupBound, status);
    }

    public MapCenter getCurrentCameraPosition() {
        return wrapCameraPositionToMapCenter(this.mAmap.getCameraPosition());
    }

    public void moveCameraTo(MapCenter position, int status) {
        this.mClusterStatus = status;
        this.mAmap.moveCamera(CameraUpdateFactory.newCameraPosition(wrapMapCenterToCameraPosition(position)));
    }

    public void setNetworkEnable(boolean enable) {
        MapsInitializer.setNetWorkEnable(enable);
    }

    public boolean isSupportNetworkControll() {
        return true;
    }
}
