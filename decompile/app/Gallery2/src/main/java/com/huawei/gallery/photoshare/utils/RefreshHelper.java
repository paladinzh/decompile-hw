package com.huawei.gallery.photoshare.utils;

import com.android.gallery3d.util.GalleryLog;

public class RefreshHelper {
    private static long sLastRefreshAlbum = 0;
    private static long sLastRefreshDiscovery = 0;
    private static boolean sSyncFailed = false;

    public static void setSyncFailed(boolean isSyncFailed) {
        sSyncFailed = isSyncFailed;
    }

    public static boolean getSyncFailedStatus() {
        return sSyncFailed;
    }

    public static void refreshAlbum(long now) {
        long interval = now - sLastRefreshAlbum;
        if (interval >= 15000 || interval <= 0 || sSyncFailed) {
            sLastRefreshAlbum = now;
            PhotoShareUtils.refreshAlbum(2);
            sSyncFailed = false;
            return;
        }
        GalleryLog.v("RefreshHelper", "refreshAlbum too often");
    }

    public static void refreshDiscovery(long now) {
        long interval = now - sLastRefreshDiscovery;
        if (interval >= 15000 || interval <= 0) {
            sLastRefreshDiscovery = now;
            PhotoShareUtils.refreshAlbum(5);
            return;
        }
        GalleryLog.v("RefreshHelper", "refreshDiscovety too often");
    }
}
