package com.android.contacts.compatibility;

import android.database.AbstractCursor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import com.android.common.io.MoreCloseables;
import com.android.contacts.util.HwLog;

public class ExtendedColumnCursor extends AbstractCursor {
    private String[] mColumnNames;
    protected final Cursor mCursor;
    private Object[] mValues;
    private int mWrappedColumnSize;

    protected ExtendedColumnCursor(Cursor cursor) {
        this.mCursor = cursor;
        this.mWrappedColumnSize = cursor.getColumnNames().length;
    }

    protected void extendColumn(String[] columnNames, Object[] values) {
        this.mColumnNames = columnNames;
        this.mValues = values;
    }

    public ExtendedColumnCursor(Cursor cursor, String[] columnNames, Object[] values) {
        this.mCursor = cursor;
        if (columnNames != null) {
            this.mColumnNames = (String[]) columnNames.clone();
        } else {
            this.mColumnNames = null;
        }
        if (values != null) {
            this.mValues = (Object[]) values.clone();
        } else {
            this.mValues = null;
        }
        this.mWrappedColumnSize = cursor.getColumnNames().length;
    }

    public int getCount() {
        if (this.mCursor == null || this.mCursor.isClosed()) {
            return -1;
        }
        return this.mCursor.getCount();
    }

    public String[] getColumnNames() {
        String[] columnNames = this.mCursor.getColumnNames();
        int length = this.mWrappedColumnSize;
        String[] extendedColumnNames = new String[(this.mColumnNames.length + length)];
        System.arraycopy(columnNames, 0, extendedColumnNames, 0, length);
        System.arraycopy(this.mColumnNames, 0, extendedColumnNames, length, this.mColumnNames.length);
        return extendedColumnNames;
    }

    public String getString(int column) {
        if (column < this.mWrappedColumnSize) {
            return this.mCursor.getString(column);
        }
        if (column - this.mWrappedColumnSize >= this.mValues.length) {
            return null;
        }
        Object value = this.mValues[column - this.mWrappedColumnSize];
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public short getShort(int column) {
        if (column < this.mWrappedColumnSize) {
            return this.mCursor.getShort(column);
        }
        if (column - this.mWrappedColumnSize >= this.mValues.length) {
            return (short) 0;
        }
        Object value = this.mValues[column - this.mWrappedColumnSize];
        if (value == null) {
            return (short) 0;
        }
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        return Short.parseShort(value.toString());
    }

    public int getInt(int column) {
        if (column < this.mWrappedColumnSize) {
            return this.mCursor.getInt(column);
        }
        if (column - this.mWrappedColumnSize >= this.mValues.length) {
            return 0;
        }
        Object value = this.mValues[column - this.mWrappedColumnSize];
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    public long getLong(int column) {
        if (column < this.mWrappedColumnSize) {
            return this.mCursor.getLong(column);
        }
        if (column - this.mWrappedColumnSize >= this.mValues.length) {
            return 0;
        }
        Object value = this.mValues[column - this.mWrappedColumnSize];
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    public float getFloat(int column) {
        if (column < this.mWrappedColumnSize) {
            return this.mCursor.getFloat(column);
        }
        if (column - this.mWrappedColumnSize >= this.mValues.length) {
            return 0.0f;
        }
        Object value = this.mValues[column - this.mWrappedColumnSize];
        if (value == null) {
            return 0.0f;
        }
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return Float.parseFloat(value.toString());
    }

    public double getDouble(int column) {
        if (column < this.mWrappedColumnSize) {
            return this.mCursor.getDouble(column);
        }
        if (column - this.mWrappedColumnSize >= this.mValues.length) {
            return 0.0d;
        }
        Object value = this.mValues[column - this.mWrappedColumnSize];
        if (value == null) {
            return 0.0d;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    public boolean isNull(int column) {
        boolean z = false;
        if (column != this.mWrappedColumnSize || this.mValues.length <= 0) {
            return this.mCursor.isNull(column);
        }
        if (this.mValues[column - this.mWrappedColumnSize] == null) {
            z = true;
        }
        return z;
    }

    public boolean onMove(int oldPosition, int newPosition) {
        boolean ret = false;
        try {
            ret = this.mCursor.moveToPosition(newPosition);
        } catch (IllegalStateException e) {
            HwLog.e("ExtendedColumnCursor", "UNKNOWN type :" + newPosition);
        }
        return ret;
    }

    public void close() {
        MoreCloseables.closeQuietly(this.mCursor);
        super.close();
    }

    public void registerContentObserver(ContentObserver observer) {
        this.mCursor.registerContentObserver(observer);
    }

    public void unregisterContentObserver(ContentObserver observer) {
        this.mCursor.unregisterContentObserver(observer);
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        this.mCursor.registerDataSetObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mCursor.unregisterDataSetObserver(observer);
    }
}
