package com.android.gallery3d.app;

public interface LoadingListener {
    void onLoadingFinished(boolean z);

    void onLoadingStarted();

    void onVisibleRangeLoadFinished();
}
