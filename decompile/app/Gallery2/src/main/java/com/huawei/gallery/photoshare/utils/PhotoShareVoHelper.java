package com.huawei.gallery.photoshare.utils;

import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.FileInfoDetail;

public class PhotoShareVoHelper {
    public static FileInfoDetail getFileInfoDetail(FileInfo fileInfo) {
        FileInfoDetail fileInfoDetail = new FileInfoDetail();
        if (fileInfo != null) {
            fileInfoDetail.setAlbumId(fileInfo.getAlbumId());
            fileInfoDetail.setShareId(fileInfo.getShareId());
            fileInfoDetail.setHash(fileInfo.getHash());
        }
        return fileInfoDetail;
    }
}
