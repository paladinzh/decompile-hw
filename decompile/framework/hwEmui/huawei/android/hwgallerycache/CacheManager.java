package huawei.android.hwgallerycache;

import android.content.Context;
import android.os.storage.StorageManager;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class CacheManager {
    private static final String KEY_CACHE_UP_TO_DATE = "cache-up-to-date";
    private static final String TAG = "CacheManager";
    private static HashMap<String, BlobCache> sCacheMap = new HashMap();
    private static String[] sVolumePaths;

    public static BlobCache getCache(Context context, String filename, int maxEntries, int maxBytes, String version) {
        BlobCache cache;
        IOException e;
        Exception e2;
        synchronized (sCacheMap) {
            BlobCache cache2 = (BlobCache) sCacheMap.get(filename);
            if (cache2 == null) {
                sVolumePaths = ((StorageManager) context.getSystemService("storage")).getVolumePaths();
                String path = new File(sVolumePaths[0], "/Android/data/com.android.gallery3d/cache").getAbsolutePath() + "/" + filename;
                Log.d(TAG, "Using cache file: " + path);
                try {
                    cache = new BlobCache(path, maxEntries, maxBytes, false, version);
                    try {
                        sCacheMap.put(filename, cache);
                    } catch (IOException e3) {
                        e = e3;
                        Log.e(TAG, "Cannot instantiate cache!", e);
                        return cache;
                    } catch (Exception e4) {
                        e2 = e4;
                        Log.e(TAG, "Cannot instantiate cache!", e2);
                        return cache;
                    }
                } catch (IOException e5) {
                    e = e5;
                    cache = cache2;
                    Log.e(TAG, "Cannot instantiate cache!", e);
                    return cache;
                } catch (Exception e6) {
                    e2 = e6;
                    cache = cache2;
                    Log.e(TAG, "Cannot instantiate cache!", e2);
                    return cache;
                }
            }
            cache = cache2;
        }
        return cache;
    }
}
