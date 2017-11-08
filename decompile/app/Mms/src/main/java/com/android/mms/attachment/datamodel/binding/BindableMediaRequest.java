package com.android.mms.attachment.datamodel.binding;

import com.android.mms.attachment.datamodel.media.MediaRequest;
import com.android.mms.attachment.datamodel.media.MediaResourceManager.MediaResourceLoadListener;
import com.android.mms.attachment.datamodel.media.RefCountedMediaResource;

public abstract class BindableMediaRequest<T extends RefCountedMediaResource> extends BindableOnceData implements MediaRequest<T>, MediaResourceLoadListener<T> {
    private MediaResourceLoadListener<T> mListener;

    public BindableMediaRequest(MediaResourceLoadListener<T> listener) {
        this.mListener = listener;
    }

    public void onMediaResourceLoaded(MediaRequest<T> request, T resource, boolean cached) {
        if (isBound() && this.mListener != null) {
            this.mListener.onMediaResourceLoaded(request, resource, cached);
        }
    }

    public void onMediaResourceLoadError(MediaRequest<T> request, Exception exception) {
        if (isBound() && this.mListener != null) {
            this.mListener.onMediaResourceLoadError(request, exception);
        }
    }

    protected void unregisterListeners() {
        this.mListener = null;
    }
}
