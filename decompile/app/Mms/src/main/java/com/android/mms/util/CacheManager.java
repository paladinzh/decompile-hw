package com.android.mms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.huawei.cspcommon.MLog;
import java.io.IOException;
import java.util.HashMap;

public class CacheManager {
    private static HashMap<String, BlobCache> sCacheMap = new HashMap();
    private static boolean sOldCheckDone = false;

    public static BlobCache getCache(Context context, String filename, int maxEntries, int maxBytes, int version) {
        BlobCache cache;
        IOException e;
        synchronized (sCacheMap) {
            if (!sOldCheckDone) {
                removeOldFilesIfNecessary(context);
                sOldCheckDone = true;
            }
            BlobCache cache2 = (BlobCache) sCacheMap.get(filename);
            if (cache2 == null) {
                try {
                    cache = new BlobCache(context.getCacheDir().getAbsolutePath() + "/" + filename, maxEntries, maxBytes, false, version);
                    try {
                        sCacheMap.put(filename, cache);
                    } catch (IOException e2) {
                        e = e2;
                        MLog.e("CacheManager", "Cannot instantiate cache!", (Throwable) e);
                        return cache;
                    }
                } catch (IOException e3) {
                    e = e3;
                    cache = cache2;
                    MLog.e("CacheManager", "Cannot instantiate cache!", (Throwable) e);
                    return cache;
                }
            }
            cache = cache2;
        }
        return cache;
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
            clear(context);
        }
    }

    public static void clear(Context context) {
        BlobCache.deleteFiles((context.getCacheDir().getAbsolutePath() + "/") + "imgcache");
        synchronized (sCacheMap) {
            sCacheMap.remove("imgcache");
        }
    }
}
