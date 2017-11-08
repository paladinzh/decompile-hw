package com.huawei.gallery.media;

import android.content.ContentValues;
import android.location.Address;
import android.net.Uri;
import com.amap.api.services.district.DistrictSearchQuery;
import com.android.gallery3d.util.ReverseGeocoder;
import com.huawei.gallery.media.database.MergedMedia;
import java.util.Locale;

public class GeoKnowledge {
    private static String[] PROJECTION = new String[]{"latitude", "longitude", "language", DistrictSearchQuery.KEYWORDS_COUNTRY, "admin_area", "sub_admin_area", "locality", "sub_locality", "thoroughfare", "sub_thoroughfare", "feature_name", "location_key"};
    public static final Uri URI = MergedMedia.URI.buildUpon().appendPath("t_geo_knowledge").build();
    final String adminArea;
    final String country;
    final String featureName;
    final String language;
    final double latitude;
    final String locality;
    final long locationKey = ReverseGeocoder.genLocationKey(this.latitude, this.longitude);
    final double longitude;
    final String subAdminArea;
    final String subLocality;
    final String subThoroughfare;
    final String thoroughfare;

    public GeoKnowledge(Address address, Locale locale, double lat, double lng) {
        this.latitude = lat;
        this.longitude = lng;
        this.language = locale.getLanguage();
        this.country = address.getCountryName();
        this.adminArea = address.getAdminArea();
        this.subAdminArea = address.getSubAdminArea();
        this.locality = address.getLocality();
        this.subLocality = address.getSubLocality();
        this.thoroughfare = address.getThoroughfare();
        this.subThoroughfare = address.getSubThoroughfare();
        this.featureName = address.getFeatureName();
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put("latitude", Double.toString(this.latitude));
        values.put("longitude", Double.toString(this.longitude));
        values.put("language", this.language);
        values.put(DistrictSearchQuery.KEYWORDS_COUNTRY, this.country);
        values.put("admin_area", this.adminArea);
        values.put("sub_admin_area", this.subAdminArea);
        values.put("locality", this.locality);
        values.put("sub_locality", this.subLocality);
        values.put("thoroughfare", this.thoroughfare);
        values.put("sub_thoroughfare", this.subThoroughfare);
        values.put("feature_name", this.featureName);
        values.put("location_key", Long.valueOf(this.locationKey));
        return values;
    }

    public String getGeoCode() {
        return this.locality;
    }
}
