package com.huawei.gallery.util;

import android.graphics.Bitmap;
import com.android.gallery3d.data.BitmapPool;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.huawei.gallery.ui.TitleLoaderListener;
import java.util.HashMap;
import java.util.LinkedList;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class GalleryPool {
    private static final Object ITEM_LOCK = new Object();
    private static final Object TITLE_LOCK = new Object();
    private static LinkedList<PathKey> sPathList = new LinkedList();
    private static HashMap<PathKey, Bitmap> sPools = new HashMap(SmsCheckResult.ESCT_217);
    private static LinkedList<TitleKey> sTitleList = new LinkedList();
    private static HashMap<TitleKey, Bitmap> sTitlePools = new HashMap(33);

    private static class PathKey {
        public Path path;
        public long timeModified;

        public PathKey(Path p, long t) {
            this.path = p;
            this.timeModified = t;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof PathKey)) {
                return false;
            }
            PathKey key = (PathKey) o;
            if (this.path.equals(key.path) && this.timeModified == key.timeModified) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return this.path.hashCode();
        }
    }

    private static class TitleKey {
        public String title;
        public int value;

        public TitleKey(String t, int v) {
            this.title = t;
            this.value = v;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof TitleKey)) {
                return false;
            }
            TitleKey key = (TitleKey) o;
            if (this.title.equals(key.title) && this.value == key.value) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return this.title.hashCode() + this.value;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean recycle(Path path, long timeModified, Bitmap bitmap, boolean forceRecycle) {
        BitmapPool pool;
        if (forceRecycle) {
            pool = MediaItem.getMicroThumbPool(bitmap.getWidth());
            if (pool != null) {
                pool.recycle(bitmap);
            }
            return false;
        }
        synchronized (ITEM_LOCK) {
            if (bitmap == null || path == null) {
            } else {
                PathKey key = new PathKey(path, timeModified);
                if (((Bitmap) sPools.get(key)) != null) {
                    sPathList.remove(key);
                    sPathList.addFirst(key);
                    return false;
                }
                sPools.put(key, bitmap);
                sPathList.addFirst(key);
                if (sPathList.size() > SmsCheckResult.ESCT_216) {
                    Bitmap bmp = (Bitmap) sPools.remove((PathKey) sPathList.removeLast());
                    if (bmp != null) {
                        pool = MediaItem.getMicroThumbPool(bmp.getWidth());
                        if (pool != null) {
                            pool.recycle(bmp);
                        }
                    }
                }
                return true;
            }
        }
    }

    public static Bitmap get(Path path, long timeModified) {
        if (path == null) {
            return null;
        }
        Bitmap bitmap;
        synchronized (ITEM_LOCK) {
            PathKey key = new PathKey(path, timeModified);
            sPathList.remove(key);
            bitmap = (Bitmap) sPools.remove(key);
        }
        return bitmap;
    }

    public static void remove(Path path, long timeModified) {
        if (path != null) {
            synchronized (ITEM_LOCK) {
                PathKey key = new PathKey(path, timeModified);
                Bitmap bmp = (Bitmap) sPools.remove(key);
                sPathList.remove(key);
                if (bmp != null) {
                    BitmapPool pool = MediaItem.getMicroThumbPool(bmp.getWidth());
                    if (pool != null) {
                        pool.recycle(bmp);
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean recycle(String title, int value, Bitmap bitmap, TitleLoaderListener listener) {
        synchronized (TITLE_LOCK) {
            if (bitmap == null || title == null) {
                return false;
            }
            TitleKey key = new TitleKey(title, value);
            Bitmap bmp = (Bitmap) sTitlePools.get(key);
            if (bmp != null) {
                sTitleList.remove(key);
                sTitleList.addFirst(key);
                if (bmp != bitmap) {
                    sTitlePools.remove(key);
                    sTitlePools.put(key, bitmap);
                    listener.recycleTitle(bmp);
                }
            } else {
                sTitlePools.put(key, bitmap);
                sTitleList.addFirst(key);
                if (sTitleList.size() > 32) {
                    listener.recycleTitle((Bitmap) sTitlePools.remove(sTitleList.removeLast()));
                }
            }
        }
    }

    public static Bitmap get(String title, int value) {
        Bitmap bitmap;
        synchronized (TITLE_LOCK) {
            TitleKey key = new TitleKey(title, value);
            sTitleList.remove(key);
            bitmap = (Bitmap) sTitlePools.remove(key);
        }
        return bitmap;
    }
}
