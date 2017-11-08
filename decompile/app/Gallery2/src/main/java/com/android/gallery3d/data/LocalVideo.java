package com.android.gallery3d.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.provider.MediaStore.Video.Media;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.UpdateHelper;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.util.VideoEditorController;
import java.io.Closeable;

public class LocalVideo extends LocalMediaItem implements IVideo {
    public static final Path ITEM_PATH = Path.fromString("/local/video/item");
    static final String[] PROJECTION;
    public int durationInSec;

    public static class LocalVideoRequest extends ImageCacheRequest {
        private String mLocalFilePath;

        public /* bridge */ /* synthetic */ boolean hasBufferCache() {
            return super.hasBufferCache();
        }

        LocalVideoRequest(GalleryApp application, Path path, long timeModified, int type, String localFilePath) {
            super(application, path, timeModified, type, MediaItem.getTargetSize(type, true));
            this.mLocalFilePath = localFilePath;
        }

        public Bitmap onDecodeOriginal(JobContext jc, int type) {
            Bitmap bitmap = BitmapUtils.createVideoThumbnail(this.mLocalFilePath);
            if (bitmap == null || jc.isCancelled()) {
                return null;
            }
            return bitmap;
        }

        public String workContent() {
            return "decode thumnail from file: " + this.mLocalFilePath;
        }

        public boolean needDecodeVideoFromOrigin() {
            return !hasBufferCache();
        }
    }

    static {
        r0 = new String[16];
        r0[13] = String.format("strftime('%%Y%%m', %s / 1000, 'unixepoch') AS normalized_date", new Object[]{"datetaken"});
        r0[14] = "0";
        r0[15] = "_display_name";
        PROJECTION = r0;
    }

    public int getDurationInSec() {
        return this.durationInSec;
    }

    public LocalVideo(Path path, GalleryApp application, Cursor cursor) {
        super(path, MediaObject.nextVersionNumber(), application);
        loadFromCursor(cursor);
    }

    public LocalVideo(Path path, GalleryApp application, Cursor cursor, boolean shouldGetIndex, SparseArray<Integer> indexMap) {
        super(path, MediaObject.nextVersionNumber(), application);
        loadFromCursor(cursor, shouldGetIndex, indexMap);
    }

    public LocalVideo(Path path, GalleryApp context, int id) {
        super(path, MediaObject.nextVersionNumber(), context);
        Closeable closeable = null;
        try {
            closeable = LocalAlbum.getItemCursor(this.mApplication.getContentResolver(), Media.EXTERNAL_CONTENT_URI, PROJECTION, id);
            if (closeable == null) {
                throw new RuntimeException("cannot get cursor for: " + path);
            } else if (closeable.moveToNext()) {
                loadFromCursor(closeable);
            } else {
                throw new RuntimeException("cannot find video data for: " + path);
            }
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("LocalVideo");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    private void loadFromCursor(Cursor cursor) {
        this.id = cursor.getInt(0);
        this.mimeType = cursor.getString(2);
        this.caption = cursor.getString(1);
        this.latitude = cursor.getDouble(3);
        this.longitude = cursor.getDouble(4);
        this.dateTakenInMs = cursor.getLong(5);
        this.dateAddedInSec = cursor.getLong(6);
        this.dateModifiedInSec = cursor.getLong(7);
        this.filePath = checkFilePathNull(cursor.getString(8));
        this.durationInSec = getDurationInSecond(cursor.getInt(9));
        this.bucketId = cursor.getInt(10);
        this.fileSize = cursor.getLong(11);
        parseResolution(cursor.getString(12));
        this.isDrm = DrmUtils.isDrmFile(this.filePath);
        this.normalizedDate = cursor.getInt(13);
        this.displayName = cursor.getString(15);
    }

    private void loadFromCursorByModifiedIndex(Cursor cursor, SparseArray<Integer> indexMap) {
        this.id = cursor.getInt(((Integer) indexMap.get(0)).intValue());
        this.caption = cursor.getString(((Integer) indexMap.get(1)).intValue());
        this.mimeType = cursor.getString(((Integer) indexMap.get(2)).intValue());
        this.latitude = cursor.getDouble(((Integer) indexMap.get(3)).intValue());
        this.longitude = cursor.getDouble(((Integer) indexMap.get(4)).intValue());
        this.dateTakenInMs = cursor.getLong(((Integer) indexMap.get(5)).intValue());
        this.dateAddedInSec = cursor.getLong(((Integer) indexMap.get(6)).intValue());
        this.dateModifiedInSec = cursor.getLong(((Integer) indexMap.get(7)).intValue());
        this.filePath = checkFilePathNull(cursor.getString(((Integer) indexMap.get(8)).intValue()));
        this.durationInSec = getDurationInSecond(cursor.getInt(((Integer) indexMap.get(9)).intValue()));
        this.bucketId = cursor.getInt(((Integer) indexMap.get(10)).intValue());
        this.fileSize = cursor.getLong(((Integer) indexMap.get(11)).intValue());
        parseResolution(cursor.getString(((Integer) indexMap.get(12)).intValue()));
        this.displayName = cursor.getString(((Integer) indexMap.get(15)).intValue());
        this.isDrm = DrmUtils.isDrmFile(this.filePath);
        this.normalizedDate = cursor.getInt(cursor.getColumnIndex("normalized_date"));
    }

    private void loadFromCursor(Cursor cursor, boolean shouldGetIndex, SparseArray<Integer> indexMap) {
        if (shouldGetIndex) {
            loadFromCursorByModifiedIndex(cursor, indexMap);
        } else {
            loadFromCursor(cursor);
        }
    }

    public void updateContent(Cursor cursor, boolean shouldGetIndex, SparseArray<Integer> indexMap) {
        if (updateFromCursor(cursor, shouldGetIndex, indexMap)) {
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
    }

    public boolean updateFromCursor(Cursor cursor, boolean shouldGetIndex, SparseArray<Integer> indexMap) {
        if (!shouldGetIndex) {
            return updateFromCursor(cursor);
        }
        UpdateHelper uh = new UpdateHelper();
        this.id = uh.update(this.id, cursor.getInt(((Integer) indexMap.get(0)).intValue()));
        this.caption = (String) uh.update(this.caption, cursor.getString(((Integer) indexMap.get(1)).intValue()));
        this.mimeType = (String) uh.update(this.mimeType, cursor.getString(((Integer) indexMap.get(2)).intValue()));
        this.latitude = uh.update(this.latitude, cursor.getDouble(((Integer) indexMap.get(3)).intValue()));
        this.longitude = uh.update(this.longitude, cursor.getDouble(((Integer) indexMap.get(4)).intValue()));
        this.dateTakenInMs = uh.update(this.dateTakenInMs, cursor.getLong(((Integer) indexMap.get(5)).intValue()));
        this.dateAddedInSec = uh.update(this.dateAddedInSec, cursor.getLong(((Integer) indexMap.get(6)).intValue()));
        this.dateModifiedInSec = uh.update(this.dateModifiedInSec, cursor.getLong(((Integer) indexMap.get(7)).intValue()));
        this.filePath = (String) uh.update(this.filePath, checkFilePathNull(cursor.getString(((Integer) indexMap.get(8)).intValue())));
        this.durationInSec = uh.update(this.durationInSec, getDurationInSecond(cursor.getInt(((Integer) indexMap.get(9)).intValue())));
        this.bucketId = uh.update(this.bucketId, cursor.getInt(((Integer) indexMap.get(10)).intValue()));
        this.fileSize = uh.update(this.fileSize, cursor.getLong(((Integer) indexMap.get(11)).intValue()));
        this.isDrm = DrmUtils.isDrmFile(this.filePath);
        this.normalizedDate = uh.update(this.normalizedDate, cursor.getInt(cursor.getColumnIndex("normalized_date")));
        this.displayName = (String) uh.update(this.displayName, cursor.getString(((Integer) indexMap.get(15)).intValue()));
        return uh.isUpdated();
    }

    protected boolean updateFromCursor(Cursor cursor) {
        UpdateHelper uh = new UpdateHelper();
        this.id = uh.update(this.id, cursor.getInt(0));
        this.caption = (String) uh.update(this.caption, cursor.getString(1));
        this.mimeType = (String) uh.update(this.mimeType, cursor.getString(2));
        this.latitude = uh.update(this.latitude, cursor.getDouble(3));
        this.longitude = uh.update(this.longitude, cursor.getDouble(4));
        GalleryLog.printDFXLog("updateFrom cursor for LocalVideo");
        this.dateTakenInMs = uh.update(this.dateTakenInMs, cursor.getLong(5));
        this.dateAddedInSec = uh.update(this.dateAddedInSec, cursor.getLong(6));
        this.dateModifiedInSec = uh.update(this.dateModifiedInSec, cursor.getLong(7));
        this.filePath = (String) uh.update(this.filePath, checkFilePathNull(cursor.getString(8)));
        this.durationInSec = uh.update(this.durationInSec, getDurationInSecond(cursor.getInt(9)));
        this.bucketId = uh.update(this.bucketId, cursor.getInt(10));
        this.fileSize = uh.update(this.fileSize, cursor.getLong(11));
        this.isDrm = DrmUtils.isDrmFile(this.filePath);
        this.normalizedDate = uh.update(this.normalizedDate, cursor.getInt(13));
        this.displayName = (String) uh.update(this.displayName, cursor.getString(15));
        return uh.isUpdated();
    }

    private int getDurationInSecond(int durationMs) {
        return (durationMs <= 0 || durationMs > 1000) ? durationMs / 1000 : 1;
    }

    public Job<Bitmap> requestImage(int type) {
        return new LocalVideoRequest(this.mApplication, getPath(), this.dateModifiedInSec, type, this.filePath);
    }

    public Job<BitmapRegionDecoder> requestLargeImage() {
        throw new UnsupportedOperationException("Cannot regquest a large image to a local video!");
    }

    public int getSupportedOperations() {
        int operation = 1098908673 | (this.isDrm ? 0 : FragmentTransaction.TRANSIT_ENTER_MASK);
        if (!this.isDrm || canForward()) {
            operation |= 4;
        }
        if (!this.isDrm || hasRight()) {
            operation |= 128;
        }
        if (GalleryUtils.isSupportMyFavorite()) {
            operation |= 536870912;
        }
        if (VideoEditorController.isSupportVideoEdit()) {
            return operation;
        }
        return operation & -4097;
    }

    public int getVirtualFlags() {
        int flag = super.getVirtualFlags();
        if (this.bucketId == MediaSetUtils.getCameraBucketId() || GalleryStorageManager.getInstance().isOuterGalleryStorageCameraBucketID(this.bucketId)) {
            flag |= 2;
        }
        if (!GalleryUtils.isScreenRecorderExist()) {
            return flag;
        }
        if (this.bucketId == MediaSetUtils.getScreenshotsBucketID() || GalleryStorageManager.getInstance().isOuterGalleryStorageScreenshotsBucketID(this.bucketId)) {
            return flag | 8;
        }
        return flag;
    }

    protected ContentValues getValues() {
        ContentValues values = new ContentValues();
        values.put("_id", Integer.valueOf(this.id));
        values.put("title", this.caption);
        values.put("mime_type", this.mimeType);
        values.put("media_type", Integer.valueOf(getMediaType()));
        values.put("longitude", Double.valueOf(this.longitude));
        values.put("latitude", Double.valueOf(this.latitude));
        values.put("datetaken", Long.valueOf(this.dateTakenInMs));
        values.put("date_added", Long.valueOf(this.dateAddedInSec));
        values.put("date_modified", Long.valueOf(this.dateModifiedInSec));
        values.put("_data", this.filePath);
        values.put("duration", Integer.valueOf(this.durationInSec));
        values.put("bucket_id", Integer.valueOf(this.bucketId));
        values.put("_size", Long.valueOf(this.fileSize));
        values.put("resolution", this.width + "x" + this.height);
        values.put("width", Integer.valueOf(this.width));
        values.put("height", Integer.valueOf(this.height));
        values.put("_display_name", this.displayName);
        values.put("special_file_type", Integer.valueOf(this.mSpecialFileType));
        values.put("special_file_offset", Long.valueOf(this.mSpecialFileOffset));
        return values;
    }

    public void delete() {
        GalleryUtils.assertNotInRenderThread();
        super.delete();
        try {
            this.mApplication.getContentResolver().delete(Media.EXTERNAL_CONTENT_URI, "_id=?", new String[]{String.valueOf(this.id)});
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("LocalVideo");
        }
        this.mApplication.getDataManager().broadcastLocalDeletion();
        GalleryLog.d("LocalVideo", "have deleted video:" + getFilePath());
    }

    public void rotate(int degrees) {
    }

    public Uri getContentUri() {
        return Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(String.valueOf(this.id)).build();
    }

    public Uri getPlayUri() {
        return getContentUri();
    }

    public int getMediaType() {
        return 4;
    }

    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        if (this.durationInSec > 0) {
            details.addDetail(8, GalleryUtils.formatDuration(this.mApplication.getAndroidContext(), this.durationInSec));
        }
        if (this.isDrm) {
            MediaDetails.extractDrmInfo(details, this);
        }
        return details;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public int getRightCount() {
        return DrmUtils.getRightCount(this.filePath, 1);
    }

    public boolean hasRight() {
        return DrmUtils.haveRightsForAction(this.filePath, 1);
    }

    public boolean getRight() {
        return DrmUtils.haveRightsForAction(this.filePath, ApiHelper.DRMSTORE_ACTION_SHOW_DIALOG | 1);
    }

    public static void initColumnIndexMap(SparseArray<Integer> indexMap, String[] projection) {
        indexMap.put(0, Integer.valueOf(getColumnIndex(projection, PROJECTION[0])));
        indexMap.put(1, Integer.valueOf(getColumnIndex(projection, PROJECTION[1])));
        indexMap.put(2, Integer.valueOf(getColumnIndex(projection, PROJECTION[2])));
        indexMap.put(3, Integer.valueOf(getColumnIndex(projection, PROJECTION[3])));
        indexMap.put(4, Integer.valueOf(getColumnIndex(projection, PROJECTION[4])));
        indexMap.put(5, Integer.valueOf(getColumnIndex(projection, PROJECTION[5])));
        indexMap.put(6, Integer.valueOf(getColumnIndex(projection, PROJECTION[6])));
        indexMap.put(7, Integer.valueOf(getColumnIndex(projection, PROJECTION[7])));
        indexMap.put(8, Integer.valueOf(getColumnIndex(projection, PROJECTION[8])));
        indexMap.put(9, Integer.valueOf(getColumnIndex(projection, PROJECTION[9])));
        indexMap.put(10, Integer.valueOf(getColumnIndex(projection, PROJECTION[10])));
        indexMap.put(11, Integer.valueOf(getColumnIndex(projection, PROJECTION[11])));
        indexMap.put(12, Integer.valueOf(getColumnIndex(projection, PROJECTION[12])));
        indexMap.put(13, Integer.valueOf(getColumnIndex(projection, PROJECTION[13])));
        indexMap.put(14, Integer.valueOf(getColumnIndex(projection, PROJECTION[14])));
        indexMap.put(15, Integer.valueOf(getColumnIndex(projection, PROJECTION[15])));
    }

    private static int getColumnIndex(String[] projection, String column) {
        for (int index = 0; index < projection.length; index++) {
            if (projection[index].equalsIgnoreCase(column)) {
                return index;
            }
        }
        return -1;
    }

    public void setAsFavorite(Context context) {
        super.setAsFavorite(context);
        int galleryMediaId = getGalleryMediaId(context);
        if (galleryMediaId != -1) {
            MediaObject object = this.mApplication.getDataManager().peekMediaObject(Path.fromString("/gallery/video/item/" + galleryMediaId));
            if (object != null) {
                ((GalleryMediaItem) object).updateMyFavorite(true);
            }
        }
    }

    public void cancelFavorite(Context context) {
        super.cancelFavorite(context);
        int galleryMediaId = getGalleryMediaId(context);
        if (galleryMediaId != -1) {
            MediaObject object = this.mApplication.getDataManager().peekMediaObject(Path.fromString("/gallery/video/item/" + galleryMediaId));
            if (object != null) {
                ((GalleryMediaItem) object).updateMyFavorite(false);
            }
        }
    }

    public String toString() {
        return new StringBuffer().append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append("\n caption: ").append(this.caption).append("\n mimeType: ").append(this.mimeType).append("\n bucketId: ").append(this.bucketId).append("\n fileSize: ").append(this.fileSize).toString();
    }
}
