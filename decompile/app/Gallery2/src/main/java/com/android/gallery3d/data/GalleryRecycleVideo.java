package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.IRecycle.RecycleItem;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.UpdateHelper;
import com.huawei.gallery.media.LocalRecycledFile;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import java.io.File;
import java.util.ArrayList;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class GalleryRecycleVideo extends GalleryVideo implements IRecycle {
    public static final Path VIDEO_PATH = Path.fromString("/gallery/recycle/video/item/");
    private RecycleItem mRecycleItem;

    public GalleryRecycleVideo(Path path, GalleryApp application, Cursor cursor) {
        super(path, application, cursor);
    }

    public GalleryRecycleVideo(Path path, GalleryApp application, String idString) {
        super(path, application, idString);
    }

    protected Uri getMediaUri() {
        return GalleryRecycleAlbum.URI;
    }

    protected String[] getProjection() {
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

    protected boolean updateFromCursor(Cursor cursor) {
        String oldFilePath = this.filePath;
        boolean isUpdate = this.mRecycleItem.updateFromCursor(cursor);
        super.updateFromCursor(cursor);
        return isUpdate | (this.filePath.equals(oldFilePath) ? 0 : 1);
    }

    protected void updateSpecialValues(UpdateHelper uh, Cursor cursor) {
        if (this.mLocalMediaId != -1 || this.mCloudMediaId <= 0) {
            super.updateSpecialValues(uh, cursor);
        } else {
            this.mRecycleItem.updateParams(this, this.mRecycleItem.mLocalRealPath);
        }
    }

    public long getRecycleTime() {
        return this.mRecycleItem.mRecycleTime;
    }

    public String getSourcePath() {
        return this.mRecycleItem.mSourcePath;
    }

    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        details.addDetail(SmsCheckResult.ESCT_200, this.mRecycleItem.mSourcePath);
        return details;
    }

    public void onDeleteThrough(SQLiteDatabase db, Bundle data) {
        int flag = data.getInt("recycle_flag");
        if (this.mLocalMediaId != -1 && flag == 3) {
            RecycleUtils.makeTempFileCopy(new File(this.filePath), data);
        }
        String sourcePath = null;
        ContentResolver resolver = this.mApplication.getContentResolver();
        if (flag == 1) {
            sourcePath = LocalRecycledFile.querySourcePath(db, resolver, getLocalMediaId());
        }
        LocalRecycledFile.delete(db, resolver, getLocalMediaId());
        if (PhotoShareUtils.isGUIDSupport() && data.getBoolean("delete_recycle_file", false)) {
            flag = 3;
        }
        onCloudRecycleProcess(db, flag, sourcePath);
    }

    public Uri getContentUri() {
        return new Builder().encodedPath(getFilePath()).build();
    }

    public boolean isHwBurstCover() {
        return false;
    }

    public ArrayList<Path> getBurstCoverPath() {
        return new ArrayList();
    }

    public void insertMediaFile() {
        try {
            this.mApplication.getContentResolver().insert(GalleryUtils.EXTERNAL_FILE_URI, getValues());
        } catch (Exception e) {
            GalleryLog.d("GalleryRecycleImage", "exception e" + e);
        }
    }

    public boolean isRecycleItem() {
        return true;
    }
}
