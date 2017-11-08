package com.huawei.cspcommon.util;

import java.util.Arrays;

public class IntArray {
    public static final int[] EmptyIntArray = new int[0];
    transient int[] array = EmptyIntArray;
    int size;

    public boolean add(int object) {
        int[] a = this.array;
        int s = this.size;
        if (s == a.length) {
            int[] newArray = new int[((s < 6 ? 12 : s >> 1) + s)];
            System.arraycopy(a, 0, newArray, 0, s);
            a = newArray;
            this.array = newArray;
        }
        a[s] = object;
        this.size = s + 1;
        return true;
    }

    public int get(int index) {
        if (index >= this.size) {
            throwIndexOutOfBoundsException(index, this.size);
        }
        return this.array[index];
    }

    public void clear() {
        if (this.size != 0) {
            Arrays.fill(this.array, 0, this.size, 0);
            this.size = 0;
        }
    }

    static IndexOutOfBoundsException throwIndexOutOfBoundsException(int index, int size) {
        throw new IndexOutOfBoundsException("Invalid index " + index + ", size is " + size);
    }

    public int size() {
        return this.size;
    }

    public int[] toArray() {
        int s = this.size;
        int[] result = new int[s];
        System.arraycopy(this.array, 0, result, 0, s);
        return result;
    }
}
