package com.android.settings.smartcover;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import java.util.List;
import java.util.Map;

public abstract class AsyncLoader<T> extends AsyncTaskLoader<T> {
    private T infos;
    protected Bundle mBundle = null;

    public AsyncLoader(Context context, Bundle args) {
        super(context);
        this.mBundle = args;
    }

    public T loadInBackground() {
        return null;
    }

    public void deliverResult(T data) {
        if (isReset() && data != null) {
            onReleaseResources(data);
        }
        T oldApps = this.infos;
        this.infos = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
        if (oldApps != null) {
            onReleaseResources(oldApps);
        }
    }

    protected void onStartLoading() {
        if (this.infos != null) {
            deliverResult(this.infos);
        }
        if (takeContentChanged() || this.infos == null) {
            forceLoad();
        }
    }

    public void onCanceled(T data) {
        super.onCanceled(data);
        onReleaseResources(this.infos);
    }

    protected void onStopLoading() {
        cancelLoad();
    }

    protected void onReset() {
        super.onReset();
        onStopLoading();
    }

    protected void onReleaseResources(T datas) {
        if (datas != null) {
            if (datas instanceof List) {
                ((List) datas).clear();
            } else if (datas instanceof Map) {
                ((Map) datas).clear();
            }
        }
    }
}
