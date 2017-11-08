package com.android.gallery3d.data;

import android.graphics.Bitmap;
import android.os.RemoteException;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.PhotoShareImage.PhotoShareImageRequest;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool.Job;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.TagFileInfo;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.io.File;

public class PhotoShareTagCover extends PhotoShareTagFile {
    private long mLastRequest = 0;

    PhotoShareTagCover(Path path, GalleryApp application, FileInfo fileInfo, int folderType, String albumName, TagFileInfo tagFileInfo) {
        super(path, application, fileInfo, folderType, albumName, tagFileInfo);
    }

    public Job<Bitmap> requestImage(int type) {
        String thumbUrl;
        long lastModify;
        if (PhotoShareUtils.isFileExists(this.mTagFileInfo.getThumbUrl())) {
            thumbUrl = this.mTagFileInfo.getThumbUrl();
            File file = new File(thumbUrl);
            if (file.exists()) {
                lastModify = file.lastModified();
            } else {
                lastModify = 0;
            }
        } else {
            thumbUrl = "";
            lastModify = 0;
        }
        return new PhotoShareImageRequest(this.mApplication, this.mPath, lastModify, type, thumbUrl, this.mNeedVideoThumbnail, this.mIsRectangleThumbnail);
    }

    public void requestFaceThumbUrl() {
        if (!PhotoShareUtils.isFileExists(this.mTagFileInfo.getThumbUrl())) {
            long now = System.currentTimeMillis();
            long interval = now - this.mLastRequest;
            if (interval >= 15000 || interval <= 0) {
                this.mLastRequest = now;
                new Thread() {
                    public void run() {
                        try {
                            PhotoShareUtils.getServer().downloadTagInfoCoverPhoto(PhotoShareTagCover.this.mTagFileInfo.getCategoryId(), PhotoShareTagCover.this.mTagFileInfo.getTagId(), new TagFileInfo[]{PhotoShareTagCover.this.mTagFileInfo});
                            GalleryLog.v("PhotoShareTagCover", "downloadTagInfoCoverPhoto tagID " + PhotoShareTagCover.this.mTagFileInfo.getTagId() + " hash " + PhotoShareTagCover.this.mTagFileInfo.getHash());
                        } catch (RemoteException e) {
                            PhotoShareUtils.dealRemoteException(e);
                        }
                    }
                }.start();
            } else {
                GalleryLog.v("PhotoShareTagCover", "request face cover too often");
            }
        }
    }
}
