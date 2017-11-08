package com.android.gallery3d.data;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.text.TextUtils;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.util.VideoEditorController;
import java.io.File;

public class PhotoShareVideo extends PhotoShareMediaItem {

    public static class PhotoShareVideoRequest extends ImageCacheRequest {
        private String mLocalFilePath;
        private boolean mVideoDecode;

        public /* bridge */ /* synthetic */ boolean hasBufferCache() {
            return super.hasBufferCache();
        }

        PhotoShareVideoRequest(GalleryApp application, Path path, long timeModified, int type, String localFilePath, boolean videoDecode) {
            super(application, path, timeModified, type, MediaItem.getTargetSize(type, true));
            this.mLocalFilePath = localFilePath;
            this.mVideoDecode = videoDecode;
        }

        public Bitmap onDecodeOriginal(JobContext jc, int type) {
            Options options = new Options();
            options.inPreferredConfig = Config.ARGB_8888;
            int targetSize = MediaItem.getTargetSize(type);
            Bitmap bitmap;
            if (this.mVideoDecode) {
                bitmap = BitmapUtils.createVideoThumbnail(this.mLocalFilePath);
                if (bitmap == null || jc.isCancelled()) {
                    return null;
                }
                return bitmap;
            }
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
                        GalleryLog.w("PhotoShareVideo", "fail to get exif thumb." + t.getMessage());
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
            return "decode thumnail from file: " + this.mLocalFilePath;
        }

        public boolean needDecodeVideoFromOrigin() {
            return this.mVideoDecode && !hasBufferCache();
        }
    }

    public PhotoShareVideo(Path path, GalleryApp application, FileInfo fileInfo, int folderType, String albumName) {
        super(path, application, fileInfo, folderType, albumName);
    }

    public int getMediaType() {
        return 4;
    }

    public Job<Bitmap> requestImage(int type) {
        boolean z = true;
        LastModifyInfo info = getLastModifyInfo();
        GalleryApp galleryApp = this.mApplication;
        Path path = getPath();
        long j = info.timeModified;
        String str = info.filePath;
        if (!this.isPreViewItem && isThumbNail()) {
            z = false;
        }
        return new PhotoShareVideoRequest(galleryApp, path, j, type, str, z);
    }

    public Job<BitmapRegionDecoder> requestLargeImage() {
        throw new UnsupportedOperationException("Cannot regquest a large image to a photoshare video!");
    }

    public Uri getPlayUri() {
        if (PhotoShareUtils.isFileExists(getFileInfo().getLocalRealPath())) {
            return Uri.fromFile(new File(this.mFileInfo.getLocalRealPath()));
        }
        return null;
    }

    public MediaDetails getDetails() {
        return super.getDetails();
    }

    public boolean isThumbNail() {
        boolean z = false;
        if (!PhotoShareUtils.isFileExists(this.mFilePath) || TextUtils.isEmpty(this.mFilePath) || this.mFilePath.lastIndexOf(".") == -1) {
            return true;
        }
        String suffix = this.mFilePath.substring(this.mFilePath.lastIndexOf(".") + 1, this.mFilePath.length());
        if (!("mp4".equalsIgnoreCase(suffix) || "3gp".equalsIgnoreCase(suffix))) {
            z = true;
        }
        return z;
    }

    public int getSupportedOperations() {
        if (this.isPreViewItem) {
            return 5;
        }
        int operation = 5124;
        if (isThumbNail()) {
            operation = 268440580;
        }
        if (!(this.mCreateID == null || this.mOwnerID == null || this.mOwnerID.equals(PhotoShareUtils.getLoginUserId()))) {
            if (this.mCreateID.equals(PhotoShareUtils.getLoginUserId())) {
            }
            operation |= 128;
            if (!VideoEditorController.isSupportVideoEdit()) {
                operation &= -4097;
            }
            return operation;
        }
        operation |= 1;
        operation |= 128;
        if (VideoEditorController.isSupportVideoEdit()) {
            operation &= -4097;
        }
        return operation;
    }

    protected void setPhotoSharePreView() {
        this.isPreViewItem = this.mFileInfo.getFileId() == null;
        if (this.isPreViewItem) {
            this.mFileInfo.setFileType(4);
        }
    }

    protected void setFileSizeAndLastModify() {
        this.mFileSize = this.mFileInfo.getSize();
        try {
            this.mDateTakenInMs = new File(this.isPreViewItem ? this.mFileInfo.getLocalRealPath() : this.mFileInfo.getLocalThumbPath()).lastModified();
        } catch (Exception e) {
            this.mDateTakenInMs = 0;
            GalleryLog.v("PhotoShareVideo", "Exception In video setFileSizeAndLastModify " + e.toString());
        }
    }

    public boolean canShare() {
        return !isThumbNail();
    }

    public boolean isLCDDownloaded() {
        return !canShare() ? PhotoShareUtils.isFileExists(this.mFileInfo.getLocalBigThumbPath()) : true;
    }
}
