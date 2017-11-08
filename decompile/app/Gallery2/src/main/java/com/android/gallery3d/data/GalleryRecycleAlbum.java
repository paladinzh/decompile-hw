package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.media.LocalRecycledFile;
import com.huawei.gallery.media.database.MergedMedia;
import com.huawei.gallery.recycle.utils.CloudRecycleUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.util.ArrayList;

public class GalleryRecycleAlbum extends GalleryMediaSetBase {
    private static final MyPrinter LOG = new MyPrinter("GalleryRecycleAlbum");
    private static final String[] PROJECTION = GalleryRecycleImage.copyProjection();
    public static final Uri URI = MergedMedia.URI.buildUpon().appendPath("gallery_recycled_file").build();
    private final Uri[] WATCH_URIS = new Uri[]{LocalRecycledFile.URI, CloudRecycleUtils.CLOUD_RECYCLED_FILE_TABLE_URI};
    private final GalleryApp mApplication;
    private final String mExcludeBurstNotCoverInSet;
    private boolean mIsCloudAutoUploadSwitchOpen = false;
    private boolean mIsDataDirty = false;
    private final ChangeNotifier mNotifier;
    private final ContentResolver mResolver;

    public GalleryRecycleAlbum(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mResolver = application.getContentResolver();
        this.mNotifier = new ChangeNotifier((MediaSet) this, this.WATCH_URIS, application);
        this.mExcludeBurstNotCoverInSet = "(substr(_source_display_name, 1, length(_source_display_name) - length('000.JPG')) NOT IN (SELECT substr(_source_display_name, 1, length(_source_display_name) - length('000_COVER.JPG'))FROM gallery_recycled_file WHERE " + GalleryUtils.getBurstQueryClause() + ")" + " OR " + "substr(_display_name, 5, length(_display_name) - length('000.JPG') - 4) NOT IN " + "(SELECT substr(_display_name, 5, length(_display_name) - length('000_COVER.JPG') - 4)" + " FROM gallery_recycled_file WHERE " + GalleryUtils.getBurstQueryClause() + "))";
    }

    protected static MediaItem loadOrUpdateItem(Cursor cursor, DataManager dataManager, GalleryApp app) {
        GalleryMediaItem item;
        synchronized (DataManager.LOCK) {
            int mediaType = cursor.getInt(24);
            int fileType = cursor.getInt(28);
            if (mediaType == 0) {
                mediaType = fileType == 4 ? 3 : 1;
            }
            boolean isImage = mediaType == 1;
            Path path = (isImage ? GalleryRecycleImage.IMAGE_PATH : GalleryRecycleVideo.VIDEO_PATH).getChild(RecycleUtils.mergeRecycleItemPathId(cursor.getInt(20), cursor.getInt(21)));
            item = (GalleryMediaItem) dataManager.peekMediaObject(path);
            if (item != null) {
                item.updateContent(cursor);
            } else if (isImage) {
                item = new GalleryRecycleImage(path, app, cursor);
            } else {
                item = new GalleryRecycleVideo(path, app, cursor);
            }
        }
        return item;
    }

    public boolean isVirtual() {
        return true;
    }

    public String getName() {
        return this.mApplication.getResources().getString(R.string.list_recentlydeletedalbum);
    }

    public ArrayList<MediaItem> getTotalWaitClearMediaItem(int start, int count, long serverTime) {
        String where;
        long localMinTime = System.currentTimeMillis() - 2592000000L;
        String isLocalSet = "cloud_media_id = -1";
        if (serverTime > 0) {
            long serverMinTime = serverTime - 2592000000L;
            where = "(" + isLocalSet + " AND " + "recycledTime" + " < " + String.valueOf(localMinTime) + ")" + "OR" + "(" + "cloud_media_id != -1" + " AND " + "recycledTime" + " < " + String.valueOf(serverMinTime) + ")";
        } else {
            where = "(" + isLocalSet + " AND " + "recycledTime" + " < " + String.valueOf(localMinTime) + ")";
        }
        return getMediaItem(start, count, where, null);
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count, String where, String[] selectArgs) {
        GalleryUtils.assertNotInRenderThread();
        DataManager dataManager = this.mApplication.getDataManager();
        ArrayList<MediaItem> list = new ArrayList();
        Uri uri = MediaSet.decorateQueryExternalFileUri(URI, "recycledTime DESC, _id DESC", start, count);
        Closeable closeable = null;
        try {
            closeable = this.mResolver.query(uri, PROJECTION, where, selectArgs, "recycledTime DESC, _id DESC");
            if (closeable == null) {
                LOG.w("query fail: " + uri);
                return list;
            }
            while (closeable.moveToNext()) {
                list.add(loadOrUpdateItem(closeable, dataManager, this.mApplication));
            }
            Utils.closeSilently(closeable);
            return list;
        } catch (SecurityException e) {
            LOG.w("No permission to query!");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        return getMediaItem(start, count, this.mExcludeBurstNotCoverInSet, null);
    }

    private int getCount(String whereClause) {
        Closeable closeable = null;
        int i;
        try {
            if (!this.mIsCloudAutoUploadSwitchOpen) {
                whereClause = "local_media_id !=-1 AND " + whereClause;
            }
            i = this.mResolver;
            closeable = i.query(URI, new String[]{"count(1)"}, whereClause, null, null);
            if (closeable != null) {
                i = closeable.moveToNext();
                if (i != null) {
                    i = closeable.getInt(0);
                    return i;
                }
            }
            MyPrinter myPrinter = LOG;
            i = new StringBuilder();
            myPrinter.w(i.append("error cursor in GalleryRecycleAlbum getCount : ").append(closeable == null ? "empty cursor" : "cursor move to next fail").toString());
            Utils.closeSilently(closeable);
            return 0;
        } catch (SecurityException e) {
            return 0;
        } catch (Exception e2) {
            i = LOG;
            i.w("query count fail." + e2.getMessage());
            return 0;
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public synchronized int getTotalVideoCount() {
        if (this.mCachedVideoCount == -1) {
            this.mCachedVideoCount = getCount("media_type != 1 OR media_type is NULL AND fileType = 4");
        }
        return this.mCachedVideoCount;
    }

    public synchronized int getMediaItemCount() {
        if (this.mCachedCount == -1) {
            this.mCachedCount = getCount(this.mExcludeBurstNotCoverInSet);
        }
        return this.mCachedCount;
    }

    public void reset() {
        this.mIsDataDirty = true;
    }

    public long reload() {
        boolean isCloudChanged = false;
        if (this.mIsCloudAutoUploadSwitchOpen != CloudSwitchHelper.isCloudAutoUploadSwitchOpen()) {
            isCloudChanged = true;
            this.mIsCloudAutoUploadSwitchOpen = CloudSwitchHelper.isCloudAutoUploadSwitchOpen();
        }
        synchronized (this) {
            if (this.mNotifier.isDirty() || isCloudChanged || this.mIsDataDirty) {
                this.mDataVersion = MediaObject.nextVersionNumber();
                this.mCachedCount = -1;
                this.mCachedVideoCount = -1;
                this.mIsDataDirty = false;
            }
        }
        return this.mDataVersion;
    }

    public String getLabel() {
        return "recycle";
    }
}
