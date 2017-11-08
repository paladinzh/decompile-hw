package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.javax.annotation.Nullable;

@GwtCompatible
public interface Predicate<T> {
    boolean apply(@Nullable T t);
}
