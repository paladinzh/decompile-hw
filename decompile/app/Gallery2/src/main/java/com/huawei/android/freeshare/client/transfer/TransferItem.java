package com.huawei.android.freeshare.client.transfer;

import android.net.Uri;

public class TransferItem {
    public int mCurrentBytes;
    public String mMimetype;
    private Mission mMission;
    public int mStatus;
    public boolean mSuccess;
    public int mTotalBytes;
    private Uri mTransferUri;
    public String mUri;

    public TransferItem(Mission mission, String uri, String mimeType) {
        this.mMission = mission;
        this.mUri = uri;
        this.mMimetype = mimeType;
    }

    public final int getPorgress() {
        if (this.mTotalBytes == 0) {
            return 0;
        }
        return (this.mCurrentBytes * 100) / this.mTotalBytes;
    }

    public final Mission getMission() {
        return this.mMission;
    }

    public final boolean isTransferring() {
        return this.mStatus == 4;
    }

    public final boolean isComplete() {
        return this.mStatus == 5;
    }

    public final void setTransferUri(Uri uri) {
        this.mTransferUri = uri;
    }

    public final Uri getTransferUri() {
        return this.mTransferUri;
    }
}
