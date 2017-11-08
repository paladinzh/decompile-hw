package com.android.mms.attachment.datamodel.media;

import android.content.Context;
import com.android.mms.attachment.utils.UriUtil;

public class FileImageRequestDescriptor extends UriImageRequestDescriptor {
    public final boolean canUseThumbnail;
    public final String path;

    public FileImageRequestDescriptor(String path, int desiredWidth, int desiredHeight, int sourceWidth, int sourceHeight, boolean canUseThumbnail, boolean canCompress, boolean isStatic) {
        super(UriUtil.getUriForResourceFile(path), desiredWidth, desiredHeight, sourceWidth, sourceHeight, canCompress, isStatic, false, 0, 0);
        this.path = path;
        this.canUseThumbnail = canUseThumbnail;
    }

    public String getKey() {
        String prefixKey = super.getKey();
        if (prefixKey == null) {
            return null;
        }
        return '|' + this.canUseThumbnail;
    }

    public MediaRequest<ImageResource> buildSyncMediaRequest(Context context) {
        return new FileImageRequest(context, this);
    }
}
