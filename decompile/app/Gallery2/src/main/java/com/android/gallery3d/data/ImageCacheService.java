package com.android.gallery3d.data;

import android.content.Context;
import com.android.gallery3d.common.BlobCache;
import com.android.gallery3d.common.BlobCache.LookupRequest;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BytesBufferPool.BytesBuffer;
import com.android.gallery3d.util.CacheManager;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.TraceController;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageCacheService {
    private BlobCache mCache;

    public ImageCacheService(Context context) {
        this.mCache = CacheManager.getCache(context, "imgcache", 20000, 419430400, 16);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean getImageData(Path path, long timeModified, int type, BytesBuffer buffer) {
        if (this.mCache == null) {
            return false;
        }
        TraceController.traceBegin("ImageCacheService.getImageData");
        byte[] key = makeKey(path, timeModified, type);
        long cacheKey = Utils.crc64Long(key);
        try {
            LookupRequest request = new LookupRequest();
            request.key = cacheKey;
            request.buffer = buffer.data;
            synchronized (this.mCache) {
                if (!this.mCache.lookup(request)) {
                    TraceController.traceEnd();
                    return false;
                }
            }
        } catch (IOException e) {
        }
        TraceController.traceEnd();
        return false;
    }

    public void putImageData(Path path, long timeModified, int type, byte[] value) {
        if (this.mCache != null) {
            TraceController.traceBegin("ImageCacheService.putImageData");
            byte[] key = makeKey(path, timeModified, type);
            long cacheKey = Utils.crc64Long(key);
            ByteBuffer buffer = ByteBuffer.allocate(key.length + value.length);
            buffer.put(key);
            buffer.put(value);
            synchronized (this.mCache) {
                try {
                    this.mCache.insert(cacheKey, buffer.array());
                } catch (IOException e) {
                }
            }
            TraceController.traceEnd();
        }
    }

    public void removeImageData(Path path, long timeModified, int type) {
        if (this.mCache != null) {
            TraceController.traceBegin("ImageCacheService.removeImageData");
            long cacheKey = Utils.crc64Long(makeKey(path, timeModified, type));
            synchronized (this.mCache) {
                try {
                    this.mCache.removeEntry(cacheKey);
                } catch (IOException e) {
                }
            }
            TraceController.traceEnd();
        }
    }

    private static byte[] makeKey(Path path, long timeModified, int type) {
        return GalleryUtils.getBytes(path.toString() + "+" + timeModified + "+" + type);
    }

    private static boolean isSameKey(byte[] key, byte[] buffer) {
        int n = key.length;
        if (buffer.length < n) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (key[i] != buffer[i]) {
                return false;
            }
        }
        return true;
    }
}
