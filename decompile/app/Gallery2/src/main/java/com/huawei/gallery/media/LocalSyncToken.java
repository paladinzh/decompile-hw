package com.huawei.gallery.media;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.util.SparseArray;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.MediaSetUtils;
import com.huawei.gallery.media.database.MergedMedia;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.util.MediaSyncerHelper;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalSyncToken {
    private static String CURNT_FORMAT = "media_type IN (1,3) AND _id > %s AND bucket_id %s ( %s )";
    private static boolean DEBUG_DB = false;
    private static String LOCAL_FORMAT_BUCKET_ID = "local_media_id >=0 AND bucket_id %s ( %s )";
    private static MyPrinter LOG = new MyPrinter("LocalSyncToken");
    static final String[] PROJECTION = new String[]{"_id", "_data", "_size", "date_added", "date_modified", "mime_type", "title", "description", "_display_name", "orientation", "latitude", "longitude", "datetaken", "bucket_id", "bucket_display_name", "duration", "resolution", "media_type", "storage_id", "width", "height", "is_hdr", "is_hw_privacy", "hw_voice_offset", "is_hw_favorite", "hw_image_refocus", "is_hw_burst", "hw_rectify_offset", "special_file_type", "special_file_offset"};

    static {
        if (!ApiHelper.HAS_MEDIA_COLUMNS_SPECIAL_FILE_OFFSET) {
            PROJECTION[29] = "0 as special_file_offset";
        }
    }

    static void syncAll(ContentResolver resolver, int from, int batchSize) {
        long startTime = System.currentTimeMillis();
        GalleryStorageManager storageManager = GalleryStorageManager.getInstance();
        LOG.d("privilege buckets " + (MediaSetUtils.getCameraBucketId() + " , " + storageManager.getOuterGalleryStorageCameraBucketIDs() + " , " + MediaSetUtils.getScreenshotsBucketID() + ", " + storageManager.getOuterGalleryStorageScreenshotsBucketIDs()));
        LOG.d("sync privilege buckets from id " + queryMaxLocalMediaId(resolver, String.format(LOCAL_FORMAT_BUCKET_ID, new Object[]{"IN", sqlLocalBucketIds}), null));
        syncInner(resolver, batchSize, String.format(CURNT_FORMAT, new Object[]{Integer.valueOf(maxId), "IN", sqlLocalBucketIds}), null);
        LOG.d("sync common buckets from id " + queryMaxLocalMediaId(resolver, String.format(LOCAL_FORMAT_BUCKET_ID, new Object[]{"NOT IN", sqlLocalBucketIds}), null));
        syncInner(resolver, batchSize, String.format(CURNT_FORMAT, new Object[]{Integer.valueOf(maxId), "NOT IN", sqlLocalBucketIds}), null);
        LOG.d("sync all record cost time: " + (System.currentTimeMillis() - startTime));
    }

    private static void syncInner(ContentResolver resolver, int batchSize, String whereClause, String[] whereArgs) {
        int totalCount = queryCurrentMediaCount(resolver, whereClause, whereArgs);
        LOG.d(String.format("[syncInner] uri:%s, whereClause:%s, count: %d", new Object[]{null, whereClause, Integer.valueOf(totalCount)}));
        int start = 0;
        while (start < totalCount) {
            long startTime = System.currentTimeMillis();
            syncBatchInner(resolver, MergedMedia.FILES_URI.buildUpon().appendQueryParameter("limit", start + "," + batchSize).build(), whereClause, whereArgs);
            start += batchSize;
            synchronized (LocalSyncToken.class) {
                LOG.d("sync batch inner cost time: " + (System.currentTimeMillis() - startTime) + ", will wait some time.");
                Utils.waitWithoutInterrupt(LocalSyncToken.class, 500);
            }
        }
    }

    private static int queryMaxLocalMediaId(ContentResolver resolver, String whereClause, String[] whereArgs) {
        int count = 0;
        try {
            Closeable c = resolver.query(GalleryMedia.URI, new String[]{"max(local_media_id)"}, whereClause, whereArgs, null);
            if (c != null && c.moveToNext()) {
                count = c.getInt(0);
            }
            Utils.closeSilently(c);
            return count;
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
    }

    private static int queryCurrentMediaCount(ContentResolver resolver, String whereClause, String[] whereArgs) {
        int count = 0;
        try {
            if (MediaSyncerHelper.isMediaSyncerTerminated()) {
                return 0;
            }
            Closeable c = resolver.query(MergedMedia.FILES_URI, new String[]{"COUNT(1)"}, whereClause, whereArgs, null);
            if (c != null && c.moveToNext()) {
                count = c.getInt(0);
            }
            Utils.closeSilently(c);
            return count;
        } finally {
            Utils.closeSilently(null);
        }
    }

    private static void syncBatchInner(ContentResolver resolver, Uri uri, String whereClause, String[] whereArgs) {
        LOG.d(String.format("[syncBatchInner] uri:%s, whereClause:%s", new Object[]{uri, whereClause}));
        try {
            List<GalleryMedia> mediaDatas = queryCurrentMedia(resolver, uri, whereClause, whereArgs, "_id ASC");
            List<ContentValues> insertBulk = new ArrayList(mediaDatas.size());
            for (GalleryMedia galleryMedia : mediaDatas) {
                if (DEBUG_DB) {
                    LOG.w("insert id:" + galleryMedia.localMediaId + ", file path: " + galleryMedia.data);
                }
                insertLocalMediaItem(resolver, galleryMedia, insertBulk);
            }
            ContentValues[] values = new ContentValues[insertBulk.size()];
            insertBulk.toArray(values);
            resolver.bulkInsert(GalleryMedia.URI, values);
        } catch (Exception e) {
            LOG.w("insert bulk failed. " + e.getMessage());
        }
    }

    static void deleteAll(ContentResolver resolver, int batchSize) {
        long startTime = System.currentTimeMillis();
        LOG.d("delete all local only data[" + resolver.delete(GalleryMedia.URI, " local_media_id >= 0 and cloud_media_id < 0", null) + "]. cost time: " + (System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
        int totalGalleryMediaCount = 0;
        List<GalleryMedia> mediaDatas = queryGalleryMediaBatch(resolver, 0, batchSize);
        while (!mediaDatas.isEmpty()) {
            totalGalleryMediaCount += mediaDatas.size();
            for (GalleryMedia currentMedia : mediaDatas) {
                currentMedia.delete(resolver);
            }
            mediaDatas = queryGalleryMediaBatch(resolver, 0, batchSize);
        }
        LOG.d("delete all bunded data[" + totalGalleryMediaCount + "]. cost time: " + (System.currentTimeMillis() - startTime));
    }

    private static List<GalleryMedia> queryGalleryMediaBatch(ContentResolver resolver, int start, int batchSize) {
        Throwable th;
        Closeable closeable = null;
        try {
            List<GalleryMedia> list;
            closeable = GalleryMedia.query(resolver, start, batchSize, "local_media_id >= 0 AND cloud_media_id > 0", null, " _id ASC ");
            if (closeable != null) {
                List<GalleryMedia> result = new ArrayList(closeable.getCount());
                while (closeable.moveToNext()) {
                    try {
                        result.add(new GalleryMedia(closeable));
                    } catch (Throwable th2) {
                        th = th2;
                        list = result;
                    }
                }
                list = result;
            } else {
                list = new ArrayList();
            }
            Utils.closeSilently(closeable);
            return list;
        } catch (Throwable th3) {
            th = th3;
            Utils.closeSilently(closeable);
            throw th;
        }
    }

    private static GalleryMedia queryGalleryMediaByLocalMediaId(ContentResolver resolver, String localMediaId) {
        Closeable closeable = null;
        try {
            closeable = GalleryMedia.query(resolver, "local_media_id =?", new String[]{localMediaId}, null);
            if (closeable == null || !closeable.moveToNext()) {
                Utils.closeSilently(closeable);
                return null;
            }
            GalleryMedia galleryMedia = new GalleryMedia(closeable);
            return galleryMedia;
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    static int queryMaxSyncId(ContentResolver resolver) {
        int count = 0;
        try {
            ContentResolver contentResolver = resolver;
            Closeable c = contentResolver.query(GalleryMedia.URI, new String[]{"max(local_media_id)"}, "local_media_id >=0", null, null);
            if (c != null && c.moveToNext()) {
                count = c.getInt(0);
            }
            Utils.closeSilently(c);
            return count;
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
    }

    static boolean syncBatch(ContentResolver resolver, List<MediaOperation> operations) {
        StringBuffer selectionArg = new StringBuffer();
        for (MediaOperation op : operations) {
            if (!op.isStartToken()) {
                selectionArg.append(", ").append(op.dbId);
            }
        }
        if (selectionArg.length() <= 1) {
            LOG.w("there is no operation for media. " + selectionArg);
            return true;
        }
        List<GalleryMedia> mediaDatas = queryCurrentMedia(resolver, MergedMedia.FILES_URI, " _id in ( " + selectionArg.substring(1) + ") AND media_type IN (1,3) ", null, null);
        SparseArray<GalleryMedia> currentMediaArray = new SparseArray(mediaDatas.size());
        for (GalleryMedia galleryMedia : mediaDatas) {
            currentMediaArray.put(galleryMedia.localMediaId, galleryMedia);
        }
        LOG.d("syncBatch  query result count " + mediaDatas.size());
        for (MediaOperation operation : operations) {
            if (!MediaSyncerHelper.isMediaSyncerTerminated()) {
                GalleryMedia currentMedia = (GalleryMedia) currentMediaArray.get(operation.dbId);
                switch (operation.dbOp) {
                    case 1:
                        if (currentMedia == null) {
                            break;
                        }
                        try {
                            String data = queryDataFromGalleryMedia(resolver, operation.dbId);
                            if (data != null && data.equals(operation.dbData)) {
                                break;
                            }
                            if (data != null) {
                                LOG.e("There several record use the same local media id: " + operation.dbId);
                            }
                            insertLocalMediaItem(resolver, currentMedia, null);
                            break;
                        } catch (Exception e) {
                            LOG.w("insert new data error . " + currentMedia + ", " + e.getMessage());
                            break;
                        }
                    case 2:
                        if (currentMedia == null) {
                            break;
                        }
                        try {
                            if (!(currentMedia.size == 0 || !PhotoShareUtils.removeCameraPath(currentMedia.data) || PhotoShareUtils.getServer() == null)) {
                                PhotoShareUtils.notifyCloudUpload("default-album-1", currentMedia.data, "/DCIM/Camera", currentMedia.hash, currentMedia.mediaType);
                            }
                            resolver.update(GalleryMedia.URI, currentMedia.getValues(), " local_media_id = ? ", new String[]{String.valueOf(currentMedia.localMediaId)});
                            break;
                        } catch (Exception e2) {
                            LOG.w("there is something wrong when update. " + currentMedia + ", " + e2.getMessage());
                            break;
                        }
                    case 3:
                        if (currentMedia != null) {
                            LOG.e("deleted data appear is abnormal.");
                            break;
                        }
                        try {
                            GalleryMedia localMedia = queryGalleryMediaByLocalMediaId(resolver, String.valueOf(operation.dbId));
                            if (localMedia == null) {
                                LOG.e("can not find in Gallery_media or mediaProvider." + operation);
                                resolver.delete(GalleryMedia.URI, " local_media_id = " + operation.dbId, null);
                                break;
                            }
                            localMedia.delete(resolver);
                            break;
                        } catch (Exception e22) {
                            LOG.w("there is something wrong when delete. dbId " + operation.dbId + ", " + currentMedia + ", " + e22.getMessage());
                            break;
                        }
                    default:
                        LOG.w("illegal operation " + operation);
                        break;
                }
            }
            LOG.d("terminate syncBatch");
            return false;
        }
        return true;
    }

    private static String queryDataFromGalleryMedia(ContentResolver resolver, int localMediaId) {
        String data = null;
        try {
            ContentResolver contentResolver = resolver;
            Closeable c = contentResolver.query(GalleryMedia.URI, new String[]{"_data"}, "local_media_id = ?", new String[]{String.valueOf(localMediaId)}, null);
            if (c != null && c.moveToNext()) {
                data = c.getString(0);
            }
            Utils.closeSilently(c);
            return data;
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
    }

    static void insertLocalMediaItem(ContentResolver resolver, GalleryMedia galleryMedia, List<ContentValues> insertBulk) {
        if (insertBulk == null) {
            galleryMedia.insert(resolver);
        } else {
            insertBulk.add(galleryMedia.getValues());
        }
        if (!RecycleUtils.supportRecycle() || !PhotoShareUtils.isGUIDSupport()) {
            PhotoShareUtils.checkAutoUpload(-1, galleryMedia.data, galleryMedia.bucketId, galleryMedia.bucketRelativePath, galleryMedia.hash, galleryMedia.mediaType, galleryMedia.size == 0);
            PhotoShareUtils.deleteDeletedPhoto(PhotoShareUtils.getDeletedFileIdentify(galleryMedia.hash, galleryMedia.bucketRelativePath));
        }
    }

    private static List<GalleryMedia> queryCurrentMedia(ContentResolver resolver, Uri uri, String selectionClause, String[] selectArgs, String orderBy) {
        Throwable th;
        List<GalleryMedia> result = Collections.emptyList();
        Closeable cursor = null;
        try {
            if (MediaSyncerHelper.isMediaSyncerTerminated()) {
                Utils.closeSilently(cursor);
                return result;
            }
            cursor = resolver.query(uri, PROJECTION, selectionClause, selectArgs, orderBy);
            if (cursor == null) {
                Utils.closeSilently(cursor);
                return result;
            }
            List<GalleryMedia> result2 = new ArrayList(cursor.getCount());
            while (cursor.moveToNext()) {
                try {
                    if (MediaSyncerHelper.isMediaSyncerTerminated()) {
                        List<GalleryMedia> emptyList = Collections.emptyList();
                        Utils.closeSilently(cursor);
                        return emptyList;
                    }
                    GalleryMedia currentMedia = new GalleryMedia();
                    currentMedia.localMediaId = cursor.getInt(0);
                    currentMedia.data = cursor.getString(1);
                    currentMedia.size = cursor.getLong(2);
                    currentMedia.dateAdded = cursor.getLong(3);
                    currentMedia.dateModified = cursor.getLong(4);
                    currentMedia.mimeType = cursor.getString(5);
                    currentMedia.title = cursor.getString(6);
                    currentMedia.description = cursor.getString(7);
                    currentMedia.displayName = cursor.getString(8);
                    currentMedia.orientation = cursor.getInt(9);
                    currentMedia.latitude = cursor.getDouble(10);
                    currentMedia.longitude = cursor.getDouble(11);
                    currentMedia.datetaken = cursor.getLong(12);
                    currentMedia.bucketId = cursor.getString(13);
                    currentMedia.bucketDisplayName = cursor.getString(14);
                    currentMedia.duration = cursor.getInt(15);
                    currentMedia.resolution = cursor.getString(16);
                    currentMedia.mediaType = cursor.getInt(17);
                    currentMedia.storageId = cursor.getInt(18);
                    currentMedia.width = cursor.getInt(19);
                    currentMedia.height = cursor.getInt(20);
                    currentMedia.isHdr = cursor.getInt(21);
                    currentMedia.isHwPrivacy = cursor.getInt(22);
                    currentMedia.hwVoiceOffset = cursor.getInt(23);
                    currentMedia.isHwFavorite = cursor.getInt(24);
                    currentMedia.hwImageRefocus = cursor.getInt(25);
                    currentMedia.isHwBurst = cursor.getInt(26);
                    currentMedia.hwRectifyOffset = cursor.getInt(27);
                    currentMedia.specialFileType = cursor.getInt(28);
                    currentMedia.specialFileOffset = cursor.getLong(29);
                    parseResolution(currentMedia);
                    result2.add(currentMedia);
                } catch (Throwable th2) {
                    th = th2;
                    result = result2;
                }
            }
            Utils.closeSilently(cursor);
            return result2;
        } catch (Throwable th3) {
            th = th3;
            Utils.closeSilently(cursor);
            throw th;
        }
    }

    private static void parseResolution(GalleryMedia item) {
        if (item.mediaType == 3 && item.resolution != null && item.width <= 0 && item.height <= 0) {
            String[] resolutions = item.resolution.split("x");
            if (resolutions.length >= 2) {
                try {
                    int w = Integer.parseInt(resolutions[0]);
                    int h = Integer.parseInt(resolutions[1]);
                    item.width = w;
                    item.height = h;
                } catch (Throwable th) {
                    LOG.w("resolve resolution failed");
                }
            }
        }
    }
}
