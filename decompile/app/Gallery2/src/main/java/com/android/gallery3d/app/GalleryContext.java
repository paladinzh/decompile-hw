package com.android.gallery3d.app;

import android.content.Context;
import android.content.res.Resources;
import android.os.Looper;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.util.ThreadPool;

public interface GalleryContext {
    Context getActivityContext();

    Context getAndroidContext();

    DataManager getDataManager();

    GLRoot getGLRoot();

    GalleryApp getGalleryApplication();

    Looper getMainLooper();

    Resources getResources();

    String getString(int i);

    String getString(int i, Object... objArr);

    ThreadPool getThreadPool();

    boolean isActivityActive();

    boolean isDestroyed();
}
