package com.huawei.gallery.media;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.text.TextUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.BlackList;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReverseGeocoder;
import com.huawei.gallery.media.database.CloudTableOperateHelper;
import com.huawei.gallery.media.database.MergedMedia;
import com.huawei.gallery.media.database.SpecialFileList;
import com.huawei.gallery.media.database.SpecialFileType;
import com.huawei.gallery.photoshare.utils.MD5Utils;
import com.huawei.gallery.photoshare.utils.PhotoShareConstants;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class GalleryMedia {
    private static final MyPrinter LOG = new MyPrinter("GalleryMedia");
    static final String[] PROJECTION = new String[]{"_id", "local_media_id", "cloud_media_id", "_data", "_size", "date_added", "date_modified", "mime_type", "title", "description", "_display_name", "orientation", "latitude", "longitude", "datetaken", "bucket_id", "bucket_display_name", "duration", "resolution", "media_type", "storage_id", "width", "height", "is_hdr", "is_hw_privacy", "hw_voice_offset", "is_hw_favorite", "hw_image_refocus", "hw_rectify_offset", "contenturi", "hash", "special_file_list", "special_file_type", "special_file_offset", "bucket_relative_path"};
    public static final Uri URI = MergedMedia.URI.buildUpon().appendPath("gallery_media").build();
    String bucketDisplayName;
    String bucketId;
    String bucketRelativePath;
    int cloudMediaId = -1;
    String contentUri;
    String data;
    long dateAdded;
    long dateModified;
    long datetaken;
    String description;
    String displayName;
    int duration;
    String hash;
    int height;
    int hwImageRefocus;
    int hwRectifyOffset;
    int hwVoiceOffset;
    int id = -1;
    int isHdr;
    int isHwBurst;
    int isHwFavorite;
    int isHwPrivacy;
    double latitude;
    int localMediaId = -1;
    double longitude;
    private boolean mIsRaw = true;
    int mediaType;
    String mimeType;
    int orientation;
    int relativeCloudMediaId = -1;
    String resolution;
    long size;
    int specialFileList = SpecialFileList.DEFAULT.listType;
    long specialFileOffset;
    int specialFileType = SpecialFileType.DEFAULT.fileType;
    int storageId;
    String title;
    int width;

    GalleryMedia() {
    }

    GalleryMedia(Cursor cursor) {
        this.id = cursor.getInt(cursor.getColumnIndex("_id"));
        this.localMediaId = cursor.getInt(cursor.getColumnIndex("local_media_id"));
        this.cloudMediaId = cursor.getInt(cursor.getColumnIndex("cloud_media_id"));
        this.data = cursor.getString(cursor.getColumnIndex("_data"));
        this.size = cursor.getLong(cursor.getColumnIndex("_size"));
        this.dateAdded = cursor.getLong(cursor.getColumnIndex("date_added"));
        this.dateModified = cursor.getLong(cursor.getColumnIndex("date_modified"));
        this.mimeType = cursor.getString(cursor.getColumnIndex("mime_type"));
        this.title = cursor.getString(cursor.getColumnIndex("title"));
        this.description = cursor.getString(cursor.getColumnIndex("description"));
        this.displayName = cursor.getString(cursor.getColumnIndex("_display_name"));
        this.orientation = cursor.getInt(cursor.getColumnIndex("orientation"));
        this.latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
        this.longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
        this.datetaken = cursor.getLong(cursor.getColumnIndex("datetaken"));
        this.bucketId = cursor.getString(cursor.getColumnIndex("bucket_id"));
        this.bucketDisplayName = cursor.getString(cursor.getColumnIndex("bucket_display_name"));
        this.duration = cursor.getInt(cursor.getColumnIndex("duration"));
        this.resolution = cursor.getString(cursor.getColumnIndex("resolution"));
        this.mediaType = cursor.getInt(cursor.getColumnIndex("media_type"));
        this.storageId = cursor.getInt(cursor.getColumnIndex("storage_id"));
        this.width = cursor.getInt(cursor.getColumnIndex("width"));
        this.height = cursor.getInt(cursor.getColumnIndex("height"));
        this.isHdr = cursor.getInt(cursor.getColumnIndex("is_hdr"));
        this.isHwPrivacy = cursor.getInt(cursor.getColumnIndex("is_hw_privacy"));
        this.hwVoiceOffset = cursor.getInt(cursor.getColumnIndex("hw_voice_offset"));
        this.isHwFavorite = cursor.getInt(cursor.getColumnIndex("is_hw_favorite"));
        this.hwImageRefocus = cursor.getInt(cursor.getColumnIndex("hw_image_refocus"));
        int index = cursor.getColumnIndex("is_hw_burst");
        if (index >= 0) {
            this.isHwBurst = cursor.getInt(index);
        }
        this.hwRectifyOffset = cursor.getInt(cursor.getColumnIndex("hw_rectify_offset"));
        this.contentUri = cursor.getString(cursor.getColumnIndex("contenturi"));
        this.hash = cursor.getString(cursor.getColumnIndex("hash"));
        this.specialFileList = cursor.getInt(cursor.getColumnIndex("special_file_list"));
        this.specialFileType = cursor.getInt(cursor.getColumnIndex("special_file_type"));
        this.specialFileOffset = cursor.getLong(cursor.getColumnIndex("special_file_offset"));
        this.bucketRelativePath = cursor.getString(cursor.getColumnIndex("bucket_relative_path"));
    }

    ContentValues getValues() {
        if (this.mIsRaw) {
            this.hash = MD5Utils.getMD5(new File(this.data));
            this.specialFileList = checkSpecialFileList(this.data);
            this.bucketRelativePath = getBucketRelativePath(this.data);
            this.mIsRaw = false;
        }
        ContentValues values = new ContentValues();
        values.put("local_media_id", Integer.valueOf(this.localMediaId));
        values.put("_data", this.data);
        values.put("_size", Long.valueOf(this.size));
        values.put("date_added", Long.valueOf(this.dateAdded));
        values.put("date_modified", Long.valueOf(this.dateModified));
        values.put("mime_type", this.mimeType);
        values.put("title", this.title);
        values.put("description", this.description);
        values.put("_display_name", this.displayName);
        values.put("orientation", Integer.valueOf(this.orientation));
        values.put("latitude", Double.valueOf(this.latitude));
        values.put("longitude", Double.valueOf(this.longitude));
        values.put("datetaken", Long.valueOf(this.datetaken));
        values.put("bucket_id", this.bucketId);
        values.put("bucket_display_name", this.bucketDisplayName);
        values.put("duration", Integer.valueOf(this.duration));
        values.put("resolution", this.resolution);
        values.put("media_type", Integer.valueOf(this.mediaType));
        values.put("storage_id", Integer.valueOf(this.storageId));
        values.put("width", Integer.valueOf(this.width));
        values.put("height", Integer.valueOf(this.height));
        values.put("is_hdr", Integer.valueOf(this.isHdr));
        values.put("is_hw_privacy", Integer.valueOf(this.isHwPrivacy));
        values.put("hw_voice_offset", Integer.valueOf(this.hwVoiceOffset));
        values.put("is_hw_favorite", Integer.valueOf(this.isHwFavorite));
        values.put("hw_image_refocus", Integer.valueOf(this.hwImageRefocus));
        values.put("is_hw_burst", Integer.valueOf(this.isHwBurst));
        values.put("hw_rectify_offset", Integer.valueOf(this.hwRectifyOffset));
        values.put("contenturi", this.contentUri);
        values.put("hash", this.hash);
        values.put("special_file_list", Integer.valueOf(this.specialFileList));
        values.put("special_file_type", Integer.valueOf(this.specialFileType));
        values.put("special_file_offset", Long.valueOf(this.specialFileOffset));
        values.put("bucket_relative_path", this.bucketRelativePath);
        if (this.relativeCloudMediaId != -1) {
            values.put("relative_cloud_media_id", Integer.valueOf(this.relativeCloudMediaId));
        }
        values.put("location_key", Long.valueOf(ReverseGeocoder.genLocationKey(this.latitude, this.longitude)));
        return values;
    }

    private int checkSpecialFileList(String filepath) {
        if (!BlackList.getInstance().match(filepath.toLowerCase(Locale.US))) {
            return SpecialFileList.DEFAULT.listType;
        }
        LOG.d("black list: " + filepath);
        return SpecialFileList.BLACK_LIST.listType;
    }

    public static String getBucketRelativePath(String data) {
        int i;
        String bucketRelativePath = null;
        GalleryStorageManager storageManager = GalleryStorageManager.getInstance();
        ArrayList<String> mountPaths = new ArrayList();
        if (storageManager.getInnerGalleryStorage() != null) {
            mountPaths.add(storageManager.getInnerGalleryStorage().getPath());
        }
        ArrayList<GalleryStorage> outStorage = storageManager.getOuterGalleryStorageList();
        for (i = 0; i < outStorage.size(); i++) {
            mountPaths.add(((GalleryStorage) outStorage.get(i)).getPath());
        }
        for (i = 0; i < mountPaths.size(); i++) {
            if (data.startsWith((String) mountPaths.get(i))) {
                String relativePath = data.substring(((String) mountPaths.get(i)).length());
                bucketRelativePath = relativePath.substring(0, relativePath.lastIndexOf("/"));
                break;
            }
        }
        if (bucketRelativePath == null) {
            LOG.d("bucketRelativePath is null, data " + data);
        }
        return bucketRelativePath;
    }

    public String toString() {
        StringBuffer thizz = new StringBuffer("GalleryMedia:[");
        thizz.append("id =").append(this.id);
        thizz.append("localMediaId =").append(this.localMediaId);
        thizz.append("cloudMediaId =").append(this.cloudMediaId);
        thizz.append("data =").append(this.data);
        thizz.append("size =").append(this.size);
        thizz.append("mimeType =").append(this.mimeType);
        thizz.append("title =").append(this.title);
        thizz.append("displayName =").append(this.displayName);
        thizz.append("latitude =").append(this.latitude);
        thizz.append("longitude =").append(this.longitude);
        thizz.append("bucketId =").append(this.bucketId);
        thizz.append("bucketDisplayName =").append(this.bucketDisplayName);
        thizz.append("mediaType =").append(this.mediaType);
        thizz.append("storageId =").append(this.storageId);
        thizz.append("width =").append(this.width);
        thizz.append("height =").append(this.height);
        thizz.append("hash =").append(this.hash);
        return thizz.toString();
    }

    static Cursor query(ContentResolver resolver, String selection, String[] selectionArgs, String orderBy) {
        return resolver.query(URI, PROJECTION, selection, selectionArgs, orderBy);
    }

    static Cursor query(ContentResolver resolver, int start, int count, String selectionClause, String[] selectArgs, String orderBy) {
        return resolver.query(URI.buildUpon().appendQueryParameter("limit", start + "," + count).build(), PROJECTION, selectionClause, selectArgs, orderBy);
    }

    public Uri insert(ContentResolver resolver) {
        return resolver.insert(URI, getValues());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int delete(ContentResolver resolver) {
        String str = null;
        String str2 = null;
        int thumbType = 0;
        if (!(this.cloudMediaId == -1 || findRelativeFile(resolver))) {
            Closeable closeable = null;
            try {
                ContentResolver contentResolver = resolver;
                closeable = contentResolver.query(PhotoShareConstants.CLOUD_FILE_TABLE_URI, new String[]{"localThumbPath", "localBigThumbPath", "albumId", "hash", "fileName"}, "id = ?", new String[]{String.valueOf(this.cloudMediaId)}, null);
                if (closeable != null && closeable.moveToNext()) {
                    String thumbPath = checkFilePath(closeable.getString(0));
                    String bigPath = checkFilePath(closeable.getString(1));
                    String albumId = closeable.getString(2);
                    str2 = closeable.getString(3);
                    String fileName = closeable.getString(4);
                    if (!TextUtils.isEmpty(bigPath)) {
                        str = bigPath;
                        thumbType = 2;
                    } else if (TextUtils.isEmpty(thumbPath)) {
                        str = "cloud-" + albumId + "-" + str2;
                        thumbType = 0;
                    } else {
                        str = thumbPath;
                        thumbType = 1;
                    }
                    updateCloudFilePath(resolver, this.cloudMediaId, "", bigPath, thumbPath);
                    updateCloudFileName(resolver, this.cloudMediaId, fileName);
                }
                Utils.closeSilently(closeable);
            } catch (SQLiteException e) {
                GalleryLog.e("photoshareLogTag", "query cloud_file exception " + e.toString());
            } catch (Throwable th) {
                Utils.closeSilently(closeable);
            }
        }
        if (str == null) {
            return resolver.delete(URI, " _id = ? ", new String[]{String.valueOf(this.id)});
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("_data", str);
        contentValues.put("thumbType", Integer.valueOf(thumbType));
        contentValues.put("date_modified", Long.valueOf(CloudTableOperateHelper.getLastModify(str)));
        contentValues.put("contenturi", "");
        contentValues.put("local_media_id", Integer.valueOf(-1));
        contentValues.put("hash", str2);
        if (str.startsWith("cloud-")) {
            contentValues.put("height", Integer.valueOf(0));
            contentValues.put("width", Integer.valueOf(0));
        } else {
            GalleryUtils.resolveWidthAndHeight(contentValues, str);
        }
        int affectCount = 0;
        try {
            ContentResolver contentResolver2 = resolver;
            affectCount = contentResolver2.update(URI, contentValues, " _id = ? ", new String[]{String.valueOf(this.id)});
        } catch (Exception e2) {
            GalleryLog.e("photoshareLogTag", "delete function, update gallery_media exception " + e2.toString());
        }
        return affectCount;
    }

    private void updateCloudFilePath(ContentResolver resolver, int cloudFileId, String localRealPath, String bigPath, String thumbPath) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("localRealPath", localRealPath);
        contentValues.put("localBigThumbPath", bigPath);
        contentValues.put("localThumbPath", thumbPath);
        resolver.update(PhotoShareConstants.CLOUD_FILE_TABLE_URI, contentValues, "id = ?", new String[]{String.valueOf(cloudFileId)});
    }

    private void updateCloudFileName(ContentResolver resolver, int cloudFileId, String fileName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("fileName", fileName);
        resolver.update(PhotoShareConstants.CLOUD_FILE_TABLE_URI, contentValues, "id = ?", new String[]{String.valueOf(cloudFileId)});
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean findRelativeFile(ContentResolver resolver) {
        int galleryMediaId = -1;
        String str = null;
        try {
            ContentResolver contentResolver = resolver;
            Closeable cursor = contentResolver.query(URI, new String[]{"_id", "_data"}, "cloud_media_id = -1 and _display_name = ? and relative_cloud_media_id = ?", new String[]{this.displayName, String.valueOf(this.cloudMediaId)}, null);
            if (cursor != null && cursor.moveToNext()) {
                galleryMediaId = cursor.getInt(0);
                str = cursor.getString(1);
            }
            Utils.closeSilently(cursor);
        } catch (RuntimeException e) {
            GalleryLog.e("photoshareLogTag", "findRelativeFile query cloud_file exception " + e.toString());
            GalleryLog.e("photoshareLogTag", "displayName " + this.displayName + ", cloudMediaId " + this.cloudMediaId);
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        updateRelativeFile(resolver, galleryMediaId, str);
        if (galleryMediaId != -1) {
            return true;
        }
        return false;
    }

    private String checkFilePath(String filePath) {
        if (PhotoShareUtils.isFileExists(filePath)) {
            return filePath;
        }
        return "";
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRelativeFile(ContentResolver resolver, int targetGalleryMediaId, String targetLocalRealPath) {
        String str = null;
        int fileType = 0;
        String str2 = null;
        String str3 = null;
        String str4 = null;
        String str5 = null;
        String str6 = null;
        String str7 = null;
        try {
            ContentResolver contentResolver = resolver;
            Closeable cursor = contentResolver.query(URI, new String[]{"cloud_bucket_id", "fileType", "fileId", "localThumbPath", "localBigThumbPath", "expand", "source", "videoThumbId"}, "_id = ?", new String[]{String.valueOf(this.id)}, null);
            if (cursor != null && cursor.moveToNext()) {
                str = cursor.getString(0);
                fileType = cursor.getInt(1);
                str2 = cursor.getString(2);
                str3 = checkFilePath(cursor.getString(3));
                str4 = checkFilePath(cursor.getString(4));
                str5 = cursor.getString(5);
                str6 = cursor.getString(6);
                str7 = cursor.getString(7);
            }
            Utils.closeSilently(cursor);
        } catch (SQLiteException e) {
            GalleryLog.e("photoshareLogTag", "query cloud_file exception " + e.toString());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("cloud_bucket_id", str);
        contentValues.put("fileType", Integer.valueOf(fileType));
        contentValues.put("fileId", str2);
        contentValues.put("localThumbPath", str3);
        contentValues.put("localBigThumbPath", str4);
        contentValues.put("expand", str5);
        contentValues.put("relative_cloud_media_id", Integer.valueOf(-1));
        contentValues.put("cloud_media_id", Integer.valueOf(this.cloudMediaId));
        contentValues.put("thumbType", Integer.valueOf(3));
        contentValues.put("source", str6);
        contentValues.put("videoThumbId", str7);
        try {
            ContentResolver contentResolver2 = resolver;
            contentResolver2.update(URI, contentValues, " _id = ? ", new String[]{String.valueOf(targetGalleryMediaId)});
        } catch (Exception e2) {
            GalleryLog.e("photoshareLogTag", "gallery_media exception " + e2.toString());
        }
        updateCloudFilePath(resolver, this.cloudMediaId, targetLocalRealPath, str4, str3);
    }
}
