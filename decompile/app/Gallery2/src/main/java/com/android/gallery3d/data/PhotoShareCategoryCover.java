package com.android.gallery3d.data;

import com.android.gallery3d.app.GalleryApp;
import com.huawei.android.cg.vo.FileInfo;

public class PhotoShareCategoryCover extends PhotoShareImage {
    PhotoShareCategoryCover(Path path, GalleryApp application, FileInfo fileInfo, int folderType, String albumName) {
        super(path, application, fileInfo, folderType, albumName);
        this.mIsRectangleThumbnail = true;
    }
}
