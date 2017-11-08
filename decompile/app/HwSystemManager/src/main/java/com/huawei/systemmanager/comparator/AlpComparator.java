package com.huawei.systemmanager.comparator;

import java.text.Collator;
import java.util.Comparator;

public abstract class AlpComparator<T> implements Comparator<T> {
    public abstract String getStringKey(T t);

    public int compare(T lhs, T rhs) {
        return Collator.getInstance().compare(getStringKey(lhs), getStringKey(rhs));
    }
}
