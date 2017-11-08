package com.android.gallery3d.app;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.os.StrictMode.VmPolicy;
import android.util.Log;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DownloadCache;
import com.android.gallery3d.data.GalleryMediaSetBase;
import com.android.gallery3d.data.ImageCacheService;
import com.android.gallery3d.data.LocalMediaSetAlbum;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.settings.HicloudAccountManager;
import com.android.gallery3d.ui.MenuExecutor;
import com.android.gallery3d.util.ActivityExWrapper;
import com.android.gallery3d.util.BlackList;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.DisplayEngineUtils;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryData;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.LauncherVibrator;
import com.android.gallery3d.util.LightCycleHelper;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.MultiWindowStatusHolder;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.TraceController;
import com.android.gallery3d.util.WhiteList;
import com.fyusion.sdk.common.FyuseSDK;
import com.huawei.gallery.app.TimeBucketItemsDataLoader;
import com.huawei.gallery.app.TimeBucketPage;
import com.huawei.gallery.editor.ui.AspectInfo;
import com.huawei.gallery.extfile.FyuseFile;
import com.huawei.gallery.media.services.StorageService;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.servicemanager.ServiceRegistry;
import com.huawei.gallery.ui.GalleryViewPager;
import com.huawei.gallery.ui.ListSlotRender;
import com.huawei.gallery.ui.ListSlotView;
import com.huawei.gallery.util.CrashHandler;
import com.huawei.gallery.util.DumpHprofThread;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.gallery.util.MediaSyncerHelper;
import com.huawei.gallery.util.TabIndexUtils;
import java.io.File;
import tmsdk.common.module.update.UpdateConfig;

public class GalleryAppImpl extends Application implements GalleryApp {
    private static boolean sIsBootStartup = false;
    private String clientid = "h84GfImnOp9sd44UyIfEwT";
    private String clientsecret = "2OJS2V_kk3f0h82khgwr9ghbrwuibgn4";
    private DataManager mDataManager;
    private DownloadCache mDownloadCache;
    private GalleryData mGalleryData;
    private Handler mHandler;
    private ImageCacheService mImageCacheService;
    private Object mLock = new Object();
    private StitchingProgressManager mStitchingProgressManager;
    private ThreadPool mThreadPool;
    private ContextedUtils mUtils;

    private class FirstInitializeThread extends Thread {
        private FirstInitializeThread() {
        }

        public void run() {
            TraceController.traceBegin("first initialize thread");
            GalleryUtils.initialize(GalleryAppImpl.this);
            if (HicloudAccountManager.PACKAGE_NAME.equals(GalleryUtils.getProcessName())) {
                PhotoShareUtils.initialize(GalleryAppImpl.this.getApplicationContext());
                GalleryLog.d("gallery2", "Initial Photoshare");
            }
            BlackList.getInstance();
            WhiteList.getBucketIdForWhiteList();
            WhiteList.getBucketIdForWhiteListWithoutPreLoadedPath();
            MediaSetUtils.bucketId2ResourceId(0, GalleryAppImpl.this.getApplicationContext());
            GalleryData data = GalleryAppImpl.this.getGalleryData();
            if (data != null) {
                data.queryFavorite(true);
            }
            StorageService.checkStorageSpace();
            ((HicloudAccountManager) GalleryAppImpl.this.getAppComponent(HicloudAccountManager.class)).registerReceiver();
            TraceController.traceEnd();
        }
    }

    private static class FirstPreloadClassThread extends Thread {
        private FirstPreloadClassThread() {
        }

        public void run() {
            TraceController.traceBegin("GalleryAppImpl 1st preload class thread");
            GalleryAppImpl.loadClass(PhotoShareUtils.class.getName());
            GalleryAppImpl.loadClass(MediaSet.class.getName());
            GalleryAppImpl.loadClass(GalleryMediaSetBase.class.getName());
            GalleryAppImpl.loadClass(LocalMediaSetAlbum.class.getName());
            GalleryAppImpl.loadClass(MultiWindowStatusHolder.class.getName());
            GalleryAppImpl.loadClass(ActivityExWrapper.class.getName());
            GalleryAppImpl.loadClass(TimeBucketPage.class.getName());
            GalleryAppImpl.loadClass(TimeBucketItemsDataLoader.class.getName());
            GalleryAppImpl.loadClass(LauncherVibrator.class.getName());
            GalleryAppImpl.loadClass(IntentChooser.class.getName());
            TraceController.traceEnd();
        }
    }

    private class SecondInitializeThread extends Thread {
        private GalleryAppImpl mOwner;

        private SecondInitializeThread() {
            this.mOwner = GalleryAppImpl.this;
        }

        public void run() {
            TraceController.traceBegin("second initialize thread");
            LayoutHelper.init(this.mOwner);
            TabIndexUtils.init(this.mOwner);
            DrmUtils.initialize(this.mOwner);
            AspectInfo.initialize(this.mOwner);
            ReportToBigData.initialize(this.mOwner);
            if (GalleryUtils.IS_DEBUG_GALLERY_HPROF) {
                new DumpHprofThread(GalleryAppImpl.this).start();
            }
            FyuseFile.checkFusePackage(this.mOwner);
            DisplayEngineUtils.init(this.mOwner.getAndroidContext());
            this.mOwner.getDataManager();
            TraceController.endSection();
        }
    }

    private static class SecondPreloadClassThread extends Thread {
        private SecondPreloadClassThread() {
        }

        public void run() {
            TraceController.beginSection("GalleryAppImpl 2nd preload class thread");
            GalleryAppImpl.loadClass(GalleryUtils.class.getName());
            GalleryAppImpl.loadClass(ListSlotView.class.getName());
            GalleryAppImpl.loadClass(MenuExecutor.class.getName());
            GalleryAppImpl.loadClass(ListSlotRender.class.getName());
            GalleryAppImpl.loadClass(GalleryViewPager.class.getName());
            TraceController.endSection();
        }
    }

    public void onCreate() {
        TraceController.beginSection("GalleryAppImpl.onCreate");
        setupExceptionHandler();
        if (Log.isLoggable("GalleryStrictMode", 3)) {
            StrictMode.setThreadPolicy(new Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().build());
        }
        super.onCreate();
        GalleryLog.d("gallery2", " FyuseSDK.init");
        FyuseSDK.initWithUserConsent(getAndroidContext(), this.clientid, this.clientsecret);
        this.mHandler = new Handler();
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                if (GalleryAppImpl.getBootStartupStatus()) {
                    GalleryLog.d("gallery2", "boot startup, don't create media observer");
                } else {
                    MediaSyncerHelper.registerMediaObserver(GalleryAppImpl.this.getAndroidContext());
                }
            }
        }, 350);
        setBootStartupStatus(false);
        initializeAsyncTask();
        this.mStitchingProgressManager = LightCycleHelper.createStitchingManagerInstance(this);
        if (this.mStitchingProgressManager != null) {
            this.mStitchingProgressManager.addChangeListener(getDataManager());
        }
        PhotoShareUtils.setApplicationContext(this);
        startInitializeTask();
        TraceController.endSection();
    }

    public GalleryAppImpl() {
        startPreloadTask();
    }

    private void startPreloadTask() {
        new FirstPreloadClassThread().start();
        new SecondPreloadClassThread().start();
    }

    private void startInitializeTask() {
        new FirstInitializeThread().start();
        new SecondInitializeThread().start();
    }

    private static void loadClass(String clazzName) {
        TraceController.beginSection("loadClass: " + clazzName);
        try {
            Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
        }
        TraceController.endSection();
    }

    public Context getAndroidContext() {
        return this;
    }

    public synchronized DataManager getDataManager() {
        if (this.mDataManager == null) {
            TraceController.beginSection("getDataManager");
            this.mDataManager = new DataManager(this);
            this.mDataManager.initializeSourceMap();
            TraceController.endSection();
        }
        return this.mDataManager;
    }

    public StitchingProgressManager getStitchingProgressManager() {
        return this.mStitchingProgressManager;
    }

    public ImageCacheService getImageCacheService() {
        ImageCacheService imageCacheService;
        synchronized (this.mLock) {
            if (this.mImageCacheService == null) {
                this.mImageCacheService = new ImageCacheService(getAndroidContext());
            }
            imageCacheService = this.mImageCacheService;
        }
        return imageCacheService;
    }

    public synchronized ThreadPool getThreadPool() {
        if (this.mThreadPool == null) {
            this.mThreadPool = new ThreadPool();
        }
        return this.mThreadPool;
    }

    public synchronized DownloadCache getDownloadCache() {
        if (this.mDownloadCache == null) {
            File cacheDir = new File(GalleryUtils.ensureExternalCacheDir(getApplicationContext()), "download");
            if (!cacheDir.isDirectory()) {
                cacheDir.mkdirs();
            }
            if (cacheDir.isDirectory()) {
                this.mDownloadCache = new DownloadCache(this, cacheDir, UpdateConfig.UPDATE_FLAG_APP_LIST);
            } else {
                throw new RuntimeException("fail to create: " + cacheDir.getAbsolutePath());
            }
        }
        return this.mDownloadCache;
    }

    private void initializeAsyncTask() {
        try {
            Class.forName(AsyncTask.class.getName());
        } catch (ClassNotFoundException e) {
        }
    }

    public GalleryData getGalleryData() {
        TraceController.beginSection("getGalleryData");
        if (this.mGalleryData == null) {
            this.mGalleryData = new GalleryData(getAndroidContext());
        }
        TraceController.endSection();
        return this.mGalleryData;
    }

    private void setupExceptionHandler() {
        CrashHandler.init(this);
    }

    public ContextedUtils getContextedUtils() {
        if (this.mUtils == null) {
            this.mUtils = new ContextedUtils(this);
        }
        return this.mUtils;
    }

    public Object getAppComponent(Class<?> cmpClass) {
        return ServiceRegistry.getComponent(this, cmpClass.getSimpleName());
    }

    public static void setBootStartupStatus(boolean bootStartup) {
        sIsBootStartup = bootStartup;
    }

    public static boolean getBootStartupStatus() {
        return sIsBootStartup;
    }
}
