package com.android.mms.attachment.datamodel.media;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.rastermill.FrameSequence;
import android.support.rastermill.FrameSequenceDrawable;
import com.huawei.cspcommon.MLog;
import java.io.IOException;
import java.io.InputStream;

public class GifImageResource extends ImageResource {
    private FrameSequence mFrameSequence;

    public GifImageResource(String key, FrameSequence frameSequence) {
        super(key, 1);
        this.mFrameSequence = frameSequence;
    }

    public static GifImageResource createGifImageResource(String key, InputStream inputStream) {
        GifImageResource gifImageResource = null;
        try {
            FrameSequence frameSequence = FrameSequence.decodeStream(inputStream);
            if (frameSequence == null) {
                return gifImageResource;
            }
            return new GifImageResource(key, frameSequence);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                gifImageResource = "createGifImageResource: fail closing the stream";
                MLog.e("GifImageResource", gifImageResource);
            }
        }
    }

    public Drawable getDrawable(Resources resources) {
        acquireLock();
        try {
            Drawable frameSequenceDrawable = new FrameSequenceDrawable(this.mFrameSequence);
            return frameSequenceDrawable;
        } finally {
            releaseLock();
        }
    }

    public Bitmap getBitmap() {
        return null;
    }

    public Bitmap reuseBitmap() {
        return null;
    }

    public boolean supportsBitmapReuse() {
        return false;
    }

    public int getMediaSize() {
        return 0;
    }

    public boolean isCacheable() {
        return false;
    }

    protected void close() {
        acquireLock();
        try {
            if (this.mFrameSequence != null) {
                this.mFrameSequence = null;
            }
            releaseLock();
        } catch (Throwable th) {
            releaseLock();
        }
    }
}
