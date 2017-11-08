package com.android.contacts;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.ImageView;
import com.android.contacts.ContactPhotoManager.DefaultImageProvider;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.MemoryUtils;
import com.android.contacts.util.UriUtils;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.location.places.Place;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huawei.cspcommon.util.BitmapUtil;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/* compiled from: ContactPhotoManager */
class ContactPhotoManagerImpl extends ContactPhotoManager implements Callback {
    private static final String[] COLUMNS = new String[]{"_id", "data15"};
    private static String[] EMPTY_STRING_ARRAY = null;
    private final LruCache<Object, Bitmap> mBitmapCache;
    private final LruCache<Object, BitmapHolder> mBitmapHolderCache;
    private volatile boolean mBitmapHolderCacheAllUnfresh = true;
    private final int mBitmapHolderCacheRedZoneBytes;
    private final Context mContext;
    private final AtomicInteger mFreshCacheOverwrite = new AtomicInteger();
    private LoaderThread mLoaderThread;
    private boolean mLoadingRequested;
    private final Handler mMainThreadHandler = new Handler(this);
    private boolean mPaused;
    private final ConcurrentHashMap<ImageView, Request> mPendingRequests = new ConcurrentHashMap();
    private final AtomicInteger mStaleCacheOverwrite = new AtomicInteger();

    /* compiled from: ContactPhotoManager */
    private static class BitmapHolder {
        Bitmap bitmap;
        Reference<Bitmap> bitmapRef;
        final byte[] bytes;
        int decodedSampleSize;
        volatile boolean fresh = true;
        Reference<Bitmap> mRoundedEdgeBitmapRef;
        final int originalSmallerExtent;

        public BitmapHolder(byte[] bytes, int originalSmallerExtent) {
            this.bytes = bytes;
            this.originalSmallerExtent = originalSmallerExtent;
        }
    }

    /* compiled from: ContactPhotoManager */
    private class LoaderThread extends HandlerThread implements Callback {
        private byte[] mBuffer;
        private Handler mLoaderThreadHandler;
        private final HashMap<Long, Request> mPhotoIds = Maps.newHashMap();
        private final Set<String> mPhotoIdsAsStrings = Sets.newHashSet();
        private final Set<Request> mPhotoUris = Sets.newHashSet();
        private final List<Long> mPreloadPhotoIds = Lists.newArrayList();
        private int mPreloadStatus = 0;
        private final ContentResolver mResolver;
        private final StringBuilder mStringBuilder = new StringBuilder();

        private void queryPhotosForPreload() {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0052 in list [B:13:0x004f]
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r7 = this;
            r6 = 0;
            r0 = android.provider.ContactsContract.Contacts.CONTENT_URI;	 Catch:{ all -> 0x0046 }
            r0 = r0.buildUpon();	 Catch:{ all -> 0x0046 }
            r2 = "limit";	 Catch:{ all -> 0x0046 }
            r3 = 100;	 Catch:{ all -> 0x0046 }
            r3 = java.lang.String.valueOf(r3);	 Catch:{ all -> 0x0046 }
            r0 = r0.appendQueryParameter(r2, r3);	 Catch:{ all -> 0x0046 }
            r1 = r0.build();	 Catch:{ all -> 0x0046 }
            r0 = r7.mResolver;	 Catch:{ all -> 0x0046 }
            r2 = 1;	 Catch:{ all -> 0x0046 }
            r2 = new java.lang.String[r2];	 Catch:{ all -> 0x0046 }
            r3 = "photo_id";	 Catch:{ all -> 0x0046 }
            r4 = 0;	 Catch:{ all -> 0x0046 }
            r2[r4] = r3;	 Catch:{ all -> 0x0046 }
            r3 = "photo_id NOT NULL AND photo_id!=0";	 Catch:{ all -> 0x0046 }
            r5 = "starred DESC, last_time_contacted DESC";	 Catch:{ all -> 0x0046 }
            r4 = 0;	 Catch:{ all -> 0x0046 }
            r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ all -> 0x0046 }
            if (r6 == 0) goto L_0x004d;	 Catch:{ all -> 0x0046 }
        L_0x0030:
            r0 = r6.moveToNext();	 Catch:{ all -> 0x0046 }
            if (r0 == 0) goto L_0x004d;	 Catch:{ all -> 0x0046 }
        L_0x0036:
            r0 = r7.mPreloadPhotoIds;	 Catch:{ all -> 0x0046 }
            r2 = 0;	 Catch:{ all -> 0x0046 }
            r2 = r6.getLong(r2);	 Catch:{ all -> 0x0046 }
            r2 = java.lang.Long.valueOf(r2);	 Catch:{ all -> 0x0046 }
            r3 = 0;	 Catch:{ all -> 0x0046 }
            r0.add(r3, r2);	 Catch:{ all -> 0x0046 }
            goto L_0x0030;
        L_0x0046:
            r0 = move-exception;
            if (r6 == 0) goto L_0x004c;
        L_0x0049:
            r6.close();
        L_0x004c:
            throw r0;
        L_0x004d:
            if (r6 == 0) goto L_0x0052;
        L_0x004f:
            r6.close();
        L_0x0052:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.ContactPhotoManagerImpl.LoaderThread.queryPhotosForPreload():void");
        }

        public LoaderThread(ContentResolver resolver) {
            super("ContactPhotoLoader", 10);
            this.mResolver = resolver;
        }

        public void ensureHandler() {
            if (this.mLoaderThreadHandler == null) {
                this.mLoaderThreadHandler = new Handler(getLooper(), this);
            }
        }

        public void requestPreloading() {
            if (this.mPreloadStatus != 2) {
                ensureHandler();
                if (!this.mLoaderThreadHandler.hasMessages(1)) {
                    this.mLoaderThreadHandler.sendEmptyMessageDelayed(0, 1000);
                }
            }
        }

        public void requestLoading() {
            ensureHandler();
            this.mLoaderThreadHandler.removeMessages(0);
            this.mLoaderThreadHandler.sendEmptyMessage(1);
        }

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    preloadPhotosInBackground();
                    break;
                case 1:
                    loadPhotosInBackground();
                    break;
            }
            return true;
        }

        private void preloadPhotosInBackground() {
            if (this.mPreloadStatus != 2) {
                if (this.mPreloadStatus == 0) {
                    queryPhotosForPreload();
                    if (this.mPreloadPhotoIds.isEmpty()) {
                        this.mPreloadStatus = 2;
                    } else {
                        this.mPreloadStatus = 1;
                    }
                    requestPreloading();
                } else if (ContactPhotoManagerImpl.this.mBitmapHolderCache.size() > ContactPhotoManagerImpl.this.mBitmapHolderCacheRedZoneBytes) {
                    this.mPreloadStatus = 2;
                } else {
                    this.mPhotoIds.clear();
                    this.mPhotoIdsAsStrings.clear();
                    int count = 0;
                    int preloadSize = this.mPreloadPhotoIds.size();
                    while (preloadSize > 0 && this.mPhotoIds.size() < 25) {
                        preloadSize--;
                        count++;
                        Long photoId = (Long) this.mPreloadPhotoIds.get(preloadSize);
                        this.mPhotoIds.put(photoId, null);
                        this.mPhotoIdsAsStrings.add(photoId.toString());
                        this.mPreloadPhotoIds.remove(preloadSize);
                    }
                    loadThumbnails(true);
                    if (preloadSize == 0) {
                        this.mPreloadStatus = 2;
                    }
                    HwLog.v("ContactPhotoManager", "Preloaded " + count + " photos.  Cached bytes: " + ContactPhotoManagerImpl.this.mBitmapHolderCache.size());
                    requestPreloading();
                }
            }
        }

        private void loadPhotosInBackground() {
            ContactPhotoManagerImpl.this.obtainPhotoIdsAndUrisToLoad(this.mPhotoIds, this.mPhotoIdsAsStrings, this.mPhotoUris);
            loadThumbnails(false);
            loadUriBasedPhotos();
            requestPreloading();
        }

        private void loadThumbnail(boolean preloading, int start, int len) {
            if (start + len <= ContactPhotoManagerImpl.EMPTY_STRING_ARRAY.length && start >= 0) {
                this.mStringBuilder.setLength(0);
                this.mStringBuilder.append("_id IN(");
                String[] idsStrings = new String[len];
                try {
                    System.arraycopy(ContactPhotoManagerImpl.EMPTY_STRING_ARRAY, start, idsStrings, 0, len);
                    for (int i = 0; i < len; i++) {
                        if (i != 0) {
                            this.mStringBuilder.append(',');
                        }
                        this.mStringBuilder.append('?');
                    }
                    this.mStringBuilder.append(')');
                    Cursor cursor = null;
                    try {
                        if (ContactPhotoManagerImpl.DEBUG) {
                            HwLog.d("ContactPhotoManager", "Loading " + TextUtils.join(",", idsStrings));
                        }
                        cursor = this.mResolver.query(Data.CONTENT_URI, ContactPhotoManagerImpl.COLUMNS, this.mStringBuilder.toString(), idsStrings, null);
                        if (cursor != null) {
                            while (cursor.moveToNext()) {
                                Long id = Long.valueOf(cursor.getLong(0));
                                byte[] bytes = cursor.getBlob(1);
                                Request lRequest = (Request) this.mPhotoIds.get(id);
                                if (lRequest != null) {
                                    lRequest.setRequestedExtent(-1);
                                }
                                ContactPhotoManagerImpl.this.cacheBitmap(id, bytes, preloading, lRequest);
                                this.mPhotoIds.remove(id);
                            }
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void loadThumbnailsBatch(boolean preloading) {
            int batchs = this.mPhotoIds.size() / VTMCDataCache.MAXSIZE;
            int last = this.mPhotoIds.size() % VTMCDataCache.MAXSIZE;
            ContactPhotoManagerImpl.EMPTY_STRING_ARRAY = new String[this.mPhotoIdsAsStrings.size()];
            this.mPhotoIdsAsStrings.toArray(ContactPhotoManagerImpl.EMPTY_STRING_ARRAY);
            int start = 0;
            for (int k = 0; k < batchs; k++) {
                loadThumbnail(preloading, start, VTMCDataCache.MAXSIZE);
                start = (k + 1) * VTMCDataCache.MAXSIZE;
            }
            if (last > 0) {
                loadThumbnail(preloading, start, last);
            }
        }

        private void loadThumbnails(boolean preloading) {
            if (!this.mPhotoIds.isEmpty()) {
                Set<Long> lPhotoIds = this.mPhotoIds.keySet();
                if (!preloading && this.mPreloadStatus == 1) {
                    for (Long id : lPhotoIds) {
                        this.mPreloadPhotoIds.remove(id);
                    }
                    if (this.mPreloadPhotoIds.isEmpty()) {
                        this.mPreloadStatus = 2;
                    }
                }
                loadThumbnailsBatch(preloading);
                for (Long id2 : lPhotoIds) {
                    Request lRequest = (Request) this.mPhotoIds.get(id2);
                    if (lRequest != null) {
                        lRequest.setRequestedExtent(-1);
                    }
                    if (ContactsContract.isProfileId(id2.longValue())) {
                        Cursor cursor = null;
                        try {
                            cursor = this.mResolver.query(ContentUris.withAppendedId(Data.CONTENT_URI, id2.longValue()), ContactPhotoManagerImpl.COLUMNS, null, null, null);
                            if (cursor == null || !cursor.moveToFirst()) {
                                ContactPhotoManagerImpl.this.cacheBitmap(id2, null, preloading, lRequest);
                            } else {
                                ContactPhotoManagerImpl.this.cacheBitmap(Long.valueOf(cursor.getLong(0)), cursor.getBlob(1), preloading, lRequest);
                            }
                            if (cursor != null) {
                                cursor.close();
                            }
                        } catch (Throwable th) {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    } else {
                        ContactPhotoManagerImpl.this.cacheBitmap(id2, null, preloading, lRequest);
                    }
                }
                ContactPhotoManagerImpl.this.mMainThreadHandler.sendEmptyMessage(2);
            }
        }

        private void loadUriBasedPhotos() {
            for (Request uriRequest : this.mPhotoUris) {
                Uri uri = uriRequest.getUri();
                if (this.mBuffer == null) {
                    this.mBuffer = new byte[16384];
                }
                InputStream is;
                try {
                    if (ContactPhotoManagerImpl.DEBUG) {
                        HwLog.d("ContactPhotoManager", "Loading " + uri);
                    }
                    is = this.mResolver.openInputStream(uri);
                    if (is != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        while (true) {
                            int size = is.read(this.mBuffer);
                            if (size == -1) {
                                break;
                            }
                            baos.write(this.mBuffer, 0, size);
                        }
                        is.close();
                        ContactPhotoManagerImpl.this.cacheBitmap(uri, baos.toByteArray(), false, uriRequest);
                        ContactPhotoManagerImpl.this.mMainThreadHandler.sendEmptyMessage(2);
                    } else {
                        HwLog.v("ContactPhotoManager", "Cannot load photo " + uri);
                        ContactPhotoManagerImpl.this.cacheBitmap(uri, null, false, uriRequest);
                    }
                } catch (Exception ex) {
                    Cursor cursor = this.mResolver.query(Data.CONTENT_URI, ContactPhotoManagerImpl.COLUMNS, "photo_uri = '" + uri + "' AND " + "mimetype" + "='" + "vnd.android.cursor.item/photo" + "'", null, null);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            ContactPhotoManagerImpl.this.cacheBitmap(uri, cursor.getBlob(1), false, uriRequest);
                            ContactPhotoManagerImpl.this.mMainThreadHandler.sendEmptyMessage(2);
                        } else {
                            HwLog.w("ContactPhotoManager", "Cannot load photo " + uri, ex);
                            ContactPhotoManagerImpl.this.cacheBitmap(uri, null, false, uriRequest);
                        }
                        cursor.close();
                    }
                } catch (Throwable th) {
                    is.close();
                }
            }
        }
    }

    /* compiled from: ContactPhotoManager */
    private static final class Request {
        private final boolean mDarkTheme;
        private final DefaultImageProvider mDefaultProvider;
        private int mFlags = 0;
        private final long mId;
        private int mRequestedExtent;
        private final Uri mUri;

        private Request(long aId, Uri aUri, int aRequestedExtent, boolean aDarkTheme, DefaultImageProvider aDefaultProvider, int aFlags) {
            this.mId = aId;
            this.mUri = aUri;
            this.mDarkTheme = aDarkTheme;
            this.mRequestedExtent = aRequestedExtent;
            this.mDefaultProvider = aDefaultProvider;
            this.mFlags = aFlags;
        }

        public static Request createFromThumbnailId(long aId, boolean aDarkTheme, DefaultImageProvider aDefaultProvider, int aFlags) {
            return new Request(aId, null, -1, aDarkTheme, aDefaultProvider, aFlags);
        }

        public static Request createFromUri(Uri uri, int requestedExtent, boolean darkTheme, DefaultImageProvider defaultProvider, int flags) {
            return new Request(0, uri, requestedExtent, darkTheme, defaultProvider, flags);
        }

        public boolean isUriRequest() {
            return this.mUri != null;
        }

        public Uri getUri() {
            return this.mUri;
        }

        public long getId() {
            return this.mId;
        }

        public int getRequestedExtent() {
            return this.mRequestedExtent;
        }

        public int getFlags() {
            return this.mFlags;
        }

        public void setRequestedExtent(int aRequestedExtent) {
            this.mRequestedExtent = aRequestedExtent;
        }

        public int hashCode() {
            return Long.valueOf((31 * ((31 * (31 + (this.mId ^ (this.mId >>> 32)))) + ((long) this.mRequestedExtent))) + ((long) (this.mUri == null ? 0 : this.mUri.hashCode()))).intValue();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Request that = (Request) obj;
            return this.mId == that.mId && this.mRequestedExtent == that.mRequestedExtent && UriUtils.areEqual(this.mUri, that.mUri);
        }

        public Object getKey() {
            return this.mUri == null ? Long.valueOf(this.mId) : this.mUri;
        }

        public void applyDefaultImage(ImageView view) {
            this.mDefaultProvider.applyDefaultImage(view, this.mRequestedExtent, this.mDarkTheme, null);
        }
    }

    public ContactPhotoManagerImpl(Context context) {
        this.mContext = context;
        float cacheSizeAdjustment = MemoryUtils.getTotalMemorySize() >= 671088640 ? 1.0f : 0.5f;
        this.mBitmapCache = new LruCache<Object, Bitmap>(Float.valueOf(1769472.0f * cacheSizeAdjustment).intValue()) {
            protected int sizeOf(Object key, Bitmap value) {
                return value.getByteCount();
            }

            protected void entryRemoved(boolean evicted, Object key, Bitmap oldValue, Bitmap newValue) {
                if (ContactPhotoManagerImpl.DEBUG) {
                    ContactPhotoManagerImpl.this.dumpStats();
                }
            }
        };
        int holderCacheSize = Float.valueOf(2000000.0f * cacheSizeAdjustment).intValue();
        this.mBitmapHolderCache = new LruCache<Object, BitmapHolder>(holderCacheSize) {
            protected int sizeOf(Object key, BitmapHolder value) {
                return value.bytes != null ? value.bytes.length : 0;
            }

            protected void entryRemoved(boolean evicted, Object key, BitmapHolder oldValue, BitmapHolder newValue) {
                if (ContactPhotoManagerImpl.DEBUG) {
                    ContactPhotoManagerImpl.this.dumpStats();
                }
            }
        };
        this.mBitmapHolderCacheRedZoneBytes = (int) (((float) holderCacheSize) * 0.75f);
        HwLog.i("ContactPhotoManager", "Cache adj: " + cacheSizeAdjustment);
        if (DEBUG) {
            HwLog.d("ContactPhotoManager", "Cache size: " + btk(this.mBitmapHolderCache.maxSize()) + " + " + btk(this.mBitmapCache.maxSize()));
        }
    }

    private static String btk(int bytes) {
        return ((bytes + Place.TYPE_SUBLOCALITY_LEVEL_1) / Place.TYPE_SUBLOCALITY_LEVEL_2) + "K";
    }

    private void dumpStats() {
        if (DEBUG) {
            Bitmap b;
            int numHolders = 0;
            int rawBytes = 0;
            int bitmapBytes = 0;
            int numBitmaps = 0;
            for (BitmapHolder h : this.mBitmapHolderCache.snapshot().values()) {
                numHolders++;
                if (h.bytes != null) {
                    rawBytes += h.bytes.length;
                }
                if (h.bitmapRef != null) {
                    b = (Bitmap) h.bitmapRef.get();
                } else {
                    b = null;
                }
                if (b != null) {
                    numBitmaps++;
                    bitmapBytes += b.getByteCount();
                }
            }
            numBitmaps = 0;
            bitmapBytes = 0;
            for (Bitmap b2 : this.mBitmapCache.snapshot().values()) {
                numBitmaps++;
                bitmapBytes += b2.getByteCount();
            }
        }
    }

    public void onTrimMemory(int level) {
        if (DEBUG) {
            HwLog.d("ContactPhotoManager", "onTrimMemory: " + level);
        }
        if (level >= 60) {
            clear();
        }
    }

    public void preloadPhotosInBackground() {
        ensureLoaderThread();
        this.mLoaderThread.requestPreloading();
    }

    public void loadThumbnail(ImageView view, long photoId, boolean darkTheme, DefaultImageRequest defaultImageRequest, DefaultImageProvider defaultProvider, long contactsId, int aFlags) {
        if (photoId <= 0) {
            defaultProvider.applyDefaultImage(view, -1, darkTheme, defaultImageRequest, aFlags, photoId);
            this.mPendingRequests.remove(view);
            return;
        }
        if (DEBUG) {
            HwLog.d("ContactPhotoManager", "loadPhoto request: " + photoId);
        }
        loadPhotoByIdOrUri(view, Request.createFromThumbnailId(photoId, darkTheme, defaultProvider, aFlags));
    }

    public void loadThumbnail(ImageView view, long photoId, boolean darkTheme, DefaultImageRequest defaultImageRequest, DefaultImageProvider defaultProvider) {
        loadThumbnail(view, photoId, darkTheme, defaultImageRequest, defaultProvider, -1, 0);
    }

    public void loadPhoto(ImageView view, Uri photoUri, int requestedExtent, boolean darkTheme, DefaultImageRequest defaultImageRequest, DefaultImageProvider defaultProvider) {
        loadPhoto(view, photoUri, requestedExtent, darkTheme, defaultImageRequest, defaultProvider, 0);
    }

    public void loadPhoto(ImageView view, Uri photoUri, int requestedExtent, boolean darkTheme, DefaultImageRequest defaultImageRequest, DefaultImageProvider defaultProvider, int flags) {
        if (DEBUG) {
            HwLog.d("ContactPhotoManager", "loadPhoto request is null: " + (photoUri == null));
        }
        if (photoUri == null) {
            defaultProvider.applyDefaultImage(view, requestedExtent, darkTheme, defaultImageRequest, flags, -1);
            this.mPendingRequests.remove(view);
            return;
        }
        if (DEBUG) {
            HwLog.d("ContactPhotoManager", "loadPhoto request: " + photoUri);
        }
        loadPhotoByIdOrUri(view, Request.createFromUri(photoUri, requestedExtent, darkTheme, defaultProvider, flags));
    }

    private void loadPhotoByIdOrUri(ImageView view, Request request) {
        if (view == null) {
            HwLog.w("ContactPhotoManager", "loadPhotoByIdOrUri imageview is null");
        } else if (loadCachedPhoto(view, request, false)) {
            this.mPendingRequests.remove(view);
        } else {
            this.mPendingRequests.put(view, request);
            if (!this.mPaused) {
                requestLoading();
            }
        }
    }

    public void refreshCache() {
        if (this.mBitmapHolderCacheAllUnfresh) {
            if (DEBUG) {
                HwLog.d("ContactPhotoManager", "refreshCache -- no fresh entries.");
            }
            return;
        }
        if (DEBUG) {
            HwLog.d("ContactPhotoManager", "refreshCache");
        }
        this.mBitmapHolderCacheAllUnfresh = true;
        for (BitmapHolder holder : this.mBitmapHolderCache.snapshot().values()) {
            holder.fresh = false;
        }
    }

    private boolean loadCachedPhoto(ImageView view, Request request, boolean fadeIn) {
        BitmapHolder holder = (BitmapHolder) this.mBitmapHolderCache.get(request.getKey());
        boolean lLoadRoundedEdgeBitmap = ContactPhotoManager.isRoundedEdgeBitMapRequired(request.getFlags());
        if (holder == null) {
            request.applyDefaultImage(view);
            return false;
        } else if (holder.bytes == null) {
            request.applyDefaultImage(view);
            return holder.fresh;
        } else {
            Bitmap bitmap = holder.bitmapRef == null ? null : (Bitmap) holder.bitmapRef.get();
            if (lLoadRoundedEdgeBitmap) {
                bitmap = holder.mRoundedEdgeBitmapRef == null ? null : (Bitmap) holder.mRoundedEdgeBitmapRef.get();
            }
            if (bitmap == null) {
                if (holder.bytes.length < 8192) {
                    inflateBitmap(view, holder, request);
                    bitmap = holder.bitmap;
                    if (lLoadRoundedEdgeBitmap) {
                        bitmap = holder.mRoundedEdgeBitmapRef == null ? null : (Bitmap) holder.mRoundedEdgeBitmapRef.get();
                    }
                    if (bitmap == null) {
                        return false;
                    }
                }
                request.applyDefaultImage(view);
                return false;
            }
            Drawable previousDrawable = view.getDrawable();
            boolean lIsPrivacyIndicatorRequired = ContactPhotoManager.isPrivacyIndicatorRequired(request.mFlags);
            int[] lWidhtAndHeight;
            if (fadeIn && previousDrawable != null) {
                Drawable[] layers = new Drawable[2];
                if (previousDrawable instanceof TransitionDrawable) {
                    TransitionDrawable previousTransitionDrawable = (TransitionDrawable) previousDrawable;
                    layers[0] = previousTransitionDrawable.getDrawable(previousTransitionDrawable.getNumberOfLayers() - 1);
                } else {
                    layers[0] = previousDrawable;
                }
                if (lIsPrivacyIndicatorRequired) {
                    lWidhtAndHeight = ContactPhotoManager.getWidthAndHeightForOverlayBitmapFromView(view);
                    layers[1] = new BitmapDrawable(this.mContext.getResources(), ContactPhotoManager.getOverlayBitmapForPrivateContact(bitmap, lWidhtAndHeight[0], lWidhtAndHeight[1]));
                } else {
                    layers[1] = new BitmapDrawable(this.mContext.getResources(), bitmap);
                }
                TransitionDrawable drawable = new TransitionDrawable(layers);
                view.setImageDrawable(drawable);
                drawable.startTransition(100);
            } else if (lIsPrivacyIndicatorRequired) {
                lWidhtAndHeight = ContactPhotoManager.getWidthAndHeightForOverlayBitmapFromView(view);
                ContactPhotoManager.setImageViewRoundPhoto(view, this.mContext.getResources(), ContactPhotoManager.getOverlayBitmapForPrivateContact(bitmap, lWidhtAndHeight[0], lWidhtAndHeight[1]));
            } else {
                view.setImageBitmap(bitmap);
            }
            if (!lLoadRoundedEdgeBitmap && bitmap.getByteCount() < this.mBitmapCache.maxSize() / 6) {
                this.mBitmapCache.put(request.getKey(), bitmap);
            }
            holder.bitmap = null;
            return holder.fresh;
        }
    }

    static void populateRoundedEdgeBitmap(BitmapHolder aHolder, Bitmap aBitmap) {
        Reference reference = null;
        Bitmap lRoundedEdgeBitMap = aBitmap;
        if (aBitmap != null) {
            reference = new SoftReference(aBitmap);
        }
        try {
            aHolder.mRoundedEdgeBitmapRef = reference;
        } catch (OutOfMemoryError e) {
            HwLog.e("ContactPhotoManager", "populateRoundedEdgeBitmap OutOfMemoryError!!!");
            e.printStackTrace();
        }
    }

    private static void inflateBitmap(ImageView imageView, BitmapHolder holder, Request request) {
        int lRequestedExtent = -1;
        int lFlags = 0;
        if (request != null) {
            lRequestedExtent = request.getRequestedExtent();
            lFlags = request.getFlags();
        }
        boolean lRoundedEdgeBitmapRequired = ContactPhotoManager.isRoundedEdgeBitMapRequired(lFlags);
        boolean lScaleRequired = ContactPhotoManager.isScaleToImageViewSize(lFlags);
        int sampleSize = BitmapUtil.findOptimalSampleSize(holder.originalSmallerExtent, lRequestedExtent);
        byte[] bytes = holder.bytes;
        if (bytes != null && bytes.length != 0) {
            Bitmap bitmap;
            if (sampleSize == holder.decodedSampleSize && holder.bitmapRef != null) {
                holder.bitmap = (Bitmap) holder.bitmapRef.get();
                if (holder.bitmap != null) {
                    if (lRoundedEdgeBitmapRequired && (holder.mRoundedEdgeBitmapRef == null || holder.mRoundedEdgeBitmapRef.get() == null)) {
                        populateRoundedEdgeBitmap(holder, holder.bitmap);
                    }
                    return;
                }
            }
            if (!lScaleRequired || imageView == null) {
                bitmap = BitmapUtil.decodeBitmapFromBytes(bytes, sampleSize);
            } else {
                try {
                    bitmap = BitmapUtil.decodeBitmapFromBytes(bytes, imageView.getWidth(), imageView.getHeight());
                } catch (OutOfMemoryError e) {
                    HwLog.e("ContactPhotoManager", "OutOfMemoryError the photo will appear to be missing");
                }
            }
            bitmap = ContactPhotoManager.createRoundPhoto(bitmap);
            holder.decodedSampleSize = sampleSize;
            holder.bitmap = bitmap;
            if (lRoundedEdgeBitmapRequired) {
                populateRoundedEdgeBitmap(holder, bitmap);
            }
            holder.bitmapRef = new SoftReference(bitmap);
            if (DEBUG) {
                HwLog.d("ContactPhotoManager", "inflateBitmap " + btk(bytes.length) + " -> " + bitmap.getWidth() + "x" + bitmap.getHeight() + ", " + btk(bitmap.getByteCount()));
            }
        }
    }

    public void clear() {
        if (DEBUG) {
            HwLog.d("ContactPhotoManager", "clear");
        }
        this.mPendingRequests.clear();
        this.mBitmapHolderCache.evictAll();
        this.mBitmapCache.evictAll();
    }

    public void pause() {
        this.mPaused = true;
    }

    public void resume() {
        this.mPaused = false;
        if (DEBUG) {
            dumpStats();
        }
        if (!this.mPendingRequests.isEmpty()) {
            requestLoading();
        }
    }

    private void requestLoading() {
        if (!this.mLoadingRequested) {
            this.mLoadingRequested = true;
            this.mMainThreadHandler.sendEmptyMessage(1);
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                this.mLoadingRequested = false;
                if (!this.mPaused) {
                    ensureLoaderThread();
                    this.mLoaderThread.requestLoading();
                }
                return true;
            case 2:
                if (!this.mPaused) {
                    processLoadedImages();
                }
                if (DEBUG) {
                    dumpStats();
                }
                return true;
            default:
                return false;
        }
    }

    public void ensureLoaderThread() {
        if (this.mLoaderThread == null) {
            this.mLoaderThread = new LoaderThread(this.mContext.getContentResolver());
            this.mLoaderThread.start();
        }
    }

    private void processLoadedImages() {
        Iterator<ImageView> iterator = this.mPendingRequests.keySet().iterator();
        while (iterator.hasNext()) {
            ImageView view = (ImageView) iterator.next();
            Request key = (Request) this.mPendingRequests.get(view);
            if (key != null && loadCachedPhoto(view, key, true)) {
                iterator.remove();
            }
        }
        softenCache();
        if (!this.mPendingRequests.isEmpty()) {
            requestLoading();
        }
    }

    private void softenCache() {
        for (BitmapHolder holder : this.mBitmapHolderCache.snapshot().values()) {
            holder.bitmap = null;
        }
    }

    private void cacheBitmap(Object key, byte[] bytes, boolean preloading, Request request) {
        if (DEBUG) {
            BitmapHolder prev = (BitmapHolder) this.mBitmapHolderCache.get(key);
            if (!(prev == null || prev.bytes == null)) {
                HwLog.d("ContactPhotoManager", "Overwriting cache: key=" + key + (prev.fresh ? " FRESH" : " stale"));
                if (prev.fresh) {
                    this.mFreshCacheOverwrite.incrementAndGet();
                } else {
                    this.mStaleCacheOverwrite.incrementAndGet();
                }
            }
            HwLog.d("ContactPhotoManager", "Caching data: key=" + key + ", " + (bytes == null ? "<null>" : btk(bytes.length)));
        }
        BitmapHolder holder = new BitmapHolder(bytes, bytes == null ? -1 : BitmapUtil.getSmallerExtentFromBytes(bytes));
        if (!preloading) {
            inflateBitmap(null, holder, request);
        }
        this.mBitmapHolderCache.put(key, holder);
        this.mBitmapHolderCacheAllUnfresh = false;
    }

    private void obtainPhotoIdsAndUrisToLoad(HashMap<Long, Request> photoIds, Set<String> photoIdsAsStrings, Set<Request> uris) {
        photoIds.clear();
        photoIdsAsStrings.clear();
        uris.clear();
        boolean jpegsDecoded = false;
        for (Request request : this.mPendingRequests.values()) {
            BitmapHolder holder = (BitmapHolder) this.mBitmapHolderCache.get(request.getKey());
            if (holder != null && holder.bytes != null && holder.fresh && (holder.bitmapRef == null || holder.bitmapRef.get() == null || (ContactPhotoManager.isRoundedEdgeBitMapRequired(request.getFlags()) && (holder.mRoundedEdgeBitmapRef == null || holder.mRoundedEdgeBitmapRef.get() == null)))) {
                inflateBitmap(null, holder, request);
                jpegsDecoded = true;
            } else if (holder == null || !holder.fresh) {
                if (request.isUriRequest()) {
                    uris.add(request);
                } else {
                    photoIds.put(Long.valueOf(request.getId()), request);
                    photoIdsAsStrings.add(String.valueOf(request.mId));
                }
            }
        }
        if (jpegsDecoded) {
            this.mMainThreadHandler.sendEmptyMessage(2);
        }
    }
}
