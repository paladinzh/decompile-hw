package com.android.mms.attachment.datamodel.media;

import android.os.AsyncTask;
import com.android.mms.attachment.Factory;
import com.android.mms.attachment.datamodel.binding.BindableMediaRequest;
import com.huawei.cspcommon.MLog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class MediaResourceManager {
    private static final Executor MEDIA_BACKGROUND_EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(Runnable runnable) {
            Thread encodingThread = new Thread(runnable);
            encodingThread.setPriority(1);
            return encodingThread;
        }
    });
    private static final Executor MEDIA_LOADING_EXECUTOR = Executors.newFixedThreadPool(10);

    public interface MediaResourceLoadListener<T extends RefCountedMediaResource> {
        void onMediaResourceLoadError(MediaRequest<T> mediaRequest, Exception exception);

        void onMediaResourceLoaded(MediaRequest<T> mediaRequest, T t, boolean z);
    }

    private class MediaLoadingResult<T extends RefCountedMediaResource> {
        public final boolean fromCache;
        public final T loadedResource;
        private final List<MediaRequest<T>> mChainedRequests;

        MediaLoadingResult(T loadedResource, boolean fromCache, List<MediaRequest<T>> chainedRequests) {
            this.loadedResource = loadedResource;
            this.fromCache = fromCache;
            this.mChainedRequests = chainedRequests;
        }

        public void scheduleChainedRequests() {
            for (MediaRequest<T> mediaRequest : this.mChainedRequests) {
                MediaResourceManager.this.scheduleAsyncMediaRequest(mediaRequest, MediaResourceManager.MEDIA_BACKGROUND_EXECUTOR);
            }
        }
    }

    public static MediaResourceManager get() {
        return Factory.get().getMediaResourceManager();
    }

    public <T extends RefCountedMediaResource> void requestMediaResourceAsync(MediaRequest<T> mediaRequest) {
        scheduleAsyncMediaRequest(mediaRequest, MEDIA_LOADING_EXECUTOR);
    }

    private <T extends RefCountedMediaResource> MediaLoadingResult<T> processMediaRequestInternal(MediaRequest<T> mediaRequest) throws Exception {
        T loadedResource;
        boolean z;
        List<MediaRequest<T>> chainedRequests = new ArrayList();
        T cachedResource = loadMediaFromCache(mediaRequest);
        if (cachedResource == null) {
            loadedResource = loadMediaFromRequest(mediaRequest, chainedRequests);
        } else if (cachedResource.isEncoded()) {
            MediaRequest<T> decodeRequest = cachedResource.getMediaDecodingRequest(mediaRequest);
            cachedResource.release();
            loadedResource = loadMediaFromRequest(decodeRequest, chainedRequests);
        } else {
            loadedResource = cachedResource;
        }
        if (cachedResource != null) {
            z = true;
        } else {
            z = false;
        }
        return new MediaLoadingResult(loadedResource, z, chainedRequests);
    }

    private <T extends RefCountedMediaResource> T loadMediaFromCache(MediaRequest<T> mediaRequest) {
        if (mediaRequest.getRequestType() != 3) {
            return null;
        }
        MediaCache<T> mediaCache = mediaRequest.getMediaCache();
        if (mediaCache != null) {
            T mediaResource = mediaCache.fetchResourceFromCache(mediaRequest.getKey());
            if (mediaResource != null) {
                return mediaResource;
            }
        }
        return null;
    }

    private <T extends RefCountedMediaResource> T loadMediaFromRequest(MediaRequest<T> mediaRequest, List<MediaRequest<T>> chainedRequests) throws Exception {
        T resource = mediaRequest.loadMediaBlocking(chainedRequests);
        resource.addRef();
        if (resource.isCacheable()) {
            addResourceToMemoryCache(mediaRequest, resource);
        }
        return resource;
    }

    private <T extends RefCountedMediaResource> void scheduleAsyncMediaRequest(final MediaRequest<T> mediaRequest, Executor executor) {
        BindableMediaRequest<T> bindableRequest;
        if (mediaRequest instanceof BindableMediaRequest) {
            bindableRequest = (BindableMediaRequest) mediaRequest;
        } else {
            bindableRequest = null;
        }
        if (bindableRequest == null || bindableRequest.isBound()) {
            new AsyncTask<Void, Void, MediaLoadingResult<T>>() {
                private Exception mException;

                protected MediaLoadingResult<T> doInBackground(Void... params) {
                    if (bindableRequest != null && !bindableRequest.isBound()) {
                        return null;
                    }
                    try {
                        return MediaResourceManager.this.processMediaRequestInternal(mediaRequest);
                    } catch (Exception e) {
                        this.mException = e;
                        return null;
                    }
                }

                protected void onPostExecute(MediaLoadingResult<T> result) {
                    if (result != null) {
                        try {
                            if (bindableRequest != null) {
                                bindableRequest.onMediaResourceLoaded(bindableRequest, result.loadedResource, result.fromCache);
                            }
                            result.loadedResource.release();
                            result.scheduleChainedRequests();
                        } catch (Throwable th) {
                            result.loadedResource.release();
                            result.scheduleChainedRequests();
                        }
                    } else if (this.mException != null) {
                        MLog.e("MediaResourceManager", "Asynchronous media loading failed, key=" + mediaRequest.getKey(), this.mException);
                        if (bindableRequest != null) {
                            bindableRequest.onMediaResourceLoadError(bindableRequest, this.mException);
                        }
                    } else if (MLog.isLoggable("Mms_app", 2)) {
                        MLog.v("MediaResourceManager", "media request not processed, no longer bound; key=");
                    }
                }
            }.executeOnExecutor(executor, new Void[]{(Void) null});
        }
    }

    <T extends RefCountedMediaResource> void addResourceToMemoryCache(MediaRequest<T> mediaRequest, T mediaResource) {
        MediaCache<T> mediaCache = mediaRequest.getMediaCache();
        if (mediaCache != null) {
            mediaCache.addResourceToCache(mediaRequest.getKey(), mediaResource);
            if (MLog.isLoggable("Mms_app", 2)) {
                MLog.v("MediaResourceManager", "added media resource to " + mediaCache.getName() + ". key=");
            }
        }
    }
}
