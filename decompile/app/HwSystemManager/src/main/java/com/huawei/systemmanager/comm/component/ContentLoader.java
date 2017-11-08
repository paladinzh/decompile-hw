package com.huawei.systemmanager.comm.component;

import android.content.AsyncTaskLoader;
import android.content.Context;

public abstract class ContentLoader<D> extends AsyncTaskLoader<D> {
    protected D mPreData;
    protected boolean mRegisterObserver;

    public abstract D loadInBackground();

    public ContentLoader(Context context) {
        super(context);
    }

    public void deliverResult(D data) {
        if (isReset() && this.mPreData != null) {
            onReleaseResources(this.mPreData);
        }
        D oldData = this.mPreData;
        this.mPreData = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
        if (oldData != null && oldData != data) {
            onReleaseResources(oldData);
        }
    }

    protected void onStartLoading() {
        if (!this.mRegisterObserver) {
            registerDataObserver();
            this.mRegisterObserver = true;
        }
        if (this.mPreData != null) {
            deliverResult(this.mPreData);
        }
        if (takeContentChanged() || this.mPreData == null) {
            forceLoad();
        }
    }

    protected void onReset() {
        super.onReset();
        onStopLoading();
        if (this.mPreData != null) {
            onReleaseResources(this.mPreData);
            this.mPreData = null;
        }
        if (this.mRegisterObserver) {
            unRegisterDataObser();
            this.mRegisterObserver = false;
        }
    }

    public void onCanceled(D data) {
        super.onCanceled(data);
        if (data != null) {
            onReleaseResources(data);
        }
    }

    protected void onStopLoading() {
        cancelLoad();
    }

    protected void onReleaseResources(D d) {
    }

    protected void registerDataObserver() {
    }

    protected void unRegisterDataObser() {
    }
}
