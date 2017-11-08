package com.huawei.systemmanager.comparator;

import java.util.Comparator;

public abstract class SizeComparator<T> implements Comparator<T> {
    public abstract long getKey(T t);

    public int compare(T lhs, T rhs) {
        long left = getKey(lhs);
        long right = getKey(rhs);
        if (left < right) {
            return 1;
        }
        if (left > right) {
            return -1;
        }
        return 0;
    }
}
