package com.android.mms.attachment;

import android.content.Context;
import com.android.mms.MmsApp;
import com.android.mms.attachment.datamodel.MemoryCacheManager;
import com.android.mms.attachment.datamodel.data.MediaCacheManager;
import com.android.mms.attachment.datamodel.media.BugleMediaCacheManager;
import com.android.mms.attachment.datamodel.media.GalleryCursorManager;
import com.android.mms.attachment.datamodel.media.MediaResourceManager;
import com.android.mms.attachment.datamodel.media.RichMessageManager;
import com.android.mms.attachment.utils.MediaUtilImpl;
import com.huawei.cspcommon.MLog;

public class FactoryImpl extends Factory {
    private static final Object PHONEUTILS_INSTANCE_LOCK = new Object();
    private static final String TAG = FactoryImpl.class.getName();
    private Context mApplicationContext;
    private GalleryCursorManager mGalleryCursorManager;
    private MediaCacheManager mMediaCacheManager;
    private MediaResourceManager mMediaResourceManager;
    private MediaUtilImpl mMediaUtilImpl;
    private MemoryCacheManager mMemoryCacheManager;
    private RichMessageManager mRichMessageManager;

    private FactoryImpl() {
    }

    public static Factory register(Context applicationContext, MmsApp application) {
        FactoryImpl factory = new FactoryImpl();
        Factory.setInstance(factory);
        factory.mApplicationContext = applicationContext;
        factory.mMediaResourceManager = new MediaResourceManager();
        factory.mMemoryCacheManager = new MemoryCacheManager();
        factory.mMediaCacheManager = new BugleMediaCacheManager();
        factory.mGalleryCursorManager = new GalleryCursorManager();
        factory.mRichMessageManager = new RichMessageManager();
        factory.mMediaUtilImpl = new MediaUtilImpl();
        return factory;
    }

    public Context getApplicationContext() {
        return this.mApplicationContext;
    }

    public void reclaimMemory() {
        this.mMemoryCacheManager.reclaimMemory();
        MLog.d(TAG, "reclaimMemory");
    }

    public MediaResourceManager getMediaResourceManager() {
        return this.mMediaResourceManager;
    }

    public MemoryCacheManager getMemoryCacheManager() {
        return this.mMemoryCacheManager;
    }

    public MediaCacheManager getMediaCacheManager() {
        return this.mMediaCacheManager;
    }

    public GalleryCursorManager getGalleryCursorManager() {
        return this.mGalleryCursorManager;
    }

    public RichMessageManager getRichMessageManager() {
        return this.mRichMessageManager;
    }
}
