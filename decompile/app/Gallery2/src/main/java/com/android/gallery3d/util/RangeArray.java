package com.android.gallery3d.util;

public class RangeArray<T> {
    private T[] mData;
    private int mOffset;

    public RangeArray(int min, int max) {
        this.mData = new Object[((max - min) + 1)];
        this.mOffset = min;
    }

    public void put(int i, T object) {
        this.mData[i - this.mOffset] = object;
    }

    public T get(int i) {
        return this.mData[i - this.mOffset];
    }
}
