package com.android.contacts.calllog;

import android.database.Cursor;
import android.os.Bundle;
import com.android.contacts.compatibility.ExtendedColumnCursor;
import com.android.contacts.compatibility.QueryUtil;

public class ExtendedCursor extends ExtendedColumnCursor {
    public ExtendedCursor(Cursor cursor, String columnName, Object value) {
        super(cursor);
        if (columnName == null) {
            extendColumn(new String[]{"subscription"}, new Object[]{Integer.valueOf(0)});
            return;
        }
        String[] extendNames;
        Object[] extendValues;
        if (QueryUtil.isContainColumn(cursor.getColumnNames(), "subscription")) {
            extendNames = new String[]{columnName};
            extendValues = new Object[]{value};
        } else {
            extendNames = new String[]{"subscription", columnName};
            extendValues = new Object[]{Integer.valueOf(0), value};
        }
        extendColumn(extendNames, extendValues);
    }

    public Bundle getExtras() {
        if (this.mCursor != null) {
            return this.mCursor.getExtras();
        }
        return null;
    }
}
