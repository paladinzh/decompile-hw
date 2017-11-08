package com.android.gallery3d.data;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.RemoteException;
import android.text.TextUtils;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.android.cg.vo.FileInfoDetail;
import com.huawei.gallery.extfile.FyuseFile;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.io.File;
import java.io.FileNotFoundException;
import org.json.JSONException;
import org.json.JSONObject;

public class PhotoShareDownUpItem extends MediaItem {
    private long mAddTime;
    private String mAlbum;
    private GalleryApp mApplication;
    public FileInfoDetail mFileInfo;
    private String mFileName;
    private String mFilePath;
    private long mFileSize;
    private long mFinishTime;
    private String mHashValue;
    private final boolean mIsDownLoad;
    private long mPanorama3dDataSize;
    private int mRotation = 0;
    private int mState;

    public static class PhotoShareDownUpItemRequest extends ImageCacheRequest {
        private String mFilePath;
        private boolean mIsVideo;

        public /* bridge */ /* synthetic */ boolean hasBufferCache() {
            return super.hasBufferCache();
        }

        PhotoShareDownUpItemRequest(GalleryApp application, Path path, boolean isVideo, long timeModified, int type, String filePath) {
            super(application, path, timeModified, type, MediaItem.getTargetSize(type));
            this.mFilePath = filePath;
            this.mIsVideo = isVideo;
        }

        public Bitmap onDecodeOriginal(JobContext jc, int type) {
            ExifInterface exifInterface;
            Throwable t;
            Bitmap bitmap;
            if (this.mIsVideo) {
                bitmap = BitmapUtils.createVideoThumbnail(this.mFilePath);
                if (bitmap == null || jc.isCancelled()) {
                    return null;
                }
                return bitmap;
            }
            int targetSize = MediaItem.getTargetSize(2);
            Options options = new Options();
            options.inPreferredConfig = Config.ARGB_8888;
            if (DrmUtils.isDrmFile(this.mFilePath)) {
                DrmUtils.inPreviewMode(options);
            } else {
                byte[] thumbData = null;
                try {
                    ExifInterface exif = new ExifInterface(this.mFilePath);
                    try {
                        thumbData = exif.getThumbnail();
                        exifInterface = exif;
                    } catch (FileNotFoundException e) {
                        GalleryLog.w("PhotoShareDownUpItem", "failed to find file to read thumbnail from exif: " + this.mFilePath);
                        if (thumbData != null) {
                            bitmap = DecodeUtils.decodeIfBigEnough(jc, thumbData, options, targetSize);
                            if (bitmap != null) {
                                return bitmap;
                            }
                        }
                        return DecodeUtils.decodeThumbnail(jc, this.mFilePath, options, targetSize, 2);
                    } catch (Throwable th) {
                        t = th;
                        exifInterface = exif;
                        GalleryLog.w("PhotoShareDownUpItem", "fail to get exif thumb." + t.getMessage());
                        if (thumbData != null) {
                            bitmap = DecodeUtils.decodeIfBigEnough(jc, thumbData, options, targetSize);
                            if (bitmap != null) {
                                return bitmap;
                            }
                        }
                        return DecodeUtils.decodeThumbnail(jc, this.mFilePath, options, targetSize, 2);
                    }
                } catch (FileNotFoundException e2) {
                    GalleryLog.w("PhotoShareDownUpItem", "failed to find file to read thumbnail from exif: " + this.mFilePath);
                    if (thumbData != null) {
                        bitmap = DecodeUtils.decodeIfBigEnough(jc, thumbData, options, targetSize);
                        if (bitmap != null) {
                            return bitmap;
                        }
                    }
                    return DecodeUtils.decodeThumbnail(jc, this.mFilePath, options, targetSize, 2);
                } catch (Throwable th2) {
                    t = th2;
                    GalleryLog.w("PhotoShareDownUpItem", "fail to get exif thumb." + t.getMessage());
                    if (thumbData != null) {
                        bitmap = DecodeUtils.decodeIfBigEnough(jc, thumbData, options, targetSize);
                        if (bitmap != null) {
                            return bitmap;
                        }
                    }
                    return DecodeUtils.decodeThumbnail(jc, this.mFilePath, options, targetSize, 2);
                }
                if (thumbData != null) {
                    bitmap = DecodeUtils.decodeIfBigEnough(jc, thumbData, options, targetSize);
                    if (bitmap != null) {
                        return bitmap;
                    }
                }
            }
            return DecodeUtils.decodeThumbnail(jc, this.mFilePath, options, targetSize, 2);
        }

        public String workContent() {
            return "decode thumnail from file: " + this.mFilePath;
        }

        public boolean needDecodeVideoFromOrigin() {
            return this.mIsVideo && !hasBufferCache();
        }
    }

    public PhotoShareDownUpItem(Path path, boolean downLoad, FileInfoDetail fileInfo, GalleryApp app) {
        super(path, MediaObject.nextVersionNumber());
        this.mIsDownLoad = downLoad;
        this.mFileInfo = fileInfo;
        this.mApplication = app;
        updateInfo();
        if (isDownLoad()) {
            parseExpand(this.mFileInfo.getExpand());
        } else {
            this.mRotation = PhotoShareUtils.getRotationFromExif(this.mFilePath);
        }
    }

    private void parseExpand(String expand) {
        if (!TextUtils.isEmpty(expand)) {
            try {
                JSONObject expandJson = new JSONObject(expand);
                if (expandJson.has("rotate")) {
                    this.mRotation = PhotoShareUtils.getOrientation(Integer.parseInt(expandJson.getString("rotate")));
                }
            } catch (JSONException e) {
                GalleryLog.i("PhotoShareDownUpItem", "parseExpand() failed, reason: JSONException.");
            }
            if (this.mFileInfo.getFileType() == 7 && FyuseFile.isSupport3DPanoramaAPK()) {
                this.mPanorama3dDataSize = (long) PhotoShareUtils.parsePanorama3dSizeFromExpand(this.mFileInfo.getExpand());
            }
        }
    }

    public int getRotation() {
        if (TextUtils.isEmpty(this.mFilePath) || this.mFilePath.equals(this.mFileInfo.getLocalBigThumbPath()) || this.mFilePath.equals(this.mFileInfo.getLocalThumbPath())) {
            return 0;
        }
        return this.mRotation;
    }

    public Job<Bitmap> requestImage(int type) {
        return new PhotoShareDownUpItemRequest(this.mApplication, this.mPath, isVideo(), this.mAddTime, type, this.mFilePath);
    }

    public Job<BitmapRegionDecoder> requestLargeImage() {
        return null;
    }

    public String getMimeType() {
        return null;
    }

    public int getWidth() {
        return 0;
    }

    public int getHeight() {
        return 0;
    }

    public void updateFileInfo(FileInfoDetail fileInfo) {
        this.mFileInfo = fileInfo;
        updateInfo();
    }

    private void updateInfo() {
        String shareId;
        if (PhotoShareUtils.isFilePathValid(this.mFileInfo.getLocalRealPath())) {
            this.mFilePath = this.mFileInfo.getLocalRealPath();
        } else if (PhotoShareUtils.isFilePathValid(this.mFileInfo.getLocalBigThumbPath())) {
            this.mFilePath = this.mFileInfo.getLocalBigThumbPath();
        } else if (PhotoShareUtils.isFilePathValid(this.mFileInfo.getLocalThumbPath())) {
            this.mFilePath = this.mFileInfo.getLocalThumbPath();
        } else {
            this.mFilePath = "";
        }
        this.mFileName = this.mFileInfo.getFileName();
        this.mFileSize = this.mFileInfo.getSize();
        if (this.mState != this.mFileInfo.getFileStatus()) {
            updateVersion();
        }
        this.mState = this.mFileInfo.getFileStatus();
        this.mAddTime = this.mFileInfo.getAddTime();
        this.mFinishTime = this.mFileInfo.getFinishTime();
        this.mHashValue = this.mFileInfo.getHash();
        if (TextUtils.isEmpty(this.mFileInfo.getAlbumId())) {
            shareId = this.mFileInfo.getShareId();
        } else {
            shareId = this.mFileInfo.getAlbumId();
        }
        this.mAlbum = shareId;
    }

    public void cancel() {
        try {
            if (this.mIsDownLoad) {
                PhotoShareUtils.getServer().cancelDownloadTask(new FileInfoDetail[]{this.mFileInfo});
                return;
            }
            PhotoShareUtils.getServer().cancelUploadTask(new FileInfoDetail[]{this.mFileInfo});
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
    }

    public boolean isDownLoad() {
        return this.mIsDownLoad;
    }

    public void start() {
        try {
            if (this.mIsDownLoad) {
                PhotoShareUtils.getServer().startDownloadTask(new FileInfoDetail[]{this.mFileInfo});
                return;
            }
            PhotoShareUtils.getServer().startUploadTask(new FileInfoDetail[]{this.mFileInfo});
            PhotoShareUtils.notifyPhotoShareContentChange(1, this.mFileInfo.getShareId());
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
    }

    public boolean equal(String album, String hash) {
        boolean z = false;
        if (album == null || hash == null) {
            return false;
        }
        if (album.equals(this.mAlbum)) {
            z = hash.equals(this.mHashValue);
        }
        return z;
    }

    public boolean isVideo() {
        return this.mFileInfo.getFileType() == 4 ? this.mFilePath.equals(this.mFileInfo.getLocalRealPath()) : false;
    }

    public Uri getUri() {
        if (PhotoShareUtils.isFileExists(this.mFilePath)) {
            return Uri.fromFile(new File(this.mFilePath));
        }
        return null;
    }

    public String getFilePath() {
        return this.mFilePath;
    }

    public String getFileName() {
        return this.mFileName;
    }

    public long getFileSize() {
        if (this.mFileInfo.getFileType() == 7 && FyuseFile.isSupport3DPanoramaAPK()) {
            return this.mPanorama3dDataSize + this.mFileSize;
        }
        return this.mFileSize;
    }

    public long getAddTime() {
        return this.mAddTime;
    }

    public long getFinishTime() {
        return this.mFinishTime;
    }

    public int getState() {
        return this.mState;
    }

    private void updateVersion() {
        this.mDataVersion = MediaObject.nextVersionNumber();
    }
}
