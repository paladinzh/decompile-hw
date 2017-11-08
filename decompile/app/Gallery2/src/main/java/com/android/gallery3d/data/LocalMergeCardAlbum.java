package com.android.gallery3d.data;

import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.util.GalleryUtils;
import java.util.ArrayList;

public abstract class LocalMergeCardAlbum extends LocalMediaAlbum {
    private String mWhereClauseDeleteMergeCard;
    private String mWhereClauseMergeCardBucket;
    private String mWhereClauseMergeCardExternal;
    private String mWhereClauseMergeCardInternal;

    protected abstract String getExternalBucketID();

    protected abstract String getInternalBucketID();

    public LocalMergeCardAlbum(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber(), application);
    }

    protected void initWhereClause() {
        initMergeCardWhereClause();
        if (MediaObject.isImageTypeFromPath(getPath())) {
            this.mQueryClause = "media_type = 1 AND " + this.mWhereClauseMergeCardBucket;
        } else {
            this.mQueryClause = "media_type IN (1,3) AND " + this.mWhereClauseMergeCardBucket;
        }
        this.mDeleteClause = "media_type IN (1,3) AND " + this.mWhereClauseDeleteMergeCard;
    }

    public void reset() {
        initWhereClause();
    }

    protected void initMergeCardWhereClause() {
        String mergeCardBucket = "bucket_id IN (" + getMergeBucketID() + ") ";
        String excludeBurstNotCoverInMergeCardBucket = "substr(_data, 1, length(_data) - length('000.JPG')) NOT IN (SELECT substr(_data, 1, length(_data) - length('000_COVER.JPG')) FROM files " + (ApiHelper.HAS_MEDIA_COLUMNS_IS_HW_BURST ? "indexed by is_hw_burst_index " : "") + "WHERE media_type = 1 AND " + mergeCardBucket + " AND " + GalleryUtils.getBurstQueryClause() + ")";
        this.mWhereClauseMergeCardBucket = "datetaken > ? AND " + mergeCardBucket + " AND " + excludeBurstNotCoverInMergeCardBucket;
        this.mWhereClauseMergeCardInternal = "datetaken > ? AND " + excludeBurstNotCoverInMergeCardBucket + " AND " + "bucket_id in (" + getInternalMergeBucketID() + ")";
        this.mWhereClauseMergeCardExternal = "datetaken > ? AND " + excludeBurstNotCoverInMergeCardBucket + " AND " + "bucket_id in (" + getExternalBucketID() + ")";
        this.mWhereClauseDeleteMergeCard = "datetaken > ? AND " + mergeCardBucket;
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        return super.getMediaItem(start, count);
    }

    public int getMediaItemCount() {
        return super.getMediaItemCount();
    }

    public long reload() {
        if (needReset()) {
            resetData();
        }
        return this.mDataVersion;
    }

    public void delete() {
        super.delete();
    }

    public int getSupportedOperations() {
        return 1029;
    }

    public void filterMergeCardLocation(int locationType) {
        this.mIsCardLocationChanged = true;
        switch (locationType) {
            case 0:
                this.mQueryClause = "media_type IN (1,3) AND " + this.mWhereClauseMergeCardBucket;
                return;
            case 1:
                this.mQueryClause = "media_type IN (1,3) AND " + this.mWhereClauseMergeCardInternal;
                return;
            case 2:
                this.mQueryClause = "media_type IN (1,3) AND " + this.mWhereClauseMergeCardExternal;
                return;
            default:
                return;
        }
    }

    protected String getMergeBucketID() {
        return getInternalBucketID() + "," + getExternalBucketID();
    }

    protected String getInternalMergeBucketID() {
        return "" + getInternalBucketID();
    }
}
