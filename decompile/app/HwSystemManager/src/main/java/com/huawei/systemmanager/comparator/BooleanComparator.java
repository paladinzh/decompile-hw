package com.huawei.systemmanager.comparator;

import java.util.Comparator;

public abstract class BooleanComparator<T> implements Comparator<T> {
    public abstract boolean getKey(T t);

    public int compare(T lhs, T rhs) {
        boolean left = getKey(lhs);
        if ((left ^ getKey(rhs)) == 0) {
            return 0;
        }
        return left ? 1 : -1;
    }
}
