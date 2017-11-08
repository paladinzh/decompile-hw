package com.android.mms.attachment.datamodel.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ExifInterface;
import com.android.mms.attachment.datamodel.media.PoolableImageCache.ReusableImageResourcePool;
import com.android.mms.attachment.utils.ImageUtils;
import com.huawei.cspcommon.MLog;
import java.io.IOException;
import java.io.InputStream;

public class FileImageRequest extends UriImageRequest {
    private final boolean mCanUseThumbnail;
    private final String mPath;

    public FileImageRequest(Context context, FileImageRequestDescriptor descriptor) {
        super(context, descriptor);
        this.mPath = descriptor.path;
        this.mCanUseThumbnail = descriptor.canUseThumbnail;
    }

    protected Bitmap loadBitmapInternal() throws IOException {
        InputStream sizeStream = getInputStreamForResource();
        if (sizeStream != null) {
            try {
                if (sizeStream.available() <= 0) {
                    throw new IOException("loadBitmapInternal failed, file size error:" + this.mPath + "," + sizeStream.available());
                }
            } finally {
                sizeStream.close();
            }
        }
        if (this.mCanUseThumbnail) {
            byte[] thumbnail = null;
            try {
                ExifInterface exif = new ExifInterface(this.mPath);
                if (exif.hasThumbnail()) {
                    thumbnail = exif.getThumbnail();
                }
            } catch (IOException e) {
                MLog.e("FileImageRequest", "loadBitmapInternal: IOException");
            }
            if (thumbnail != null) {
                Options options = PoolableImageCache.getBitmapOptionsForPool(false, 0, 0);
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length, options);
                options.inSampleSize = ImageUtils.get().calculateInSampleSize(options, this.mDescriptor.desiredWidth, this.mDescriptor.desiredHeight);
                options.inJustDecodeBounds = false;
                try {
                    this.mOrientation = ImageUtils.getOrientation(getInputStreamForResource());
                    if (com.android.mms.exif.ExifInterface.getOrientationParams(this.mOrientation).invertDimensions) {
                        this.mDescriptor.updateSourceDimensions(options.outHeight, options.outWidth);
                    } else {
                        this.mDescriptor.updateSourceDimensions(options.outWidth, options.outHeight);
                    }
                    ReusableImageResourcePool bitmapPool = getBitmapPool();
                    if (bitmapPool == null) {
                        return BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length, options);
                    }
                    return bitmapPool.decodeByteArray(thumbnail, options, options.outWidth / options.inSampleSize, options.outHeight / options.inSampleSize);
                } catch (IOException ex) {
                    MLog.e("FileImageRequest", "FileImageRequest: failed to load thumbnail from Exif", (Throwable) ex);
                }
            }
        }
        return super.loadBitmapInternal();
    }
}
