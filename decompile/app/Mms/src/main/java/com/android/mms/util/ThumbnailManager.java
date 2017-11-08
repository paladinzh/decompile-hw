package com.android.mms.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.TempFileProvider;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.UriImage;
import com.android.mms.util.ImageCacheService.ImageData;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.ResEx;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

public class ThumbnailManager extends BackgroundLoaderManager {
    private final Context mContext;
    private ImageCacheService mImageCacheService;
    private final SimpleCache<Uri, Bitmap> mThumbnailCache = new SimpleCache(8, 16, 0.75f, true);

    public static class ImageLoaded {
        public final Bitmap mBitmap;
        public final boolean mIsVideo;

        public ImageLoaded(Bitmap bitmap, boolean isVideo) {
            this.mBitmap = bitmap;
            this.mIsVideo = isVideo;
        }
    }

    public class ThumbnailTask implements Runnable {
        private final boolean mIsVideo;
        private final Uri mUri;
        private int maxHeight;
        private int maxWidth;

        public ThumbnailTask(Uri uri, boolean isVideo) {
            if (uri == null) {
                throw new NullPointerException();
            }
            this.mUri = uri;
            this.mIsVideo = isVideo;
        }

        public void run() {
            Bitmap bitmap = null;
            try {
                bitmap = getBitmap(this.mIsVideo, this.maxHeight, this.maxWidth);
            } catch (IllegalArgumentException e) {
                MLog.e("ThumbnailManager", "Couldn't load bitmap for Uri", (Throwable) e);
            } catch (OutOfMemoryError e2) {
                MLog.e("ThumbnailManager", "Couldn't load bitmap for Uri", (Throwable) e2);
            } catch (NullPointerException e3) {
                MLog.e("ThumbnailManager", "Couldn't load bitmap for Uri", (Throwable) e3);
            }
            final Bitmap resultBitmap = bitmap;
            ThumbnailManager.this.mCallbackHandler.post(new Runnable() {
                public void run() {
                    Set<ItemLoadedCallback> callbacks = (Set) ThumbnailManager.this.mCallbacks.get(ThumbnailTask.this.mUri);
                    if (callbacks != null) {
                        Bitmap bitmap = resultBitmap == null ? ThumbnailTask.this.mIsVideo ? ResEx.self().getEmptyVedio() : ResEx.self().getEmptyImage() : resultBitmap;
                        for (ItemLoadedCallback<ImageLoaded> callback : BackgroundLoaderManager.asList(callbacks)) {
                            if (MLog.isLoggable("Mms_thumbnailcache", 3)) {
                                MLog.d("ThumbnailManager", "Invoking item loaded callback " + callback);
                            }
                            callback.onItemLoaded(new ImageLoaded(bitmap, ThumbnailTask.this.mIsVideo), null);
                        }
                    } else if (MLog.isLoggable("ThumbnailManager", 3)) {
                        MLog.d("ThumbnailManager", "No image callback!");
                    }
                    if (resultBitmap != null) {
                        ThumbnailManager.this.mThumbnailCache.put(ThumbnailTask.this.mUri, resultBitmap);
                        if (MLog.isLoggable("Mms_thumbnailcache", 3)) {
                            MLog.v("ThumbnailManager", "in callback runnable: bitmap uri:  width: " + resultBitmap.getWidth() + " height: " + resultBitmap.getHeight() + " size: " + resultBitmap.getByteCount());
                        }
                    }
                    ThumbnailManager.this.mCallbacks.remove(ThumbnailTask.this.mUri);
                    ThumbnailManager.this.mPendingTaskUris.remove(ThumbnailTask.this.mUri);
                    if (MLog.isLoggable("Mms_thumbnailcache", 3)) {
                        MLog.d("ThumbnailManager", "Image task for Uri exiting " + ThumbnailManager.this.mPendingTaskUris.size() + " remain");
                    }
                }
            });
        }

        private Bitmap getBitmap(boolean isVideo, int maxHeight, int maxWidth) {
            ImageCacheService cacheService = ThumbnailManager.this.getImageCacheService();
            String path = new UriImage(ThumbnailManager.this.mContext, this.mUri).getFullpath();
            if (path == null) {
                return null;
            }
            boolean isTempFile = TempFileProvider.isTempFile(path);
            ImageData data = null;
            boolean hasCached = ThumbnailManager.this.mThumbnailCache.get(this.mUri) != null;
            if (!isTempFile && hasCached) {
                data = cacheService.getImageData(path, 1);
            }
            Bitmap bitmap;
            if (data != null) {
                Options options = new Options();
                options.inPreferredConfig = Config.ARGB_8888;
                bitmap = requestDecode(data.mData, data.mOffset, data.mData.length - data.mOffset, options);
                if (bitmap == null) {
                    MLog.w("ThumbnailManager", "decode cached failed ");
                }
                return bitmap;
            }
            if (isVideo) {
                bitmap = getVideoBitmap();
            } else {
                bitmap = onDecodeOriginal(this.mUri, 1);
            }
            if (bitmap == null) {
                MLog.w("ThumbnailManager", "decode orig failed ");
                return null;
            }
            bitmap = getCutBitmapNew(resizeDownBySideLength(bitmap, 640, true), maxHeight, maxWidth);
            if (bitmap == null) {
                MLog.e("ThumbnailManager", "resizeDownBySideLength return null, resize bitmap failed");
                return null;
            }
            if (!isTempFile) {
                cacheService.putImageData(path, 1, compressBitmap(bitmap));
            }
            return bitmap;
        }

        private Bitmap getCutBitmapNew(Bitmap compressBitmap, int maxHeight, int maxWidth) {
            int width = compressBitmap.getWidth();
            int height = compressBitmap.getHeight();
            if (width <= 0 || height <= 0) {
                return compressBitmap;
            }
            int[] widthAndHeight = MessageUtils.getImgWidthAndHeightLimit(width, height, maxWidth, maxHeight, ThumbnailManager.this.mContext);
            int targetWidth = widthAndHeight[0];
            int targetHeight = widthAndHeight[1];
            float scale = Math.max(((float) targetWidth) / ((float) width), ((float) targetHeight) / ((float) height));
            Bitmap bitmap = null;
            try {
                bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.translate(((float) targetWidth) / 2.0f, ((float) targetHeight) / 2.0f);
                canvas.rotate(0.0f);
                canvas.scale(scale, scale);
                Bitmap bitmap2 = compressBitmap;
                canvas.drawBitmap(bitmap2, ((float) (-width)) / 2.0f, ((float) (-height)) / 2.0f, new Paint(6));
            } catch (Throwable thow) {
                MLog.i("ThumbnailManager", " compress getCutBitmapNew cutBitmap err!!!");
                thow.printStackTrace();
            }
            compressBitmap.recycle();
            return bitmap;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private Bitmap getVideoBitmap() {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(ThumbnailManager.this.mContext, this.mUri);
                Bitmap frameAtTime = retriever.getFrameAtTime(-1);
                try {
                    retriever.release();
                } catch (RuntimeException e) {
                }
                return frameAtTime;
            } catch (RuntimeException e2) {
                return null;
            } catch (Throwable th) {
                try {
                    retriever.release();
                } catch (RuntimeException e3) {
                }
            }
        }

        private byte[] compressBitmap(Bitmap bitmap) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, 90, os);
            return os.toByteArray();
        }

        private Bitmap requestDecode(byte[] bytes, int offset, int length, Options options) {
            if (options == null) {
                options = new Options();
            }
            Bitmap bitmap = null;
            try {
                bitmap = ensureGLCompatibleBitmap(BitmapFactory.decodeByteArray(bytes, offset, length, options));
            } catch (Exception ex) {
                MLog.e("ThumbnailManager", "decodeByteArray has exception : " + ex);
            } catch (Error e) {
                MLog.e("ThumbnailManager", "decodeByteArray has error : " + e);
            }
            return bitmap;
        }

        private Bitmap resizeDownBySideLength(Bitmap bitmap, int maxLength, boolean recycle) {
            float scale = Math.min(((float) maxLength) / ((float) bitmap.getWidth()), ((float) maxLength) / ((float) bitmap.getHeight()));
            if (scale >= ContentUtil.FONT_SIZE_NORMAL) {
                return bitmap;
            }
            return resizeBitmapByScale(bitmap, scale, recycle);
        }

        private Bitmap resizeBitmapByScale(Bitmap bitmap, float scale, boolean recycle) {
            int width = Math.round(((float) bitmap.getWidth()) * scale);
            int height = Math.round(((float) bitmap.getHeight()) * scale);
            if (width == bitmap.getWidth() && height == bitmap.getHeight()) {
                return bitmap;
            }
            Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
            if (target == null) {
                MLog.e("ThumbnailManager", "resizeBitmapByScale return null in createBitmap");
                return null;
            }
            Canvas canvas = new Canvas(target);
            canvas.scale(scale, scale);
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, new Paint(6));
            if (recycle) {
                bitmap.recycle();
            }
            return target;
        }

        private Config getConfig(Bitmap bitmap) {
            Config config = bitmap.getConfig();
            if (config == null) {
                return Config.ARGB_8888;
            }
            return config;
        }

        private Bitmap ensureGLCompatibleBitmap(Bitmap bitmap) {
            if (bitmap == null || bitmap.getConfig() != null) {
                return bitmap;
            }
            Bitmap newBitmap = bitmap.copy(Config.ARGB_8888, false);
            bitmap.recycle();
            return newBitmap;
        }

        private Bitmap onDecodeOriginal(Uri uri, int type) {
            Options options = new Options();
            options.inPreferredConfig = Config.ARGB_8888;
            return requestDecode(uri, options, 640);
        }

        private void closeSilently(Closeable c) {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException e) {
                    MLog.w("ThumbnailManager", "close fail", e);
                }
            }
        }

        private Bitmap requestDecode(Uri uri, Options options, int targetSize) {
            if (options == null) {
                options = new Options();
            }
            try {
                InputStream inputStream = ThumbnailManager.this.mContext.getContentResolver().openInputStream(uri);
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                closeSilently(inputStream);
                try {
                    inputStream = ThumbnailManager.this.mContext.getContentResolver().openInputStream(uri);
                    options.inSampleSize = computeSampleSizeLarger(options.outWidth, options.outHeight, targetSize);
                    options.inJustDecodeBounds = false;
                    Bitmap result = BitmapFactory.decodeStream(inputStream, null, options);
                    closeSilently(inputStream);
                    if (result == null) {
                        return null;
                    }
                    result = ensureGLCompatibleBitmap(resizeDownIfTooBig(result, targetSize, true));
                    int orientation = 0;
                    MLog.i("ThumbnailManager", "ThumbnailManager uri.toString() " + uri.toString());
                    if (uri.toString().startsWith("file") || !isFileDir(uri.toString())) {
                        MLog.i("ThumbnailManager", "ThumbnailManager uri.toString().startsWith(file)" + uri.toString().startsWith("file"));
                        orientation = UriImage.getOrientation(ThumbnailManager.this.mContext, uri);
                    }
                    if (!(result == null || orientation == 0)) {
                        result = UriImage.rotateBitmap(result, orientation);
                    }
                    return result;
                } catch (FileNotFoundException e) {
                    MLog.e("ThumbnailManager", "Can't open uri: ", (Throwable) e);
                    return null;
                } catch (NullPointerException e2) {
                    MLog.e("ThumbnailManager", "NullPointerException ", (Throwable) e2);
                    return null;
                } catch (Exception e3) {
                    MLog.e("ThumbnailManager", "Exception ", (Throwable) e3);
                    return null;
                }
            } catch (FileNotFoundException e4) {
                MLog.e("ThumbnailManager", "Can't open uri ", (Throwable) e4);
                return null;
            } catch (NullPointerException e22) {
                MLog.e("ThumbnailManager", "NullPointerException ", (Throwable) e22);
                return null;
            } catch (Exception e32) {
                MLog.e("ThumbnailManager", "Exception ", (Throwable) e32);
                return null;
            }
        }

        private boolean isFileDir(String file) {
            if (TextUtils.isEmpty(file)) {
                return false;
            }
            String fileLowCase = file.toLowerCase(Locale.getDefault());
            if (fileLowCase.endsWith(".png") || fileLowCase.endsWith(".jpg") || fileLowCase.endsWith(".jpeg") || fileLowCase.endsWith(".gif")) {
                return true;
            }
            return false;
        }

        private int computeSampleSizeLarger(int w, int h, int minSideLength) {
            int initialSize = Math.max(w / minSideLength, h / minSideLength);
            if (initialSize <= 1) {
                return 1;
            }
            int prevPowerOf2;
            if (initialSize <= 8) {
                prevPowerOf2 = prevPowerOf2(initialSize);
            } else {
                prevPowerOf2 = (initialSize / 8) * 8;
            }
            return prevPowerOf2;
        }

        private int prevPowerOf2(int n) {
            if (n > 0) {
                return Integer.highestOneBit(n);
            }
            throw new IllegalArgumentException();
        }

        private Bitmap resizeDownIfTooBig(Bitmap bitmap, int targetSize, boolean recycle) {
            float scale = Math.max(((float) targetSize) / ((float) bitmap.getWidth()), ((float) targetSize) / ((float) bitmap.getHeight()));
            if (scale > 0.5f) {
                return bitmap;
            }
            return resizeBitmapByScale(bitmap, scale, recycle);
        }

        public void setMaxHeight(int maxheight) {
            this.maxHeight = maxheight;
        }

        public void setMaxWidth(int maxwidth) {
            this.maxWidth = maxwidth;
        }
    }

    public ThumbnailManager(Context context, Handler handler) {
        super(context, handler);
        this.mContext = context;
    }

    public ItemLoadedFuture getThumbnail(Uri uri, ItemLoadedCallback<ImageLoaded> callback) {
        return getThumbnail(uri, false, callback);
    }

    public ItemLoadedFuture getThumbnail(Uri uri, ItemLoadedCallback<ImageLoaded> callback, int maxHeight, int maxWidth) {
        return getThumbnail(uri, false, callback, maxHeight, maxWidth);
    }

    public ItemLoadedFuture getVideoThumbnail(Uri uri, ItemLoadedCallback<ImageLoaded> callback) {
        return getThumbnail(uri, true, callback);
    }

    public ItemLoadedFuture getVideoThumbnail(Uri uri, ItemLoadedCallback<ImageLoaded> callback, int maxHeight, int maxWidth) {
        return getThumbnail(uri, true, callback, maxHeight, maxWidth);
    }

    private ItemLoadedFuture getThumbnail(Uri uri, boolean isVideo, ItemLoadedCallback<ImageLoaded> callback) {
        return getThumbnail(uri, isVideo, callback, -1, -1);
    }

    private ItemLoadedFuture getThumbnail(Uri uri, boolean isVideo, final ItemLoadedCallback<ImageLoaded> callback, int maxHeight, int maxWidth) {
        if (uri == null) {
            throw new NullPointerException();
        }
        Bitmap thumbnail = (Bitmap) this.mThumbnailCache.get(uri);
        boolean thumbnailExists = thumbnail != null;
        boolean taskExists = this.mPendingTaskUris.contains(uri);
        boolean newTaskRequired = (thumbnailExists || taskExists) ? false : true;
        boolean callbackRequired = callback != null;
        if (MLog.isLoggable("Mms_thumbnailcache", 3)) {
            MLog.v("ThumbnailManager", "getThumbnail mThumbnailCache.get for  callback: " + callback + " thumbnailExists: " + thumbnailExists + " taskExists: " + taskExists + " newTaskRequired: " + newTaskRequired + " callbackRequired: " + callbackRequired);
        }
        if (thumbnailExists) {
            if (callbackRequired) {
                callback.onItemLoaded(new ImageLoaded(thumbnail, isVideo), null);
            }
            return new NullItemLoadedFuture();
        }
        if (callbackRequired) {
            addCallback(uri, callback);
        }
        if (newTaskRequired) {
            this.mPendingTaskUris.add(uri);
            ThumbnailTask task = new ThumbnailTask(uri, isVideo);
            task.setMaxHeight(maxHeight);
            task.setMaxWidth(maxWidth);
            this.mExecutor.execute(task);
        }
        return new ItemLoadedFuture() {
            private boolean mIsDone;

            public void cancel(Uri uri) {
                ThumbnailManager.this.cancelCallback(callback);
                ThumbnailManager.this.removeThumbnail(uri);
            }

            public void setIsDone(boolean done) {
                this.mIsDone = done;
            }

            public boolean isDone() {
                return this.mIsDone;
            }
        };
    }

    public synchronized void clear() {
        super.clear();
        for (Uri uriKey : this.mThumbnailCache.keySet()) {
            removeThumbnail(uriKey);
        }
        this.mThumbnailCache.clear();
        clearBackingStore();
    }

    public synchronized void clearBackingStore() {
        if (this.mImageCacheService == null) {
            CacheManager.clear(this.mContext);
        } else {
            getImageCacheService().clear();
            this.mImageCacheService = null;
        }
    }

    public void removeThumbnail(Uri uri) {
        if (MLog.isLoggable("ThumbnailManager", 3)) {
            MLog.d("ThumbnailManager", "removeThumbnail.");
        }
        if (uri != null) {
            this.mThumbnailCache.remove(uri);
            Bitmap bitmap = (Bitmap) this.mThumbnailCache.get(uri);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    public String getTag() {
        return "ThumbnailManager";
    }

    private synchronized ImageCacheService getImageCacheService() {
        if (this.mImageCacheService == null) {
            this.mImageCacheService = new ImageCacheService(this.mContext);
        }
        return this.mImageCacheService;
    }
}
