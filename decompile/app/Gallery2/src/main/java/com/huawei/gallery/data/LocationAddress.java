package com.huawei.gallery.data;

import android.content.Context;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReverseGeocoder;
import java.util.HashMap;
import java.util.Map;

public class LocationAddress {
    private Map<LocationKey, LocationInfo> mCache = new HashMap();
    private ReverseGeocoder mGeocoder;

    public static class LocationInfo {
        public String[] loccationDetails = new String[8];

        protected LocationInfo clone() {
            LocationInfo info = new LocationInfo();
            System.arraycopy(this.loccationDetails, 0, info.loccationDetails, 0, 8);
            return info;
        }

        public String toString() {
            return "mCountryName:" + this.loccationDetails[0] + ", mAdminArea:" + this.loccationDetails[1] + ", mSubAdminArea:" + this.loccationDetails[2] + ", mLocality:" + this.loccationDetails[3] + ", mSubLocality:" + this.loccationDetails[4] + ", mThoroughfare:" + this.loccationDetails[4] + ", mSubThoroughfare:" + this.loccationDetails[6] + ", mFeatureName:" + this.loccationDetails[7];
        }
    }

    private static class LocationKey {
        double lat;
        double lng;

        private LocationKey() {
        }
    }

    public LocationAddress(Context context) {
        this.mGeocoder = new ReverseGeocoder(context);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public LocationInfo getAddress(double lat, double lng, boolean fromCache) {
        synchronized (this) {
            for (LocationKey key : this.mCache.keySet()) {
                boolean LatLngFromCache = key.lat == lat && key.lng == lng;
                if (LatLngFromCache) {
                    LatLngFromCache = true;
                    continue;
                } else if (((double) ((float) GalleryUtils.fastDistanceMeters(Math.toRadians(lat), Math.toRadians(lng), Math.toRadians(key.lat), Math.toRadians(key.lng)))) < 100.0d) {
                    LatLngFromCache = true;
                    continue;
                } else {
                    LatLngFromCache = false;
                    continue;
                }
                if (LatLngFromCache) {
                    LocationInfo clone = ((LocationInfo) this.mCache.get(key)).clone();
                    return clone;
                }
            }
        }
    }
}
