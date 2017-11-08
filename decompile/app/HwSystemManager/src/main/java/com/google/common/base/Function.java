package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.javax.annotation.Nullable;

@GwtCompatible
public interface Function<F, T> {
    @Nullable
    T apply(@Nullable F f);
}
