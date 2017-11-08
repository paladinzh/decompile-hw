package com.huawei.gallery.map.data;

public class MapLatLng {
    public double latitude;
    public double longitude;

    public MapLatLng() {
        this.longitude = 0.0d;
        this.latitude = 0.0d;
    }

    public MapLatLng(double lat, double lng) {
        this.longitude = lng;
        this.latitude = lat;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == null || !(o instanceof MapLatLng)) {
            return false;
        }
        MapLatLng other = (MapLatLng) o;
        if (this.longitude == other.longitude && this.latitude == other.latitude) {
            z = true;
        }
        return z;
    }
}
