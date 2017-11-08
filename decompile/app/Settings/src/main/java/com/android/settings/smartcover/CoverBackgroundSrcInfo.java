package com.android.settings.smartcover;

public class CoverBackgroundSrcInfo {
    private int mImageIndex;
    private int mImageSrcId;
    private int mType;

    public int getmImageIndex() {
        return this.mImageIndex;
    }

    public void setmImageIndex(int mImageIndex) {
        this.mImageIndex = mImageIndex;
    }

    public int getType() {
        return this.mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public int getImageSrcId() {
        return this.mImageSrcId;
    }

    public void setImageSrcId(int imageSrcId) {
        this.mImageSrcId = imageSrcId;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof CoverBackgroundSrcInfo)) {
            return false;
        }
        CoverBackgroundSrcInfo info = (CoverBackgroundSrcInfo) o;
        if (info.getImageSrcId() == this.mImageSrcId && info.getType() == this.mType) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return getImageSrcId() + 31;
    }
}
