package com.android.mms.attachment.datamodel.media;

import android.content.Context;

public abstract class ImageRequestDescriptor extends MediaRequestDescriptor<ImageResource> {
    public final int circleBackgroundColor;
    public final int circleStrokeColor;
    public final boolean cropToCircle;
    public final int desiredHeight;
    public final int desiredWidth;
    public final boolean isStatic;
    public final int sourceHeight;
    public final int sourceWidth;

    public abstract MediaRequest<ImageResource> buildSyncMediaRequest(Context context);

    public ImageRequestDescriptor() {
        this(-1, -1, -1, -1, false, false, 0, 0);
    }

    public ImageRequestDescriptor(int desiredWidth, int desiredHeight, int sourceWidth, int sourceHeight, boolean isStatic, boolean cropToCircle, int circleBackgroundColor, int circleStrokeColor) {
        this.desiredWidth = desiredWidth;
        this.desiredHeight = desiredHeight;
        this.sourceWidth = sourceWidth;
        this.sourceHeight = sourceHeight;
        this.isStatic = isStatic;
        this.cropToCircle = cropToCircle;
        this.circleBackgroundColor = circleBackgroundColor;
        this.circleStrokeColor = circleStrokeColor;
    }

    public String getKey() {
        return this.desiredWidth + '|' + this.desiredHeight + '|' + String.valueOf(this.cropToCircle) + '|' + String.valueOf(this.circleBackgroundColor) + '|' + String.valueOf(this.isStatic);
    }

    public boolean isStatic() {
        return this.isStatic;
    }

    public void updateSourceDimensions(int sourceWidth, int sourceHeight) {
    }
}
