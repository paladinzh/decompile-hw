package com.android.gallery3d.app;

import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;

public class AlbumSetDataBackup {
    private static AlbumSetDataBackup sAlbumSetDataBackup = null;
    private static final BackUpKey sDefaultKey = new BackUpKey(DataManager.getTopOutSideSetPath(), "android.intent.action.MAIN");
    private MediaItem[][] mCoverItem;
    private MediaSet[] mData;
    private long[] mItemVersion;
    private long[] mSetVersion;
    private int[] mTotalCount;
    private int[] mTotalImageCount;

    public static class BackUpKey {
        private final String mAction;
        private final String mMediaSetPath;

        public BackUpKey(String mediaSetPath, String action) {
            this.mMediaSetPath = mediaSetPath;
            this.mAction = action;
        }

        public int hashCode() {
            if (this.mMediaSetPath == null) {
                return 0;
            }
            return this.mMediaSetPath.hashCode();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean equals(Object o) {
            boolean z = false;
            if (o == null || this.mMediaSetPath == null || this.mAction == null || !(o instanceof BackUpKey)) {
                return false;
            }
            BackUpKey backupKey = (BackUpKey) o;
            if (this.mAction.equals(backupKey.mAction)) {
                z = this.mMediaSetPath.equals(backupKey.mMediaSetPath);
            }
            return z;
        }
    }

    private AlbumSetDataBackup() {
        clear();
    }

    public static synchronized AlbumSetDataBackup getInstance() {
        AlbumSetDataBackup albumSetDataBackup;
        synchronized (AlbumSetDataBackup.class) {
            if (sAlbumSetDataBackup == null) {
                sAlbumSetDataBackup = new AlbumSetDataBackup();
            }
            albumSetDataBackup = sAlbumSetDataBackup;
        }
        return albumSetDataBackup;
    }

    public synchronized void clear() {
        this.mData = null;
        this.mCoverItem = null;
        this.mTotalCount = null;
        this.mTotalImageCount = null;
        this.mItemVersion = null;
        this.mSetVersion = null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean backup(BackUpKey key, MediaSet[] mediaSet, MediaItem[][] cover, int[] count, int[] imageCount, long[] itemVersion, long[] setVersion) {
        if (key != null && mediaSet != null && cover != null && count != null && imageCount != null && itemVersion != null && setVersion != null) {
            if (!sDefaultKey.equals(key)) {
                return false;
            }
            int cacheCount = Math.min(mediaSet.length, 10);
            this.mCoverItem = new MediaItem[cacheCount][];
            this.mData = new MediaSet[cacheCount];
            this.mTotalCount = new int[cacheCount];
            this.mTotalImageCount = new int[cacheCount];
            this.mItemVersion = new long[cacheCount];
            this.mSetVersion = new long[cacheCount];
            for (int i = 0; i < cacheCount; i++) {
                this.mCoverItem[i] = cover[i];
                this.mData[i] = mediaSet[i];
                this.mTotalCount[i] = count[i];
                this.mTotalImageCount[i] = imageCount[i];
                this.mItemVersion[i] = itemVersion[i];
                this.mSetVersion[i] = setVersion[i];
            }
            return false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean comeback(BackUpKey key, MediaSet[] mediaSet, MediaItem[][] cover, int[] count, int[] imageCount, long[] itemVersion, long[] setVersion) {
        if (key != null && mediaSet != null && cover != null && count != null && imageCount != null && itemVersion != null && setVersion != null) {
            if (!(this.mCoverItem == null || this.mData == null)) {
                if (!(this.mTotalCount == null || this.mTotalImageCount == null || this.mItemVersion == null || this.mSetVersion == null)) {
                    if (!sDefaultKey.equals(key)) {
                        return false;
                    }
                    int cacheCount = Math.min(mediaSet.length, this.mData.length);
                    for (int i = 0; i < cacheCount; i++) {
                        cover[i] = this.mCoverItem[i];
                        mediaSet[i] = this.mData[i];
                        count[i] = this.mTotalCount[i];
                        imageCount[i] = this.mTotalImageCount[i];
                        itemVersion[i] = this.mItemVersion[i];
                        setVersion[i] = this.mSetVersion[i];
                    }
                    return false;
                }
            }
        }
    }
}
