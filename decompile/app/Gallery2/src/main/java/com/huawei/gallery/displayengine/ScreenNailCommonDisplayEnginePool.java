package com.huawei.gallery.displayengine;

import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.GalleryLog;
import java.util.HashMap;
import java.util.Map;

public class ScreenNailCommonDisplayEnginePool {
    private final Map<ScreenNailHashKey, ScreenNailCommonDisplayEngine> mScreenNailCommonDisplayEngineMap = new HashMap();

    public static class ScreenNailHashKey {
        private long mDateModifiedInSec;
        private String mName;
        private Path mPath;

        public ScreenNailHashKey(Path path, long time, String name) {
            this.mPath = path;
            this.mDateModifiedInSec = time;
            this.mName = name;
        }

        public Path getPath() {
            return this.mPath;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof ScreenNailHashKey)) {
                return false;
            }
            if (this.mPath == ((ScreenNailHashKey) obj).getPath() && this.mDateModifiedInSec == ((ScreenNailHashKey) obj).mDateModifiedInSec) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return (this.mPath.toString() + "+" + this.mDateModifiedInSec).hashCode();
        }

        public String toString() {
            return "Path:" + this.mPath + ", name:" + this.mName + ", modified:" + this.mDateModifiedInSec + ", hashCode:" + hashCode();
        }
    }

    private static ScreenNailHashKey getScreenNailHashKey(MediaItem mediaItem) {
        return new ScreenNailHashKey(mediaItem.getPath(), mediaItem.getDateModifiedInSec(), mediaItem.getName());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void add(MediaItem mediaItem, ScreenNailCommonDisplayEngine displayEngine) {
        if (mediaItem != null && displayEngine != null) {
            ScreenNailHashKey key = getScreenNailHashKey(mediaItem);
            ScreenNailCommonDisplayEngine oldDisplayEngine = (ScreenNailCommonDisplayEngine) this.mScreenNailCommonDisplayEngineMap.get(key);
            if (oldDisplayEngine != null) {
                GalleryLog.w("ScreenNailCommonDisplayEnginePool", "there is an old display engine " + oldDisplayEngine + ", key is " + key);
            }
            this.mScreenNailCommonDisplayEngineMap.put(key, displayEngine);
            printPool("add key:" + key);
        }
    }

    public synchronized void remove(MediaItem mediaItem) {
        if (mediaItem != null) {
            ScreenNailHashKey key = getScreenNailHashKey(mediaItem);
            printPool("remove key:" + key);
            this.mScreenNailCommonDisplayEngineMap.remove(key);
        }
    }

    public synchronized ScreenNailCommonDisplayEngine get(MediaItem mediaItem) {
        if (mediaItem == null) {
            return null;
        }
        ScreenNailHashKey key = getScreenNailHashKey(mediaItem);
        printPool("find key:" + key);
        return (ScreenNailCommonDisplayEngine) this.mScreenNailCommonDisplayEngineMap.get(key);
    }

    public synchronized void clear() {
        printPool("clear");
        this.mScreenNailCommonDisplayEngineMap.clear();
    }

    private void printPool(String method) {
    }
}
