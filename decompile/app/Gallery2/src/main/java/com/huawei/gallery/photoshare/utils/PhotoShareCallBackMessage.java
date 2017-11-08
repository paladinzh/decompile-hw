package com.huawei.gallery.photoshare.utils;

import android.text.TextUtils;

public class PhotoShareCallBackMessage {
    private String mAlbumPath = "";
    private int mAlbumSetType = -1;
    private String mCategoryType = "";
    private int mMessageType = -1;
    private String mTagAlbumID = "";

    public void setMessageType(int messageType) {
        this.mMessageType = messageType;
    }

    public void setAlbumSetType(int albumSetType) {
        this.mAlbumSetType = albumSetType;
    }

    public void setAlbumPath(String albumPath) {
        if (TextUtils.isEmpty(albumPath)) {
            this.mAlbumPath = "";
        } else {
            this.mAlbumPath = albumPath;
        }
    }

    public void setCategoryType(String categoryType) {
        if (TextUtils.isEmpty(categoryType)) {
            this.mCategoryType = "";
        } else {
            this.mCategoryType = categoryType;
        }
    }

    public void setTagAlbumID(String tagAlbumID) {
        if (TextUtils.isEmpty(tagAlbumID)) {
            this.mTagAlbumID = "";
        } else {
            this.mTagAlbumID = tagAlbumID;
        }
    }

    public int getMessageType() {
        return this.mMessageType;
    }

    public int getAlbumSetType() {
        return this.mAlbumSetType;
    }

    public String getAlbumPath() {
        return this.mAlbumPath;
    }

    public String getCategoryType() {
        return this.mCategoryType;
    }

    public String getTagAlbumID() {
        return this.mTagAlbumID;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PhotoShareCallBackMessage)) {
            return false;
        }
        PhotoShareCallBackMessage message = (PhotoShareCallBackMessage) o;
        return this.mAlbumSetType == message.mAlbumSetType && this.mMessageType == message.mMessageType && this.mAlbumPath.equals(message.mAlbumPath) && this.mCategoryType.equals(message.mCategoryType) && this.mTagAlbumID.equals(message.mTagAlbumID);
    }

    public int hashCode() {
        return (((((((this.mMessageType * 31) + this.mAlbumSetType) * 31) + this.mAlbumPath.hashCode()) * 31) + this.mCategoryType.hashCode()) * 31) + this.mTagAlbumID.hashCode();
    }

    public String toString() {
        return "MessageType: " + this.mMessageType + " AlbumSetType: " + this.mAlbumSetType + " CategoryType: " + this.mCategoryType + " AlbumPath: " + this.mAlbumPath + " TagAlbumID: " + this.mTagAlbumID;
    }
}
