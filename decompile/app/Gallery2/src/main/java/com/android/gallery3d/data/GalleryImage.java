package com.android.gallery3d.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.app.PanoramaMetadataSupport;
import com.android.gallery3d.app.StitchingProgressManager;
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
import com.fyusion.sdk.common.ext.util.exif.ExifInterface.GpsMeasureMode;
import com.huawei.gallery.burst.BurstPhotoSet;
import com.huawei.gallery.extfile.FyuseFile;
import com.huawei.gallery.livephoto.LiveUtils;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.photorectify.RectifyUtils;
import com.huawei.gallery.photoshare.utils.MD5Utils;
import com.huawei.gallery.photoshare.utils.PhotoShareConstants;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.threedmodel.ThreeDModelImageUtils;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class GalleryImage extends GalleryMediaItem implements IImage {
    public static final Path IMAGE_PATH = Path.fromString("/gallery/image/item/");
    private PanoramaMetadataSupport mPanoramaMetadata = new PanoramaMetadataSupport(this);

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
                GalleryLog.d("GalleryImage", "decodeFromCache sourcePath:" + this.mFilePath + " time:" + (System.currentTimeMillis() - startTime) + " Bitmap:" + bitmap);
            }
            TraceController.traceEnd();
            return bitmap;
        }

        public String workContent() {
            return String.format("request cache image. bucketId: %s, filePath: %s", new Object[]{Integer.valueOf(this.mBucketId), this.mFilePath});
        }
    }

    private static class GalleryImageRequest extends ImageCacheRequest {
        private final String mLocalFilePath;
        private final GalleryImage mMediaItem;

        GalleryImageRequest(GalleryApp application, Path path, GalleryImage mediaItem, long timeModified, int type, String localFilePath) {
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
                        GalleryLog.w("GalleryImage", "failed to find file to read thumbnail: " + this.mLocalFilePath);
                    } catch (Throwable t) {
                        GalleryLog.w("GalleryImage", "fail to get exif thumb." + t.getMessage());
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
            GalleryLog.d("GalleryImage", "onDecodeOriginal filePath:" + this.mLocalFilePath + " type:" + type + " time:" + (System.currentTimeMillis() - startTime));
            GalleryLog.d("GalleryImage", "onDecodeOriginal result:" + result);
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
                    GalleryLog.d("GalleryImage", "decodeFromCache sourcePath:" + this.mLocalFilePath + " type:" + type + " time:" + (System.currentTimeMillis() - startTime));
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
                    GalleryLog.w("GalleryImage", "get region decoder is null:" + this.mLocalFilePath);
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

    public GalleryImage(Path path, GalleryApp application, Cursor cursor) {
        super(path, application, cursor);
    }

    public GalleryImage(Path path, GalleryApp application, int id) {
        super(path, application, id);
    }

    public GalleryImage(Path path, GalleryApp application, String idString) {
        super(path, application, idString);
    }

    public Job<Bitmap> requestImage(int type) {
        return new GalleryImageRequest(this.mApplication, this.mPath, this, this.dateModifiedInSec, type, this.filePath);
    }

    protected ContentValues getLocalValues() {
        ContentValues values = new ContentValues();
        values.put("_id", Integer.valueOf(this.mLocalMediaId));
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
        values.put("is_hdr", Boolean.valueOf(this.is_hdr));
        values.put("hw_voice_offset", Long.valueOf(this.mVoiceOffset));
        values.put("hw_image_refocus", Integer.valueOf(this.mRefocusPhoto));
        values.put("hw_rectify_offset", Integer.valueOf(this.mRectifyOffset));
        values.put("special_file_type", Integer.valueOf(this.mSpecialFileType));
        values.put("special_file_offset", Long.valueOf(this.mSpecialFileOffset));
        values.put("_display_name", this.mDisplayName);
        values.put("is_hw_burst", Integer.valueOf(this.mIsBurstCover ? 1 : 0));
        return values;
    }

    public Bitmap getLatestCacheImage() {
        TraceController.traceBegin("LocalImage.getLatestCacheImage");
        if (this.bucketId == MediaSetUtils.getCameraBucketId() || GalleryStorageManager.getInstance().isOuterGalleryStorageCameraBucketID(this.bucketId)) {
            long startTime = System.currentTimeMillis();
            TraceController.beginSection("getLatestCacheImage decode from DCIM/Camera/latest");
            Bitmap result = DecodeUtils.decodeFromCacheLatest(this.filePath);
            TraceController.endSection();
            GalleryLog.d("GalleryImage", "getLatestCacheImage sourcePath:" + this.filePath + " time:" + (System.currentTimeMillis() - startTime) + " Bitmap:" + result);
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
        if (!GpsMeasureMode.MODE_3_DIMENSIONAL.equals(this.mMediaType)) {
            return new LocalLargeImageRequest(this.filePath);
        }
        throw new UnsupportedOperationException("Cannot regquest a large image to a local video!");
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
        boolean startsWith = TextUtils.isEmpty(this.filePath) ? false : this.filePath.startsWith("cloud-");
        TraceController.traceBegin("LocalImage.getSupportedOperations");
        int operation = 1025;
        if (this.mLocalMediaId != -1) {
            operation = 25166849;
        }
        if (this.mLocalMediaId != -1 || this.mThumbType >= 2) {
            operation |= 131072;
        }
        if (this.mVoiceOffset > 0 || (this.mVoiceOffset == -1 && this.mCloudMediaId != -1)) {
            operation |= 134217728;
        }
        if (this.mRefocusPhoto > 0) {
            operation |= 262144;
        }
        if (this.mSupportRename == -1 && !startsWith) {
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
        if (GalleryUtils.isSupportMyFavorite() && this.mLocalMediaId != -1) {
            operation |= 536870912;
        }
        boolean canEditOrCrop = BitmapUtils.isFilterShowSupported(this.mimeType);
        if (!this.isDrm && canEditOrCrop && (this.mLocalMediaId != -1 || this.mThumbType >= 2)) {
            operation |= 520;
        }
        if (!this.isDrm || canForward()) {
            operation |= 4;
        }
        if (DrmUtils.canSetAsWallPaper(this) && (this.mLocalMediaId != -1 || this.mThumbType >= 2)) {
            operation |= 32;
        }
        if (BitmapUtils.isSupportedByRegionDecoder(this.mimeType) && ((!this.isDrm || hasRight()) && !startsWith)) {
            operation |= 64;
        }
        if (!(!BitmapUtils.isRotationSupported(this.mimeType) || this.mIsBurstCover || this.mLocalMediaId == -1 || is3DPanorama())) {
            operation |= 2;
        }
        if (GalleryUtils.isValidLocation(this.latitude, this.longitude) && !startsWith) {
            operation |= 16;
        }
        if (this.mCloudMediaId != -1 && this.mLocalMediaId == -1) {
            operation |= 268435456;
        }
        TraceController.traceEnd();
        return operation;
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
        ContentValues galleryValues = new ContentValues();
        int rotation = (this.rotation + degrees) % 360;
        if (rotation < 0) {
            rotation += 360;
        }
        if ("image/jpeg".equalsIgnoreCase(this.mimeType)) {
            ExifInterface exif = new ExifInterface();
            ExifTag tag = exif.buildTag(ExifInterface.TAG_ORIENTATION, Short.valueOf(ExifInterface.getOrientationValueForRotation(rotation)));
            if (tag != null) {
                exif.setTag(tag);
                try {
                    exif.forceRewriteExif(this.filePath);
                    this.fileSize = new File(this.filePath).length();
                    values.put("_size", Long.valueOf(this.fileSize));
                    galleryValues.put("_size", Long.valueOf(this.fileSize));
                    galleryValues.put("hash", MD5Utils.getMD5(new File(this.filePath)));
                } catch (FileNotFoundException e) {
                    GalleryLog.w("GalleryImage", "cannot find file to set exif: " + this.filePath);
                } catch (IOException e2) {
                    GalleryLog.w("GalleryImage", "cannot set exif data: " + this.filePath);
                }
            } else {
                GalleryLog.w("GalleryImage", "Could not build tag: " + ExifInterface.TAG_ORIENTATION);
            }
        }
        galleryValues.put("orientation", Integer.valueOf(rotation));
        values.put("orientation", Integer.valueOf(rotation));
        try {
            this.mApplication.getContentResolver().update(GalleryMedia.URI, galleryValues, "_id=?", new String[]{String.valueOf(this.id)});
            this.mApplication.getContentResolver().update(baseUri, values, "_id=?", new String[]{String.valueOf(this.mLocalMediaId)});
        } catch (SecurityException e3) {
            GalleryLog.noPermissionForMediaProviderLog("GalleryImage");
        }
    }

    public boolean supportComment() {
        return "image/jpeg".equalsIgnoreCase(this.mimeType) && this.mLocalMediaId != -1;
    }

    public Uri getContentUri() {
        if (this.mLocalMediaId > 0) {
            return Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(String.valueOf(this.mLocalMediaId)).build();
        }
        if (this.mThumbType >= 2) {
            return Uri.fromFile(new File(this.filePath));
        }
        return null;
    }

    public int getMediaType() {
        return 2;
    }

    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        if (this.mIsBurstCover) {
            details.addDetail(5, Long.valueOf(getSize()));
        }
        return details;
    }

    public long getSize() {
        if (this.mIsBurstCover) {
            BurstPhotoSet burstSet = (BurstPhotoSet) this.mApplication.getDataManager().getMediaSet(getBurstSetPath());
            if (burstSet != null) {
                return burstSet.getBurstSize();
            }
        }
        return super.getSize();
    }

    public long getVoiceOffset() {
        if (this.mLocalMediaId != -1) {
            return this.mVoiceOffset;
        }
        return -1;
    }

    public boolean isVoiceImage() {
        if (this.mVoiceOffset <= 0) {
            return this.mVoiceOffset == -1 && this.mCloudMediaId != -1;
        } else {
            return true;
        }
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

    public int getSpecialFileType() {
        if (50 != this.mSpecialFileType || LiveUtils.LIVE_ENABLE) {
            return this.mSpecialFileType;
        }
        return 0;
    }

    public boolean isRefocusPhoto() {
        return this.mRefocusPhoto > 0;
    }

    public boolean isRectifyImage() {
        if (!RectifyUtils.isRectifyNativeSupport() || !BitmapUtils.isRectifySupported(this.mimeType)) {
            return false;
        }
        if (this.mRectifyOffset <= 0) {
            return this.mRectifyOffset == -1 && this.mCloudMediaId != -1;
        } else {
            return true;
        }
    }

    public boolean is3DModelImage() {
        return ThreeDModelImageUtils.is3DModelImageSpecialFileType(this.mSpecialFileType);
    }

    public int getRectifyOffset() {
        if (this.mLocalMediaId != -1) {
            return this.mRectifyOffset;
        }
        return -1;
    }

    public boolean isBurstCover() {
        return this.mIsBurstCover;
    }

    public Path getBurstSetPath() {
        return this.mBurstSetPath;
    }

    public void setAsFavorite(Context context) {
        super.setAsFavorite(context);
        MediaObject object = this.mApplication.getDataManager().peekMediaObject(Path.fromString("/local/image/item/" + this.mLocalMediaId));
        if (object != null) {
            ((LocalMediaItem) object).updateMyFavorite(true);
        }
    }

    public void cancelFavorite(Context context) {
        super.cancelFavorite(context);
        MediaObject object = this.mApplication.getDataManager().peekMediaObject(Path.fromString("/local/image/item/" + this.mLocalMediaId));
        if (object != null) {
            ((LocalMediaItem) object).updateMyFavorite(false);
        }
    }

    public boolean canShare() {
        return this.mLocalMediaId != -1 || this.mThumbType >= 2;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public File getDestinationDirectory() {
        if (this.mLocalMediaId != -1) {
            return null;
        }
        Object obj = null;
        try {
            Closeable cursor = this.mApplication.getContentResolver().query(PhotoShareConstants.CLOUD_ALBUM_TABLE_URI, new String[]{"lpath"}, "albumId=?", new String[]{this.mCloudBucketID}, null);
            if (cursor != null && cursor.moveToNext()) {
                obj = cursor.getString(0);
            }
            Utils.closeSilently(cursor);
        } catch (SQLiteException e) {
            GalleryLog.e("photoshareLogTag", "getDestinationDirectory exception " + e.toString());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        if (TextUtils.isEmpty(obj)) {
            return null;
        }
        File file = null;
        GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        if (innerGalleryStorage != null) {
            file = new File(innerGalleryStorage.getPath() + obj);
        }
        return file;
    }

    public boolean isWaitToUpload() {
        boolean z = false;
        if (this.mIsBurstCover) {
            if (supportAutoUpload() && !this.mIsAllBurstFileUploaded) {
                z = true;
            }
            return z;
        }
        if (this.mRelativeCloudMediaId == -1 && this.mCloudMediaId == -1) {
            z = supportAutoUpload();
        }
        return z;
    }

    public boolean isSupportTranslateVoiceImageToVideo() {
        return (!isVoiceImage() || isDrm() || getLocalMediaId() == -1) ? false : true;
    }
}
