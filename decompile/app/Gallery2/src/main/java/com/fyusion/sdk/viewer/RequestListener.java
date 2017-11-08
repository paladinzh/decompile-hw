package com.fyusion.sdk.viewer;

import android.support.annotation.Nullable;

/* compiled from: Unknown */
public interface RequestListener<R> {
    boolean onLoadFailed(@Nullable FyuseException fyuseException, Object obj);

    void onProgress(int i);

    boolean onResourceReady(Object obj);
}
