package com.fyusion.sdk.share;

import android.graphics.Bitmap;

/* compiled from: Unknown */
public interface ShareListener {
    void onError(Exception exception);

    void onProgress(int i);

    void onSuccess(String str);

    void onSuccess(String str, Bitmap bitmap);

    void onSuccess(String str, String str2);
}
