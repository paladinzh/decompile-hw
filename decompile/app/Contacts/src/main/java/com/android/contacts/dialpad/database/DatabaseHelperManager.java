package com.android.contacts.dialpad.database;

import android.content.Context;

public class DatabaseHelperManager {
    public static DialerDatabaseHelper getDatabaseHelper(Context context) {
        return DialerDatabaseHelper.getInstance(context);
    }
}
