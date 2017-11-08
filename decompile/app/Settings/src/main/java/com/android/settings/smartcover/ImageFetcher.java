package com.android.settings.smartcover;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import com.android.settings.smartcover.ImageCache.ImageCacheParams;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class ImageFetcher extends ImageResizer {
    private static WeakReference<ImageFetcher> sImageFetcher;
    private File mHttpCacheDir;
    private DiskLruCache mHttpDiskCache;
    private final Object mHttpDiskCacheLock = new Object();

    public static synchronized ImageFetcher getGridImageFetcher(Activity activity) {
        ImageFetcher imageFetcher;
        synchronized (ImageFetcher.class) {
            imageFetcher = null;
            if (sImageFetcher != null) {
                imageFetcher = (ImageFetcher) sImageFetcher.get();
            }
            if (imageFetcher == null && activity != null) {
                imageFetcher = createImageFetcher(activity, activity.getResources().getDimensionPixelSize(2131559142), activity.getResources().getDimensionPixelSize(2131559143), 0.1f, "thumbs", 2130838166);
                sImageFetcher = new WeakReference(imageFetcher);
            }
        }
        return imageFetcher;
    }

    public ImageFetcher(Context context, int imageWidth, int imageHeight) {
        super(context, imageWidth, imageHeight);
        init(context);
    }

    @SuppressLint({"NewApi"})
    public static ImageFetcher createImageFetcher(Activity activity, int width, int height, float percent, String cacheName, int defaultDraw) {
        ImageCacheParams cacheParams = new ImageCacheParams(activity, cacheName);
        cacheParams.setMemCacheSizePercent(percent);
        ImageFetcher imageWorker = new ImageFetcher(activity, width, height);
        if (activity != null) {
            imageWorker.addImageCache(activity.getFragmentManager(), cacheParams);
        }
        imageWorker.setImageFadeIn(false);
        if (defaultDraw != 0) {
            imageWorker.setLoadingImage(defaultDraw);
        }
        return imageWorker;
    }

    private void init(Context context) {
        this.mHttpCacheDir = ImageCache.getDiskCacheDir(context, "http");
    }

    protected void initDiskCacheInternal() {
        super.initDiskCacheInternal();
        initHttpDiskCache();
    }

    private void initHttpDiskCache() {
        if (this.mHttpDiskCacheLock != null && this.mHttpCacheDir != null) {
            if (this.mHttpCacheDir.exists() || this.mHttpCacheDir.mkdirs()) {
                synchronized (this.mHttpDiskCacheLock) {
                    if (ImageCache.getUsableSpace(this.mHttpCacheDir) > 10485760) {
                        try {
                            this.mHttpDiskCache = DiskLruCache.open(this.mHttpCacheDir, 1, 1, 10485760);
                        } catch (IOException e) {
                            this.mHttpDiskCache = null;
                        }
                    }
                    this.mHttpDiskCacheLock.notifyAll();
                }
            }
        }
    }

    protected void clearCacheInternal() {
        super.clearCacheInternal();
        if (this.mHttpDiskCacheLock != null) {
            synchronized (this.mHttpDiskCacheLock) {
                if (!(this.mHttpDiskCache == null || this.mHttpDiskCache.isClosed())) {
                    try {
                        this.mHttpDiskCache.delete();
                    } catch (IOException e) {
                    }
                    this.mHttpDiskCache = null;
                    initHttpDiskCache();
                }
            }
        }
    }

    protected void flushCacheInternal() {
        super.flushCacheInternal();
        if (this.mHttpDiskCacheLock != null) {
            synchronized (this.mHttpDiskCacheLock) {
                if (this.mHttpDiskCache != null) {
                    try {
                        this.mHttpDiskCache.flush();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    protected void closeCacheInternal() {
        super.closeCacheInternal();
        if (this.mHttpDiskCacheLock != null) {
            synchronized (this.mHttpDiskCacheLock) {
                if (this.mHttpDiskCache != null) {
                    try {
                        if (!this.mHttpDiskCache.isClosed()) {
                            this.mHttpDiskCache.close();
                            this.mHttpDiskCache = null;
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    protected Bitmap processBitmap(Object data) {
        return super.processBitmap(data);
    }
}
