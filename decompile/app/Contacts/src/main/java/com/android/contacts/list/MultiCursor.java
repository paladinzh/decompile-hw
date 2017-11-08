package com.android.contacts.list;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.DataSetObserver;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import java.util.ArrayList;

public class MultiCursor extends CursorWrapper {
    private boolean DEBUG = HwLog.HWDBG;
    private Cursor mCurrentCursor = null;
    private int mCurrentCursorIndex = 0;
    private ArrayList<Cursor> mCursorList = null;

    public static class DummyCursor extends CursorWrapper {
        public DummyCursor() {
            super(new MatrixCursor(new String[]{"_id"}));
        }
    }

    public MultiCursor(Cursor cursor) {
        super(null);
        if (this.DEBUG) {
            HwLog.d("MultiCursor", "MultiCursor create:" + cursor);
        }
        this.mCursorList = new ArrayList();
        this.mCursorList.add(cursor);
        this.mCurrentCursor = cursor;
        this.mCurrentCursorIndex = 0;
    }

    public String[] getColumnNames() {
        if (this.mCurrentCursor != null) {
            return this.mCurrentCursor.getColumnNames();
        }
        return null;
    }

    public int getCount() {
        return getCount0();
    }

    private int getCount0() {
        if (this.mCurrentCursor == null || this.mCurrentCursor.isClosed()) {
            return 0;
        }
        return this.mCurrentCursor.getCount();
    }

    public double getDouble(int arg0) {
        if (this.mCurrentCursor != null) {
            return this.mCurrentCursor.getDouble(arg0);
        }
        return 0.0d;
    }

    public float getFloat(int arg0) {
        if (this.mCurrentCursor != null) {
            return this.mCurrentCursor.getFloat(arg0);
        }
        return 0.0f;
    }

    public int getInt(int arg0) {
        if (this.mCurrentCursor != null) {
            return this.mCurrentCursor.getInt(arg0);
        }
        return 0;
    }

    public long getLong(int arg0) {
        if (this.mCurrentCursor != null) {
            return this.mCurrentCursor.getLong(arg0);
        }
        return 0;
    }

    public short getShort(int arg0) {
        if (this.mCurrentCursor != null) {
            return this.mCurrentCursor.getShort(arg0);
        }
        return (short) 0;
    }

    public String getString(int arg0) {
        if (this.mCurrentCursor != null) {
            return this.mCurrentCursor.getString(arg0);
        }
        return null;
    }

    public boolean isNull(int arg0) {
        if (this.mCurrentCursor != null) {
            return this.mCurrentCursor.isNull(arg0);
        }
        return false;
    }

    public Cursor getCurrentCursor() {
        return this.mCurrentCursor;
    }

    public boolean switchCursor(int cursorIndex) {
        if (this.DEBUG) {
            HwLog.d("MultiCursor", "switchCursor to " + cursorIndex + " of " + this.mCursorList.size());
        }
        if (cursorIndex >= this.mCursorList.size()) {
            throw new IllegalArgumentException("Failed in switch cursor to index: " + cursorIndex + ", cursor list's size is:" + this.mCursorList.size());
        }
        this.mCurrentCursorIndex = cursorIndex;
        this.mCurrentCursor = (Cursor) this.mCursorList.get(cursorIndex);
        if (this.DEBUG) {
            HwLog.d("MultiCursor", "switchCursor:" + this.mCurrentCursor);
        }
        return true;
    }

    public Cursor getCursor(int index) {
        if (index >= this.mCursorList.size()) {
            return null;
        }
        return (Cursor) this.mCursorList.get(index);
    }

    public void close() {
        if (this.DEBUG) {
            HwLog.d("MultiCursor", "close");
        }
        for (Cursor cursor : this.mCursorList) {
            if (!(cursor == null || cursor.isClosed())) {
                cursor.close();
            }
        }
    }

    public void changeCursor(int cursorIndex, Cursor cursor) {
        if (cursorIndex >= this.mCursorList.size()) {
            throw new IllegalArgumentException("Failed in change cursor of index " + cursorIndex + ", cursor list's size is:" + this.mCursorList.size());
        }
        Cursor tempCursor = (Cursor) this.mCursorList.get(cursorIndex);
        if (tempCursor == cursor) {
            if (this.DEBUG) {
                HwLog.d("MultiCursor", "changeCursor(), same cursor, index=" + cursorIndex);
            }
            return;
        }
        if (this.DEBUG) {
            HwLog.d("MultiCursor", "changeCursor(), Index:" + cursorIndex + "; old_cursor: " + tempCursor);
        }
        addCursorToCursorList(cursorIndex, cursor, "MultiCursor:changeCursor");
        if (this.mCurrentCursorIndex == cursorIndex) {
            this.mCurrentCursor = cursor;
            if (this.DEBUG) {
                HwLog.d("MultiCursor", "changeCursor:" + this.mCurrentCursor);
            }
        }
    }

    public int addCursor(Cursor cursor) {
        if (this.DEBUG) {
            HwLog.d("MultiCursor", "addCursor");
        }
        return addCursorToCursorList(cursor, "MultiCursor:addCursor");
    }

    public boolean isClosed() {
        return this.mCurrentCursor.isClosed();
    }

    public boolean moveToFirst() {
        return this.mCurrentCursor.moveToFirst();
    }

    public int getColumnCount() {
        return this.mCurrentCursor.getColumnCount();
    }

    public int getColumnIndex(String columnName) {
        return this.mCurrentCursor.getColumnIndex(columnName);
    }

    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        return this.mCurrentCursor.getColumnIndexOrThrow(columnName);
    }

    public String getColumnName(int columnIndex) {
        return this.mCurrentCursor.getColumnName(columnIndex);
    }

    public Bundle getExtras() {
        return this.mCurrentCursor.getExtras();
    }

    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        this.mCurrentCursor.copyStringToBuffer(columnIndex, buffer);
    }

    public byte[] getBlob(int columnIndex) {
        return this.mCurrentCursor.getBlob(columnIndex);
    }

    public boolean getWantsAllOnMoveCalls() {
        return this.mCurrentCursor.getWantsAllOnMoveCalls();
    }

    public boolean isAfterLast() {
        return this.mCurrentCursor.isAfterLast();
    }

    public boolean isBeforeFirst() {
        return this.mCurrentCursor.isBeforeFirst();
    }

    public boolean isFirst() {
        return this.mCurrentCursor.isFirst();
    }

    public boolean isLast() {
        return this.mCurrentCursor.isLast();
    }

    public int getType(int columnIndex) {
        return this.mCurrentCursor.getType(columnIndex);
    }

    public boolean moveToLast() {
        return this.mCurrentCursor.moveToLast();
    }

    public boolean move(int offset) {
        return this.mCurrentCursor.move(offset);
    }

    public boolean moveToPosition(int position) {
        return this.mCurrentCursor.moveToPosition(position);
    }

    public boolean moveToNext() {
        return this.mCurrentCursor.moveToNext();
    }

    public int getPosition() {
        return this.mCurrentCursor.getPosition();
    }

    public boolean moveToPrevious() {
        return this.mCurrentCursor.moveToPrevious();
    }

    public void registerContentObserver(ContentObserver observer) {
        this.mCurrentCursor.registerContentObserver(observer);
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        this.mCurrentCursor.registerDataSetObserver(observer);
    }

    public Bundle respond(Bundle extras) {
        return this.mCurrentCursor.respond(extras);
    }

    public void setNotificationUri(ContentResolver cr, Uri uri) {
        this.mCurrentCursor.setNotificationUri(cr, uri);
    }

    public Uri getNotificationUri() {
        return this.mCurrentCursor.getNotificationUri();
    }

    public void unregisterContentObserver(ContentObserver observer) {
        this.mCurrentCursor.unregisterContentObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mCurrentCursor.unregisterDataSetObserver(observer);
    }

    public void deactivate() {
        this.mCurrentCursor.deactivate();
    }

    public boolean requery() {
        return this.mCurrentCursor.requery();
    }

    private int addCursorToCursorList(Cursor cursor, String methodName) {
        if (cursor != null) {
            this.mCursorList.add(cursor);
            return this.mCursorList.size() - 1;
        }
        ExceptionCapture.captureMultiCursorAddNullToCursorListException(methodName, this.mCursorList.size());
        return -1;
    }

    private int addCursorToCursorList(int cursorIndex, Cursor cursor, String methodName) {
        if (cursor != null) {
            this.mCursorList.set(cursorIndex, cursor);
            return this.mCursorList.size() - 1;
        }
        ExceptionCapture.captureMultiCursorAddNullToCursorListException(methodName, this.mCursorList.size());
        return -1;
    }
}
