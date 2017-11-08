package com.android.gallery3d.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.UpdateHelper;
import com.huawei.gallery.media.database.CloudTableOperateHelper;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.util.ArrayList;

public interface IRecycle {

    public static class RecycleItem {
        private static int INDEX_GALLERY_ID;
        private static int INDEX_HW_BURST;
        private static int INDEX_RECYCLE_TIME;
        private static int INDEX_SOURCE_PATH;
        static String[] sProjection;
        public int mGalleryId;
        public boolean mIsHwBurst;
        public String mLocalRealPath;
        public long mRecycleTime;
        public String mSourcePath;

        public void loadFromCursor(Cursor cursor) {
            boolean z = false;
            this.mRecycleTime = cursor.getLong(INDEX_RECYCLE_TIME);
            this.mGalleryId = cursor.getInt(INDEX_GALLERY_ID);
            this.mSourcePath = cursor.getString(INDEX_SOURCE_PATH);
            this.mLocalRealPath = cursor.getString(cursor.getColumnIndex("localRealPath"));
            if (cursor.getInt(INDEX_HW_BURST) > 0) {
                z = true;
            }
            this.mIsHwBurst = z;
        }

        public boolean updateFromCursor(Cursor cursor) {
            boolean z;
            UpdateHelper uh = new UpdateHelper();
            this.mRecycleTime = uh.update(this.mRecycleTime, cursor.getLong(INDEX_RECYCLE_TIME));
            this.mGalleryId = uh.update(this.mGalleryId, cursor.getInt(INDEX_GALLERY_ID));
            this.mSourcePath = (String) uh.update(this.mSourcePath, cursor.getString(INDEX_SOURCE_PATH));
            this.mLocalRealPath = (String) uh.update(this.mLocalRealPath, cursor.getString(cursor.getColumnIndex("localRealPath")));
            Object valueOf = Boolean.valueOf(this.mIsHwBurst);
            if (cursor.getInt(INDEX_HW_BURST) > 0) {
                z = true;
            } else {
                z = false;
            }
            this.mIsHwBurst = ((Boolean) uh.update(valueOf, Boolean.valueOf(z))).booleanValue();
            return uh.isUpdated();
        }

        public void updateParams(GalleryMediaItem mediaItem, String localRealPath) {
            int i;
            mediaItem.mimeType = CloudTableOperateHelper.getMimeType(mediaItem.mFileType);
            if (mediaItem.mFileType == 4) {
                i = 3;
            } else {
                i = 1;
            }
            mediaItem.mMediaType = String.valueOf(i);
            if (mediaItem.mFileType == 2) {
                i = -1;
            } else {
                i = 0;
            }
            mediaItem.mVoiceOffset = (long) i;
            if (mediaItem.mFileType == 8) {
                i = -1;
            } else {
                i = 0;
            }
            mediaItem.mRectifyOffset = i;
            mediaItem.mRefocusPhoto = CloudTableOperateHelper.getRefocus(mediaItem.mFileType);
            mediaItem.mSpecialFileOffset = 0;
            if (mediaItem.mFileType == 9) {
                mediaItem.mSpecialFileType = 50;
                mediaItem.mSpecialFileOffset = -1;
            }
            if (!TextUtils.isEmpty(localRealPath) && !mediaItem.isOnlyCloudItem()) {
                mediaItem.filePath = localRealPath;
                mediaItem.mThumbType = 3;
            } else if (!TextUtils.isEmpty(mediaItem.mLocalBigThumbPath)) {
                mediaItem.filePath = mediaItem.mLocalBigThumbPath;
                mediaItem.mThumbType = 2;
            } else if (TextUtils.isEmpty(mediaItem.mLocalThumbPath)) {
                mediaItem.filePath = "cloud--" + mediaItem.mHash;
                mediaItem.mThumbType = 0;
            } else {
                mediaItem.filePath = mediaItem.mLocalThumbPath;
                mediaItem.mThumbType = 1;
            }
            if (!mediaItem.filePath.startsWith("cloud-")) {
                try {
                    Options opts = new Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(mediaItem.filePath, opts);
                    if (opts.outWidth > 0 && opts.outHeight > 0) {
                        mediaItem.width = opts.outWidth;
                        mediaItem.height = opts.outHeight;
                    }
                } catch (Throwable throwable) {
                    GalleryLog.d("Recycle_RecycleItem", throwable.getMessage());
                }
            }
            if (mediaItem.getLocalMediaId() == -1) {
                mediaItem.rotation = PhotoShareUtils.getOrientation(mediaItem.rotation);
            }
        }

        public static synchronized String[] getProjection(String[] oldProjection) {
            synchronized (RecycleItem.class) {
                if (sProjection == null || sProjection.length <= oldProjection.length) {
                    String[] old = (String[]) oldProjection.clone();
                    ArrayList<String> list = new ArrayList();
                    for (Object add : old) {
                        list.add(add);
                    }
                    INDEX_RECYCLE_TIME = list.size();
                    list.add("recycledTime");
                    INDEX_GALLERY_ID = list.size();
                    list.add("galleryId");
                    INDEX_SOURCE_PATH = list.size();
                    list.add("sourcePath");
                    list.add("localRealPath");
                    INDEX_HW_BURST = list.size();
                    list.add("is_hw_burst");
                    list.add("_source_display_name");
                    sProjection = new String[list.size()];
                    list.toArray(sProjection);
                    return (String[]) sProjection.clone();
                }
                return (String[]) sProjection.clone();
            }
        }
    }

    ArrayList<Path> getBurstCoverPath();

    long getRecycleTime();

    String getSourcePath();

    void insertMediaFile();

    boolean isHwBurstCover();

    void onDeleteThrough(SQLiteDatabase sQLiteDatabase, Bundle bundle);
}
