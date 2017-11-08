package com.android.gallery3d.data;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.app.PanoramaMetadataSupport;
import com.android.gallery3d.app.StitchingProgressManager;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.exif.ExifTag;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.BusinessRadar.BugType;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.OptionsCode;
import com.android.gallery3d.util.RegionDecoderReporter;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.ThumbnailReporter;
import com.android.gallery3d.util.TraceController;
import com.android.gallery3d.util.UpdateHelper;
import com.huawei.gallery.burst.BurstPhotoSet;
import com.huawei.gallery.extfile.FyuseFile;
import com.huawei.gallery.livephoto.LiveUtils;
import com.huawei.gallery.photorectify.RectifyUtils;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.threedmodel.ThreeDModelImageUtils;
import com.huawei.gallery.util.BurstUtils;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LocalImage extends LocalMediaItem implements IImage {
    public static final Path ITEM_PATH = Path.fromString("/local/image/item");
    static final String[] PROJECTION;
    private Path mBurstSetPath = null;
    private boolean mIsBurstCover = false;
    private PanoramaMetadataSupport mPanoramaMetadata = new PanoramaMetadataSupport(this);
    private int mRectifyOffset;
    private boolean mRefocusFlagChange = false;
    private int mRefocusPhoto;
    private int mSupportRename = -1;
    private long mVoiceOffset;
    public int rotation;

    private static class CacheImageRequest extends BaseJob<Bitmap> {
        private final int mBucketId;
        private final String mFilePath;

        public CacheImageRequest(int bucketId, String localFilePath) {
            this.mBucketId = bucketId;
            this.mFilePath = localFilePath;
        }

        public Bitmap run(JobContext jc) {
            Bitmap bitmap = null;
            TraceController.traceBegin("LocalImage.CacheImageRequest.run");
            if (this.mBucketId == MediaSetUtils.getCameraBucketId() || GalleryStorageManager.getInstance().isOuterGalleryStorageCameraBucketID(this.mBucketId)) {
                long startTime = System.currentTimeMillis();
                bitmap = DecodeUtils.decodeFromCache(this.mFilePath);
                GalleryLog.d("LocalImage", "decodeFromCache sourcePath:" + this.mFilePath + " time:" + (System.currentTimeMillis() - startTime) + " Bitmap:" + bitmap);
            }
            TraceController.traceEnd();
            return bitmap;
        }

        public String workContent() {
            return String.format("request cache image. bucketId: %s, filePath: %s", new Object[]{Integer.valueOf(this.mBucketId), this.mFilePath});
        }
    }

    private static class LocalImageRequest extends ImageCacheRequest {
        private final String mLocalFilePath;
        private final LocalImage mMediaItem;

        LocalImageRequest(GalleryApp application, Path path, LocalImage mediaItem, long timeModified, int type, String localFilePath) {
            super(application, path, timeModified, type, MediaItem.getTargetSize(type));
            this.mLocalFilePath = localFilePath;
            this.mMediaItem = mediaItem;
        }

        public Bitmap onDecodeOriginal(JobContext jc, int type) {
            long startTime = System.currentTimeMillis();
            Options options = new Options();
            options.inPreferredConfig = Config.ARGB_8888;
            int targetSize = MediaItem.getTargetSize(type);
            if (DrmUtils.isDrmFile(this.mLocalFilePath)) {
                DrmUtils.inDrmMode(options);
            }
            if (type == 2) {
                if (DrmUtils.isDrmFile(this.mLocalFilePath)) {
                    DrmUtils.inPreviewMode(options);
                } else if (this.mMediaItem == null || !(this.mMediaItem.isRefocusPhoto() || this.mMediaItem.isRectifyImage())) {
                    ExifInterface exif = new ExifInterface();
                    byte[] thumbData = null;
                    try {
                        exif.readExif(this.mLocalFilePath);
                        thumbData = exif.getThumbnail();
                    } catch (FileNotFoundException e) {
                        GalleryLog.w("LocalImage", "failed to find file to read thumbnail: " + this.mLocalFilePath);
                    } catch (Throwable t) {
                        GalleryLog.w("LocalImage", "fail to get exif thumb." + t.getMessage());
                    }
                    if (thumbData != null) {
                        Bitmap bitmap = DecodeUtils.decodeIfBigEnough(jc, thumbData, options, targetSize);
                        if (bitmap != null) {
                            deleteCacheFile();
                            return bitmap;
                        }
                    }
                }
            }
            TraceController.beginSection("decodeThumbnail type: " + type + " from file: " + this.mLocalFilePath);
            Bitmap result = DecodeUtils.decodeThumbnail(jc, this.mLocalFilePath, options, targetSize, type);
            TraceController.endSection();
            GalleryLog.d("LocalImage", "onDecodeOriginal filePath:" + this.mLocalFilePath + " type:" + type + " time:" + (System.currentTimeMillis() - startTime));
            if (result != null) {
                deleteCacheFile();
                return result;
            }
            return decodeFromCache(jc, this.mLocalFilePath, this.mMediaItem.bucketId, type, OptionsCode.getErrorCode(options));
        }

        private Bitmap decodeFromCache(JobContext jc, String path, int bucketId, int type, int errorCode) {
            Bitmap bitmap = null;
            boolean shouldReportHint = true;
            if (bucketId == MediaSetUtils.getCameraBucketId() || GalleryStorageManager.getInstance().isOuterGalleryStorageCameraBucketID(bucketId)) {
                if (type == 2) {
                    long startTime = System.currentTimeMillis();
                    TraceController.beginSection("decodeFromCache DCIM/Camera/cache");
                    bitmap = DecodeUtils.decodeFromCache(path);
                    TraceController.endSection();
                    GalleryLog.d("LocalImage", "decodeFromCache sourcePath:" + this.mLocalFilePath + " type:" + type + " time:" + (System.currentTimeMillis() - startTime));
                }
            } else if (type == 1) {
                shouldReportHint = false;
            }
            if (shouldReportHint && !jc.isCancelled() && (errorCode != 0 || bitmap == null)) {
                ThumbnailReporter.reportThumbnailFail(BugType.DECODE_THUMB_FAILED_IMAGE, errorCode, this.mLocalFilePath, null);
            }
            return bitmap;
        }

        private void deleteCacheFile() {
            int bucketId = this.mMediaItem.bucketId;
            TraceController.traceBegin("LocalImage.deleteCacheFile");
            if (bucketId == MediaSetUtils.getCameraBucketId() || GalleryStorageManager.getInstance().isOuterGalleryStorageCameraBucketID(bucketId)) {
                DecodeUtils.deleteCacheFile(this.mLocalFilePath);
            }
            TraceController.traceEnd();
        }

        public String workContent() {
            return "decode thumbnail for file: " + this.mLocalFilePath;
        }
    }

    public static class LocalLargeImageRequest extends BaseJob<BitmapRegionDecoder> {
        byte[] mDataBytes;
        int mDataLength;
        int mDataOffset;
        String mLocalFilePath;

        public LocalLargeImageRequest(String localFilePath) {
            this.mLocalFilePath = localFilePath;
        }

        public LocalLargeImageRequest(byte[] bytes, int offset, int length) {
            if (bytes != null) {
                this.mDataBytes = (byte[]) bytes.clone();
            }
            this.mDataOffset = offset;
            this.mDataLength = length;
        }

        public BitmapRegionDecoder run(JobContext jc) {
            BitmapRegionDecoder brd;
            TraceController.traceBegin("LocalImage.LocalLargeImageRequest.createBitmapRegionDecoder");
            if (this.mDataBytes != null) {
                brd = DecodeUtils.createBitmapRegionDecoder(jc, this.mDataBytes, this.mDataOffset, this.mDataLength, false);
            } else {
                brd = DecodeUtils.createBitmapRegionDecoder(jc, this.mLocalFilePath, false);
                if (brd == null) {
                    GalleryLog.w("LocalImage", "get region decoder is null:" + this.mLocalFilePath);
                    RegionDecoderReporter.reportRegionDecoderFail(BugType.REGION_DECODER_IS_NULL_FAILED, this.mLocalFilePath, null);
                }
            }
            TraceController.traceEnd();
            return brd;
        }

        public String workContent() {
            return "create region decoder with " + (this.mDataBytes != null ? "bytes" : "file: " + this.mLocalFilePath);
        }
    }

    static {
        r0 = new String[23];
        r0[15] = String.format("strftime('%%Y%%m', %s / 1000, 'unixepoch') AS normalized_date", new Object[]{"datetaken"});
        r0[16] = "0";
        r0[17] = "0";
        r0[18] = "0";
        r0[19] = "0";
        r0[20] = "0";
        r0[21] = "0";
        r0[22] = "_display_name";
        PROJECTION = r0;
        updateWidthAndHeightProjection();
    }

    public static String[] copyProjection() {
        return (String[]) PROJECTION.clone();
    }

    @TargetApi(16)
    private static void updateWidthAndHeightProjection() {
        if (ApiHelper.HAS_MEDIA_COLUMNS_WIDTH_AND_HEIGHT) {
            PROJECTION[12] = "width";
            PROJECTION[13] = "height";
        }
        if (ApiHelper.HAS_IMAGES_COLUMNS_IS_HDR) {
            PROJECTION[14] = "is_hdr";
        }
        if (ApiHelper.HAS_MEDIA_COLUMNS_HW_VOICE_OFFSET) {
            PROJECTION[16] = "IFNULL(hw_voice_offset,0)";
        }
        if (ApiHelper.HAS_MEDIA_COLUMNS_HW_REFOCUS_IMAGE) {
            PROJECTION[18] = "IFNULL(hw_image_refocus,0)";
        }
        if (ApiHelper.HAS_MEDIA_COLUMNS_HW_RECTIFY_OFFSET) {
            PROJECTION[19] = "IFNULL(hw_rectify_offset,0)";
        }
        if (ApiHelper.HAS_MEDIA_COLUMNS_SPECIAL_FILE_TYPE) {
            PROJECTION[20] = "special_file_type";
        }
        if (ApiHelper.HAS_MEDIA_COLUMNS_SPECIAL_FILE_OFFSET) {
            PROJECTION[21] = "special_file_offset";
        }
    }

    public LocalImage(Path path, GalleryApp application, Cursor cursor) {
        super(path, MediaObject.nextVersionNumber(), application);
        loadFromCursor(cursor);
    }

    public LocalImage(Path path, GalleryApp application, int id) {
        super(path, MediaObject.nextVersionNumber(), application);
        Closeable closeable = null;
        try {
            closeable = LocalAlbum.getItemCursor(this.mApplication.getContentResolver(), Media.EXTERNAL_CONTENT_URI, PROJECTION, id);
            if (closeable == null) {
                throw new RuntimeException("cannot get cursor for: " + path);
            } else if (closeable.moveToNext()) {
                loadFromCursor(closeable);
            } else {
                path.clearObject();
                throw new RuntimeException("cannot find image data for: " + path);
            }
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("LocalImage");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    private void loadFromCursor(Cursor cursor) {
        boolean z;
        boolean z2 = true;
        this.id = cursor.getInt(0);
        this.caption = cursor.getString(1);
        this.mimeType = cursor.getString(2);
        this.longitude = cursor.getDouble(4);
        this.latitude = cursor.getDouble(3);
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
        this.mSpecialFileType = cursor.getInt(20);
        this.mSpecialFileOffset = cursor.getLong(21);
        this.displayName = cursor.getString(22);
        updateBurstCover();
    }

    protected boolean updateFromCursor(Cursor cursor) {
        boolean z;
        boolean z2 = true;
        UpdateHelper uh = new UpdateHelper();
        this.id = uh.update(this.id, cursor.getInt(0));
        this.caption = (String) uh.update(this.caption, cursor.getString(1));
        this.mimeType = (String) uh.update(this.mimeType, cursor.getString(2));
        this.latitude = uh.update(this.latitude, cursor.getDouble(3));
        this.longitude = uh.update(this.longitude, cursor.getDouble(4));
        this.dateTakenInMs = uh.update(this.dateTakenInMs, cursor.getLong(5));
        this.dateAddedInSec = uh.update(this.dateAddedInSec, cursor.getLong(6));
        this.dateModifiedInSec = uh.update(this.dateModifiedInSec, cursor.getLong(7));
        this.filePath = (String) uh.update(this.filePath, checkFilePathNull(cursor.getString(8)));
        this.rotation = uh.update(this.rotation, cursor.getInt(9));
        this.bucketId = uh.update(this.bucketId, cursor.getInt(10));
        this.fileSize = uh.update(this.fileSize, cursor.getLong(11));
        this.height = uh.update(this.height, cursor.getInt(13));
        this.width = uh.update(this.width, cursor.getInt(12));
        Object valueOf = Boolean.valueOf(this.is_hdr);
        if (cursor.getInt(14) > 0) {
            z = true;
        } else {
            z = false;
        }
        this.is_hdr = ((Boolean) uh.update(valueOf, Boolean.valueOf(z))).booleanValue();
        this.isDrm = DrmUtils.isDrmFile(this.filePath);
        this.normalizedDate = uh.update(this.normalizedDate, cursor.getInt(15));
        this.mVoiceOffset = uh.update(this.mVoiceOffset, cursor.getLong(16));
        int oldRefocus = this.mRefocusPhoto;
        this.mRefocusPhoto = uh.update(this.mRefocusPhoto, cursor.getInt(18));
        if (oldRefocus == this.mRefocusPhoto) {
            z2 = false;
        }
        this.mRefocusFlagChange = z2;
        this.mRectifyOffset = uh.update(this.mRectifyOffset, cursor.getInt(19));
        this.mSpecialFileType = uh.update(this.mSpecialFileType, cursor.getInt(20));
        this.mSpecialFileOffset = uh.update(this.mSpecialFileOffset, cursor.getLong(21));
        this.displayName = (String) uh.update(this.displayName, cursor.getString(22));
        updateBurstCover();
        return uh.isUpdated();
    }

    private void updateBurstCover() {
        boolean z = false;
        this.mBurstSetPath = BurstUtils.getBurstSetPath(this.displayName, this.bucketId, false, this.mApplication.getContentResolver());
        if (this.mBurstSetPath != null) {
            z = true;
        }
        this.mIsBurstCover = z;
    }

    public Job<Bitmap> requestImage(int type) {
        return new LocalImageRequest(this.mApplication, this.mPath, this, this.dateModifiedInSec, type, this.filePath);
    }

    public Bitmap getLatestCacheImage() {
        TraceController.traceBegin("LocalImage.getLatestCacheImage");
        if (this.bucketId == MediaSetUtils.getCameraBucketId() || GalleryStorageManager.getInstance().isOuterGalleryStorageCameraBucketID(this.bucketId)) {
            long startTime = System.currentTimeMillis();
            TraceController.beginSection("getLatestCacheImage decode from DCIM/Camera/latest");
            Bitmap result = DecodeUtils.decodeFromCacheLatest(this.filePath);
            TraceController.endSection();
            GalleryLog.d("LocalImage", "getLatestCacheImage sourcePath:" + this.filePath + " time:" + (System.currentTimeMillis() - startTime) + " Bitmap:" + result);
            TraceController.traceEnd();
            return result;
        }
        TraceController.traceEnd();
        return null;
    }

    public Job<Bitmap> requestCacheImage() {
        return new CacheImageRequest(this.bucketId, this.filePath);
    }

    public Job<BitmapRegionDecoder> requestLargeImage() {
        return new LocalLargeImageRequest(this.filePath);
    }

    public Job<BitmapRegionDecoder> requestLargeImage(byte[] bytes, int offset, int length) {
        return new LocalLargeImageRequest(bytes, offset, length);
    }

    public int getSupportedOperations() {
        int i = 0;
        StitchingProgressManager progressManager = this.mApplication.getStitchingProgressManager();
        if (progressManager != null && progressManager.getProgress(getContentUri()) != null) {
            return 0;
        }
        TraceController.traceBegin("LocalImage.getSupportedOperations");
        int operation = 25297921;
        if (this.mVoiceOffset > 0) {
            operation = 159515649;
        }
        if (this.mRefocusPhoto > 0) {
            operation |= 262144;
        }
        if (this.mSupportRename == -1 && this.filePath != null) {
            File file = new File(this.filePath);
            if (file.exists()) {
                if (file.canWrite()) {
                    i = 1;
                }
                this.mSupportRename = i;
            }
        }
        if (!this.mIsBurstCover && this.mSupportRename == 1) {
            operation |= 1073741824;
        }
        if (GalleryUtils.isSupportMyFavorite()) {
            operation |= 536870912;
        }
        boolean canEditOrCrop = BitmapUtils.isFilterShowSupported(this.mimeType);
        if (!this.isDrm && canEditOrCrop) {
            operation |= 520;
        }
        if (!this.isDrm || canForward()) {
            operation |= 4;
        }
        if (DrmUtils.canSetAsWallPaper(this)) {
            operation |= 32;
        }
        if (BitmapUtils.isSupportedByRegionDecoder(this.mimeType) && (!this.isDrm || hasRight())) {
            operation |= 64;
        }
        if (!(!BitmapUtils.isRotationSupported(this.mimeType) || this.mIsBurstCover || is3DPanorama())) {
            operation |= 2;
        }
        if (GalleryUtils.isValidLocation(this.latitude, this.longitude)) {
            operation |= 16;
        }
        TraceController.traceEnd();
        return operation;
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
        values.put("orientation", Integer.valueOf(this.rotation));
        values.put("bucket_id", Integer.valueOf(this.bucketId));
        values.put("_size", Long.valueOf(this.fileSize));
        values.put("width", Integer.valueOf(this.width));
        values.put("height", Integer.valueOf(this.height));
        values.put("_display_name", this.displayName);
        values.put("is_hw_burst", Integer.valueOf(this.mIsBurstCover ? 1 : 0));
        values.put("is_hdr", Boolean.valueOf(this.is_hdr));
        values.put("hw_voice_offset", Long.valueOf(this.mVoiceOffset));
        values.put("hw_image_refocus", Integer.valueOf(this.mRefocusPhoto));
        values.put("hw_rectify_offset", Integer.valueOf(this.mRectifyOffset));
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
            GalleryLog.noPermissionForMediaProviderLog("LocalImage");
        }
        this.mApplication.getDataManager().broadcastLocalDeletion();
        GalleryLog.d("LocalImage", "have deleted image:" + getFilePath());
    }

    public int getVirtualFlags() {
        int flag = super.getVirtualFlags();
        if (!is3DPanorama()) {
            return flag;
        }
        GalleryStorage innerStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        if (innerStorage == null || !this.filePath.contains(innerStorage.getPath() + Constant.CAMERA_PATH)) {
            return flag;
        }
        return flag | 16;
    }

    public void rotate(int degrees) {
        GalleryUtils.assertNotInRenderThread();
        Uri baseUri = Media.EXTERNAL_CONTENT_URI;
        ContentValues values = new ContentValues();
        int rotation = (this.rotation + degrees) % 360;
        if (rotation < 0) {
            rotation += 360;
        }
        if ("image/jpeg".equalsIgnoreCase(this.mimeType)) {
            ExifInterface exifInterface = new ExifInterface();
            ExifTag tag = exifInterface.buildTag(ExifInterface.TAG_ORIENTATION, Short.valueOf(ExifInterface.getOrientationValueForRotation(rotation)));
            if (tag != null) {
                exifInterface.setTag(tag);
                try {
                    exifInterface.forceRewriteExif(this.filePath);
                    this.fileSize = new File(this.filePath).length();
                    values.put("_size", Long.valueOf(this.fileSize));
                } catch (FileNotFoundException e) {
                    GalleryLog.w("LocalImage", "cannot find file to set exif: " + this.filePath);
                } catch (IOException e2) {
                    GalleryLog.w("LocalImage", "cannot set exif data: " + this.filePath);
                }
            } else {
                GalleryLog.w("LocalImage", "Could not build tag: " + ExifInterface.TAG_ORIENTATION);
            }
        }
        values.put("orientation", Integer.valueOf(rotation));
        try {
            this.mApplication.getContentResolver().update(baseUri, values, "_id=?", new String[]{String.valueOf(this.id)});
        } catch (SecurityException e3) {
            GalleryLog.noPermissionForMediaProviderLog("LocalImage");
        }
    }

    public boolean supportComment() {
        return "image/jpeg".equalsIgnoreCase(this.mimeType);
    }

    public Uri getContentUri() {
        return Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(String.valueOf(this.id)).build();
    }

    public int getMediaType() {
        return 2;
    }

    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        details.addDetail(998, Integer.valueOf(this.rotation));
        if (this.isDrm) {
            MediaDetails.extractDrmInfo(details, this);
        }
        if ("image/jpeg".equals(this.mimeType)) {
            MediaDetails.extractExifInfo(details, this.filePath);
        }
        if (isBurstCover()) {
            details.addDetail(5, Long.valueOf(getSize()));
        }
        return details;
    }

    public long getSize() {
        if (isBurstCover()) {
            BurstPhotoSet burstSet = (BurstPhotoSet) this.mApplication.getDataManager().getMediaSet(getBurstSetPath());
            if (burstSet != null) {
                return burstSet.getBurstSize();
            }
        }
        return super.getSize();
    }

    public int getRotation() {
        return this.rotation;
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

    public long getVoiceOffset() {
        return this.mVoiceOffset;
    }

    public boolean isVoiceImage() {
        return this.mVoiceOffset > 0;
    }

    public boolean is3DPanorama() {
        if (FyuseFile.isSupport3DPanoramaSDK() && this.mSpecialFileType == 20) {
            return true;
        }
        if (FyuseFile.isSupport3DPanoramaAPK() && this.mSpecialFileType == 11) {
            return true;
        }
        return false;
    }

    public boolean isRefocusPhoto() {
        return this.mRefocusPhoto > 0;
    }

    public boolean isRectifyImage() {
        if (RectifyUtils.isRectifyNativeSupport() && BitmapUtils.isRectifySupported(this.mimeType) && this.mRectifyOffset > 0) {
            return true;
        }
        return false;
    }

    public boolean is3DModelImage() {
        return ThreeDModelImageUtils.is3DModelImageSpecialFileType(this.mSpecialFileType);
    }

    public int getSpecialFileType() {
        if (50 != this.mSpecialFileType || LiveUtils.LIVE_ENABLE) {
            return this.mSpecialFileType;
        }
        return 0;
    }

    public int getRectifyOffset() {
        return this.mRectifyOffset;
    }

    public int getRefocusPhotoType() {
        return this.mRefocusPhoto;
    }

    public int getDrmType() {
        return DrmUtils.getObjectType(getFilePath());
    }

    public int getRightCount() {
        return DrmUtils.getRightCount(this.filePath, 7);
    }

    public boolean hasRight() {
        return DrmUtils.haveRightsForAction(this.filePath, 7);
    }

    public boolean getRight() {
        return DrmUtils.haveRightsForAction(this.filePath, ApiHelper.DRMSTORE_ACTION_SHOW_DIALOG | 7);
    }

    public boolean hasCountConstraint() {
        return DrmUtils.haveCountConstraints(this.filePath, 7);
    }

    protected void updateWidthAndHeight() {
        if (this.width == 0 || this.height == 0) {
            Point bounds = GalleryUtils.decodeBounds(this.filePath);
            if (bounds != null) {
                GalleryLog.v("LocalImage", "decode bounds for LocalImage.");
                this.width = bounds.x;
                this.height = bounds.y;
            }
        }
    }

    public boolean isBurstCover() {
        return this.mIsBurstCover;
    }

    public Path getBurstSetPath() {
        return this.mBurstSetPath;
    }

    public void setAsFavorite(Context context) {
        super.setAsFavorite(context);
        int galleryMediaId = getGalleryMediaId(context);
        if (galleryMediaId != -1) {
            MediaObject object = this.mApplication.getDataManager().peekMediaObject(Path.fromString("/gallery/image/item/" + galleryMediaId));
            if (object != null) {
                ((GalleryMediaItem) object).updateMyFavorite(true);
            }
        }
    }

    public void cancelFavorite(Context context) {
        super.cancelFavorite(context);
        int galleryMediaId = getGalleryMediaId(context);
        if (galleryMediaId != -1) {
            MediaObject object = this.mApplication.getDataManager().peekMediaObject(Path.fromString("/gallery/image/item/" + galleryMediaId));
            if (object != null) {
                ((GalleryMediaItem) object).updateMyFavorite(false);
            }
        }
    }

    public boolean isSupportTranslateVoiceImageToVideo() {
        return isVoiceImage() && !isDrm();
    }

    public String toString() {
        return new StringBuffer().append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append("\n caption: ").append(this.caption).append("\n mimeType: ").append(this.mimeType).append("\n bucketId: ").append(this.bucketId).append("\n fileSize: ").append(this.fileSize).toString();
    }
}
