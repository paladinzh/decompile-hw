package com.android.gallery3d.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.android.gallery3d.common.BlobCache;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class CacheManager {
    private static HashMap<String, BlobCache> sCacheMap = new HashMap();
    private static boolean sOldCheckDone = false;

    public static BlobCache getCache(Context context, String filename, int maxEntries, int maxBytes, int version) {
        IOException e;
        synchronized (sCacheMap) {
            if (!sOldCheckDone) {
                removeOldFilesIfNecessary(context);
                sOldCheckDone = true;
            }
            BlobCache cache = (BlobCache) sCacheMap.get(filename);
            BlobCache cache2;
            if (cache == null) {
                File cacheDir = GalleryUtils.ensureExternalCacheDir(context);
                if (cacheDir == null) {
                    return null;
                }
                try {
                    cache2 = new BlobCache(cacheDir.getAbsolutePath() + "/" + filename, maxEntries, maxBytes, false, version);
                    try {
                        sCacheMap.put(filename, cache2);
                    } catch (IOException e2) {
                        e = e2;
                        GalleryLog.e("CacheManager", "Cannot instantiate cache!" + e.getMessage());
                        return cache2;
                    }
                } catch (IOException e3) {
                    e = e3;
                    cache2 = cache;
                    GalleryLog.e("CacheManager", "Cannot instantiate cache!" + e.getMessage());
                    return cache2;
                }
            }
            cache2 = cache;
        }
    }

    private static void removeOldFilesIfNecessary(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int n = 0;
        try {
            n = pref.getInt("cache-up-to-date", 0);
        } catch (Throwable th) {
        }
        if (n == 0) {
            pref.edit().putInt("cache-up-to-date", 1).commit();
            File cacheDir = GalleryUtils.ensureExternalCacheDir(context);
            if (cacheDir != null) {
                String prefix = cacheDir.getAbsolutePath() + "/";
                BlobCache.deleteFiles(prefix + "imgcache");
                BlobCache.deleteFiles(prefix + "rev_geocoding");
                BlobCache.deleteFiles(prefix + "bookmark");
                BlobCache.deleteFiles(prefix + "faceimgcache");
            }
        }
    }
}
