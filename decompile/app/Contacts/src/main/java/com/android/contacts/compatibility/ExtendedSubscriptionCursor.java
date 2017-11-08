package com.android.contacts.compatibility;

import android.database.Cursor;

public class ExtendedSubscriptionCursor extends ExtendedColumnCursor {
    public ExtendedSubscriptionCursor(Cursor cursor) {
        super(cursor, new String[]{"subscription"}, new Object[]{Integer.valueOf(-1)});
    }
}
