package com.android.mms.attachment.datamodel.media;

import android.content.Context;
import android.net.Uri;
import com.android.mms.attachment.utils.UriUtil;

public class UriImageRequestDescriptor extends ImageRequestDescriptor {
    public final boolean allowCompression;
    public final Uri uri;

    public UriImageRequestDescriptor(Uri uri, int desiredWidth, int desiredHeight, int sourceWidth, int sourceHeight, boolean allowCompression, boolean isStatic, boolean cropToCircle, int circleBackgroundColor, int circleStrokeColor) {
        super(desiredWidth, desiredHeight, sourceWidth, sourceHeight, isStatic, cropToCircle, circleBackgroundColor, circleStrokeColor);
        this.uri = uri;
        this.allowCompression = allowCompression;
    }

    public String getKey() {
        if (this.uri == null) {
            return null;
        }
        return this.uri + '|' + String.valueOf(this.allowCompression) + '|' + super.getKey();
    }

    public MediaRequest<ImageResource> buildSyncMediaRequest(Context context) {
        if (this.uri == null || UriUtil.isLocalUri(this.uri)) {
            return new UriImageRequest(context, this);
        }
        return new NetworkUriImageRequest(context, this);
    }

    public Long getMediaStoreId() {
        return null;
    }
}
