package com.android.mms.attachment.datamodel.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.RectF;
import com.android.mms.attachment.Factory;
import com.android.mms.attachment.datamodel.media.PoolableImageCache.ReusableImageResourcePool;
import com.android.mms.attachment.utils.ImageUtils;
import com.android.mms.exif.ExifInterface;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class ImageRequest<D extends ImageRequestDescriptor> implements MediaRequest<ImageResource> {
    protected final Context mContext;
    protected final D mDescriptor;
    protected int mOrientation;

    protected abstract InputStream getInputStreamForResource() throws FileNotFoundException;

    public ImageRequest(Context context, D descriptor) {
        this.mContext = context;
        this.mDescriptor = descriptor;
    }

    public String getKey() {
        return this.mDescriptor.getKey();
    }

    public D getDescriptor() {
        return this.mDescriptor;
    }

    public int getRequestType() {
        return 3;
    }

    protected boolean hasBitmapObject() {
        return false;
    }

    protected Bitmap getBitmapForResource() throws IOException {
        return null;
    }

    public final ImageResource loadMediaBlocking(List<MediaRequest<ImageResource>> chainedTask) throws IOException {
        return postProcessOnBitmapResourceLoaded(loadMediaInternal(chainedTask));
    }

    protected ImageResource loadMediaInternal(List<MediaRequest<ImageResource>> list) throws IOException {
        if (this.mDescriptor.isStatic() || !isGif()) {
            Bitmap loadedBitmap = loadBitmapInternal();
            if (loadedBitmap != null) {
                return new DecodedImageResource(getKey(), loadedBitmap, this.mOrientation);
            }
            throw new RuntimeException("failed decoding bitmap");
        }
        GifImageResource gifImageResource = GifImageResource.createGifImageResource(getKey(), getInputStreamForResource());
        if (gifImageResource != null) {
            return gifImageResource;
        }
        throw new RuntimeException("Error decoding gif");
    }

    protected boolean isGif() throws FileNotFoundException {
        return ImageUtils.isGif(getInputStreamForResource());
    }

    protected Bitmap loadBitmapInternal() throws IOException {
        InputStream inputStream;
        boolean unknownSize = this.mDescriptor.sourceWidth != -1 ? this.mDescriptor.sourceHeight == -1 : true;
        if (hasBitmapObject()) {
            Bitmap bitmap = getBitmapForResource();
            if (bitmap != null && unknownSize) {
                this.mDescriptor.updateSourceDimensions(bitmap.getWidth(), bitmap.getHeight());
            }
            return bitmap;
        }
        this.mOrientation = ImageUtils.getOrientation(getInputStreamForResource());
        Options options = PoolableImageCache.getBitmapOptionsForPool(false, 0, 0);
        if (unknownSize) {
            inputStream = getInputStreamForResource();
            if (inputStream != null) {
                try {
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(inputStream, null, options);
                    if (ExifInterface.getOrientationParams(this.mOrientation).invertDimensions) {
                        this.mDescriptor.updateSourceDimensions(options.outHeight, options.outWidth);
                    } else {
                        this.mDescriptor.updateSourceDimensions(options.outWidth, options.outHeight);
                    }
                    inputStream.close();
                } catch (Throwable th) {
                    inputStream.close();
                }
            } else {
                throw new FileNotFoundException();
            }
        }
        options.outWidth = this.mDescriptor.sourceWidth;
        options.outHeight = this.mDescriptor.sourceHeight;
        options.inSampleSize = ImageUtils.get().calculateInSampleSize(options, this.mDescriptor.desiredWidth, this.mDescriptor.desiredHeight);
        inputStream = getInputStreamForResource();
        if (inputStream != null) {
            try {
                options.inJustDecodeBounds = false;
                ReusableImageResourcePool bitmapPool = getBitmapPool();
                Bitmap decodeStream;
                if (bitmapPool == null) {
                    decodeStream = BitmapFactory.decodeStream(inputStream, null, options);
                    return decodeStream;
                }
                decodeStream = bitmapPool.decodeSampledBitmapFromInputStream(inputStream, options, ((options.outWidth + options.inSampleSize) - 1) / options.inSampleSize, ((options.outHeight + options.inSampleSize) - 1) / options.inSampleSize);
                inputStream.close();
                return decodeStream;
            } finally {
                inputStream.close();
            }
        } else {
            throw new FileNotFoundException();
        }
    }

    private ImageResource postProcessOnBitmapResourceLoaded(ImageResource loadedResource) {
        boolean z = false;
        if (!this.mDescriptor.cropToCircle || !(loadedResource instanceof DecodedImageResource)) {
            return loadedResource;
        }
        int width = this.mDescriptor.desiredWidth;
        int height = this.mDescriptor.desiredHeight;
        Bitmap sourceBitmap = loadedResource.getBitmap();
        Bitmap targetBitmap = getBitmapPool().createOrReuseBitmap(width, height);
        RectF dest = new RectF(0.0f, 0.0f, (float) width, (float) height);
        RectF source = new RectF(0.0f, 0.0f, (float) sourceBitmap.getWidth(), (float) sourceBitmap.getHeight());
        int backgroundColor = this.mDescriptor.circleBackgroundColor;
        int strokeColor = this.mDescriptor.circleStrokeColor;
        Canvas canvas = new Canvas(targetBitmap);
        if (backgroundColor != 0) {
            z = true;
        }
        ImageUtils.drawBitmapWithCircleOnCanvas(sourceBitmap, canvas, source, dest, null, z, backgroundColor, strokeColor);
        return new DecodedImageResource(getKey(), targetBitmap, loadedResource.getOrientation());
    }

    protected ReusableImageResourcePool getBitmapPool() {
        return Factory.get().getMediaCacheManager().getOrCreateBitmapPoolForCache(getCacheId());
    }

    public MediaCache<ImageResource> getMediaCache() {
        return Factory.get().getMediaCacheManager().getOrCreateMediaCacheById(getCacheId());
    }

    public int getCacheId() {
        return 1;
    }
}
