package com.android.mms.util;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import com.android.mms.model.SlideshowModel;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.MultimediaMessagePdu;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.util.PduCache;
import com.google.android.mms.util.PduCacheEntry;
import com.huawei.cspcommon.MLog;
import java.util.Set;

public class PduLoaderManager extends BackgroundLoaderManager {
    private final Context mContext;
    private final PduCache mPduCache = PduCache.getInstance();
    private final PduPersister mPduPersister;
    private final SimpleCache<Uri, SlideshowModel> mSlideshowCache = new SimpleCache(8, 16, 0.75f, false);

    public static class PduLoaded {
        public final GenericPdu mPdu;
        public final SlideshowModel mSlideshow;

        public PduLoaded(GenericPdu pdu, SlideshowModel slideshow) {
            this.mPdu = pdu;
            this.mSlideshow = slideshow;
        }
    }

    public class PduTask implements Runnable {
        private final boolean mRequestSlideshow;
        private final Uri mUri;

        public PduTask(Uri uri, boolean requestSlideshow) {
            if (uri == null) {
                throw new NullPointerException();
            }
            this.mUri = uri;
            this.mRequestSlideshow = requestSlideshow;
        }

        public void run() {
            GenericPdu pdu = null;
            SlideshowModel slideshow = null;
            Throwable exception = null;
            try {
                pdu = PduLoaderManager.this.mPduPersister.load(this.mUri);
                if (pdu != null && this.mRequestSlideshow) {
                    slideshow = SlideshowModel.createFromPduBody(PduLoaderManager.this.mContext, ((MultimediaMessagePdu) pdu).getBody());
                }
            } catch (Throwable e) {
                MLog.e("Mms:PduLoaderManager", "MmsException loading uri ", e);
                exception = e;
            } catch (Throwable ex) {
                MLog.e("Mms:PduLoaderManager", "ClassCastException NotificationInd cannot be cast to MultimediaMessagePdu", ex);
                exception = ex;
            }
            final GenericPdu resultPdu = pdu;
            final SlideshowModel resultSlideshow = slideshow;
            final Throwable resultException = exception;
            PduLoaderManager.this.mCallbackHandler.post(new Runnable() {
                public void run() {
                    Set<ItemLoadedCallback> callbacks = (Set) PduLoaderManager.this.mCallbacks.get(PduTask.this.mUri);
                    if (callbacks != null) {
                        for (ItemLoadedCallback<PduLoaded> callback : BackgroundLoaderManager.asList(callbacks)) {
                            if (MLog.isLoggable("Mms:PduLoaderManager", 3)) {
                                MLog.d("Mms:PduLoaderManager", "Invoking pdu callback " + callback);
                            }
                            callback.onItemLoaded(new PduLoaded(resultPdu, resultSlideshow), resultException);
                        }
                    }
                    if (resultSlideshow != null) {
                        PduLoaderManager.this.mSlideshowCache.put(PduTask.this.mUri, resultSlideshow);
                    }
                    PduLoaderManager.this.mCallbacks.remove(PduTask.this.mUri);
                    PduLoaderManager.this.mPendingTaskUris.remove(PduTask.this.mUri);
                    if (MLog.isLoggable("Mms_pducache", 3)) {
                        MLog.d("Mms:PduLoaderManager", "Pdu task for mUri exiting; " + PduLoaderManager.this.mPendingTaskUris.size() + " remain");
                    }
                }
            });
        }
    }

    public PduLoaderManager(Context context, Handler handler) {
        super(context, handler);
        this.mPduPersister = PduPersister.getPduPersister(context);
        this.mContext = context;
    }

    public SlideshowModel getCachedModel(Uri uri) {
        return (SlideshowModel) this.mSlideshowCache.get(uri);
    }

    public ItemLoadedFuture getPdu(Uri uri, boolean requestSlideshow, final ItemLoadedCallback<PduLoaded> callback) {
        if (uri == null) {
            throw new NullPointerException();
        }
        PduCacheEntry pduCacheEntry = null;
        synchronized (this.mPduCache) {
            if (!this.mPduCache.isUpdating(uri)) {
                pduCacheEntry = (PduCacheEntry) this.mPduCache.get(uri);
            }
        }
        SlideshowModel slideshowModel = requestSlideshow ? (SlideshowModel) this.mSlideshowCache.get(uri) : null;
        boolean slideshowExists = (requestSlideshow && slideshowModel == null) ? false : true;
        boolean pduExists = (pduCacheEntry == null || pduCacheEntry.getPdu() == null) ? false : true;
        boolean newTaskRequired = ((pduExists && slideshowExists) || this.mPendingTaskUris.contains(uri)) ? false : true;
        boolean callbackRequired = callback != null;
        if (pduExists && slideshowExists) {
            if (callbackRequired) {
                callback.onItemLoaded(new PduLoaded(pduCacheEntry.getPdu(), slideshowModel), null);
            }
            return new NullItemLoadedFuture();
        }
        if (callbackRequired) {
            addCallback(uri, callback);
        }
        if (newTaskRequired) {
            this.mPendingTaskUris.add(uri);
            this.mExecutor.execute(new PduTask(uri, requestSlideshow));
        }
        return new ItemLoadedFuture() {
            private boolean mIsDone;

            public void cancel(Uri uri) {
                PduLoaderManager.this.cancelCallback(callback);
                PduLoaderManager.this.removePdu(uri);
            }

            public void setIsDone(boolean done) {
                this.mIsDone = done;
            }

            public boolean isDone() {
                return this.mIsDone;
            }
        };
    }

    public void clear() {
        super.clear();
        synchronized (this.mPduCache) {
            this.mPduCache.purgeAll();
        }
        this.mSlideshowCache.clear();
    }

    public void removePdu(Uri uri) {
        if (MLog.isLoggable("Mms:PduLoaderManager", 3)) {
            MLog.d("Mms:PduLoaderManager", "removePdu.");
        }
        if (uri != null) {
            synchronized (this.mPduCache) {
                this.mPduCache.purge(uri);
            }
            this.mSlideshowCache.remove(uri);
        }
    }

    public String getTag() {
        return "Mms:PduLoaderManager";
    }
}
