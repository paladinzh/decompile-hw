package com.android.gallery3d.data;

import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.ShareInfo;
import java.util.ArrayList;
import java.util.List;

public class PhotoShareLocalAlbumInfo extends PhotoShareAlbumInfo {
    private String mAlbumId;
    private String mAlbumName;

    public PhotoShareLocalAlbumInfo(String albumId, String albumName) {
        this.mAlbumId = albumId;
        this.mAlbumName = albumName;
    }

    public String getName() {
        return this.mAlbumName;
    }

    public String getId() {
        return this.mAlbumId;
    }

    public boolean delete() {
        return false;
    }

    public int addFileToAlbum(String[] fileNames) {
        return 1;
    }

    public int modifyName(String name) {
        return 1;
    }

    public String getOwnerAcc() {
        return null;
    }

    public int getItemCount(int fileType) {
        return 0;
    }

    public List<FileInfo> getMediaItems(int fileType, int start, int count) {
        return new ArrayList();
    }

    public int getReceiverCount() {
        return 0;
    }

    public int getPreItemCount(int fileType) {
        return 0;
    }

    public List<FileInfo> getPreMediaItems(int fileType, int start, int count) {
        return null;
    }

    public long getCloudSize() {
        return 0;
    }

    public ShareInfo getShareInfo() {
        return null;
    }
}
