package com.android.mms.util;

public interface ItemLoadedCallback<T> {
    void onItemLoaded(T t, Throwable th);
}
