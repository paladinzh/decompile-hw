package com.android.mms.attachment;

import android.content.Context;
import com.android.mms.attachment.datamodel.MemoryCacheManager;
import com.android.mms.attachment.datamodel.data.MediaCacheManager;
import com.android.mms.attachment.datamodel.media.GalleryCursorManager;
import com.android.mms.attachment.datamodel.media.MediaResourceManager;
import com.android.mms.attachment.datamodel.media.RichMessageManager;

public abstract class Factory {
    private static volatile Factory sInstance;

    public abstract Context getApplicationContext();

    public abstract GalleryCursorManager getGalleryCursorManager();

    public abstract MediaCacheManager getMediaCacheManager();

    public abstract MediaResourceManager getMediaResourceManager();

    public abstract MemoryCacheManager getMemoryCacheManager();

    public abstract RichMessageManager getRichMessageManager();

    public abstract void reclaimMemory();

    public static Factory get() {
        return sInstance;
    }

    protected static void setInstance(Factory factory) {
        sInstance = factory;
    }
}
