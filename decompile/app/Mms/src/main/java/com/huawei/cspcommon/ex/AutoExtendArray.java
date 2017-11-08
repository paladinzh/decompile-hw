package com.huawei.cspcommon.ex;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;

public class AutoExtendArray<T> {
    private final Class<T> clazz;
    T[] mArray;
    private int mExtendSize;
    private int mSize = 0;

    public AutoExtendArray(Class<T> cType) {
        this.clazz = cType;
        this.mExtendSize = 20;
        this.mArray = (Object[]) Array.newInstance(this.clazz, this.mExtendSize);
    }

    public void sort(Comparator<T> c) {
        Arrays.sort(this.mArray, 0, this.mSize, c);
    }

    private void extend() {
        this.mExtendSize += this.mExtendSize >> 1;
        Object[] newArray = (Object[]) Array.newInstance(this.clazz, this.mArray.length + this.mExtendSize);
        for (int i = 0; i < this.mSize; i++) {
            newArray[i] = this.mArray[i];
        }
        this.mArray = newArray;
    }

    public void add(T o) {
        if (this.mSize == this.mArray.length) {
            extend();
        }
        Object[] objArr = this.mArray;
        int i = this.mSize;
        this.mSize = i + 1;
        objArr[i] = o;
    }

    public T get(int idx) {
        return (idx >= this.mSize || idx < 0) ? null : this.mArray[idx];
    }

    public int size() {
        return this.mSize;
    }
}
