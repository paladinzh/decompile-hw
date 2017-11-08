package com.android.gallery3d.data;

import android.net.Uri;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import java.io.Closeable;
import java.util.ArrayList;

public class LocalBucketAlbum extends LocalBucketMediaSet {
    private static final String[] COUNT_PROJECTION = new String[]{"count(*)", "SUM((CASE WHEN media_type=3 THEN 1 ELSE 0 END))"};
    private static final Uri[] WATCH_URIS = new Uri[]{Media.EXTERNAL_CONTENT_URI, Video.Media.EXTERNAL_CONTENT_URI};
    private static final String WHERE_CLAUSE_BUCKET_BURST;
    private final ChangeNotifier mNotifier;
    private final ChangeNotifier mNotifierSettings;
    private final String mOrderClause;
    private final ReloadNotifier mReloader;

    static {
        String str;
        StringBuilder append = new StringBuilder().append("media_type IN (1,3) AND bucket_id=? AND substr(_data, 1, length(_data) - length('000.JPG')) NOT IN ( SELECT substr(_data, 1, length(_data) - length('000_COVER.JPG')) ");
        if (ApiHelper.HAS_MEDIA_COLUMNS_IS_HW_BURST) {
            str = "FROM files indexed by is_hw_burst_index WHERE media_type=1 AND " + GalleryUtils.getBurstQueryClause() + " AND " + "bucket_id" + "=?)";
        } else {
            str = "FROM images WHERE bucket_id=? and " + GalleryUtils.getBurstQueryClause() + ")";
        }
        WHERE_CLAUSE_BUCKET_BURST = append.append(str).toString();
    }

    public LocalBucketAlbum(Path path, GalleryApp application, int bucketId, String name) {
        super(path, application, bucketId, name);
        this.mOrderClause = "datetaken DESC, _id DESC";
        this.mBaseUri = EXTERNAL_FILE_URI;
        this.mNotifier = new ChangeNotifier((MediaSet) this, WATCH_URIS, application);
        this.mNotifierSettings = new ChangeNotifier((MediaSet) this, Constant.SETTIGNS_URI, application);
        this.mReloader = new ReloadNotifier(this, Constant.RELOAD_URI_ALBUM, application);
    }

    public LocalBucketAlbum(Path path, GalleryApp application, int bucketId) {
        this(path, application, bucketId, BucketHelper.getBucketName(application.getContentResolver(), bucketId));
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        GalleryUtils.assertNotInRenderThread();
        long startTime = System.currentTimeMillis();
        DataManager dataManager = this.mApplication.getDataManager();
        ArrayList<MediaItem> list = new ArrayList();
        Uri uri = MediaSet.decorateQueryExternalFileUri(EXTERNAL_FILE_URI, this.mOrderClause, start, count);
        Closeable closeable = null;
        try {
            closeable = this.mResolver.query(uri, PROJECTION, WHERE_CLAUSE_BUCKET_BURST, new String[]{String.valueOf(this.mBucketId), String.valueOf(this.mBucketId)}, this.mOrderClause);
            if (closeable == null) {
                GalleryLog.w("LocalBucketMediaAlbum", "query fail: " + uri);
                printExcuteInfo(startTime, "getMediaItem");
                return list;
            }
            while (closeable.moveToNext()) {
                list.add(LocalMergeQuerySet.loadOrUpdateItem(closeable, dataManager, this.mApplication));
            }
            Utils.closeSilently(closeable);
            printExcuteInfo(startTime, "getMediaItem");
            return list;
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("LocalBucketMediaAlbum");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getMediaItemCount() {
        long startTime = System.currentTimeMillis();
        if (this.mCachedCount == -1 || this.mCachedVideoCount == -1) {
            Closeable closeable = null;
            try {
                closeable = this.mResolver.query(EXTERNAL_FILE_URI, COUNT_PROJECTION, WHERE_CLAUSE_BUCKET_BURST, new String[]{String.valueOf(this.mBucketId), String.valueOf(this.mBucketId)}, null);
                if (closeable == null || closeable.getCount() == 0) {
                    GalleryLog.w("LocalBucketMediaAlbum", "query fail");
                    printExcuteInfo(startTime, "getMediaItemCount");
                    Utils.closeSilently(closeable);
                    Utils.closeSilently(closeable);
                    return 0;
                } else if (closeable.moveToNext()) {
                    this.mCachedCount = closeable.getInt(0);
                    this.mCachedVideoCount = closeable.getInt(1);
                    Utils.closeSilently(closeable);
                } else {
                    Utils.closeSilently(closeable);
                    return 0;
                }
            } catch (SecurityException e) {
                GalleryLog.noPermissionForMediaProviderLog("LocalBucketMediaAlbum");
                return 0;
            } catch (Exception e2) {
                GalleryLog.w("LocalBucketMediaAlbum", "move to next fail." + e2.getMessage());
                return 0;
            } catch (Throwable th) {
                Utils.closeSilently(closeable);
            }
        }
        printExcuteInfo(startTime, "getMediaItemCount");
        return this.mCachedCount;
    }

    public long reload() {
        if (((this.mNotifier.isDirty() | this.mNotifierSettings.isDirty()) | this.mReloader.isDirty()) != 0) {
            this.mDataVersion = MediaObject.nextVersionNumber();
            invalidCachedCount();
        }
        return this.mDataVersion;
    }

    protected String getDeleteClause() {
        return "media_type IN (1,3) AND bucket_id=?";
    }

    public Uri getContentUri() {
        return Files.getContentUri("external").buildUpon().appendQueryParameter("bucketId", String.valueOf(this.mBucketId)).build();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected int getHintIndex(Path path, int hint) {
        MediaObject mediaObject = this.mApplication.getDataManager().getMediaObject(path);
        if (!(mediaObject instanceof MediaItem)) {
            return hint;
        }
        MediaItem mediaItem = (MediaItem) mediaObject;
        Closeable closeable = null;
        try {
            closeable = this.mResolver.query(EXTERNAL_FILE_URI, COUNT_PROJECTION, "datetaken >= ? AND " + WHERE_CLAUSE_BUCKET_BURST, new String[]{String.valueOf(mediaItem.getDateInMs()), String.valueOf(this.mBucketId), String.valueOf(this.mBucketId)}, null);
            if (closeable == null || closeable.getCount() == 0) {
                GalleryLog.w("LocalBucketMediaAlbum", "query fail");
                Utils.closeSilently(closeable);
                Utils.closeSilently(closeable);
                return hint;
            } else if (closeable.moveToNext()) {
                int count = closeable.getInt(0);
                Utils.closeSilently(closeable);
                return count;
            } else {
                Utils.closeSilently(closeable);
                return hint;
            }
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("LocalBucketMediaAlbum");
            return hint;
        } catch (Exception e2) {
            GalleryLog.w("LocalBucketMediaAlbum", "move to next fail." + e2.getMessage());
            return hint;
        } catch (Throwable th) {
            Utils.closeSilently(closeable);
        }
    }
}
