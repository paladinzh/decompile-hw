package com.android.contacts.hap.list;

import android.database.Cursor;
import android.database.CursorWrapper;

public class ReoderedCursor extends CursorWrapper {
    private int mCurrentPos = 0;
    private int[] mIndexs = null;
    private boolean mSingleMoveToPosition = false;

    ReoderedCursor(Cursor cursor) {
        super(cursor);
        this.mIndexs = new int[cursor.getCount()];
        for (int i = 0; i < this.mIndexs.length; i++) {
            this.mIndexs[i] = i;
        }
        this.mCurrentPos = 0;
    }

    ReoderedCursor(Cursor cursor, int[] indexs) {
        super(cursor);
        this.mIndexs = new int[indexs.length];
        System.arraycopy(indexs, 0, this.mIndexs, 0, indexs.length);
        this.mCurrentPos = 0;
    }

    public int[] getIndexs() {
        return (int[]) this.mIndexs.clone();
    }

    public boolean move(int offset) {
        this.mCurrentPos += offset;
        if (this.mCurrentPos < 0 || this.mCurrentPos >= this.mIndexs.length) {
            return false;
        }
        return super.moveToPosition(this.mIndexs[this.mCurrentPos]);
    }

    public boolean moveToFirst() {
        if (this.mIndexs.length == 0) {
            return false;
        }
        this.mCurrentPos = 0;
        return super.moveToPosition(this.mIndexs[this.mCurrentPos]);
    }

    public boolean moveToLast() {
        if (this.mIndexs.length == 0) {
            return false;
        }
        this.mCurrentPos = this.mIndexs.length - 1;
        return super.moveToPosition(this.mIndexs[this.mCurrentPos]);
    }

    public boolean moveToPrevious() {
        if (this.mCurrentPos <= 0) {
            return false;
        }
        this.mCurrentPos--;
        return super.moveToPosition(this.mIndexs[this.mCurrentPos]);
    }

    public boolean moveToNext() {
        if (this.mCurrentPos >= this.mIndexs.length - 1) {
            return false;
        }
        this.mCurrentPos++;
        return super.moveToPosition(this.mIndexs[this.mCurrentPos]);
    }

    void setSingleMoveToPosition(boolean flag) {
        this.mSingleMoveToPosition = flag;
    }

    public boolean moveToPosition(int position) {
        this.mCurrentPos = position;
        if (this.mSingleMoveToPosition) {
            return super.moveToPosition(this.mCurrentPos);
        }
        if (this.mCurrentPos < 0 || this.mCurrentPos >= this.mIndexs.length) {
            return false;
        }
        return super.moveToPosition(this.mIndexs[this.mCurrentPos]);
    }

    public int getPosition() {
        return this.mCurrentPos;
    }

    public boolean isAfterLast() {
        return this.mCurrentPos == this.mIndexs.length;
    }

    public boolean isBeforeFirst() {
        return this.mCurrentPos == -1;
    }

    public boolean isFirst() {
        return this.mCurrentPos == 0;
    }

    public boolean isLast() {
        return this.mCurrentPos == this.mIndexs.length + -1;
    }

    void reorder(int source, int desc) {
        if (source != desc) {
            int temp;
            if (source < desc) {
                temp = this.mIndexs[source];
                System.arraycopy(this.mIndexs, source + 1, this.mIndexs, source, desc - source);
                this.mIndexs[desc] = temp;
            } else {
                temp = this.mIndexs[source];
                System.arraycopy(this.mIndexs, desc, this.mIndexs, desc + 1, source - desc);
                this.mIndexs[desc] = temp;
            }
        }
    }
}
