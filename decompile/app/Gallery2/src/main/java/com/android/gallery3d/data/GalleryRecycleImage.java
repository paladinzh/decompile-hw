package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.IRecycle.RecycleItem;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.UpdateHelper;
import com.huawei.gallery.media.LocalRecycledFile;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;

public class GalleryRecycleImage extends GalleryImage implements IRecycle {
    public static final Path IMAGE_PATH = Path.fromString("/gallery/recycle/image/item/");
    private RecycleItem mRecycleItem;

    public GalleryRecycleImage(Path path, GalleryApp application, Cursor cursor) {
        super(path, application, cursor);
    }

    public GalleryRecycleImage(Path path, GalleryApp application, String idString) {
        super(path, application, idString);
    }

    protected Uri getMediaUri() {
        return GalleryRecycleAlbum.URI;
    }

    public String[] getProjection() {
        return RecycleItem.getProjection(super.getProjection());
    }

    protected String getQueryWhereById(String id) {
        String[] idArray = RecycleUtils.splitRecycleItemPathId(id);
        return "local_media_id = " + idArray[0] + " AND " + "cloud_media_id" + " = " + idArray[1];
    }

    protected void loadFromCursor(Cursor cursor) {
        super.loadFromCursor(cursor);
        this.mRecycleItem = new RecycleItem();
        this.mRecycleItem.loadFromCursor(cursor);
        if (this.mLocalMediaId == -1 && this.mCloudMediaId > 0) {
            this.mRecycleItem.updateParams(this, this.mRecycleItem.mLocalRealPath);
        }
    }

    protected void updateSpecialValues(UpdateHelper uh, Cursor cursor) {
        if (this.mLocalMediaId != -1 || this.mCloudMediaId <= 0) {
            super.updateSpecialValues(uh, cursor);
        } else {
            this.mRecycleItem.updateParams(this, this.mRecycleItem.mLocalRealPath);
        }
    }

    protected boolean updateFromCursor(Cursor cursor) {
        String oldFilePath = this.filePath;
        boolean isUpdate = this.mRecycleItem.updateFromCursor(cursor);
        super.updateFromCursor(cursor);
        return isUpdate | (this.filePath.equals(oldFilePath) ? 0 : 1);
    }

    public long getRecycleTime() {
        return this.mRecycleItem.mRecycleTime;
    }

    public String getSourcePath() {
        return this.mRecycleItem.mSourcePath;
    }

    public static String[] copyProjection() {
        return (String[]) RecycleItem.getProjection(GalleryMediaItem.copyProjection()).clone();
    }

    public void onDeleteThrough(SQLiteDatabase db, Bundle data) {
        int flag = data.getInt("recycle_flag");
        if (this.mLocalMediaId != -1 && flag == 3) {
            RecycleUtils.makeTempFileCopy(new File(this.filePath), data);
        }
        ContentResolver resolver = this.mApplication.getContentResolver();
        String sourcePath = null;
        if (flag == 1) {
            sourcePath = LocalRecycledFile.querySourcePath(db, resolver, getLocalMediaId());
        }
        LocalRecycledFile.delete(db, resolver, getLocalMediaId());
        if (PhotoShareUtils.isGUIDSupport() && data.getBoolean("delete_recycle_file", false)) {
            flag = 3;
        }
        onCloudRecycleProcess(db, flag, sourcePath);
    }

    public boolean isHwBurstCover() {
        return this.mRecycleItem.mIsHwBurst;
    }

    public boolean isBurstCover() {
        return isHwBurstCover();
    }

    public ArrayList<Path> getBurstCoverPath() {
        ArrayList<Path> paths = new ArrayList();
        if (this.mRecycleItem.mIsHwBurst) {
            ArrayList<String> ids = new ArrayList();
            Closeable closeable = null;
            try {
                closeable = this.mApplication.getContentResolver().query(GalleryRecycleAlbum.URI, new String[]{"local_media_id", "cloud_media_id"}, (this.bucketId == 0 ? "" : "bucket_id = " + this.bucketId + " AND ") + "_display_name" + " like '%" + this.mDisplayName.substring(4, (this.mDisplayName.length() - "000_COVER.JPG".length()) - 4) + "%'", null, null, null);
                while (closeable.moveToNext()) {
                    ids.add(RecycleUtils.mergeRecycleItemPathId(closeable.getInt(0), closeable.getInt(1)));
                }
            } catch (Exception e) {
                GalleryLog.e("Recycle_GalleryRecycleImage", "exception " + e.getMessage());
            } finally {
                Utils.closeSilently(closeable);
            }
            int size = ids.size();
            for (int i = 0; i < size; i++) {
                paths.add(IMAGE_PATH.getChild((String) ids.get(i)));
            }
            return paths;
        }
        paths.add(this.mPath);
        return paths;
    }

    public void insertMediaFile() {
        try {
            ContentValues values = getValues();
            values.put("_data", this.mRecycleItem.mSourcePath);
            values.put("is_hw_burst", Integer.valueOf(this.mRecycleItem.mIsHwBurst ? 1 : 0));
            this.mApplication.getContentResolver().insert(GalleryUtils.EXTERNAL_FILE_URI, values);
        } catch (Exception e) {
            GalleryLog.e("Recycle_GalleryRecycleImage", "exception " + e.getMessage());
        }
    }

    public boolean isRecycleItem() {
        return true;
    }
}
