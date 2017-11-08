package com.android.gallery3d.util;

public class IntArray {
    private int[] mData = new int[8];
    private int mSize = 0;

    public void add(int value) {
        if (this.mData.length == this.mSize) {
            int[] temp = new int[(this.mSize + this.mSize)];
            System.arraycopy(this.mData, 0, temp, 0, this.mSize);
            this.mData = temp;
        }
        int[] iArr = this.mData;
        int i = this.mSize;
        this.mSize = i + 1;
        iArr[i] = value;
    }

    public int size() {
        return this.mSize;
    }

    public int[] toArray(int[] result) {
        if (result == null || result.length < this.mSize) {
            result = new int[this.mSize];
        }
        System.arraycopy(this.mData, 0, result, 0, this.mSize);
        return result;
    }

    public int[] getInternalArray() {
        return this.mData;
    }

    public void clear() {
        this.mSize = 0;
        if (this.mData.length != 8) {
            this.mData = new int[8];
        }
    }
}
