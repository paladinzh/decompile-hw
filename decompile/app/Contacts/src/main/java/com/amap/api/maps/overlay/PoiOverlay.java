package com.amap.api.maps.overlay;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.PoiItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PoiOverlay {
    private List<PoiItem> a;
    private AMap b;
    private ArrayList<Marker> c = new ArrayList();

    public PoiOverlay(AMap aMap, List<PoiItem> list) {
        this.b = aMap;
        this.a = list;
    }

    public void addToMap() {
        int i = 0;
        while (i < this.a.size()) {
            try {
                Marker addMarker = this.b.addMarker(a(i));
                addMarker.setObject(Integer.valueOf(i));
                this.c.add(addMarker);
                i++;
            } catch (Throwable th) {
                th.printStackTrace();
                return;
            }
        }
    }

    public void removeFromMap() {
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            ((Marker) it.next()).remove();
        }
    }

    public void zoomToSpan() {
        try {
            if (this.a != null && this.a.size() > 0 && this.b != null) {
                if (this.a.size() != 1) {
                    this.b.moveCamera(CameraUpdateFactory.newLatLngBounds(a(), 5));
                } else {
                    this.b.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(((PoiItem) this.a.get(0)).getLatLonPoint().getLatitude(), ((PoiItem) this.a.get(0)).getLatLonPoint().getLongitude()), 18.0f));
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private LatLngBounds a() {
        Builder builder = LatLngBounds.builder();
        for (int i = 0; i < this.a.size(); i++) {
            builder.include(new LatLng(((PoiItem) this.a.get(i)).getLatLonPoint().getLatitude(), ((PoiItem) this.a.get(i)).getLatLonPoint().getLongitude()));
        }
        return builder.build();
    }

    private MarkerOptions a(int i) {
        return new MarkerOptions().position(new LatLng(((PoiItem) this.a.get(i)).getLatLonPoint().getLatitude(), ((PoiItem) this.a.get(i)).getLatLonPoint().getLongitude())).title(getTitle(i)).snippet(getSnippet(i)).icon(getBitmapDescriptor(i));
    }

    protected BitmapDescriptor getBitmapDescriptor(int i) {
        return null;
    }

    protected String getTitle(int i) {
        return ((PoiItem) this.a.get(i)).getTitle();
    }

    protected String getSnippet(int i) {
        return ((PoiItem) this.a.get(i)).getSnippet();
    }

    public int getPoiIndex(Marker marker) {
        for (int i = 0; i < this.c.size(); i++) {
            if (((Marker) this.c.get(i)).equals(marker)) {
                return i;
            }
        }
        return -1;
    }

    public PoiItem getPoiItem(int i) {
        if (i >= 0 && i < this.a.size()) {
            return (PoiItem) this.a.get(i);
        }
        return null;
    }
}
