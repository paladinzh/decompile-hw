package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.Closeable;
import java.util.ArrayList;

public class LocalAlbum extends LocalBucketMediaSet {
    private static final String[] COUNT_PROJECTION = new String[]{"count(*)"};
    private static final String WHERE_CLAUSE_BUCKET_BURST = ("bucket_id = ? AND substr(_data, 1, length(_data) - length('000.JPG')) NOT IN ( SELECT substr(_data, 1, length(_data) - length('000_COVER.JPG')) FROM images WHERE " + GalleryUtils.getBurstQueryClause() + ")");
    private final boolean mIsImage;
    private final Path mItemPath;
    private final ChangeNotifier mMoveInOutNotifier;
    private final ChangeNotifier mNotifier;
    private final String mOrderClause;
    private final String[] mProjection;
    private int mReloadType;
    private final ReloadNotifier mReloader;
    private String mWhereClause;
    private final String mWhereDeleteClause;

    public LocalAlbum(Path path, GalleryApp application, int bucketId, boolean isImage, String name) {
        super(path, application, bucketId, name);
        this.mReloadType = 6;
        this.mIsImage = isImage;
        this.mWhereDeleteClause = "bucket_id = ?";
        if (isImage) {
            determineWhereClause();
            this.mOrderClause = "datetaken DESC, _id DESC";
            this.mBaseUri = Media.EXTERNAL_CONTENT_URI;
            this.mProjection = LocalImage.PROJECTION;
            this.mItemPath = LocalImage.ITEM_PATH;
        } else {
            this.mWhereClause = "bucket_id = ?";
            this.mOrderClause = "datetaken DESC, _id DESC";
            this.mBaseUri = Video.Media.EXTERNAL_CONTENT_URI;
            this.mProjection = LocalVideo.PROJECTION;
            this.mItemPath = LocalVideo.ITEM_PATH;
        }
        this.mNotifier = new ChangeNotifier((MediaSet) this, this.mBaseUri, application);
        this.mMoveInOutNotifier = new ChangeNotifier((MediaSet) this, Constant.MOVE_OUT_IN_URI, application);
        this.mReloader = new ReloadNotifier(this, Constant.RELOAD_URI_ALBUM, application);
    }

    public LocalAlbum(Path path, GalleryApp application, int bucketId, boolean isImage) {
        this(path, application, bucketId, isImage, BucketHelper.getBucketName(application.getContentResolver(), bucketId));
    }

    public Uri getContentUri() {
        if (this.mIsImage) {
            return Media.EXTERNAL_CONTENT_URI.buildUpon().appendQueryParameter("bucketId", String.valueOf(this.mBucketId)).build();
        }
        return Video.Media.EXTERNAL_CONTENT_URI.buildUpon().appendQueryParameter("bucketId", String.valueOf(this.mBucketId)).build();
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        long startTime = System.currentTimeMillis();
        if (isInvalid()) {
            return new ArrayList(0);
        }
        DataManager dataManager = this.mApplication.getDataManager();
        Uri uri = this.mBaseUri.buildUpon().appendQueryParameter("limit", start + "," + count).build();
        ArrayList<MediaItem> list = new ArrayList();
        GalleryUtils.assertNotInRenderThread();
        determineWhereClause();
        Closeable closeable = null;
        try {
            closeable = this.mResolver.query(uri, this.mProjection, this.mWhereClause, new String[]{String.valueOf(this.mBucketId)}, this.mOrderClause);
            if (closeable == null) {
                GalleryLog.w("LocalAlbum", "query fail: " + uri);
                printExcuteInfo(startTime, "getMediaItem");
                return list;
            }
            while (closeable.moveToNext()) {
                ArrayList<MediaItem> arrayList = list;
                arrayList.add(loadOrUpdateItem(this.mItemPath.getChild(closeable.getInt(0)), closeable, dataManager, this.mApplication, this.mIsImage));
            }
            Utils.closeSilently(closeable);
            printExcuteInfo(startTime, "getMediaItem");
            return list;
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("LocalAlbum");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    private static MediaItem loadOrUpdateItem(Path path, Cursor cursor, DataManager dataManager, GalleryApp app, boolean isImage) {
        LocalMediaItem item;
        synchronized (DataManager.LOCK) {
            item = (LocalMediaItem) dataManager.peekMediaObject(path);
            if (item != null) {
                item.updateContent(cursor);
            } else if (isImage) {
                item = new LocalImage(path, app, cursor);
            } else {
                item = new LocalVideo(path, app, cursor);
            }
        }
        return item;
    }

    @SuppressWarnings({"NP_LOAD_OF_KNOWN_NULL_VALUE"})
    public static MediaItem[] getMediaItemById(GalleryApp application, boolean isImage, ArrayList<Integer> ids) {
        MediaItem[] result = new MediaItem[ids.size()];
        if (ids.isEmpty()) {
            return result;
        }
        Uri baseUri;
        String[] projection;
        Path itemPath;
        int idLow = ((Integer) ids.get(0)).intValue();
        int idHigh = ((Integer) ids.get(ids.size() - 1)).intValue();
        if (isImage) {
            baseUri = Media.EXTERNAL_CONTENT_URI;
            projection = LocalImage.PROJECTION;
            itemPath = LocalImage.ITEM_PATH;
        } else {
            baseUri = Video.Media.EXTERNAL_CONTENT_URI;
            projection = LocalVideo.PROJECTION;
            itemPath = LocalVideo.ITEM_PATH;
        }
        ContentResolver resolver = application.getContentResolver();
        DataManager dataManager = application.getDataManager();
        Closeable closeable = null;
        try {
            closeable = resolver.query(baseUri, projection, "_id BETWEEN ? AND ?", new String[]{String.valueOf(idLow), String.valueOf(idHigh)}, "_id");
            if (closeable == null) {
                GalleryLog.w("LocalAlbum", "query fail" + baseUri);
                return result;
            }
            int n = ids.size();
            int i = 0;
            while (i < n && closeable.moveToNext()) {
                int id = closeable.getInt(0);
                while (((Integer) ids.get(i)).intValue() < id) {
                    i++;
                    if (i >= n) {
                        Utils.closeSilently(closeable);
                        return result;
                    }
                }
                if (((Integer) ids.get(i)).intValue() <= id) {
                    result[i] = loadOrUpdateItem(itemPath.getChild(id), closeable, dataManager, application, isImage);
                    i++;
                }
            }
            Utils.closeSilently(closeable);
            return result;
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("LocalAlbum");
            return result;
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static Cursor getItemCursor(ContentResolver resolver, Uri uri, String[] projection, int id) {
        return resolver.query(uri, projection, "_id=?", new String[]{String.valueOf(id)}, null);
    }

    public static Cursor getItemCursor(ContentResolver resolver, Uri uri, String[] projection, String where) {
        return resolver.query(uri, projection, where, null, null);
    }

    public int getMediaItemCount() {
        long startTime = System.currentTimeMillis();
        if (isInvalid()) {
            return 0;
        }
        if (this.mCachedCount == -1) {
            determineWhereClause();
            Closeable closeable = null;
            try {
                closeable = this.mResolver.query(this.mBaseUri, COUNT_PROJECTION, this.mWhereClause, new String[]{String.valueOf(this.mBucketId)}, null);
                if (closeable == null) {
                    GalleryLog.w("LocalAlbum", "query fail");
                    printExcuteInfo(startTime, "getMediaItemCount");
                    return 0;
                }
                Utils.assertTrue(closeable.moveToNext());
                this.mCachedCount = closeable.getInt(0);
                Utils.closeSilently(closeable);
            } catch (SecurityException e) {
                GalleryLog.noPermissionForMediaProviderLog("LocalAlbum");
                return 0;
            } catch (Exception e2) {
                GalleryLog.w("LocalAlbum", "query item count fail." + e2.getMessage());
                return 0;
            } finally {
                Utils.closeSilently(closeable);
            }
        }
        printExcuteInfo(startTime, "getMediaItemCount");
        return this.mCachedCount;
    }

    public int getTotalVideoCount() {
        if (this.mIsImage) {
            return 0;
        }
        return getMediaItemCount();
    }

    private void determineWhereClause() {
        if ((this.mReloadType & FragmentTransaction.TRANSIT_EXIT_MASK) != 0) {
            this.mWhereClause = "bucket_id = ?";
        } else {
            this.mWhereClause = WHERE_CLAUSE_BUCKET_BURST;
        }
    }

    public long reload() {
        boolean reloadFlag = this.mReloader.isDirty();
        if (reloadFlag) {
            this.mReloadType = this.mReloader.getReloadType();
        }
        if (((this.mNotifier.isDirty() | this.mMoveInOutNotifier.isDirty()) | reloadFlag) != 0) {
            this.mDataVersion = MediaObject.nextVersionNumber();
            invalidCachedCount();
        }
        return this.mDataVersion;
    }

    private boolean isInvalid() {
        if (this.mIsImage && (this.mReloadType & 2) == 0) {
            return true;
        }
        if (this.mIsImage || (this.mReloadType & 4) != 0) {
            return false;
        }
        return true;
    }

    protected String getDeleteClause() {
        return this.mWhereDeleteClause;
    }
}
