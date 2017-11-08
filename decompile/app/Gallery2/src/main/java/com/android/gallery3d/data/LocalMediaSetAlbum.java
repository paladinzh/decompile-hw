package com.android.gallery3d.data;

import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.util.BlackList;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.WhiteList;
import com.huawei.gallery.storage.GalleryStorageManager;

public class LocalMediaSetAlbum extends LocalMediaAlbum {
    private boolean mIsQuickMode = true;

    public LocalMediaSetAlbum(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber(), application);
    }

    protected void initLocalClause() {
        String str;
        StringBuilder append = new StringBuilder().append("((bucket_id IN (").append(MediaSetUtils.getCameraBucketId()).append(" , ").append(GalleryStorageManager.getInstance().getOuterGalleryStorageCameraBucketIDs());
        if (GalleryUtils.isScreenRecorderExist()) {
            str = "," + MediaSetUtils.getScreenshotsBucketID() + ", " + GalleryStorageManager.getInstance().getOuterGalleryStorageScreenshotsBucketIDs();
        } else {
            str = "";
        }
        String bucketSet = append.append(str).append(") OR ").append("bucket_id IN (SELECT DISTINCT bucket_id FROM files WHERE title='.outside') OR ").append("( (storage_id not in (0,65537) AND ").append(getBlackClause()).append(") OR bucket_id IN (").append(WhiteList.getBucketIdForWhiteListWithoutPreLoadedPath()).append(" ) ) )").append(" AND bucket_id NOT IN  (SELECT DISTINCT bucket_id FROM files WHERE title='.hidden' OR title='.inside') ) ").toString();
        this.mTimeLatLng.initWhereClause(bucketSet, "substr(_data, 1, length(_data) - length('000.JPG')) NOT IN (SELECT substr(_data, 1, length(_data) - length('000_COVER.JPG')) FROM files " + (ApiHelper.HAS_MEDIA_COLUMNS_IS_HW_BURST ? "indexed by is_hw_burst_index " : "") + "WHERE media_type = 1 AND " + bucketSet + " AND " + GalleryUtils.getBurstQueryClause() + ")");
    }

    protected void initWhereClause() {
        initLocalClause();
        this.mQueryClause = "media_type IN (1,3) AND " + this.mTimeLatLng.mWhereClauseSet;
        this.mDeleteClause = "media_type IN (1,3) AND " + this.mTimeLatLng.mWhereClauseDeleteSet;
        this.mQueryClauseGroup = this.mTimeLatLng.mQueryClauseGroup;
    }

    public void reset() {
        WhiteList.getBucketIdForWhiteListWithoutPreLoadedPath(true);
        initWhereClause();
    }

    public void setQuickMode(boolean enable) {
        this.mIsDataDirty = this.mIsQuickMode != enable;
        this.mIsQuickMode = enable;
    }

    public String getName() {
        return null;
    }

    private static String getInStringBlackClause() {
        return " bucket_id NOT IN (" + BlackList.getInstance().getOuterVolumeBucketId() + ")";
    }

    public boolean isQuickMode() {
        return this.mIsQuickMode;
    }

    protected String getQuickClause() {
        if (this.mIsQuickMode) {
            return " _id IN (select _id from files  where media_type IN (1,3) ORDER BY datetaken DESC limit 0, 500)  AND ";
        }
        return super.getQuickClause();
    }

    protected static String getBlackClause() {
        return getInStringBlackClause();
    }
}
