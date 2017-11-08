package com.android.gallery3d.data;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.media.MediaFile;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.gallery.barcode.BarcodeScanResultItem;
import com.huawei.gallery.extfile.FyuseFile;
import com.huawei.gallery.livephoto.LiveUtils;
import com.huawei.gallery.photorectify.RectifyUtils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.threedmodel.ThreeDModelImageUtils;
import java.io.File;

public class PhotoShareImage extends PhotoShareMediaItem implements IImage {
    private BarcodeScanResultItem mBarcodeScanResult;
    private boolean mIsBarcodeScanned = false;
    private boolean mIsBarcodeStartScan = false;
    protected boolean mIsRectangleThumbnail = false;
    private boolean mNeedReScanBarcode = false;
    protected boolean mNeedVideoThumbnail = false;
    private int mPanorama3dDataSize = 0;
    private int mRectifyOffset = 0;
    private long mSpecialFileOffset = 0;
    private long mVoiceOffset = 0;

    public static class PhotoShareImageRequest extends ImageCacheRequest {
        private String mFilePath;

        public /* bridge */ /* synthetic */ boolean hasBufferCache() {
            return super.hasBufferCache();
        }

        PhotoShareImageRequest(GalleryApp application, Path path, long timeModified, int type, String filePath, boolean needVideoThumbnail, boolean isRectangleThumbnail) {
            super(application, path, timeModified, type, MediaItem.getTargetSize(type, needVideoThumbnail), isRectangleThumbnail, PhotoShareUtils.getCoverWidth(), PhotoShareUtils.getCoverHeight());
            this.mFilePath = filePath;
        }

        public Bitmap onDecodeOriginal(JobContext jc, int type) {
            Bitmap ret;
            Options options = new Options();
            options.inPreferredConfig = Config.ARGB_8888;
            int targetSize = MediaItem.getTargetSize(type);
            if (DrmUtils.isDrmFile(this.mFilePath)) {
                DrmUtils.inDrmMode(options);
            }
            if (type == 2 && !this.mIsRectangleThumbnail && DrmUtils.isDrmFile(this.mFilePath)) {
                DrmUtils.inPreviewMode(options);
            }
            if (this.mIsRectangleThumbnail) {
                ret = DecodeUtils.decodeRectThumbnail(jc, this.mFilePath, options, PhotoShareUtils.getCoverWidth(), PhotoShareUtils.getCoverHeight());
            } else {
                ret = DecodeUtils.decodeThumbnail(jc, this.mFilePath, options, targetSize, type);
            }
            if (type != 20) {
                return ret;
            }
            Bitmap face = BitmapUtils.findFace(ret);
            if (face == null || face == ret) {
                return ret;
            }
            ret.recycle();
            return face;
        }

        public String workContent() {
            return "decode thumnail from file: " + this.mFilePath;
        }
    }

    public static class PhotoShareLargeImageRequest extends BaseJob<BitmapRegionDecoder> {
        byte[] mDataBytes;
        int mDataLength;
        int mDataOffset;
        String mLocalFilePath;

        public PhotoShareLargeImageRequest(FileInfo fileInfo) {
            if (PhotoShareUtils.isFileExists(fileInfo.getLocalRealPath())) {
                this.mLocalFilePath = fileInfo.getLocalRealPath();
            } else {
                this.mLocalFilePath = fileInfo.getLocalBigThumbPath();
            }
        }

        public PhotoShareLargeImageRequest(byte[] bytes, int offset, int length) {
            if (bytes != null) {
                this.mDataBytes = (byte[]) bytes.clone();
            }
            this.mDataOffset = offset;
            this.mDataLength = length;
        }

        public BitmapRegionDecoder run(JobContext jc) {
            if (this.mDataBytes != null) {
                return DecodeUtils.createBitmapRegionDecoder(jc, this.mDataBytes, this.mDataOffset, this.mDataLength, false);
            }
            return DecodeUtils.createBitmapRegionDecoder(jc, this.mLocalFilePath, false);
        }

        public String workContent() {
            return "create region decoder with " + (this.mDataBytes != null ? "bytes" : "file: " + this.mLocalFilePath);
        }
    }

    public PhotoShareImage(Path path, GalleryApp application, FileInfo fileInfo, int folderType, String albumName) {
        super(path, application, fileInfo, folderType, albumName);
        if (this.mFileInfo.getFileType() == 2) {
            calcVoiceOffset();
        }
        updateWidthAndHeight();
        if (this.mFileInfo.getFileType() == 7 && FyuseFile.isSupport3DPanoramaAPK()) {
            this.mPanorama3dDataSize = PhotoShareUtils.parsePanorama3dSizeFromExpand(this.mFileInfo.getExpand());
        }
        if (this.mFileInfo.getFileType() == 8) {
            calcRectifyOffset();
        }
        if (this.mFileInfo.getFileType() == 9) {
            calcLivePhotoOffset();
        }
    }

    public Job<Bitmap> requestImage(int type) {
        LastModifyInfo info = getLastModifyInfo();
        return new PhotoShareImageRequest(this.mApplication, this.mPath, info.timeModified, type, info.filePath, this.mNeedVideoThumbnail, this.mIsRectangleThumbnail);
    }

    public Job<BitmapRegionDecoder> requestLargeImage() {
        return new PhotoShareLargeImageRequest(this.mFileInfo);
    }

    public Job<BitmapRegionDecoder> requestLargeImage(byte[] bytes, int offset, int length) {
        return new PhotoShareLargeImageRequest(bytes, offset, length);
    }

    public boolean isRefocusPhoto() {
        return this.mFileInfo.getFileType() == 3 || this.mFileInfo.getFileType() == 6;
    }

    public int getPanorama3dDataSize() {
        return this.mPanorama3dDataSize;
    }

    public int getRefocusPhotoType() {
        if (this.mFileInfo.getFileType() == 3) {
            return 1;
        }
        if (this.mFileInfo.getFileType() == 6) {
            return 2;
        }
        return 0;
    }

    public int getSupportedOperations() {
        if (this.isPreViewItem) {
            return 5;
        }
        int operation = 132132;
        if (BitmapUtils.isFilterShowSupported(this.mimeType)) {
            operation = 132652;
        }
        if (isThumbNail()) {
            operation |= 268435456;
        }
        if (isRefocusPhoto()) {
            operation |= 262144;
        }
        if (!(this.mCreateID == null || this.mOwnerID == null || this.mOwnerID.equals(PhotoShareUtils.getLoginUserId()))) {
            if (this.mCreateID.equals(PhotoShareUtils.getLoginUserId())) {
            }
            if (BitmapUtils.isSupportedByRegionDecoder(this.mimeType) && PhotoShareUtils.isFileExists(this.mFileInfo.getLocalRealPath())) {
                operation |= 64;
            }
            if (GalleryUtils.isValidLocation(this.latitude, this.longitude)) {
                operation |= 16;
            }
            return operation;
        }
        operation |= 1;
        operation |= 64;
        if (GalleryUtils.isValidLocation(this.latitude, this.longitude)) {
            operation |= 16;
        }
        return operation;
    }

    public void updateWidthAndHeight() {
        try {
            Options opts = new Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(this.mFilePath, opts);
            if (opts.outWidth > 0) {
                this.mWidth = opts.outWidth;
                this.mHeight = opts.outHeight;
            }
        } catch (Exception e) {
            GalleryLog.d("PhotoShareImage", "Exception in decodeToGetWidthAndHeight." + e.getMessage());
        }
    }

    public int getMediaType() {
        return 2;
    }

    public int getRotation() {
        return this.mRotation;
    }

    protected void setLocation(String location) {
        super.setLocation(location);
        this.mVoiceOffset = 0;
        this.mRectifyOffset = 0;
        if (this.mIsBarcodeStartScan) {
            this.mNeedReScanBarcode = true;
        } else {
            this.mBarcodeScanResult = null;
            this.mIsBarcodeScanned = false;
        }
        this.mimeType = MediaFile.getMimeTypeForFile(this.mFilePath);
        if (this.mFileInfo.getFileType() == 2) {
            calcVoiceOffset();
        }
        if (this.mFileInfo.getFileType() == 8) {
            calcRectifyOffset();
        }
        if (this.mFileInfo.getFileType() == 9) {
            calcLivePhotoOffset();
        }
    }

    private void calcLivePhotoOffset() {
        if (!isThumbNail()) {
            long offSet = LiveUtils.getVideoOffset(this.mFilePath);
            if (offSet > 0) {
                this.mSpecialFileOffset = offSet;
            }
        }
    }

    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        if (GalleryUtils.isValidLocation(this.latitude, this.longitude)) {
            details.addDetail(4, new double[]{this.latitude, this.longitude});
        }
        if (!(!PhotoShareUtils.isFileExists(this.mFilePath) || this.mFilePath.equals(this.mFileInfo.getLocalThumbPath()) || this.mFilePath.equals(this.mFileInfo.getLocalBigThumbPath()))) {
            MediaDetails.extractExifInfo(details, this.mFilePath);
        }
        return details;
    }

    public void setBarcodeResult(BarcodeScanResultItem result) {
        if (this.mNeedReScanBarcode) {
            this.mBarcodeScanResult = null;
            this.mIsBarcodeStartScan = false;
            this.mIsBarcodeScanned = false;
            return;
        }
        this.mBarcodeScanResult = result;
        this.mIsBarcodeScanned = true;
        this.mIsBarcodeStartScan = false;
    }

    public BarcodeScanResultItem getBarcodeResult() {
        return this.mBarcodeScanResult;
    }

    public boolean isBarcodeNeedScan() {
        return (this.mIsBarcodeScanned || this.mIsBarcodeStartScan) ? false : true;
    }

    public void setBarcodeScanFlag(boolean flag) {
        this.mIsBarcodeStartScan = flag;
    }

    public long getVoiceOffset() {
        return this.mVoiceOffset;
    }

    public boolean isVoiceImage() {
        return this.mVoiceOffset > 0 || this.mFileInfo.getFileType() == 2;
    }

    public boolean is3DPanorama() {
        if (this.mFileInfo.getFileType() == 7) {
            return FyuseFile.isSupport3DPanoramaAPK();
        }
        return false;
    }

    public int getSpecialFileType() {
        switch (this.mFileInfo.getFileType()) {
            case 7:
                return 11;
            case 9:
                if (LiveUtils.LIVE_ENABLE) {
                    return 50;
                }
                return 0;
            default:
                return 0;
        }
    }

    public void calcVoiceOffset() {
        if (!isThumbNail()) {
            long offSet = Utils.getVoiceOffset(this.mFilePath);
            if (offSet > 0) {
                this.mVoiceOffset = offSet;
            }
        }
    }

    public boolean isRectifyImage() {
        if (RectifyUtils.isRectifyNativeSupport() && BitmapUtils.isRectifySupported(this.mimeType)) {
            return this.mRectifyOffset > 0 || this.mFileInfo.getFileType() == 8;
        } else {
            return false;
        }
    }

    public boolean is3DModelImage() {
        return ThreeDModelImageUtils.is3DModelImageSpecialFileType(this.mFileInfo.getFileType());
    }

    public int getRectifyOffset() {
        return this.mRectifyOffset;
    }

    public void calcRectifyOffset() {
        if (!isThumbNail()) {
            int offSet = RectifyUtils.getRectifyOffset(this.mFilePath);
            if (offSet > 0) {
                this.mRectifyOffset = offSet;
            }
        }
    }

    protected void setPhotoSharePreView() {
        this.isPreViewItem = this.mFileInfo.getFileId() == null;
        if (this.isPreViewItem) {
            this.mFileInfo.setFileType(1);
        }
    }

    public boolean isThumbNail() {
        if (!PhotoShareUtils.isFileExists(this.mFilePath) || this.mFilePath.equals(this.mFileInfo.getLocalThumbPath())) {
            return true;
        }
        return this.mFilePath.equals(this.mFileInfo.getLocalBigThumbPath());
    }

    protected void setFileSizeAndLastModify() {
        try {
            File file = new File(this.mFilePath);
            this.mFileSize = file.length();
            this.mDateTakenInMs = file.lastModified();
        } catch (Exception e) {
            this.mDateTakenInMs = 0;
            this.mFileSize = 0;
            GalleryLog.v("PhotoShareImage", "Exception In image setFileSizeAndLastModify " + e.toString());
        }
    }

    public boolean isSupportTranslateVoiceImageToVideo() {
        return isVoiceImage() && !isThumbNail();
    }

    public boolean canShare() {
        if (PhotoShareUtils.isFileExists(this.mFileInfo.getLocalBigThumbPath())) {
            return true;
        }
        return PhotoShareUtils.isFileExists(this.mFileInfo.getLocalRealPath());
    }
}
