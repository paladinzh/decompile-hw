package com.android.mms.attachment.datamodel.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore.Video.Thumbnails;
import com.android.mms.attachment.Factory;
import com.android.mms.attachment.utils.MediaMetadataRetrieverWrapper;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class VideoThumbnailRequest extends ImageRequest<UriImageRequestDescriptor> {
    public VideoThumbnailRequest(Context context, UriImageRequestDescriptor descriptor) {
        super(context, descriptor);
    }

    protected InputStream getInputStreamForResource() throws FileNotFoundException {
        return null;
    }

    protected boolean hasBitmapObject() {
        return true;
    }

    protected Bitmap getBitmapForResource() throws IOException {
        Long mediaId = ((UriImageRequestDescriptor) this.mDescriptor).getMediaStoreId();
        Bitmap bitmap = null;
        if (mediaId != null) {
            bitmap = Thumbnails.getThumbnail(Factory.get().getApplicationContext().getContentResolver(), mediaId.longValue(), 3, null);
        } else {
            MediaMetadataRetrieverWrapper retriever = new MediaMetadataRetrieverWrapper();
            try {
                retriever.setDataSource(((UriImageRequestDescriptor) this.mDescriptor).uri);
                bitmap = retriever.getFrameAtTime();
            } finally {
                retriever.release();
            }
        }
        if (bitmap != null) {
            ((UriImageRequestDescriptor) this.mDescriptor).updateSourceDimensions(bitmap.getWidth(), bitmap.getHeight());
        }
        return bitmap;
    }
}
