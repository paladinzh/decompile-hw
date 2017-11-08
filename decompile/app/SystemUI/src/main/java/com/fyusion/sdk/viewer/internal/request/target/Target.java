package com.fyusion.sdk.viewer.internal.request.target;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import com.fyusion.sdk.viewer.internal.c.i;
import com.fyusion.sdk.viewer.internal.request.b;

/* compiled from: Unknown */
public interface Target<R> extends i {
    @Nullable
    b getRequest();

    Object getWrappedObject();

    void onLoadCleared(@Nullable Drawable drawable);

    void onLoadFailed(@Nullable Drawable drawable);

    void onLoadStarted(@Nullable Drawable drawable);

    void onMetadataReady(R r);

    void onProcessingSliceProgress(int i, int i2, Object obj);

    void onResourceReady(R r);

    void setRequest(@Nullable b bVar);
}
