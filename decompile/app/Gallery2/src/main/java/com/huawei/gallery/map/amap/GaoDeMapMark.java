package com.huawei.gallery.map.amap;

import android.graphics.Bitmap;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.huawei.gallery.map.app.MapFragmentBase;
import com.huawei.gallery.map.app.MapMarkBase;
import com.huawei.gallery.map.data.MapLatLng;

public class GaoDeMapMark extends MapMarkBase {
    private Marker mMark = null;

    public GaoDeMapMark(MapFragmentBase fragment) {
        super(fragment);
    }

    public void create(AMap amap, MapLatLng latlng, Bitmap icon) {
        super.init(latlng);
        LatLng position = new LatLng(latlng.latitude, latlng.longitude);
        this.mMark = amap.addMarker(new MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(icon)));
        if (!amap.getProjection().getVisibleRegion().latLngBounds.contains(position)) {
            stopAnim();
        }
    }

    public void updateIcon(Bitmap icon) {
        this.mMark.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    protected void setPosition(MapLatLng position) {
        this.mMark.setPosition(new LatLng(position.latitude, position.longitude));
    }

    public void destory() {
        super.destory();
        this.mMark.remove();
    }
}
