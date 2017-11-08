package com.android.deskclock.worldclock;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class City {
    public String cityIndex;
    public String displayName;
    public int sortId;
    public String timeZone;

    public static class LocationColumns implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.deskclock/locations");
    }

    public static class WidgetColumns implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.deskclock/widgets");
    }

    public City(Context context, Cursor c) {
        this.sortId = c.getInt(c.getColumnIndex("sort_order"));
        this.cityIndex = c.getString(c.getColumnIndex("city_index"));
        this.timeZone = c.getString(c.getColumnIndex("timezone"));
        this.displayName = getDisplayName(context, this.cityIndex);
    }

    public String getDisplayName(Context context, String index) {
        if (index == null) {
            return "";
        }
        return TimeZoneUtils.getTimeZoneMapValue(context, index);
    }

    public CharSequence getDisplayName(Context context) {
        return getDisplayName(context, this.cityIndex);
    }
}
