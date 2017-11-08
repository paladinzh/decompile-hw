package com.android.gallery3d.util;

public class RangeIntArray {
    private int[] mData;
    private int mOffset;

    public RangeIntArray(int[] src, int min, int max) {
        this.mData = src;
        this.mOffset = min;
    }

    public int get(int i) {
        return this.mData[i - this.mOffset];
    }
}
