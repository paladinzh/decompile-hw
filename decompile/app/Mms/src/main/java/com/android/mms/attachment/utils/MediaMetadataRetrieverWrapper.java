package com.android.mms.attachment.utils;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import com.android.mms.attachment.Factory;
import com.huawei.cspcommon.MLog;
import java.io.IOException;

public class MediaMetadataRetrieverWrapper {
    private final MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();

    public void setDataSource(Uri uri) throws IOException {
        AssetFileDescriptor fd = Factory.get().getApplicationContext().getContentResolver().openAssetFileDescriptor(uri, "r");
        if (fd == null) {
            throw new IOException("openAssetFileDescriptor returned null for " + uri);
        }
        try {
            this.mRetriever.setDataSource(fd.getFileDescriptor());
            fd.close();
        } catch (RuntimeException e) {
            release();
            throw new IOException(e);
        } catch (Throwable th) {
            fd.close();
        }
    }

    public Bitmap getFrameAtTime() {
        return this.mRetriever.getFrameAtTime();
    }

    public void release() {
        try {
            this.mRetriever.release();
        } catch (RuntimeException e) {
            MLog.e("MediaMetadataRetrieverWrapper", "MediaMetadataRetriever.release failed", (Throwable) e);
        }
    }
}
