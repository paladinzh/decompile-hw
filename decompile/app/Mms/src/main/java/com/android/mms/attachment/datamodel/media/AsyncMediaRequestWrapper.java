package com.android.mms.attachment.datamodel.media;

import com.android.mms.attachment.datamodel.binding.BindableMediaRequest;
import com.android.mms.attachment.datamodel.media.MediaResourceManager.MediaResourceLoadListener;
import java.util.List;

public class AsyncMediaRequestWrapper<T extends RefCountedMediaResource> extends BindableMediaRequest<T> {
    private final MediaRequest<T> mWrappedRequest;

    public static <T extends RefCountedMediaResource> AsyncMediaRequestWrapper<T> createWith(MediaRequest<T> wrappedRequest, MediaResourceLoadListener<T> listener) {
        return new AsyncMediaRequestWrapper(listener, wrappedRequest);
    }

    private AsyncMediaRequestWrapper(MediaResourceLoadListener<T> listener, MediaRequest<T> wrappedRequest) {
        super(listener);
        this.mWrappedRequest = wrappedRequest;
    }

    public String getKey() {
        return this.mWrappedRequest.getKey();
    }

    public MediaCache<T> getMediaCache() {
        return this.mWrappedRequest.getMediaCache();
    }

    public int getRequestType() {
        return this.mWrappedRequest.getRequestType();
    }

    public T loadMediaBlocking(List<MediaRequest<T>> chainedTask) throws Exception {
        return this.mWrappedRequest.loadMediaBlocking(chainedTask);
    }

    public MediaRequestDescriptor<T> getDescriptor() {
        return this.mWrappedRequest.getDescriptor();
    }
}
