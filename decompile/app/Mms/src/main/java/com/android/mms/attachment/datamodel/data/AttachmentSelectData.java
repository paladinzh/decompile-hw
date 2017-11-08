package com.android.mms.attachment.datamodel.data;

import android.net.Uri;

public class AttachmentSelectData {
    private int mAttachmentType;
    private Uri mAttachmentUri;
    private int mPosition;

    public AttachmentSelectData(int attachmentType) {
        this.mAttachmentType = attachmentType;
    }

    public int getAttachmentType() {
        return this.mAttachmentType;
    }

    public void setAttachmentUri(Uri attachmentUri) {
        this.mAttachmentUri = attachmentUri;
    }

    public Uri getAttachmentUri() {
        return this.mAttachmentUri;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    public int getPosition() {
        return this.mPosition;
    }
}
