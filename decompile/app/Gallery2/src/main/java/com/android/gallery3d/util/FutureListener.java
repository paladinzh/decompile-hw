package com.android.gallery3d.util;

public interface FutureListener<T> {
    void onFutureDone(Future<T> future);
}
