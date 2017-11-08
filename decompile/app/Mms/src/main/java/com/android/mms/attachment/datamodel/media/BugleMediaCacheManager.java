package com.android.mms.attachment.datamodel.media;

import com.android.mms.attachment.datamodel.data.MediaCacheManager;

public class BugleMediaCacheManager extends MediaCacheManager {
    protected MediaCache<?> createMediaCacheById(int id) {
        switch (id) {
            case 1:
                return new PoolableImageCache(10240, id, "DefaultImageCache");
            case 2:
                return new PoolableImageCache(id, "AvatarImageCache");
            default:
                return null;
        }
    }
}
