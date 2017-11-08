package com.android.settings;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.preference.Preference;
import java.util.Locale;

public class ThemeQueryHandler extends AsyncQueryHandler {
    public static final Uri URI_MODULE_INFO = Uri.parse("content://com.huawei.android.thememanager.ContentProvider/moduleInfo");
    private Preference mPreference;

    public ThemeQueryHandler(ContentResolver cr, Preference preference) {
        super(cr);
        this.mPreference = preference;
    }

    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if ((token == 0 || token == 1) && this.mPreference != null) {
            CharSequence displayName = "";
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        if (Locale.CHINA.toString().equals(Locale.getDefault().toString())) {
                            displayName = cursor.getString(1);
                        } else if (Locale.ENGLISH.getLanguage().equals(Locale.getDefault().getLanguage())) {
                            displayName = cursor.getString(0);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MLog.e("AsyncQueryHandler", e.getMessage());
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            this.mPreference.setSummary(displayName);
        }
    }
}
