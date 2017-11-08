package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video.Media;
import android.text.TextUtils;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.app.PanoramaMetadataSupport;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.UpdateHelper;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface.GpsMeasureMode;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.media.LocalRecycledFile;
import com.huawei.gallery.media.database.CloudRecycleTableOperateHelper;
import com.huawei.gallery.media.services.StorageService;
import com.huawei.gallery.photoshare.utils.PhotoShareConstants;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.story.utils.StoryAlbumUtils;
import com.huawei.gallery.util.BurstUtils;
import java.io.Closeable;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public abstract class GalleryMediaItem extends LocalMediaItem {
    private static final String[] PROJECTION;
    protected String mBucketRelativePath;
    protected Path mBurstSetPath;
    protected String mCloudBucketID;
    protected int mCloudMediaId;
    protected String mDisplayName;
    public int mDownloadState;
    protected int mDurationInSec;
    protected String mExpand;
    protected String mFileId;
    protected FileInfo mFileInfo;
    protected int mFileType;
    public String mHash;
    protected boolean mIsAllBurstFileUploaded;
    protected boolean mIsBurstCover;
    protected String mLocalBigThumbPath;
    protected String mLocalContentUri;
    protected int mLocalMediaId;
    protected String mLocalThumbPath;
    protected String mMediaType;
    private PanoramaMetadataSupport mPanoramaMetadata;
    protected int mRectifyOffset;
    protected boolean mRefocusFlagChange;
    protected int mRefocusPhoto;
    protected int mRelativeCloudMediaId;
    protected String mResolution;
    protected long mShowDateToken;
    protected String mSource;
    protected int mSupportRename;
    protected int mThumbType;
    protected String mUniqueId;
    protected String mVideoThumbId;
    protected long mVoiceOffset;
    public int rotation;

    protected abstract ContentValues getLocalValues();

    public static String[] copyProjection() {
        return (String[]) PROJECTION.clone();
    }

    static {
        r0 = new String[43];
        r0[15] = String.format("strftime('%%Y%%m', %s / 1000, 'unixepoch') AS normalized_date", new Object[]{"datetaken"});
        r0[16] = "hw_voice_offset";
        r0[17] = "is_hw_favorite";
        r0[18] = "hw_image_refocus";
        r0[19] = "hw_rectify_offset";
        r0[20] = "local_media_id";
        r0[21] = "cloud_media_id";
        r0[22] = "contenturi";
        r0[23] = "hash";
        r0[24] = "media_type";
        r0[25] = "cloud_bucket_id";
        r0[26] = "bucket_relative_path";
        r0[27] = "duration";
        r0[28] = "fileType";
        r0[29] = "fileId";
        r0[30] = "videoThumbId";
        r0[31] = "thumbType";
        r0[32] = "localThumbPath";
        r0[33] = "localBigThumbPath";
        r0[34] = "expand";
        r0[35] = "_display_name";
        r0[36] = "showDateToken";
        r0[37] = "source";
        r0[38] = "resolution";
        r0[39] = "special_file_type";
        r0[40] = "relative_cloud_media_id";
        r0[41] = "special_file_offset";
        r0[42] = "uniqueId";
        PROJECTION = r0;
    }

    public GalleryMediaItem(Path path, GalleryApp application, Cursor cursor) {
        super(path, MediaObject.nextVersionNumber(), application);
        this.mSupportRename = -1;
        this.mBurstSetPath = null;
        this.mIsBurstCover = false;
        this.mIsAllBurstFileUploaded = false;
        this.mRefocusFlagChange = false;
        this.mPanoramaMetadata = new PanoramaMetadataSupport(this);
        this.mRelativeCloudMediaId = -1;
        this.mDownloadState = -1;
        loadFromCursor(cursor);
    }

    public int getLocalMediaId() {
        return this.mLocalMediaId;
    }

    public int getCloudMediaId() {
        return this.mCloudMediaId;
    }

    protected Uri getMediaUri() {
        return GalleryMedia.URI;
    }

    protected String getQueryWhereById(String id) {
        return "_id = " + id;
    }

    protected String[] getProjection() {
        return PROJECTION;
    }

    public GalleryMediaItem(Path path, GalleryApp application, int id) {
        this(path, application, String.valueOf(id));
    }

    public GalleryMediaItem(Path path, GalleryApp application, String id) {
        super(path, MediaObject.nextVersionNumber(), application);
        this.mSupportRename = -1;
        this.mBurstSetPath = null;
        this.mIsBurstCover = false;
        this.mIsAllBurstFileUploaded = false;
        this.mRefocusFlagChange = false;
        this.mPanoramaMetadata = new PanoramaMetadataSupport(this);
        this.mRelativeCloudMediaId = -1;
        this.mDownloadState = -1;
        Closeable closeable = null;
        try {
            closeable = LocalAlbum.getItemCursor(this.mApplication.getContentResolver(), getMediaUri(), getProjection(), getQueryWhereById(id));
            if (closeable == null) {
                throw new RuntimeException("cannot get cursor for: " + path);
            } else if (closeable.moveToNext()) {
                loadFromCursor(closeable);
            } else {
                path.clearObject();
                throw new RuntimeException("cannot find data for: " + path);
            }
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("GalleryMediaItem");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    protected void loadFromCursor(Cursor cursor) {
        boolean z;
        boolean z2 = true;
        int i = 0;
        GalleryLog.d("GalleryMediaItem", "load item from cursor");
        this.id = cursor.getInt(0);
        this.caption = cursor.getString(1);
        this.mimeType = cursor.getString(2);
        this.latitude = cursor.getDouble(3);
        this.longitude = cursor.getDouble(4);
        this.dateTakenInMs = cursor.getLong(5);
        this.dateAddedInSec = cursor.getLong(6);
        this.dateModifiedInSec = cursor.getLong(7);
        this.filePath = checkFilePathNull(cursor.getString(8));
        this.rotation = cursor.getInt(9);
        this.bucketId = cursor.getInt(10);
        this.fileSize = cursor.getLong(11);
        this.width = cursor.getInt(12);
        this.height = cursor.getInt(13);
        if (cursor.getInt(14) > 0) {
            z = true;
        } else {
            z = false;
        }
        this.is_hdr = z;
        this.mVoiceOffset = cursor.getLong(16);
        int oldRefocus = this.mRefocusPhoto;
        this.mRefocusPhoto = cursor.getInt(18);
        if (oldRefocus == this.mRefocusPhoto) {
            z2 = false;
        }
        this.mRefocusFlagChange = z2;
        this.isDrm = DrmUtils.isDrmFile(this.filePath);
        this.normalizedDate = cursor.getInt(15);
        this.mRectifyOffset = cursor.getInt(19);
        this.mLocalMediaId = cursor.getInt(20);
        this.mCloudMediaId = cursor.getInt(21);
        this.mLocalContentUri = cursor.getString(22);
        this.mHash = cursor.getString(23);
        this.mMediaType = cursor.getString(24);
        this.mCloudBucketID = cursor.getString(25);
        this.mBucketRelativePath = cursor.getString(26);
        this.mDurationInSec = getDurationInSecond(cursor.getInt(27));
        this.mFileType = cursor.getInt(28);
        this.mFileId = cursor.getString(29);
        this.mVideoThumbId = cursor.getString(30);
        this.mThumbType = cursor.getInt(31);
        this.mLocalThumbPath = cursor.getString(32);
        this.mLocalBigThumbPath = cursor.getString(33);
        this.mExpand = cursor.getString(34);
        this.mDisplayName = cursor.getString(35);
        this.mShowDateToken = cursor.getLong(36);
        this.mSource = cursor.getString(37);
        this.mResolution = cursor.getString(38);
        this.mSpecialFileType = cursor.getInt(39);
        this.mSpecialFileOffset = cursor.getLong(41);
        this.mRelativeCloudMediaId = cursor.getInt(40);
        this.mUniqueId = cursor.getString(42);
        this.mFileInfo = new FileInfo();
        updateFileInfo();
        if (this.mLocalMediaId != -1) {
            i = 2;
        }
        this.mDownloadState = i;
        if (!isVideo()) {
            getBurstSetCover();
        }
    }

    private void updateFileInfo() {
        this.mFileInfo.setFileName(this.mDisplayName);
        this.mFileInfo.setFileType(this.mFileType);
        this.mFileInfo.setHash(this.mHash);
        this.mFileInfo.setAlbumId(this.mCloudBucketID);
        this.mFileInfo.setAddTime(this.dateTakenInMs);
        this.mFileInfo.setExpand(this.mExpand);
        this.mFileInfo.setLocalThumbPath(this.mLocalThumbPath);
        this.mFileInfo.setLocalBigThumbPath(this.mLocalBigThumbPath);
        this.mFileInfo.setVideoThumbId(this.mVideoThumbId);
        this.mFileInfo.setFileId(this.mFileId);
        this.mFileInfo.setSize(this.fileSize);
        this.mFileInfo.setShareId("");
        this.mFileInfo.setSource(TextUtils.isEmpty(this.mSource) ? "UNKNOW" : this.mSource);
        this.mFileInfo.setLocalRealPath(this.mLocalMediaId != -1 ? this.filePath : null);
        this.mFileInfo.setUniqueId(this.mUniqueId);
        if (PhotoShareUtils.isGUIDSupport()) {
            this.mFileInfo.setFileAttribute(getFileAttribute());
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getFileAttribute() {
        int result = 0;
        try {
            Closeable cursor = this.mApplication.getContentResolver().query(StorageService.sCloudFileRecycleUri, new String[]{"recycleFlag"}, "uniqueId=?", new String[]{this.mUniqueId}, null);
            if (cursor != null && cursor.moveToNext()) {
                result = cursor.getInt(0);
            }
            Utils.closeSilently(cursor);
        } catch (RuntimeException e) {
            GalleryLog.w("GalleryMediaItem", "queryRecycleFlag exception: " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return (result == -1 || result == -3 || result == 0) ? 0 : 1;
    }

    public FileInfo getFileInfo() {
        return this.mFileInfo;
    }

    public boolean isOnlyCloudItem() {
        return this.mLocalMediaId == -1;
    }

    public boolean isContainCloud() {
        return this.mCloudMediaId != -1;
    }

    public int getThumbType() {
        return this.mThumbType;
    }

    public String getUniqueId() {
        return this.mUniqueId;
    }

    protected boolean updateFromCursor(Cursor cursor) {
        boolean z;
        boolean z2 = true;
        int i = 0;
        UpdateHelper uh = new UpdateHelper();
        this.id = uh.update(this.id, cursor.getInt(0));
        this.caption = (String) uh.update(this.caption, cursor.getString(1));
        this.latitude = uh.update(this.latitude, cursor.getDouble(3));
        this.longitude = uh.update(this.longitude, cursor.getDouble(4));
        this.dateTakenInMs = uh.update(this.dateTakenInMs, cursor.getLong(5));
        this.dateAddedInSec = uh.update(this.dateAddedInSec, cursor.getLong(6));
        this.dateModifiedInSec = uh.update(this.dateModifiedInSec, cursor.getLong(7));
        if (!(TextUtils.isEmpty(this.filePath) || this.filePath.equals(cursor.getString(8)))) {
            this.isMyFavorite = -1;
        }
        int oldRefocus = this.mRefocusPhoto;
        int old = this.mLocalMediaId;
        this.mLocalMediaId = uh.update(this.mLocalMediaId, cursor.getInt(20));
        this.mCloudMediaId = uh.update(this.mCloudMediaId, cursor.getInt(21));
        this.mFileType = uh.update(this.mFileType, cursor.getInt(28));
        this.mLocalThumbPath = (String) uh.update(this.mLocalThumbPath, cursor.getString(32));
        this.mLocalBigThumbPath = (String) uh.update(this.mLocalBigThumbPath, cursor.getString(33));
        this.rotation = uh.update(this.rotation, cursor.getInt(9));
        updateSpecialValues(uh, cursor);
        if (oldRefocus != this.mRefocusPhoto) {
            z = true;
        } else {
            z = false;
        }
        this.mRefocusFlagChange = z;
        this.isDrm = DrmUtils.isDrmFile(this.filePath);
        this.bucketId = uh.update(this.bucketId, cursor.getInt(10));
        this.fileSize = uh.update(this.fileSize, cursor.getLong(11));
        Object valueOf = Boolean.valueOf(this.is_hdr);
        if (cursor.getInt(14) <= 0) {
            z2 = false;
        }
        this.is_hdr = ((Boolean) uh.update(valueOf, Boolean.valueOf(z2))).booleanValue();
        this.normalizedDate = uh.update(this.normalizedDate, cursor.getInt(15));
        this.mLocalContentUri = (String) uh.update(this.mLocalContentUri, cursor.getString(22));
        this.mHash = (String) uh.update(this.mHash, cursor.getString(23));
        this.mCloudBucketID = (String) uh.update(this.mCloudBucketID, cursor.getString(25));
        this.mBucketRelativePath = (String) uh.update(this.mBucketRelativePath, cursor.getString(26));
        this.mDurationInSec = uh.update(this.mDurationInSec, getDurationInSecond(cursor.getInt(27)));
        this.mFileId = (String) uh.update(this.mFileId, cursor.getString(29));
        this.mVideoThumbId = (String) uh.update(this.mVideoThumbId, cursor.getString(30));
        this.mExpand = (String) uh.update(this.mExpand, cursor.getString(34));
        this.mDisplayName = (String) uh.update(this.mDisplayName, cursor.getString(35));
        this.mShowDateToken = uh.update(this.mShowDateToken, cursor.getLong(36));
        this.mSource = (String) uh.update(this.mSource, cursor.getString(37));
        this.mResolution = (String) uh.update(this.mResolution, cursor.getString(38));
        this.mRelativeCloudMediaId = uh.update(this.mRelativeCloudMediaId, cursor.getInt(40));
        if (old != this.mLocalMediaId) {
            if (this.mLocalMediaId != -1) {
                i = 2;
            }
            this.mDownloadState = i;
        } else if (this.mLocalMediaId == -1 && this.mThumbType == 3) {
            this.mDownloadState = 2;
        }
        this.mUniqueId = (String) uh.update(this.mUniqueId, cursor.getString(42));
        updateFileInfo();
        if (!isVideo()) {
            updateBurstSetCover(uh);
        }
        return uh.isUpdated();
    }

    private int getDurationInSecond(int durationMs) {
        return (durationMs <= 0 || durationMs > 1000) ? durationMs / 1000 : 1;
    }

    private void getBurstSetCover() {
        boolean z = true;
        this.mBurstSetPath = BurstUtils.getBurstSetPath(this.mDisplayName, this.bucketId, true, this.mApplication.getContentResolver());
        if (this.mBurstSetPath == null) {
            z = false;
        }
        this.mIsBurstCover = z;
        if (this.mIsBurstCover) {
            this.mIsAllBurstFileUploaded = BurstUtils.isAllBurstFileUploaded(this.bucketId, this.mBurstSetPath.getSuffix(), this.mApplication.getContentResolver());
        }
    }

    protected void updateSpecialValues(UpdateHelper uh, Cursor cursor) {
        this.filePath = (String) uh.update(this.filePath, checkFilePathNull(cursor.getString(8)));
        this.mimeType = (String) uh.update(this.mimeType, cursor.getString(2));
        this.mMediaType = (String) uh.update(this.mMediaType, cursor.getString(24));
        this.mThumbType = uh.update(this.mThumbType, cursor.getInt(31));
        this.mVoiceOffset = uh.update(this.mVoiceOffset, cursor.getLong(16));
        this.mRefocusPhoto = uh.update(this.mRefocusPhoto, cursor.getInt(18));
        this.mRectifyOffset = uh.update(this.mRectifyOffset, cursor.getInt(19));
        this.mSpecialFileType = uh.update(this.mSpecialFileType, cursor.getInt(39));
        this.mSpecialFileOffset = uh.update(this.mSpecialFileOffset, cursor.getLong(41));
        this.width = uh.update(this.width, cursor.getInt(12));
        this.height = uh.update(this.height, cursor.getInt(13));
    }

    private void updateBurstSetCover(UpdateHelper uh) {
        this.mBurstSetPath = (Path) uh.update(this.mBurstSetPath, BurstUtils.getBurstSetPath(this.mDisplayName, this.bucketId, true, this.mApplication.getContentResolver()));
        this.mIsBurstCover = ((Boolean) uh.update(Boolean.valueOf(this.mIsBurstCover), Boolean.valueOf(this.mBurstSetPath != null))).booleanValue();
        if (this.mIsBurstCover && this.mBurstSetPath != null) {
            this.mIsAllBurstFileUploaded = ((Boolean) uh.update(Boolean.valueOf(this.mIsAllBurstFileUploaded), Boolean.valueOf(BurstUtils.isAllBurstFileUploaded(this.bucketId, this.mBurstSetPath.getSuffix(), this.mApplication.getContentResolver())))).booleanValue();
        }
    }

    public boolean isVideo() {
        return GpsMeasureMode.MODE_3_DIMENSIONAL.equals(this.mMediaType);
    }

    public void recycle(SQLiteDatabase db, Bundle data) {
        if (RecycleUtils.supportRecycle()) {
            if (this.mLocalMediaId != -1) {
                ContentValues values = getLocalValues();
                if (PhotoShareUtils.isGUIDSupport() && PhotoShareUtils.getLocalSwitch() && !TextUtils.isEmpty(this.mUniqueId)) {
                    values.put("uniqueId", this.mUniqueId);
                }
                LocalRecycledFile.insert(db, this.mApplication.getContentResolver(), this.id, values, data);
            }
            if (this.mCloudMediaId != -1) {
                onCloudRecycleProcess(db, 2, null);
            }
        }
        delete(db, 3);
    }

    protected ContentValues getValues() {
        return getLocalValues();
    }

    public boolean delete(SQLiteDatabase db, int flag) {
        GalleryUtils.assertNotInRenderThread();
        boolean result = true;
        super.delete();
        Uri baseUri = isVideo() ? Media.EXTERNAL_CONTENT_URI : Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = this.mApplication.getContentResolver();
        try {
            if (!(RecycleUtils.supportRecycle() && PhotoShareUtils.isGUIDSupport())) {
                if (!(this.mLocalMediaId == -1 || this.mCloudMediaId != -1 || TextUtils.isEmpty(PhotoShareUtils.getAutoUploadAlbumIdByBucketId(String.valueOf(this.bucketId))))) {
                    PhotoShareUtils.addPhotos(PhotoShareUtils.getDeletedFileIdentify(this.mHash, this.mBucketRelativePath));
                }
            }
            if ((flag & 1) != 0) {
                if (this.mLocalMediaId != -1) {
                    resolver.delete(baseUri, "_id=?", new String[]{String.valueOf(this.mLocalMediaId)});
                    this.mDownloadState = 0;
                    if (this.mCloudMediaId == -1) {
                        if (db != null) {
                            db.delete("gallery_media", "_id= ? ", new String[]{String.valueOf(this.id)});
                            resolver.notifyChange(GalleryMedia.URI, null);
                        } else {
                            resolver.delete(GalleryMedia.URI, "_id= ? ", new String[]{String.valueOf(this.id)});
                        }
                        result = true;
                    } else {
                        result = false;
                    }
                } else {
                    result = false;
                }
            }
            if ((flag & 2) != 0) {
                if (this.mCloudMediaId != -1) {
                    if (!RecycleUtils.supportRecycle()) {
                        ContentValues values = new ContentValues();
                        values.put("deleteFlag", Integer.valueOf(1));
                        if (db != null) {
                            db.update("cloud_file", values, "id=?", new String[]{String.valueOf(this.mCloudMediaId)});
                            resolver.notifyChange(PhotoShareConstants.CLOUD_FILE_TABLE_URI, null);
                        } else {
                            resolver.update(PhotoShareConstants.CLOUD_FILE_TABLE_URI, values, "id=?", new String[]{String.valueOf(this.mCloudMediaId)});
                        }
                    }
                    if (!(PhotoShareUtils.getServer() == null || TextUtils.isEmpty(this.mCloudBucketID) || RecycleUtils.supportRecycle())) {
                        new Thread() {
                            public void run() {
                                GalleryMediaItem.detachRelativePath(GalleryMediaItem.this.mApplication.getAndroidContext(), GalleryMediaItem.this.mCloudMediaId);
                            }
                        }.start();
                    }
                    if (db != null) {
                        db.delete("gallery_media", "_id= ? ", new String[]{String.valueOf(this.id)});
                    } else {
                        resolver.delete(GalleryMedia.URI, "_id= ? ", new String[]{String.valueOf(this.id)});
                    }
                }
                result = true;
            }
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("GalleryMediaItem");
        }
        this.mApplication.getDataManager().broadcastLocalDeletion();
        GalleryLog.d("GalleryMediaItem", "have deleted image:" + getFilePath());
        return result;
    }

    public static void detachRelativePath(Context context, int cloudMediaId) {
        SQLiteException e;
        int i;
        Throwable th;
        String str;
        String str2;
        String str3;
        Closeable closeable = null;
        String str4;
        try {
            closeable = context.getContentResolver().query(GalleryMedia.URI, new String[]{"_data", "bucket_id", "bucket_relative_path", "hash", "media_type", "cloud_media_id"}, "relative_cloud_media_id=?", new String[]{String.valueOf(cloudMediaId)}, null, null);
            if (closeable == null) {
                Utils.closeSilently(closeable);
                return;
            }
            GalleryLog.d("GalleryMediaItem", "relative cloudMediaId " + cloudMediaId + " count " + closeable.getCount());
            if (closeable.moveToNext()) {
                try {
                    try {
                        try {
                            try {
                            } catch (SQLiteException e2) {
                                e = e2;
                                i = -1;
                                try {
                                    GalleryLog.e("photoshareLogTag", "relativeWithOtherFile query cloud album err " + e.toString());
                                    Utils.closeSilently(closeable);
                                } catch (Throwable th2) {
                                    th = th2;
                                    Utils.closeSilently(closeable);
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                i = -1;
                                Utils.closeSilently(closeable);
                                throw th;
                            }
                        } catch (SQLiteException e3) {
                            e = e3;
                            i = -1;
                            str = null;
                            GalleryLog.e("photoshareLogTag", "relativeWithOtherFile query cloud album err " + e.toString());
                            Utils.closeSilently(closeable);
                        } catch (Throwable th4) {
                            th = th4;
                            i = -1;
                            str = null;
                            Utils.closeSilently(closeable);
                            throw th;
                        }
                        try {
                            PhotoShareUtils.checkAutoUpload(closeable.getInt(5), closeable.getString(0), closeable.getString(1), closeable.getString(2), closeable.getString(3), closeable.getInt(4), false);
                            ContentValues values = new ContentValues();
                            values.put("relative_cloud_media_id", Integer.valueOf(-1));
                            context.getContentResolver().update(GalleryMedia.URI, values, "relative_cloud_media_id=?", new String[]{String.valueOf(cloudMediaId)});
                        } catch (SQLiteException e4) {
                            e = e4;
                            GalleryLog.e("photoshareLogTag", "relativeWithOtherFile query cloud album err " + e.toString());
                            Utils.closeSilently(closeable);
                        }
                    } catch (SQLiteException e5) {
                        e = e5;
                        i = -1;
                        str = null;
                        str2 = null;
                        GalleryLog.e("photoshareLogTag", "relativeWithOtherFile query cloud album err " + e.toString());
                        Utils.closeSilently(closeable);
                    } catch (Throwable th5) {
                        th = th5;
                        i = -1;
                        str = null;
                        str2 = null;
                        Utils.closeSilently(closeable);
                        throw th;
                    }
                } catch (SQLiteException e6) {
                    e = e6;
                    i = -1;
                    str = null;
                    str2 = null;
                    str3 = null;
                    GalleryLog.e("photoshareLogTag", "relativeWithOtherFile query cloud album err " + e.toString());
                    Utils.closeSilently(closeable);
                } catch (Throwable th6) {
                    th = th6;
                    i = -1;
                    str = null;
                    str2 = null;
                    str3 = null;
                    Utils.closeSilently(closeable);
                    throw th;
                }
            }
            i = -1;
            str = null;
            str2 = null;
            str3 = null;
            str4 = null;
            Utils.closeSilently(closeable);
        } catch (SQLiteException e7) {
            e = e7;
            i = -1;
            str = null;
            str2 = null;
            str3 = null;
            str4 = null;
            GalleryLog.e("photoshareLogTag", "relativeWithOtherFile query cloud album err " + e.toString());
            Utils.closeSilently(closeable);
        } catch (Throwable th7) {
            th = th7;
            i = -1;
            str = null;
            str2 = null;
            str3 = null;
            str4 = null;
            Utils.closeSilently(closeable);
            throw th;
        }
    }

    public boolean delete(int flag) {
        return delete(null, flag);
    }

    public void delete() {
        delete(1);
    }

    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        details.addDetail(998, Integer.valueOf(this.rotation));
        if (this.isDrm) {
            GalleryLog.printDFXLog("image is drm");
            MediaDetails.extractDrmInfo(details, this);
        }
        if ("image/jpeg".equals(this.mimeType)) {
            GalleryLog.printDFXLog("image is jpeg");
            MediaDetails.extractExifInfo(details, this.filePath);
        }
        if (isBurstCover()) {
            details.addDetail(5, Long.valueOf(getSize()));
        }
        if (this.mLocalMediaId == -1) {
            details.deleteDetail(SmsCheckResult.ESCT_200);
            details.deleteDetail(5);
            details.addDetail(5, Long.valueOf(PhotoShareUtils.getFileSize(this.filePath)));
            details.deleteDetail(6);
            details.deleteDetail(7);
            updateWidthAndHeight();
            details.addDetail(6, Integer.valueOf(this.width));
            details.addDetail(7, Integer.valueOf(this.height));
        }
        details.deleteDetail(3);
        details.addDetail(3, Long.valueOf(this.mShowDateToken));
        return details;
    }

    public long getSize() {
        if (this.mLocalMediaId == -1) {
            return PhotoShareUtils.getFileSize(this.filePath);
        }
        return super.getSize();
    }

    public int getWidth() {
        return this.width;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public int getRotation() {
        return this.rotation;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean rename(String newName) {
        if (this.mLocalMediaId != -1 && !super.rename(newName)) {
            return false;
        }
        if (this.mCloudMediaId != -1) {
            String displayName = newName + this.mDisplayName.substring(this.mDisplayName.lastIndexOf("."));
            try {
                ContentValues values = new ContentValues();
                values.put("dirty", Integer.valueOf(2));
                values.put("title", newName);
                values.put("_display_name", displayName);
                this.mApplication.getContentResolver().update(GalleryMedia.URI, values, "_id=?", new String[]{String.valueOf(this.id)});
            } catch (Exception e) {
                GalleryLog.e("photoshareLogTag", "update name exception " + e.toString());
            }
            final FileInfo fileInfo = PhotoShareTagAlbum.getFileInfo(this.mApplication, this.mCloudBucketID, this.mHash);
            if (PhotoShareUtils.isGUIDSupport()) {
                this.mFileInfo.setFileName(displayName);
            } else if (fileInfo == null) {
                return false;
            } else {
                fileInfo.setFileName(displayName);
            }
            new Thread() {
                public void run() {
                    try {
                        int result = PhotoShareUtils.getServer().modifyGeneralFile(PhotoShareUtils.isGUIDSupport() ? GalleryMediaItem.this.mFileInfo : fileInfo);
                        GalleryLog.e("photoshareLogTag", "mCloudBucketID " + GalleryMediaItem.this.mCloudBucketID + " " + "filePath " + GalleryMediaItem.this.filePath + " mBucketRelativePath " + GalleryMediaItem.this.mBucketRelativePath);
                        GalleryLog.e("photoshareLogTag", "renameGeneralFile result " + result);
                    } catch (RemoteException e) {
                        PhotoShareUtils.dealRemoteException(e);
                    }
                }
            }.start();
        }
        return true;
    }

    public int getRefocusPhotoType() {
        return this.mRefocusPhoto;
    }

    public int getDrmType() {
        return DrmUtils.getObjectType(getFilePath());
    }

    public boolean hasRight() {
        return DrmUtils.haveRightsForAction(this.filePath, 7);
    }

    public int getRightCount() {
        return DrmUtils.getRightCount(this.filePath, 7);
    }

    public boolean hasCountConstraint() {
        return DrmUtils.haveCountConstraints(this.filePath, 7);
    }

    public boolean getRight() {
        return DrmUtils.haveRightsForAction(this.filePath, ApiHelper.DRMSTORE_ACTION_SHOW_DIALOG | 7);
    }

    protected void updateWidthAndHeight() {
        if (this.width == 0 || this.height == 0) {
            Point bounds = GalleryUtils.decodeBounds(this.filePath);
            if (bounds != null) {
                GalleryLog.v("GalleryMediaItem", "decode bounds for GalleryMediaItem.");
                this.width = bounds.x;
                this.height = bounds.y;
            }
        }
    }

    public boolean isCloudPlaceholder() {
        if (this.mLocalMediaId == -1 && this.filePath != null && this.filePath.startsWith("cloud-")) {
            return true;
        }
        return false;
    }

    protected boolean supportAutoUpload() {
        if (PhotoShareUtils.getHasNeverSynchronizedCloudDataFromCache() || TextUtils.isEmpty(PhotoShareUtils.getUserId()) || !PhotoShareUtils.getLocalSwitch() || PhotoShareUtils.getAutoUploadAlbumIdByBucketId(String.valueOf(this.bucketId)) == null) {
            return false;
        }
        return true;
    }

    public boolean isLCDDownloaded() {
        return canShare();
    }

    public void removeFromStoryAlbum(String code) {
        GalleryUtils.assertNotInRenderThread();
        StoryAlbumUtils.removeStoryAlbumFile(this.id, code, this.mApplication.getAndroidContext().getContentResolver());
    }

    public String toString() {
        return new StringBuffer().append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append("\n caption: ").append(this.caption).append("\n mimeType: ").append(this.mimeType).append("\n bucketId: ").append(this.bucketId).append("\n fileSize: ").append(this.fileSize).append("\n mLocalMediaId: ").append(this.mLocalMediaId).append("\n mCloudMediaId: ").append(this.mCloudMediaId).append("\n mMediaType: ").append(this.mMediaType).toString();
    }

    public void onCloudRecycleProcess(SQLiteDatabase db, int recycleFlag, String sourcePath) {
        if (this.mCloudMediaId != -1) {
            switch (recycleFlag) {
                case 1:
                    CloudRecycleTableOperateHelper.recoverFromRecycleBin(db, this.mCloudMediaId, sourcePath, this.mApplication.getAndroidContext());
                    break;
                case 2:
                    CloudRecycleTableOperateHelper.moveToRecycleBin(db, (long) this.id, (long) this.mCloudMediaId, this.mApplication.getAndroidContext());
                    break;
                case 3:
                    CloudRecycleTableOperateHelper.deleteFromRecycleBin(db, this.mCloudMediaId);
                    break;
            }
        }
    }
}
