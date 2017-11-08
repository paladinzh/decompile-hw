package com.android.gallery3d.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore.Video.Media;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.gallery.util.VideoEditorController;

public class GalleryVideo extends GalleryMediaItem implements IVideo {
    public static final Path VIDEO_PATH = Path.fromString("/gallery/video/item/");

    public static class LocalVideoRequest extends ImageCacheRequest {
        private String mLocalFilePath;
        private int mLocalId;

        public /* bridge */ /* synthetic */ boolean hasBufferCache() {
            return super.hasBufferCache();
        }

        LocalVideoRequest(GalleryApp application, Path path, long timeModified, int type, String localFilePath, int localMediaId) {
            super(application, path, timeModified, type, MediaItem.getTargetSize(type, true));
            this.mLocalFilePath = localFilePath;
            this.mLocalId = localMediaId;
        }

        public Bitmap onDecodeOriginal(JobContext jc, int type) {
            Bitmap bitmap;
            if (this.mLocalId > 0) {
                bitmap = BitmapUtils.createVideoThumbnail(this.mLocalFilePath);
                if (bitmap == null || jc.isCancelled()) {
                    return null;
                }
                return bitmap;
            }
            Options options = new Options();
            options.inPreferredConfig = Config.ARGB_8888;
            int targetSize = MediaItem.getTargetSize(type);
            if (DrmUtils.isDrmFile(this.mLocalFilePath)) {
                DrmUtils.inDrmMode(options);
            }
            if (type == 2) {
                if (DrmUtils.isDrmFile(this.mLocalFilePath)) {
                    DrmUtils.inPreviewMode(options);
                } else {
                    byte[] thumbData = null;
                    try {
                        thumbData = new ExifInterface(this.mLocalFilePath).getThumbnail();
                    } catch (Throwable t) {
                        GalleryLog.w("GalleryVideo", "fail to get exif thumb for gallery video." + t.getMessage());
                    }
                    if (thumbData != null) {
                        bitmap = DecodeUtils.decodeIfBigEnough(jc, thumbData, options, targetSize);
                        if (bitmap != null) {
                            return bitmap;
                        }
                    }
                }
            }
            return DecodeUtils.decodeThumbnail(jc, this.mLocalFilePath, options, targetSize, type);
        }

        public String workContent() {
            return "decode thumnail from video file: " + this.mLocalFilePath;
        }

        public boolean needDecodeVideoFromOrigin() {
            return this.mLocalId > 0 && !hasBufferCache();
        }
    }

    public GalleryVideo(Path path, GalleryApp application, Cursor cursor) {
        super(path, application, cursor);
    }

    public GalleryVideo(Path path, GalleryApp application, int id) {
        super(path, application, id);
    }

    public GalleryVideo(Path path, GalleryApp application, String id) {
        super(path, application, id);
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
        values.put("duration", Integer.valueOf(this.mDurationInSec));
        values.put("resolution", this.width + "x" + this.height);
        values.put("_size", Long.valueOf(this.fileSize));
        values.put("width", Integer.valueOf(this.width));
        values.put("height", Integer.valueOf(this.height));
        values.put("is_hdr", Boolean.valueOf(this.is_hdr));
        values.put("hw_voice_offset", Long.valueOf(this.mVoiceOffset));
        values.put("hw_image_refocus", Integer.valueOf(this.mRefocusPhoto));
        values.put(String.format("strftime('%%Y%%m', %s / 1000, 'unixepoch') AS normalized_date", new Object[]{"datetaken"}), Integer.valueOf(this.normalizedDate));
        values.put("hw_rectify_offset", Integer.valueOf(this.mRectifyOffset));
        values.put("special_file_type", Integer.valueOf(this.mSpecialFileType));
        values.put("special_file_offset", Long.valueOf(this.mSpecialFileOffset));
        values.put("_display_name", this.mDisplayName);
        return values;
    }

    public Uri getPlayUri() {
        if (this.mLocalMediaId <= 0) {
            return null;
        }
        return getContentUri();
    }

    public Job<Bitmap> requestImage(int type) {
        return new LocalVideoRequest(this.mApplication, getPath(), this.dateModifiedInSec, type, this.filePath, this.mLocalMediaId);
    }

    public Job<BitmapRegionDecoder> requestLargeImage() {
        throw new UnsupportedOperationException("Cannot regquest a large image to a local video!");
    }

    public int getSupportedOperations() {
        int operation = 1073742849 | (this.isDrm ? 0 : FragmentTransaction.TRANSIT_ENTER_MASK);
        if (this.mLocalMediaId != -1) {
            operation |= 25165824;
        }
        if (!this.isDrm || canForward()) {
            operation |= 4;
        }
        if (!this.isDrm || hasRight()) {
            operation |= 128;
        }
        if (GalleryUtils.isSupportMyFavorite() && this.mLocalMediaId != -1) {
            operation |= 536870912;
        }
        if (!VideoEditorController.isSupportVideoEdit()) {
            operation &= -4097;
        }
        if (this.mCloudMediaId == -1 || this.mLocalMediaId != -1) {
            return operation;
        }
        return operation | 268435456;
    }

    public void rotate(int degrees) {
    }

    public Uri getContentUri() {
        if (this.mLocalMediaId <= 0) {
            return null;
        }
        return Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(String.valueOf(this.mLocalMediaId)).build();
    }

    public int getMediaType() {
        return 4;
    }

    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        if (this.mDurationInSec > 0) {
            details.addDetail(8, GalleryUtils.formatDuration(this.mApplication.getAndroidContext(), this.mDurationInSec));
        }
        if (this.isDrm && this.mLocalMediaId != -1) {
            MediaDetails.extractDrmInfo(details, this);
        }
        return details;
    }

    public int getDurationInSec() {
        return this.mDurationInSec;
    }

    public void setAsFavorite(Context context) {
        super.setAsFavorite(context);
        MediaObject object = this.mApplication.getDataManager().peekMediaObject(Path.fromString("/local/video/item/" + this.mLocalMediaId));
        if (object != null) {
            ((LocalMediaItem) object).updateMyFavorite(true);
        }
    }

    public void cancelFavorite(Context context) {
        super.cancelFavorite(context);
        MediaObject object = this.mApplication.getDataManager().peekMediaObject(Path.fromString("/local/video/item/" + this.mLocalMediaId));
        if (object != null) {
            ((LocalMediaItem) object).updateMyFavorite(false);
        }
    }

    public boolean isWaitToUpload() {
        if (this.mRelativeCloudMediaId == -1 && this.mCloudMediaId == -1) {
            return supportAutoUpload();
        }
        return false;
    }

    public boolean canShare() {
        return this.mLocalMediaId != -1;
    }

    public boolean isLCDDownloaded() {
        return this.mLocalMediaId != -1 || this.mThumbType >= 2;
    }
}
