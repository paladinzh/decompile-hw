package com.huawei.gallery.map.google;

import android.graphics.Bitmap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.huawei.gallery.map.app.MapFragmentBase;
import com.huawei.gallery.map.app.MapMarkBase;
import com.huawei.gallery.map.data.MapLatLng;

public class GoogleMapMark extends MapMarkBase {
    private MapLatLng mAlbumPosition;
    private Marker mMark;

    public GoogleMapMark(MapFragmentBase fragment) {
        super(fragment);
    }

    public void create(GoogleMap map, MapLatLng latlng, Bitmap icon) {
        this.mAlbumPosition = latlng;
        LatLng position = new LatLng(latlng.latitude, latlng.longitude);
        this.mMark = map.addMarker(new MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(icon)));
        LatLng realLatLng = this.mMark.getPosition();
        super.init(new MapLatLng(realLatLng.latitude, realLatLng.longitude));
        if (!map.getProjection().getVisibleRegion().latLngBounds.contains(realLatLng)) {
            stopAnim();
        }
    }

    public void updateIcon(Bitmap icon) {
        this.mMark.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    protected void setPosition(MapLatLng position) {
        this.mMark.setPosition(new LatLng(position.latitude, position.longitude));
    }

    public MapLatLng getAlbumPosition() {
        return this.mAlbumPosition;
    }

    public void destory() {
        super.destory();
        this.mMark.remove();
    }
}
