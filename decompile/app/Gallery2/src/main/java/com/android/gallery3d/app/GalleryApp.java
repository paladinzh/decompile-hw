package com.android.gallery3d.app;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Looper;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DownloadCache;
import com.android.gallery3d.data.ImageCacheService;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryData;
import com.android.gallery3d.util.ThreadPool;

public interface GalleryApp {
    Context getAndroidContext();

    Object getAppComponent(Class<?> cls);

    ContentResolver getContentResolver();

    ContextedUtils getContextedUtils();

    DataManager getDataManager();

    DownloadCache getDownloadCache();

    GalleryData getGalleryData();

    ImageCacheService getImageCacheService();

    Looper getMainLooper();

    Resources getResources();

    StitchingProgressManager getStitchingProgressManager();

    ThreadPool getThreadPool();
}
