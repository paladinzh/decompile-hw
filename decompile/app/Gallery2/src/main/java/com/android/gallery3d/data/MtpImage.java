package com.android.gallery3d.data;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.hardware.usb.UsbDevice;
import android.mtp.MtpObjectInfo;
import android.net.Uri;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.provider.GalleryProvider;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;

@TargetApi(12)
public class MtpImage extends MediaItem {
    private final Context mContext;
    private long mDateTaken;
    private final int mDeviceId;
    private String mFileName;
    private final int mImageHeight;
    private final int mImageWidth;
    private final MtpContext mMtpContext;
    private final MtpObjectInfo mObjInfo;
    private int mObjectId;
    private int mObjectSize;

    MtpImage(Path path, GalleryApp application, int deviceId, MtpObjectInfo objInfo, MtpContext mtpContext) {
        super(path, MediaObject.nextVersionNumber());
        this.mContext = application.getAndroidContext();
        this.mDeviceId = deviceId;
        this.mObjInfo = objInfo;
        this.mObjectId = objInfo.getObjectHandle();
        this.mObjectSize = objInfo.getCompressedSize();
        this.mDateTaken = objInfo.getDateCreated();
        this.mFileName = objInfo.getName();
        this.mImageWidth = objInfo.getImagePixWidth();
        this.mImageHeight = objInfo.getImagePixHeight();
        this.mMtpContext = mtpContext;
    }

    MtpImage(Path path, GalleryApp app, int deviceId, int objectId, MtpContext mtpContext) {
        this(path, app, deviceId, MtpDevice.getObjectInfo(mtpContext, deviceId, objectId), mtpContext);
    }

    public long getDateInMs() {
        return this.mDateTaken;
    }

    public Job<Bitmap> requestImage(final int type) {
        return new BaseJob<Bitmap>() {
            public Bitmap run(JobContext jc) {
                byte[] thumbnail = MtpImage.this.mMtpContext.getMtpClient().getThumbnail(UsbDevice.getDeviceName(MtpImage.this.mDeviceId), MtpImage.this.mObjectId);
                if (thumbnail == null) {
                    GalleryLog.w("MtpImage", "decoding thumbnail failed");
                    return null;
                }
                Bitmap bitmap = DecodeUtils.decode(jc, thumbnail, null);
                if (type == 2) {
                    return BitmapUtils.resizeAndCropCenter(bitmap, MediaItem.getTargetSize(type), true);
                }
                return bitmap;
            }

            public String workContent() {
                return "decode thumbnail for mtp";
            }
        };
    }

    public Job<BitmapRegionDecoder> requestLargeImage() {
        return new BaseJob<BitmapRegionDecoder>() {
            public BitmapRegionDecoder run(JobContext jc) {
                byte[] bytes = MtpImage.this.mMtpContext.getMtpClient().getObject(UsbDevice.getDeviceName(MtpImage.this.mDeviceId), MtpImage.this.mObjectId, MtpImage.this.mObjectSize);
                return DecodeUtils.createBitmapRegionDecoder(jc, bytes, 0, bytes.length, false);
            }

            public String workContent() {
                return "create region decoder for mtp";
            }
        };
    }

    public byte[] getImageData() {
        return this.mMtpContext.getMtpClient().getObject(UsbDevice.getDeviceName(this.mDeviceId), this.mObjectId, this.mObjectSize);
    }

    public boolean Import() {
        return this.mMtpContext.copyFile(UsbDevice.getDeviceName(this.mDeviceId), this.mObjInfo);
    }

    public int getSupportedOperations() {
        return 2112;
    }

    public void updateContent(MtpObjectInfo info) {
        if (this.mObjectId != info.getObjectHandle() || this.mDateTaken != info.getDateCreated()) {
            this.mObjectId = info.getObjectHandle();
            this.mDateTaken = info.getDateCreated();
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
    }

    public String getMimeType() {
        return "image/jpeg";
    }

    public int getMediaType() {
        return 2;
    }

    public long getSize() {
        return (long) this.mObjectSize;
    }

    public Uri getContentUri() {
        return GalleryProvider.getUriFor(this.mContext, this.mPath);
    }

    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        details.addDetail(1, this.mFileName);
        details.addDetail(3, Long.valueOf(this.mDateTaken));
        details.addDetail(6, Integer.valueOf(this.mImageWidth));
        details.addDetail(7, Integer.valueOf(this.mImageHeight));
        details.addDetail(5, Long.valueOf((long) this.mObjectSize));
        return details;
    }

    public int getWidth() {
        return this.mImageWidth;
    }

    public int getHeight() {
        return this.mImageHeight;
    }
}
