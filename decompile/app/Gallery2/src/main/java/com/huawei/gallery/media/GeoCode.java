package com.huawei.gallery.media;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.gallery.media.database.MergedMedia;
import com.huawei.gallery.util.MyPrinter;
import java.util.Locale;

public class GeoCode {
    private static final MyPrinter LOG = new MyPrinter("GeoCode");
    private static String[] PROJECTION = new String[]{"geo_code", "language", "geo_name"};
    public static final Uri URI = MergedMedia.URI.buildUpon().appendPath("t_geo_dictionary").build();
    String geoCode;
    String geoName = "";
    String language;

    public static String[] PROJECTION() {
        return (String[]) PROJECTION.clone();
    }

    public GeoCode(Cursor c) {
        this.geoCode = c.getString(0);
        this.language = c.getString(1);
        this.geoName = c.getString(2);
    }

    ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put("geo_code", this.geoCode);
        values.put("language", this.language);
        values.put("geo_name", this.geoName);
        return values;
    }

    public void insert(ContentResolver resolver) {
        resolver.insert(URI, toContentValues());
    }

    public String toString() {
        return String.format("geoCode:%s, language:%s, geoName:%s", new Object[]{this.geoCode, this.language, this.geoName});
    }

    public String getLanguage() {
        return this.language;
    }

    public String getGeoName() {
        return this.geoName;
    }

    public void setGeoName(Locale locale, String geoName) {
        this.language = locale.getLanguage();
        if (TextUtils.isEmpty(geoName)) {
            geoName = "";
        }
        this.geoName = geoName;
    }

    public String getGeoCode() {
        return this.geoCode;
    }

    public void setGeoCode(String geoCode) {
        this.geoCode = geoCode;
    }
}
