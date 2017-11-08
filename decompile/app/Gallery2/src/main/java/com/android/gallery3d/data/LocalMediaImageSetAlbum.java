package com.android.gallery3d.data;

import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.WhiteList;
import com.huawei.gallery.storage.GalleryStorageManager;

public class LocalMediaImageSetAlbum extends LocalMediaSetAlbum {
    public LocalMediaImageSetAlbum(Path path, GalleryApp application) {
        super(path, application);
    }

    protected void initLocalClause() {
        String str;
        StringBuilder append = new StringBuilder().append("((bucket_id IN (").append(MediaSetUtils.getCameraBucketId()).append(" , ").append(GalleryStorageManager.getInstance().getOuterGalleryStorageCameraBucketIDs());
        if (GalleryUtils.isScreenRecorderExist()) {
            str = "," + MediaSetUtils.getScreenshotsBucketID() + ", " + GalleryStorageManager.getInstance().getOuterGalleryStorageScreenshotsBucketIDs();
        } else {
            str = "";
        }
        String bucketSet = append.append(str).append(") OR ").append("bucket_id IN (SELECT DISTINCT bucket_id FROM files WHERE title='.outside') OR ").append("( (storage_id not in (0,65537) AND ").append(LocalMediaSetAlbum.getBlackClause()).append(") OR bucket_id IN (").append(WhiteList.getBucketIdForWhiteList()).append(" ) ) )").append(" AND bucket_id NOT IN  (SELECT DISTINCT bucket_id FROM files WHERE title='.hidden' OR title='.inside') ) ").toString();
        this.mTimeLatLng.initWhereClause(bucketSet, "substr(_data, 1, length(_data) - length('000.JPG')) NOT IN (SELECT substr(_data, 1, length(_data) - length('000_COVER.JPG')) FROM files " + (ApiHelper.HAS_MEDIA_COLUMNS_IS_HW_BURST ? "indexed by is_hw_burst_index " : "") + "WHERE media_type = 1 AND " + bucketSet + " AND " + GalleryUtils.getBurstQueryClause() + ")");
    }

    protected void initWhereClause() {
        initLocalClause();
        this.mQueryClause = "media_type=1 AND " + this.mTimeLatLng.mWhereClauseSet;
        this.mDeleteClause = "media_type=1 AND " + this.mTimeLatLng.mWhereClauseDeleteSet;
        this.mQueryClauseGroup = this.mTimeLatLng.mQueryClauseGroup;
    }

    public void reset() {
        initWhereClause();
    }
}
