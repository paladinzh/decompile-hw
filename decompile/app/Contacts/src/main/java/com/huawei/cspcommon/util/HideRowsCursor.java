package com.huawei.cspcommon.util;

import android.database.Cursor;
import android.database.CursorWrapper;
import com.android.contacts.util.HwLog;
import java.util.TreeMap;

public class HideRowsCursor extends CursorWrapper {
    private final Cursor mCursor;
    private TreeMap mHideRowsMap = new TreeMap();

    private boolean isValidPos(int realPos) {
        if (realPos < 0 || realPos >= this.mCursor.getCount() || this.mHideRowsMap.containsKey(Integer.valueOf(realPos))) {
            return false;
        }
        return true;
    }

    private boolean hasHideRows() {
        if (this.mCursor.getCount() <= 0 || this.mHideRowsMap.size() <= 0) {
            return false;
        }
        return true;
    }

    public int getHideRowsCount() {
        return this.mHideRowsMap.size();
    }

    private int pos2RealPos(int pos) {
        if (pos < 0) {
            return pos;
        }
        if (pos >= getCount()) {
            return this.mCursor.getCount();
        }
        int realPos = -1;
        int indexPos = 0;
        while (true) {
            realPos++;
            if (realPos < this.mCursor.getCount()) {
                if (!this.mHideRowsMap.containsKey(Integer.valueOf(realPos))) {
                    if (indexPos >= pos) {
                        break;
                    }
                    indexPos++;
                }
            } else {
                break;
            }
        }
        return realPos;
    }

    private boolean realPosInHideRows(int realPos) {
        return this.mHideRowsMap.containsKey(Integer.valueOf(realPos));
    }

    private int getHideRowsCountBetween(int start, int end) {
        int count = 0;
        int realPos = start;
        while (realPos < this.mCursor.getCount() && realPos <= end) {
            if (this.mHideRowsMap.containsKey(Integer.valueOf(realPos))) {
                count++;
            }
            realPos++;
        }
        return count;
    }

    private int getHideRowsCountBeforePos(int realPos) {
        return getHideRowsCountBetween(0, realPos);
    }

    private int getFirstPos() {
        int realPos = 0;
        while (realPos < this.mCursor.getCount() && this.mHideRowsMap.containsKey(Integer.valueOf(realPos))) {
            realPos++;
        }
        return realPos;
    }

    private int getLastPos() {
        int realPos = this.mCursor.getCount() - 1;
        while (realPos >= 0 && this.mHideRowsMap.containsKey(Integer.valueOf(realPos))) {
            realPos--;
        }
        return realPos;
    }

    private int getNextPos() {
        int realPos = this.mCursor.getPosition() + 1;
        while (realPos < this.mCursor.getCount() && this.mHideRowsMap.containsKey(Integer.valueOf(realPos))) {
            realPos++;
        }
        return realPos;
    }

    private int getPreviousPos() {
        int realPos = this.mCursor.getPosition() - 1;
        while (realPos >= 0 && this.mHideRowsMap.containsKey(Integer.valueOf(realPos))) {
            realPos--;
        }
        return realPos;
    }

    public HideRowsCursor(Cursor cursor) {
        super(cursor);
        this.mCursor = cursor;
        this.mHideRowsMap.clear();
    }

    public boolean addHideRow(int hidePosition) {
        int realPos;
        if (this.mHideRowsMap.size() == 0) {
            realPos = hidePosition;
        } else {
            realPos = pos2RealPos(hidePosition);
        }
        if (!isValidPos(realPos) || this.mHideRowsMap.size() + 1 >= this.mCursor.getCount()) {
            return false;
        }
        this.mHideRowsMap.put(Integer.valueOf(realPos), Integer.valueOf(realPos));
        return true;
    }

    public int getCount() {
        if (hasHideRows()) {
            return this.mCursor.getCount() - getHideRowsCount();
        }
        return this.mCursor.getCount();
    }

    public int getPosition() {
        int realPos = this.mCursor.getPosition();
        int pos = realPos - getHideRowsCountBeforePos(realPos);
        if (pos >= 0) {
            return pos;
        }
        return -1;
    }

    public boolean moveToPosition(int position) {
        if (hasHideRows()) {
            return this.mCursor.moveToPosition(pos2RealPos(position));
        }
        return this.mCursor.moveToPosition(position);
    }

    public boolean moveToFirst() {
        if (hasHideRows()) {
            return this.mCursor.moveToPosition(getFirstPos());
        }
        return this.mCursor.moveToFirst();
    }

    public boolean moveToLast() {
        if (hasHideRows()) {
            return this.mCursor.moveToPosition(getLastPos());
        }
        return this.mCursor.moveToLast();
    }

    public boolean move(int offset) {
        if (!hasHideRows()) {
            return this.mCursor.move(offset);
        }
        int offset2;
        if (offset > 0) {
            do {
                offset2 = offset;
                offset = offset2 - 1;
                if (offset2 > 0) {
                }
            } while (moveToNext());
            return false;
        } else if (offset < 0) {
            do {
                offset2 = offset;
                offset = offset2 + 1;
                if (offset2 < 0) {
                }
            } while (moveToPrevious());
            return false;
        }
        return true;
    }

    public boolean moveToPrevious() {
        if (!hasHideRows()) {
            return this.mCursor.moveToPrevious();
        }
        if (!realPosInHideRows(this.mCursor.getPosition())) {
            return this.mCursor.moveToPosition(getPreviousPos());
        }
        HwLog.e("HideRowsCursor", "current position " + this.mCursor.getPosition() + " has been hided.");
        return false;
    }

    public boolean moveToNext() {
        if (!hasHideRows()) {
            return this.mCursor.moveToNext();
        }
        if (!realPosInHideRows(this.mCursor.getPosition())) {
            return this.mCursor.moveToPosition(getNextPos());
        }
        HwLog.e("HideRowsCursor", "current position " + this.mCursor.getPosition() + " has been hided.");
        return false;
    }

    public boolean isFirst() {
        if (!hasHideRows()) {
            return this.mCursor.isFirst();
        }
        if (isValidPos(this.mCursor.getPosition()) && getFirstPos() == this.mCursor.getPosition()) {
            return true;
        }
        return false;
    }

    public boolean isLast() {
        if (!hasHideRows()) {
            return this.mCursor.isLast();
        }
        if (isValidPos(this.mCursor.getPosition()) && getLastPos() == this.mCursor.getPosition()) {
            return true;
        }
        return false;
    }
}
