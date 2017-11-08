package com.android.gallery3d.data;

import com.android.gallery3d.app.GalleryApp;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.TagFileInfo;

public class PhotoShareTagFile extends PhotoShareImage {
    protected TagFileInfo mTagFileInfo;

    PhotoShareTagFile(Path path, GalleryApp application, FileInfo fileInfo, int folderType, String albumName, TagFileInfo tagFileInfo) {
        super(path, application, fileInfo, folderType, albumName);
        this.mTagFileInfo = tagFileInfo;
        this.mNeedVideoThumbnail = true;
    }

    public TagFileInfo getTagFileInfo() {
        return this.mTagFileInfo;
    }

    public void updateFromTagFileInfo(TagFileInfo tagFileInfo, FileInfo fileInfo) {
        this.mTagFileInfo = tagFileInfo;
        updateFromFileInfo(fileInfo);
    }
}
