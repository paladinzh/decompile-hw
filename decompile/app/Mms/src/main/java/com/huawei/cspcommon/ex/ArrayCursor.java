package com.huawei.cspcommon.ex;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.amap.api.maps.model.WeightedLatLng;

public class ArrayCursor<T extends ICursorData> implements Cursor {
    private String[] mColumnNames;
    private AutoExtendArray<T> mDatas;
    private int mPosition = -1;

    public interface ICursorData {
        byte[] getBlob(int i);

        double getDouble(int i);

        float getFloat(int i);

        int getInt(int i);

        long getLong(int i);

        short getShort(int i);

        String getString(int i);
    }

    public static abstract class CursorData implements ICursorData {
        private static byte[] sEmptyByte = new byte[0];

        public byte[] getBlob(int columnIndex) {
            return sEmptyByte;
        }

        public String getString(int columnIndex) {
            return null;
        }

        public short getShort(int columnIndex) {
            return (short) -1;
        }

        public int getInt(int columnIndex) {
            return -1;
        }

        public long getLong(int columnIndex) {
            return -1;
        }

        public float getFloat(int columnIndex) {
            return 0.0f;
        }

        public double getDouble(int columnIndex) {
            return WeightedLatLng.DEFAULT_INTENSITY;
        }
    }

    public ArrayCursor(AutoExtendArray<T> datas, String[] columnNames) {
        this.mColumnNames = columnNames;
        this.mDatas = datas;
    }

    public void setExtras(Bundle extras) {
    }

    public int getCount() {
        return this.mDatas.size();
    }

    public int getPosition() {
        return this.mPosition;
    }

    public boolean moveToPosition(int position) {
        if (position < -1 || position > this.mDatas.size()) {
            return false;
        }
        this.mPosition = position;
        return true;
    }

    public boolean moveToFirst() {
        return moveToPosition(0);
    }

    public boolean moveToLast() {
        return moveToPosition(this.mDatas.size());
    }

    public boolean move(int offset) {
        return moveToPosition(this.mPosition + offset);
    }

    public boolean moveToNext() {
        return move(1);
    }

    public boolean moveToPrevious() {
        return move(-1);
    }

    public boolean isFirst() {
        return this.mPosition == 0;
    }

    public boolean isLast() {
        return this.mPosition == this.mDatas.size() + -1;
    }

    public boolean isBeforeFirst() {
        return this.mPosition == -1;
    }

    public boolean isAfterLast() {
        return this.mPosition == this.mDatas.size();
    }

    public int getColumnIndex(String columnName) {
        for (int i = 0; i < this.mColumnNames.length; i++) {
            if (TextUtils.equals(this.mColumnNames[i], columnName)) {
                return i;
            }
        }
        return -1;
    }

    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        int idx = getColumnIndex(columnName);
        if (idx >= 0) {
            return idx;
        }
        throw new IllegalArgumentException("CSP-ArrayCurosr, getColumnIndexOrThrow for " + columnName);
    }

    public String getColumnName(int columnIndex) {
        return (columnIndex < 0 || columnIndex >= this.mColumnNames.length) ? null : this.mColumnNames[columnIndex];
    }

    public String[] getColumnNames() {
        return this.mColumnNames == null ? this.mColumnNames : (String[]) this.mColumnNames.clone();
    }

    public int getColumnCount() {
        return this.mDatas.size();
    }

    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
    }

    public int getType(int columnIndex) {
        return 0;
    }

    public boolean isNull(int columnIndex) {
        return false;
    }

    public void deactivate() {
    }

    public boolean requery() {
        return false;
    }

    public void close() {
    }

    public boolean isClosed() {
        return false;
    }

    public void registerContentObserver(ContentObserver observer) {
    }

    public void unregisterContentObserver(ContentObserver observer) {
    }

    public void registerDataSetObserver(DataSetObserver observer) {
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    public void setNotificationUri(ContentResolver cr, Uri uri) {
    }

    public Uri getNotificationUri() {
        return null;
    }

    public boolean getWantsAllOnMoveCalls() {
        return false;
    }

    public Bundle getExtras() {
        return null;
    }

    public Bundle respond(Bundle extras) {
        return null;
    }

    public byte[] getBlob(int columnIndex) {
        return ((ICursorData) this.mDatas.get(this.mPosition)).getBlob(columnIndex);
    }

    public String getString(int columnIndex) {
        return ((ICursorData) this.mDatas.get(this.mPosition)).getString(columnIndex);
    }

    public short getShort(int columnIndex) {
        return ((ICursorData) this.mDatas.get(this.mPosition)).getShort(columnIndex);
    }

    public int getInt(int columnIndex) {
        return ((ICursorData) this.mDatas.get(this.mPosition)).getInt(columnIndex);
    }

    public long getLong(int columnIndex) {
        return ((ICursorData) this.mDatas.get(this.mPosition)).getLong(columnIndex);
    }

    public float getFloat(int columnIndex) {
        return ((ICursorData) this.mDatas.get(this.mPosition)).getFloat(columnIndex);
    }

    public double getDouble(int columnIndex) {
        return ((ICursorData) this.mDatas.get(this.mPosition)).getDouble(columnIndex);
    }
}
