package com.android.mms.attachment.datamodel.media;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import java.util.List;

public class EncodedImageResource extends ImageResource {
    private byte[] mImageBytes;

    private class DecodeImageRequest implements MediaRequest<ImageResource> {
        public DecodeImageRequest() {
            EncodedImageResource.this.addRef();
        }

        public String getKey() {
            return EncodedImageResource.this.getKey();
        }

        public ImageResource loadMediaBlocking(List<MediaRequest<ImageResource>> list) throws Exception {
            EncodedImageResource.this.acquireLock();
            try {
                ImageResource decodedImageResource = new DecodedImageResource(getKey(), BitmapFactory.decodeByteArray(EncodedImageResource.this.mImageBytes, 0, EncodedImageResource.this.mImageBytes.length), EncodedImageResource.this.getOrientation());
                return decodedImageResource;
            } finally {
                EncodedImageResource.this.releaseLock();
                EncodedImageResource.this.release();
            }
        }

        public MediaCache<ImageResource> getMediaCache() {
            return null;
        }

        public int getRequestType() {
            return 2;
        }

        public MediaRequestDescriptor<ImageResource> getDescriptor() {
            return null;
        }
    }

    public EncodedImageResource(String key, byte[] imageBytes, int orientation) {
        super(key, orientation);
        setImageBytes(imageBytes);
    }

    private void setImageBytes(byte[] imageBytes) {
        this.mImageBytes = imageBytes;
    }

    public Bitmap getBitmap() {
        acquireLock();
        try {
            Bitmap decodeByteArray = BitmapFactory.decodeByteArray(this.mImageBytes, 0, this.mImageBytes.length);
            return decodeByteArray;
        } finally {
            releaseLock();
        }
    }

    public Bitmap reuseBitmap() {
        return null;
    }

    public boolean supportsBitmapReuse() {
        return false;
    }

    public int getMediaSize() {
        acquireLock();
        try {
            int length = this.mImageBytes.length;
            return length;
        } finally {
            releaseLock();
        }
    }

    protected void close() {
    }

    public Drawable getDrawable(Resources resources) {
        return null;
    }

    boolean isEncoded() {
        return true;
    }

    MediaRequest<? extends RefCountedMediaResource> getMediaDecodingRequest(MediaRequest<? extends RefCountedMediaResource> mediaRequest) {
        return new DecodeImageRequest();
    }
}
