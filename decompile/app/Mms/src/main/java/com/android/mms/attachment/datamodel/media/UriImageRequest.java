package com.android.mms.attachment.datamodel.media;

import android.content.Context;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class UriImageRequest<D extends UriImageRequestDescriptor> extends ImageRequest<D> {
    public UriImageRequest(Context context, D descriptor) {
        super(context, descriptor);
    }

    protected InputStream getInputStreamForResource() throws FileNotFoundException {
        return this.mContext.getContentResolver().openInputStream(((UriImageRequestDescriptor) this.mDescriptor).uri);
    }

    protected ImageResource loadMediaInternal(List<MediaRequest<ImageResource>> chainedTasks) throws IOException {
        ImageResource resource = super.loadMediaInternal(chainedTasks);
        if (((UriImageRequestDescriptor) this.mDescriptor).allowCompression && chainedTasks != null) {
            MediaRequest<ImageResource> chainedTask = resource.getMediaEncodingRequest(this);
            if (chainedTask != null) {
                chainedTasks.add(chainedTask);
                if (resource instanceof DecodedImageResource) {
                    ((DecodedImageResource) resource).setCacheable(false);
                }
            }
        }
        return resource;
    }
}
