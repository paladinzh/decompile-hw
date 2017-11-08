package com.common.imageloader.core;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.view.PointerIconCompat;
import com.common.imageloader.core.DisplayImageOptions.Builder;
import com.common.imageloader.core.assist.FailReason;
import com.common.imageloader.core.assist.FailReason.FailType;
import com.common.imageloader.core.assist.ImageScaleType;
import com.common.imageloader.core.assist.ImageSize;
import com.common.imageloader.core.assist.LoadedFrom;
import com.common.imageloader.core.assist.ViewScaleType;
import com.common.imageloader.core.decode.ImageDecoder;
import com.common.imageloader.core.decode.ImageDecodingInfo;
import com.common.imageloader.core.download.ImageDownloader;
import com.common.imageloader.core.download.ImageDownloader.Scheme;
import com.common.imageloader.core.imageaware.ImageAware;
import com.common.imageloader.core.listener.ImageLoadingListener;
import com.common.imageloader.core.listener.ImageLoadingProgressListener;
import com.common.imageloader.utils.IoUtils;
import com.common.imageloader.utils.IoUtils.CopyListener;
import com.common.imageloader.utils.L;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.power.util.SpecialAppUid;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

final class LoadAndDisplayImageTask implements Runnable, CopyListener {
    private static final String ERROR_NO_IMAGE_STREAM = "No stream for image [%s]";
    private static final String ERROR_POST_PROCESSOR_NULL = "Post-processor returned null [%s]";
    private static final String ERROR_PRE_PROCESSOR_NULL = "Pre-processor returned null [%s]";
    private static final String ERROR_PROCESSOR_FOR_DISK_CACHE_NULL = "Bitmap processor for disk cache returned null [%s]";
    private static final String LOG_CACHE_IMAGE_IN_MEMORY = "Cache image in memory [%s]";
    private static final String LOG_CACHE_IMAGE_ON_DISK = "Cache image on disk [%s]";
    private static final String LOG_DELAY_BEFORE_LOADING = "Delay %d ms before loading...  [%s]";
    private static final String LOG_GET_IMAGE_FROM_MEMORY_CACHE_AFTER_WAITING = "...Get cached bitmap from memory after waiting. [%s]";
    private static final String LOG_LOAD_IMAGE_FROM_DISK_CACHE = "Load image from disk cache [%s]";
    private static final String LOG_LOAD_IMAGE_FROM_NETWORK = "Load image from network [%s]";
    private static final String LOG_POSTPROCESS_IMAGE = "PostProcess image before displaying [%s]";
    private static final String LOG_PREPROCESS_IMAGE = "PreProcess image before caching in memory [%s]";
    private static final String LOG_PROCESS_IMAGE_BEFORE_CACHE_ON_DISK = "Process image before cache on disk [%s]";
    private static final String LOG_RESIZE_CACHED_IMAGE_FILE = "Resize image in disk cache [%s]";
    private static final String LOG_RESUME_AFTER_PAUSE = ".. Resume loading [%s]";
    private static final String LOG_START_DISPLAY_IMAGE_TASK = "Start display image task [%s]";
    private static final String LOG_TASK_CANCELLED_IMAGEAWARE_COLLECTED = "ImageAware was collected by GC. Task is cancelled. [%s]";
    private static final String LOG_TASK_CANCELLED_IMAGEAWARE_REUSED = "ImageAware is reused for another image. Task is cancelled. [%s]";
    private static final String LOG_TASK_INTERRUPTED = "Task was interrupted [%s]";
    private static final String LOG_WAITING_FOR_IMAGE_LOADED = "Image already is loading. Waiting... [%s]";
    private static final String LOG_WAITING_FOR_RESUME = "ImageLoader is paused. Waiting...  [%s]";
    private final ImageLoaderConfiguration configuration;
    private final ImageDecoder decoder;
    private final ImageDownloader downloader;
    private final ImageLoaderEngine engine;
    private final Handler handler;
    final ImageAware imageAware;
    private final ImageLoadingInfo imageLoadingInfo;
    final ImageLoadingListener listener;
    private LoadedFrom loadedFrom = LoadedFrom.NETWORK;
    private Context mAppContext = null;
    private PackageManager mPm;
    private final String memoryCacheKey;
    private final ImageDownloader networkDeniedDownloader;
    final DisplayImageOptions options;
    final ImageLoadingProgressListener progressListener;
    private final ImageDownloader slowNetworkDownloader;
    private final boolean syncLoading;
    private final ImageSize targetSize;
    final String uri;

    static class TaskCancelledException extends Exception {
        TaskCancelledException() {
        }
    }

    public LoadAndDisplayImageTask(ImageLoaderEngine engine, ImageLoadingInfo imageLoadingInfo, Handler handler) {
        this.engine = engine;
        this.imageLoadingInfo = imageLoadingInfo;
        this.handler = handler;
        this.configuration = engine.configuration;
        this.downloader = this.configuration.downloader;
        this.networkDeniedDownloader = this.configuration.networkDeniedDownloader;
        this.slowNetworkDownloader = this.configuration.slowNetworkDownloader;
        if (imageLoadingInfo.options.isUseApkDecoder()) {
            this.decoder = this.configuration.apkDecoder;
        } else {
            this.decoder = this.configuration.decoder;
        }
        this.uri = imageLoadingInfo.uri;
        this.memoryCacheKey = imageLoadingInfo.memoryCacheKey;
        this.imageAware = imageLoadingInfo.imageAware;
        this.targetSize = imageLoadingInfo.targetSize;
        this.options = imageLoadingInfo.options;
        this.listener = imageLoadingInfo.listener;
        this.progressListener = imageLoadingInfo.progressListener;
        this.syncLoading = this.options.isSyncLoading();
    }

    private Drawable getIconFromUid(String uid) {
        Drawable icon;
        int mUid = Integer.parseInt(uid);
        if (this.mAppContext == null) {
            this.mAppContext = getApplicationContext();
        }
        if (mUid == 0) {
            icon = this.mAppContext.getDrawable(R.drawable.ic_robot_battery);
        } else if (PointerIconCompat.TYPE_ALL_SCROLL == mUid) {
            icon = this.mAppContext.getDrawable(R.drawable.ic_multimedia_battery);
        } else if (-100 == mUid) {
            icon = this.mAppContext.getPackageManager().getDefaultActivityIcon();
        } else {
            icon = SysCoreUtils.getExtAppIconByUid(this.mAppContext, mUid);
        }
        int tempUid = SpecialAppUid.collapseUidsTogether(mUid, ActivityManager.getCurrentUser());
        if (!SpecialAppUid.isOtherUserUid(tempUid)) {
            return icon;
        }
        Drawable drawable = SpecialAppUid.getBadgedIconOfSpecialUidApp(icon, Math.abs(tempUid) - 2000);
        if (drawable != null) {
            return drawable;
        }
        return icon;
    }

    private Drawable getIconFromPm(String pkgName) {
        try {
            if (this.mPm == null) {
                this.mPm = getApplicationContext().getPackageManager();
            }
            return this.mPm.getApplicationInfo(pkgName, 8192).loadIcon(this.mPm);
        } catch (NameNotFoundException e) {
            L.w("getIconFromPm", "can't get application info:" + pkgName);
            return null;
        }
    }

    protected Context getApplicationContext() {
        return GlobalContext.getContext();
    }

    public void run() {
        if (!waitIfPaused() && !delayIfNeed()) {
            ReentrantLock loadFromUriLock = this.imageLoadingInfo.loadFromUriLock;
            L.d(LOG_START_DISPLAY_IMAGE_TASK, this.memoryCacheKey);
            if (loadFromUriLock.isLocked()) {
                L.d(LOG_WAITING_FOR_IMAGE_LOADED, this.memoryCacheKey);
            }
            loadFromUriLock.lock();
            try {
                checkTaskNotActual();
                Bitmap bmp = this.configuration.memoryCache.get(this.memoryCacheKey);
                if (bmp == null || bmp.isRecycled()) {
                    bmp = tryLoadBitmap();
                    if (bmp != null) {
                        checkTaskNotActual();
                        checkTaskInterrupted();
                        if (this.options.shouldPreProcess()) {
                            L.d(LOG_PREPROCESS_IMAGE, this.memoryCacheKey);
                            bmp = this.options.getPreProcessor().process(bmp);
                            if (bmp == null) {
                                L.e(ERROR_PRE_PROCESSOR_NULL, this.memoryCacheKey);
                            }
                        }
                        if (bmp != null && this.options.isCacheInMemory()) {
                            L.d(LOG_CACHE_IMAGE_IN_MEMORY, this.memoryCacheKey);
                            this.configuration.memoryCache.put(this.memoryCacheKey, bmp);
                        }
                    } else {
                        return;
                    }
                }
                this.loadedFrom = LoadedFrom.MEMORY_CACHE;
                L.d(LOG_GET_IMAGE_FROM_MEMORY_CACHE_AFTER_WAITING, this.memoryCacheKey);
                if (bmp != null && this.options.shouldPostProcess()) {
                    L.d(LOG_POSTPROCESS_IMAGE, this.memoryCacheKey);
                    bmp = this.options.getPostProcessor().process(bmp);
                    if (bmp == null) {
                        L.e(ERROR_POST_PROCESSOR_NULL, this.memoryCacheKey);
                    }
                }
                checkTaskNotActual();
                checkTaskInterrupted();
                loadFromUriLock.unlock();
                runTask(new DisplayBitmapTask(bmp, this.imageLoadingInfo, this.engine, this.loadedFrom), this.syncLoading, this.handler, this.engine);
            } catch (TaskCancelledException e) {
                fireCancelEvent();
            } finally {
                loadFromUriLock.unlock();
            }
        }
    }

    private boolean waitIfPaused() {
        AtomicBoolean pause = this.engine.getPause();
        if (pause.get()) {
            synchronized (this.engine.getPauseLock()) {
                if (pause.get()) {
                    L.d(LOG_WAITING_FOR_RESUME, this.memoryCacheKey);
                    try {
                        this.engine.getPauseLock().wait();
                        L.d(LOG_RESUME_AFTER_PAUSE, this.memoryCacheKey);
                    } catch (InterruptedException e) {
                        L.e(LOG_TASK_INTERRUPTED, this.memoryCacheKey);
                        return true;
                    }
                }
            }
        }
        return isTaskNotActual();
    }

    private boolean delayIfNeed() {
        if (!this.options.shouldDelayBeforeLoading()) {
            return false;
        }
        L.d(LOG_DELAY_BEFORE_LOADING, Integer.valueOf(this.options.getDelayBeforeLoading()), this.memoryCacheKey);
        try {
            Thread.sleep((long) this.options.getDelayBeforeLoading());
            return isTaskNotActual();
        } catch (InterruptedException e) {
            L.e(LOG_TASK_INTERRUPTED, this.memoryCacheKey);
            return true;
        }
    }

    private Bitmap tryLoadBitmap() throws TaskCancelledException {
        Bitmap bitmap = null;
        try {
            File imageFile = this.configuration.diskCache.get(this.uri);
            if (imageFile != null && imageFile.exists() && imageFile.length() > 0 && this.options.isCacheOnDisk()) {
                L.d(LOG_LOAD_IMAGE_FROM_DISK_CACHE, this.memoryCacheKey);
                this.loadedFrom = LoadedFrom.DISC_CACHE;
                checkTaskNotActual();
                bitmap = decodeImage(Scheme.FILE.wrap(imageFile.getAbsolutePath()));
            }
            if (bitmap != null && bitmap.getWidth() > 0) {
                if (bitmap.getHeight() <= 0) {
                }
                return bitmap;
            }
            L.d(LOG_LOAD_IMAGE_FROM_NETWORK, this.memoryCacheKey);
            this.loadedFrom = LoadedFrom.NETWORK;
            String imageUriForDecoding = this.uri;
            if (this.options.isCacheOnDisk() && tryCacheImageOnDisk()) {
                imageFile = this.configuration.diskCache.get(this.uri);
                if (imageFile != null) {
                    imageUriForDecoding = Scheme.FILE.wrap(imageFile.getAbsolutePath());
                }
            }
            checkTaskNotActual();
            Drawable drawable;
            if (this.options.isLoadDrawableByPkgInfo()) {
                drawable = getIconFromPm(this.uri);
                if (drawable != null) {
                    bitmap = drawableToBitmap(drawable);
                }
            } else if (this.options.isloadDrawableByUid()) {
                drawable = getIconFromUid(this.uri);
                if (drawable != null) {
                    bitmap = drawableToBitmap(drawable);
                }
            } else {
                bitmap = decodeImage(imageUriForDecoding);
            }
            if (bitmap != null && bitmap.getWidth() > 0) {
                if (bitmap.getHeight() <= 0) {
                }
                return bitmap;
            }
            fireFailEvent(FailType.DECODING_ERROR, null);
        } catch (IllegalStateException e) {
            fireFailEvent(FailType.NETWORK_DENIED, null);
        } catch (TaskCancelledException e2) {
            throw e2;
        } catch (IOException e3) {
            L.e(e3);
            fireFailEvent(FailType.IO_ERROR, e3);
        } catch (OutOfMemoryError e4) {
            L.e(e4);
            fireFailEvent(FailType.OUT_OF_MEMORY, e4);
        } catch (Throwable e5) {
            L.e(e5);
            fireFailEvent(FailType.UNKNOWN, e5);
        }
        return bitmap;
    }

    static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Config config;
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (drawable.getOpacity() != -1) {
            config = Config.ARGB_8888;
        } else {
            config = Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    private Bitmap decodeImage(String imageUri) throws IOException {
        String str = imageUri;
        return this.decoder.decode(new ImageDecodingInfo(this.memoryCacheKey, str, this.uri, this.targetSize, this.imageAware.getScaleType(), getDownloader(), this.options));
    }

    private boolean tryCacheImageOnDisk() throws TaskCancelledException {
        L.d(LOG_CACHE_IMAGE_ON_DISK, this.memoryCacheKey);
        try {
            boolean loaded = downloadImage();
            if (!loaded) {
                return loaded;
            }
            int width = this.configuration.maxImageWidthForDiskCache;
            int height = this.configuration.maxImageHeightForDiskCache;
            if (width <= 0 && height <= 0) {
                return loaded;
            }
            L.d(LOG_RESIZE_CACHED_IMAGE_FILE, this.memoryCacheKey);
            resizeAndSaveImage(width, height);
            return loaded;
        } catch (IOException e) {
            L.e(e);
            return false;
        }
    }

    private boolean downloadImage() throws IOException {
        InputStream is = getDownloader().getStream(this.uri, this.options.getExtraForDownloader());
        if (is == null) {
            L.e(ERROR_NO_IMAGE_STREAM, this.memoryCacheKey);
            return false;
        }
        try {
            boolean save = this.configuration.diskCache.save(this.uri, is, this);
            return save;
        } finally {
            IoUtils.closeSilently(is);
        }
    }

    private boolean resizeAndSaveImage(int maxWidth, int maxHeight) throws IOException {
        File targetFile = this.configuration.diskCache.get(this.uri);
        if (targetFile == null || !targetFile.exists()) {
            return false;
        }
        Bitmap bmp = this.decoder.decode(new ImageDecodingInfo(this.memoryCacheKey, Scheme.FILE.wrap(targetFile.getAbsolutePath()), this.uri, new ImageSize(maxWidth, maxHeight), ViewScaleType.FIT_INSIDE, getDownloader(), new Builder().cloneFrom(this.options).imageScaleType(ImageScaleType.IN_SAMPLE_INT).build()));
        if (!(bmp == null || this.configuration.processorForDiskCache == null)) {
            L.d(LOG_PROCESS_IMAGE_BEFORE_CACHE_ON_DISK, this.memoryCacheKey);
            bmp = this.configuration.processorForDiskCache.process(bmp);
            if (bmp == null) {
                L.e(ERROR_PROCESSOR_FOR_DISK_CACHE_NULL, this.memoryCacheKey);
            }
        }
        if (bmp == null) {
            return false;
        }
        boolean saved = this.configuration.diskCache.save(this.uri, bmp);
        bmp.recycle();
        return saved;
    }

    public boolean onBytesCopied(int current, int total) {
        return !this.syncLoading ? fireProgressEvent(current, total) : true;
    }

    private boolean fireProgressEvent(final int current, final int total) {
        if (isTaskInterrupted() || isTaskNotActual()) {
            return false;
        }
        if (this.progressListener != null) {
            runTask(new Runnable() {
                public void run() {
                    LoadAndDisplayImageTask.this.progressListener.onProgressUpdate(LoadAndDisplayImageTask.this.uri, LoadAndDisplayImageTask.this.imageAware.getWrappedView(), current, total);
                }
            }, false, this.handler, this.engine);
        }
        return true;
    }

    private void fireFailEvent(final FailType failType, final Throwable failCause) {
        if (!this.syncLoading && !isTaskInterrupted() && !isTaskNotActual()) {
            runTask(new Runnable() {
                public void run() {
                    if (LoadAndDisplayImageTask.this.options.shouldShowImageOnFail()) {
                        LoadAndDisplayImageTask.this.imageAware.setImageDrawable(LoadAndDisplayImageTask.this.options.getImageOnFail(LoadAndDisplayImageTask.this.configuration.resources));
                    }
                    LoadAndDisplayImageTask.this.listener.onLoadingFailed(LoadAndDisplayImageTask.this.uri, LoadAndDisplayImageTask.this.imageAware.getWrappedView(), new FailReason(failType, failCause));
                }
            }, false, this.handler, this.engine);
        }
    }

    private void fireCancelEvent() {
        if (!this.syncLoading && !isTaskInterrupted()) {
            runTask(new Runnable() {
                public void run() {
                    LoadAndDisplayImageTask.this.listener.onLoadingCancelled(LoadAndDisplayImageTask.this.uri, LoadAndDisplayImageTask.this.imageAware.getWrappedView());
                }
            }, false, this.handler, this.engine);
        }
    }

    private ImageDownloader getDownloader() {
        if (this.engine.isNetworkDenied()) {
            return this.networkDeniedDownloader;
        }
        if (this.engine.isSlowNetwork()) {
            return this.slowNetworkDownloader;
        }
        return this.downloader;
    }

    private void checkTaskNotActual() throws TaskCancelledException {
        checkViewCollected();
        checkViewReused();
    }

    private boolean isTaskNotActual() {
        return !isViewCollected() ? isViewReused() : true;
    }

    private void checkViewCollected() throws TaskCancelledException {
        if (isViewCollected()) {
            throw new TaskCancelledException();
        }
    }

    private boolean isViewCollected() {
        if (!this.imageAware.isCollected()) {
            return false;
        }
        L.d(LOG_TASK_CANCELLED_IMAGEAWARE_COLLECTED, this.memoryCacheKey);
        return true;
    }

    private void checkViewReused() throws TaskCancelledException {
        if (isViewReused()) {
            throw new TaskCancelledException();
        }
    }

    private boolean isViewReused() {
        boolean imageAwareWasReused;
        if (this.memoryCacheKey.equals(this.engine.getLoadingUriForView(this.imageAware))) {
            imageAwareWasReused = false;
        } else {
            imageAwareWasReused = true;
        }
        if (!imageAwareWasReused) {
            return false;
        }
        L.d(LOG_TASK_CANCELLED_IMAGEAWARE_REUSED, this.memoryCacheKey);
        return true;
    }

    private void checkTaskInterrupted() throws TaskCancelledException {
        if (isTaskInterrupted()) {
            throw new TaskCancelledException();
        }
    }

    private boolean isTaskInterrupted() {
        if (!Thread.interrupted()) {
            return false;
        }
        L.d(LOG_TASK_INTERRUPTED, this.memoryCacheKey);
        return true;
    }

    String getLoadingUri() {
        return this.uri;
    }

    static void runTask(Runnable r, boolean sync, Handler handler, ImageLoaderEngine engine) {
        if (sync) {
            r.run();
        } else if (handler == null) {
            engine.fireCallback(r);
        } else {
            handler.post(r);
        }
    }
}
