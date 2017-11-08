package com.huawei.gallery.media.services;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Message;
import android.text.TextUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.GalleryRecycleAlbum;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.media.database.CloudTableOperateHelper;
import com.huawei.gallery.media.database.MergedMedia;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.service.AsyncService;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StorageService extends AsyncService {
    private static final Uri GALLERY_MEDIA_NO_NOTIFY_URI = GalleryMedia.URI.buildUpon().appendQueryParameter("nonotify", "1").build();
    private static final MyPrinter LOG = new MyPrinter("StorageService");
    private static final String[] PROJECTION = new String[]{"_id", "cloud_media_id", "_data", "hash", "cloud_bucket_id", "localThumbPath", "localBigThumbPath"};
    public static final Uri sCloudFileRecycleUri = MergedMedia.URI.buildUpon().appendPath("cloud_recycled_file").build();
    public static final Uri sCloudFileUri = MergedMedia.URI.buildUpon().appendPath("cloud_file").build();

    static class FileInfo {
        private String cloudBucketId;
        private int cloudMediaId = -1;
        private String data;
        private String hash;
        private int id;
        private String localBigThumbPath;
        private String localThumbPath;

        FileInfo(Cursor c) {
            this.id = c.getInt(0);
            this.cloudMediaId = c.getInt(1);
            this.data = c.getString(2);
            this.hash = c.getString(3);
            this.cloudBucketId = c.getString(4);
            this.localThumbPath = c.getString(5);
            this.localBigThumbPath = c.getString(6);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean handleMessage(Message msg) {
        try {
            if (PhotoShareUtils.isSupportPhotoShare()) {
                switch (msg.what) {
                    case 1:
                        updateFileInfo((Intent) msg.obj);
                        break;
                    case 2:
                        LOG.d("precess pre check storage info.");
                        this.mServiceHandler.removeMessages(5);
                        this.mServiceHandler.removeMessages(3);
                        this.mServiceHandler.sendEmptyMessageDelayed(3, 10000);
                        break;
                    case 3:
                        checkStorageUseage();
                        break;
                    case 4:
                        trimLCDThumb();
                        break;
                    case 5:
                        if (!this.mServiceHandler.hasMessages(3)) {
                            LOG.d("Service will stop.");
                            stopSelf();
                            break;
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }
            LOG.d("don't support photoshare");
            return false;
        } catch (Exception e) {
            LOG.d("exception when handle message. ", e);
        }
    }

    private void updateFileInfo(Intent intent) {
        this.mServiceHandler.removeMessages(5);
        String filePath = intent.getStringExtra("file-name");
        ContentValues values = new ContentValues();
        values.put("visit_time", Long.valueOf(intent.getLongExtra("visit-time", System.currentTimeMillis())));
        getContentResolver().update(GALLERY_MEDIA_NO_NOTIFY_URI, values, "_data = ?", new String[]{filePath});
        this.mServiceHandler.sendEmptyMessageDelayed(5, 5000);
    }

    private void checkStorageUseage() {
        this.mServiceHandler.removeMessages(5);
        int lcdCount = queryLCDCount();
        int maxCountLimit = PhotoShareUtils.sMaxLcdThumbCount;
        LOG.d("[checkStorageUseage] total LCD count: " + lcdCount + ", max count limit: " + maxCountLimit);
        if (lcdCount > maxCountLimit) {
            trimLCDThumb();
        }
        this.mServiceHandler.sendEmptyMessageDelayed(5, 5000);
    }

    private void trimLCDThumb() {
        int totalLcdCount = queryLCDCount();
        int defaultCount = Math.max(0, 2000);
        int maxCountLimit = Math.max(defaultCount, PhotoShareUtils.sMaxLcdThumbCount);
        if (totalLcdCount <= maxCountLimit) {
            LOG.d("[trimLCDThumb] total lcd count: " + totalLcdCount + ", max count limit: " + maxCountLimit);
            return;
        }
        LOG.d("[trimLCDThumb] default count: " + defaultCount + ", max count limit: " + maxCountLimit + ", lcd count: " + totalLcdCount);
        printCurrentLCDUseage("trim LCD start");
        trimGalleryMediaFileLCDThumb(defaultCount, maxCountLimit);
        if (RecycleUtils.supportRecycle()) {
            trimGalleryRecycleFileLCDThumb(defaultCount / 10);
        }
        printCurrentLCDUseage("trim LCD end");
    }

    private void trimGalleryMediaFileLCDThumb(int defaultCount, int maxCountLimit) {
        ContentResolver resolver = getContentResolver();
        String latestIdClause = "(SELECT _id FROM gallery_media ORDER BY showDateToken DESC LIMIT 0," + defaultCount + ")";
        int latestLcdCount = queryLatestLcdCount(resolver, latestIdClause);
        LOG.d("latest " + latestLcdCount);
        String excludeLatestClause = "_id NOT IN " + latestIdClause;
        Uri queryUri = GalleryMedia.URI.buildUpon().appendQueryParameter("limit", (Math.max(maxCountLimit - PhotoShareUtils.sTrimThumbCount, defaultCount) - latestLcdCount) + "," + 500).build();
        List<FileInfo> toBeDelete;
        do {
            toBeDelete = queryFileInfoBatch(resolver, queryUri, excludeLatestClause);
            TraceController.beginSection("deleteLCDFile");
            deleteLCDFile(resolver, toBeDelete);
            TraceController.endSection();
        } while (!toBeDelete.isEmpty());
    }

    private void trimGalleryRecycleFileLCDThumb(int defaultCount) {
        ContentResolver resolver = getContentResolver();
        Uri queryUri = GalleryRecycleAlbum.URI.buildUpon().appendQueryParameter("limit", defaultCount + "," + 500).build();
        List<FileInfo> toBeDelete;
        do {
            toBeDelete = queryFileInfoBatch(resolver, queryUri, "1 = 1");
            TraceController.beginSection("deleteLCDFile");
            deleteLCDFile(resolver, toBeDelete);
            TraceController.endSection();
        } while (!toBeDelete.isEmpty());
    }

    private void deleteLCDFile(ContentResolver resolver, List<FileInfo> toBeDelete) {
        LOG.d("current batch count " + toBeDelete.size());
        if (!toBeDelete.isEmpty()) {
            for (FileInfo fileInfo : toBeDelete) {
                if (!(fileInfo.cloudMediaId == -1 || TextUtils.isEmpty(fileInfo.localBigThumbPath) || !new File(fileInfo.localBigThumbPath).delete())) {
                    LOG.d("delete: " + fileInfo.localBigThumbPath);
                    ContentValues cloudFileValues = new ContentValues();
                    cloudFileValues.put("localBigThumbPath", "");
                    resolver.update(sCloudFileUri, cloudFileValues, "id = ?", new String[]{String.valueOf(fileInfo.cloudMediaId)});
                    if (RecycleUtils.supportRecycle()) {
                        resolver.update(sCloudFileRecycleUri, cloudFileValues, "localBigThumbPath = ?", new String[]{String.valueOf(fileInfo.localBigThumbPath)});
                    }
                    ContentValues galleryMediaValues = new ContentValues();
                    galleryMediaValues.put("localBigThumbPath", "");
                    if (fileInfo.localBigThumbPath.equals(fileInfo.data)) {
                        String defaultPath = fileInfo.localThumbPath;
                        galleryMediaValues.put("thumbType", Integer.valueOf(1));
                        if (TextUtils.isEmpty(defaultPath)) {
                            defaultPath = CloudTableOperateHelper.genDefaultFilePath(fileInfo.cloudBucketId, fileInfo.hash);
                            galleryMediaValues.put("thumbType", Integer.valueOf(0));
                        }
                        galleryMediaValues.put("_data", defaultPath);
                    }
                    resolver.update(GalleryMedia.URI, galleryMediaValues, "_id = ? ", new String[]{String.valueOf(fileInfo.id)});
                }
            }
        }
    }

    private int queryLatestLcdCount(ContentResolver resolver, String latestIdSet) {
        int result = 0;
        try {
            Closeable cursor = resolver.query(GalleryMedia.URI, new String[]{"count(1)"}, "_id IN " + latestIdSet + " AND " + "localBigThumbPath IS NOT NULL AND localBigThumbPath !='' ", null, null);
            if (cursor != null && cursor.moveToNext()) {
                result = cursor.getInt(0);
            }
            Utils.closeSilently(cursor);
            return result;
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
    }

    private void printCurrentLCDUseage(String status) {
        try {
            LOG.d(status + " current LCD cache is: " + ((PhotoShareUtils.getServer().getPhotoThumbSize(2) / 1024) / 1024) + " M ");
        } catch (Exception e) {
            LOG.d("get thumb cache size failed", e);
        }
    }

    private List<FileInfo> queryFileInfoBatch(ContentResolver resolver, Uri uri, String excludeClause) {
        Throwable th;
        List<FileInfo> result = Collections.emptyList();
        Closeable cursor = null;
        try {
            cursor = resolver.query(uri, PROJECTION, excludeClause + " AND " + "localBigThumbPath IS NOT NULL AND localBigThumbPath !='' ", null, "visit_time DESC, showDateToken DESC ");
            if (cursor != null) {
                List<FileInfo> result2 = new ArrayList(cursor.getCount());
                while (cursor.moveToNext()) {
                    try {
                        result2.add(new FileInfo(cursor));
                    } catch (Throwable th2) {
                        th = th2;
                        result = result2;
                    }
                }
                result = result2;
            }
            Utils.closeSilently(cursor);
            return result;
        } catch (Throwable th3) {
            th = th3;
            Utils.closeSilently(cursor);
            throw th;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int queryLCDCount(Uri uri) {
        int count = 0;
        try {
            Uri uri2 = uri;
            Closeable c = getContentResolver().query(uri2, new String[]{"count(1)"}, "localBigThumbPath IS NOT NULL AND localBigThumbPath !='' ", null, null);
            if (c != null && c.moveToNext()) {
                count = c.getInt(0);
            }
            Utils.closeSilently(c);
        } catch (Exception e) {
            LOG.d("query count from cloud file failed.", e);
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return count;
    }

    private int queryLCDCount() {
        return (RecycleUtils.supportRecycle() ? queryLCDCount(GalleryRecycleAlbum.URI) : 0) + queryLCDCount(GalleryMedia.URI);
    }

    protected String getServiceTag() {
        return "StorageService-manage-storage-space-for-cloud-cache";
    }

    protected void decorateMsg(Message message, Intent intent, int startId) {
        String cmd = intent.getStringExtra("storage-service-cmd");
        if ("update-use-info".equals(cmd)) {
            message.what = 1;
            message.obj = intent;
        } else if ("check-storage".equals(cmd)) {
            message.what = 2;
        }
    }

    public static void checkStorageSpace() {
        Context context = GalleryUtils.getContext();
        if (context != null) {
            Intent intent = new Intent();
            intent.putExtra("storage-service-cmd", "check-storage");
            intent.setClass(context, StorageService.class);
            context.startService(intent);
        }
    }

    public static void updateVisitInfo(Context context, MediaItem item, int type) {
        if (type == 1) {
            Intent intent = new Intent();
            intent.putExtra("file-name", item.getFilePath());
            intent.putExtra("visit-time", System.currentTimeMillis());
            intent.putExtra("storage-service-cmd", "update-use-info");
            intent.setClass(context, StorageService.class);
            context.startService(intent);
        }
    }
}
