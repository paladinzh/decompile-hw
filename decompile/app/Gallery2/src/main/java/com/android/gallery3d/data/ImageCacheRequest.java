package com.android.gallery3d.data;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.data.BytesBufferPool.BytesBuffer;
import com.android.gallery3d.settings.HicloudAccountReceiver;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.media.services.StorageService;
import com.huawei.gallery.util.LayoutHelper;
import java.io.ByteArrayOutputStream;

abstract class ImageCacheRequest extends BaseJob<Bitmap> {
    protected GalleryApp mApplication;
    private int mHeight;
    protected boolean mIsRectangleThumbnail;
    private Path mPath;
    private int mTargetSize;
    private long mTimeModified;
    private int mType;
    private int mWidth;

    public abstract Bitmap onDecodeOriginal(JobContext jobContext, int i);

    public ImageCacheRequest(GalleryApp application, Path path, long timeModified, int type, int targetSize) {
        this.mApplication = application;
        this.mPath = path;
        this.mType = type;
        this.mTargetSize = targetSize;
        this.mTimeModified = timeModified;
    }

    public ImageCacheRequest(GalleryApp application, Path path, long timeModified, int type, int targetSize, boolean isRectangleThumbnail, int width, int height) {
        this(application, path, timeModified, type, targetSize);
        this.mIsRectangleThumbnail = isRectangleThumbnail;
        this.mWidth = width;
        this.mHeight = height;
    }

    private String debugTag() {
        StringBuilder append = new StringBuilder().append(this.mPath).append(",").append(this.mTimeModified).append(",");
        String str = this.mType == 1 ? "THUMB" : this.mType == 2 ? "MICROTHUMB" : "?";
        return append.append(str).toString();
    }

    public Bitmap run(JobContext jc) {
        Bitmap bitmap;
        MediaObject object = this.mPath.getObject();
        if (object instanceof MediaItem) {
            StorageService.updateVisitInfo(this.mApplication.getAndroidContext(), (MediaItem) object, this.mType);
        }
        boolean z = false;
        boolean isLocalVideo = object instanceof IVideo;
        if (object instanceof LocalMediaItem) {
            LocalMediaItem item = (LocalMediaItem) object;
            z = item.isDrm();
            if (item.isDrm() && !item.hasRight()) {
                Bitmap drmBitmap = null;
                if (this.mType == 2) {
                    drmBitmap = DrmUtils.getPlaceHolder(this.mApplication.getAndroidContext().getResources());
                    if (drmBitmap != null) {
                        drmBitmap = BitmapUtils.resizeAndCropCenter(drmBitmap, this.mTargetSize, true);
                    }
                }
                return drmBitmap;
            }
        }
        ImageCacheService cacheService = this.mApplication.getImageCacheService();
        if (!z || this.mType == 2) {
            BytesBuffer buffer = MediaItem.getBytesBufferPool().get();
            try {
                boolean found = cacheService.getImageData(this.mPath, this.mTimeModified, this.mType, buffer);
                if (jc.isCancelled()) {
                    return null;
                }
                if (found) {
                    Options options = new Options();
                    options.inPreferredConfig = Config.ARGB_8888;
                    if (this.mType == 2) {
                        bitmap = DecodeUtils.decode(jc, buffer.data, buffer.offset, buffer.length, options, MediaItem.getMicroThumbPool(this.mTargetSize));
                    } else {
                        boolean needRGB565 = false;
                        if (isLocalVideo) {
                            DecodeUtils.decodeBounds(jc, buffer.data, buffer.offset, buffer.length, options);
                            needRGB565 = Math.max(options.outWidth, options.outHeight) > this.mTargetSize;
                        }
                        options.inPreferredConfig = needRGB565 ? Config.RGB_565 : Config.ARGB_8888;
                        bitmap = DecodeUtils.decode(jc, buffer.data, buffer.offset, buffer.length, options, needRGB565 ? null : MediaItem.getThumbPool());
                    }
                    if (bitmap == null && !jc.isCancelled()) {
                        GalleryLog.w("ImageCacheRequest", "decode cached failed " + debugTag());
                    }
                    MediaItem.getBytesBufferPool().recycle(buffer);
                    return bitmap;
                }
                MediaItem.getBytesBufferPool().recycle(buffer);
            } finally {
                MediaItem.getBytesBufferPool().recycle(buffer);
            }
        }
        bitmap = onDecodeOriginal(jc, this.mType);
        if (jc.isCancelled()) {
            return null;
        }
        if (bitmap == null) {
            GalleryLog.w("ImageCacheRequest", "decode orig failed " + debugTag());
            return null;
        } else if (this.mType == 16) {
            return bitmap;
        } else {
            if (this.mIsRectangleThumbnail) {
                bitmap = BitmapUtils.resizeAndCropCenter(bitmap, this.mWidth, this.mHeight, true);
            } else if (this.mType == 2) {
                bitmap = BitmapUtils.resizeAndCropCenter(bitmap, this.mTargetSize, true);
            } else {
                int targetSize = this.mTargetSize;
                if (isLocalVideo && Config.RGB_565.equals(bitmap.getConfig())) {
                    targetSize = Math.max(LayoutHelper.getScreenLongSide(), this.mTargetSize);
                }
                bitmap = BitmapUtils.resizeDownBySideLength(bitmap, targetSize, true);
            }
            if (jc.isCancelled()) {
                return null;
            }
            byte[] array;
            if (isLocalVideo) {
                array = compressVideoBitmapToBytes(bitmap);
            } else {
                array = BitmapUtils.compressToBytes(bitmap);
            }
            if (jc.isCancelled()) {
                return null;
            }
            cacheService.putImageData(this.mPath, this.mTimeModified, this.mType, array);
            return bitmap;
        }
    }

    public boolean hasBufferCache() {
        MediaObject object = this.mPath.getObject();
        boolean z = false;
        if (object instanceof LocalMediaItem) {
            LocalMediaItem item = (LocalMediaItem) object;
            z = item.isDrm();
            if (z && !item.hasRight()) {
                return true;
            }
        }
        ImageCacheService cacheService = this.mApplication.getImageCacheService();
        if (z && this.mType != 2) {
            return false;
        }
        BytesBuffer buffer = MediaItem.getBytesBufferPool().get();
        try {
            boolean imageData = cacheService.getImageData(this.mPath, this.mTimeModified, this.mType, buffer);
            return imageData;
        } finally {
            MediaItem.getBytesBufferPool().recycle(buffer);
        }
    }

    private static byte[] compressVideoBitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT);
        EditorUtils.compressToJpeg(bitmap, -1, baos);
        return baos.toByteArray();
    }
}
