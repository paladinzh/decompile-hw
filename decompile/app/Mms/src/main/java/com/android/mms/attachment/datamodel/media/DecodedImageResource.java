package com.android.mms.attachment.datamodel.media;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.messaging.util.OsUtil;
import com.android.mms.attachment.ui.OrientedBitmapDrawable;
import com.android.mms.attachment.utils.ImageUtils;
import com.huawei.cspcommon.MLog;
import java.util.List;

public class DecodedImageResource extends ImageResource {
    private Bitmap mBitmap;
    private boolean mCacheable = true;
    private final int mOrientation;

    private class EncodeImageRequest implements MediaRequest<ImageResource> {
        private final MediaRequest<ImageResource> mOriginalImageRequest;

        public EncodeImageRequest(MediaRequest<ImageResource> originalImageRequest) {
            this.mOriginalImageRequest = originalImageRequest;
            DecodedImageResource.this.addRef();
        }

        public String getKey() {
            return DecodedImageResource.this.getKey();
        }

        public ImageResource loadMediaBlocking(List<MediaRequest<ImageResource>> list) throws Exception {
            DecodedImageResource.this.acquireLock();
            Bitmap bitmap = null;
            ImageResource encodedImageResource;
            try {
                Bitmap bitmap2 = DecodedImageResource.this.getBitmap();
                int bitmapWidth = bitmap2.getWidth();
                int bitmapHeight = bitmap2.getHeight();
                if (bitmapWidth > 0 && bitmapHeight > 0 && (this.mOriginalImageRequest instanceof ImageRequest)) {
                    ImageRequestDescriptor descriptor = ((ImageRequest) this.mOriginalImageRequest).getDescriptor();
                    float targetScale = Math.max(((float) descriptor.desiredWidth) / ((float) bitmapWidth), ((float) descriptor.desiredHeight) / ((float) bitmapHeight));
                    int targetWidth = (int) (((float) bitmapWidth) * targetScale);
                    int targetHeight = (int) (((float) bitmapHeight) * targetScale);
                    if (targetScale < ContentUtil.FONT_SIZE_NORMAL && targetWidth > 0 && targetHeight > 0 && targetWidth != bitmapWidth && targetHeight != bitmapHeight) {
                        bitmap2 = Bitmap.createScaledBitmap(bitmap2, targetWidth, targetHeight, false);
                        bitmap = bitmap2;
                    }
                }
                encodedImageResource = new EncodedImageResource(getKey(), ImageUtils.bitmapToBytes(bitmap2, 50), DecodedImageResource.this.getOrientation());
                return encodedImageResource;
            } catch (Exception ex) {
                MLog.e("DecodedImageResource", "Error compressing bitmap", (Throwable) ex);
                encodedImageResource = DecodedImageResource.this;
                return encodedImageResource;
            } finally {
                if (!(bitmap == null || bitmap == DecodedImageResource.this.getBitmap())) {
                    bitmap.recycle();
                }
                DecodedImageResource.this.releaseLock();
                DecodedImageResource.this.release();
            }
        }

        public MediaCache<ImageResource> getMediaCache() {
            return this.mOriginalImageRequest.getMediaCache();
        }

        public int getRequestType() {
            return 1;
        }

        public MediaRequestDescriptor<ImageResource> getDescriptor() {
            return this.mOriginalImageRequest.getDescriptor();
        }
    }

    public DecodedImageResource(String key, Bitmap bitmap, int orientation) {
        super(key, orientation);
        this.mBitmap = bitmap;
        this.mOrientation = orientation;
    }

    public Bitmap getBitmap() {
        acquireLock();
        try {
            Bitmap bitmap = this.mBitmap;
            return bitmap;
        } finally {
            releaseLock();
        }
    }

    public Bitmap reuseBitmap() {
        acquireLock();
        try {
            assertSingularRefCount();
            Bitmap retBitmap = this.mBitmap;
            this.mBitmap = null;
            return retBitmap;
        } finally {
            releaseLock();
        }
    }

    public boolean supportsBitmapReuse() {
        return true;
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public int getMediaSize() {
        acquireLock();
        try {
            int byteCount;
            if (OsUtil.isAtLeastKLP()) {
                byteCount = this.mBitmap.getByteCount();
                return byteCount;
            }
            byteCount = this.mBitmap.getRowBytes() * this.mBitmap.getHeight();
            releaseLock();
            return byteCount;
        } finally {
            releaseLock();
        }
    }

    protected void close() {
        acquireLock();
        try {
            if (this.mBitmap != null) {
                this.mBitmap.recycle();
                this.mBitmap = null;
            }
            releaseLock();
        } catch (Throwable th) {
            releaseLock();
        }
    }

    public Drawable getDrawable(Resources resources) {
        acquireLock();
        try {
            Drawable create = OrientedBitmapDrawable.create(getOrientation(), resources, this.mBitmap);
            return create;
        } finally {
            releaseLock();
        }
    }

    boolean isCacheable() {
        return this.mCacheable;
    }

    public void setCacheable(boolean cacheable) {
        this.mCacheable = cacheable;
    }

    MediaRequest<? extends RefCountedMediaResource> getMediaEncodingRequest(MediaRequest<? extends RefCountedMediaResource> originalRequest) {
        if (getBitmap().hasAlpha()) {
            return null;
        }
        return new EncodeImageRequest(originalRequest);
    }
}
