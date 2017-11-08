package com.android.contacts.util;

import android.database.Cursor;

public class CloseUtils {
    public static void closeCursorIfNotNull(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }
}
