package com.android.mms.attachment.datamodel.media;

import android.content.Context;
import com.android.mms.attachment.utils.UriUtil;

public class VideoThumbnailRequestDescriptor extends UriImageRequestDescriptor {
    protected final long mMediaId;

    public VideoThumbnailRequestDescriptor(long id, String path, int desiredWidth, int desiredHeight, int sourceWidth, int sourceHeight) {
        super(UriUtil.getUriForResourceFile(path), desiredWidth, desiredHeight, sourceWidth, sourceHeight, false, false, false, 0, 0);
        this.mMediaId = id;
    }

    public MediaRequest<ImageResource> buildSyncMediaRequest(Context context) {
        return new VideoThumbnailRequest(context, this);
    }

    public Long getMediaStoreId() {
        return Long.valueOf(this.mMediaId);
    }
}
