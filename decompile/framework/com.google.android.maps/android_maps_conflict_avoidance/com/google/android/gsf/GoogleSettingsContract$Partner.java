package android_maps_conflict_avoidance.com.google.android.gsf;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

public final class GoogleSettingsContract$Partner extends GoogleSettingsContract$NameValueTable {
    public static final Uri CONTENT_URI = Uri.parse("content://com.google.settings/partner");

    public static String getString(ContentResolver resolver, String name) {
        String str = null;
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = resolver;
            cursor = contentResolver.query(CONTENT_URI, new String[]{"value"}, "name=?", new String[]{name}, null);
            if (cursor != null && cursor.moveToNext()) {
                str = cursor.getString(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException e) {
            Log.e("GoogleSettings", "Can't get key " + name + " from " + CONTENT_URI, e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return str;
    }

    public static String getString(ContentResolver resolver, String name, String defaultValue) {
        String value = getString(resolver, name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
