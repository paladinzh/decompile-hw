package com.huawei.gallery.ui;

import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.BitmapLoader;

public class MediaItemEntry extends BaseEntry {
    private int mFlag = 0;
    public MediaItem mItem;
    public int mediaType;

    public MediaItemEntry(ThumbnailLoaderListener listener, int index, MediaItem item) {
        int i = 1;
        BitmapLoader bitmapLoader = null;
        int i2 = 0;
        this.mItem = item;
        this.path = item == null ? null : item.getPath();
        this.rotation = item == null ? 0 : item.getRotation();
        this.mediaType = item == null ? 1 : item.getMediaType();
        this.isPreview = item == null ? false : item.isPhotoSharePreView();
        this.isUploadFailed = item == null ? false : item.isPhotoShareUploadFailItem();
        this.isCloudPlaceHolder = item == null ? false : item.isCloudPlaceholder();
        this.isWaitToUpload = item == null ? false : item.isWaitToUpload();
        if (item == null) {
            this.mFlag = 0;
        } else {
            int i3 = this.mFlag;
            if (!item.isVoiceImage()) {
                i = 0;
            }
            this.mFlag = i3 | i;
            i = this.mFlag;
            if (item.isMyFavorite()) {
                i3 = 2;
            } else {
                i3 = 0;
            }
            this.mFlag = i3 | i;
            i = this.mFlag;
            if (item.isBurstCover()) {
                i3 = 4;
            } else {
                i3 = 0;
            }
            this.mFlag = i3 | i;
            i = this.mFlag;
            if (item.isRefocusPhoto()) {
                i3 = 16;
            } else {
                i3 = 0;
            }
            this.mFlag = i3 | i;
            i = this.mFlag;
            if (item.is3DPanorama()) {
                i3 = 256;
            } else {
                i3 = 0;
            }
            this.mFlag = i3 | i;
            i = this.mFlag;
            if (4 == item.getMediaType()) {
                i3 = 8;
            } else {
                i3 = 0;
            }
            this.mFlag = i3 | i;
            i = this.mFlag;
            if ((item.getExtraTag() & 1) != 0) {
                i3 = 32;
            } else {
                i3 = 0;
            }
            this.mFlag = i3 | i;
            i = this.mFlag;
            if ((item.getExtraTag() & 2) != 0) {
                i3 = 64;
            } else {
                i3 = 0;
            }
            this.mFlag = i3 | i;
            i = this.mFlag;
            if ((item.getExtraTag() & 4) != 0) {
                i3 = 128;
            } else {
                i3 = 0;
            }
            this.mFlag = i3 | i;
            i = this.mFlag;
            if (item.isRectifyImage()) {
                i3 = 512;
            } else {
                i3 = 0;
            }
            this.mFlag = i3 | i;
            i = this.mFlag;
            if (item.is3DModelImage()) {
                i3 = 1024;
            } else {
                i3 = 0;
            }
            this.mFlag = i3 | i;
            i3 = this.mFlag;
            if (item.getSpecialFileType() == 50) {
                i2 = 2048;
            }
            this.mFlag = i3 | i2;
        }
        if (item != null) {
            bitmapLoader = new MediaItemLoader(listener, index, item);
        }
        this.contentLoader = bitmapLoader;
    }

    public boolean startLoad() {
        return super.startLoad();
    }

    public void cancelLoad() {
        super.cancelLoad();
    }

    public boolean isSupportFlag(int flag) {
        return (this.mFlag & flag) != 0;
    }
}
