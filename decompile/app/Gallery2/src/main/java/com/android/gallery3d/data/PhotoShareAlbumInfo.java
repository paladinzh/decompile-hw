package com.android.gallery3d.data;

import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.ShareInfo;
import java.util.List;

public abstract class PhotoShareAlbumInfo {
    public abstract int addFileToAlbum(String[] strArr);

    public abstract boolean delete();

    public abstract long getCloudSize();

    public abstract String getId();

    public abstract int getItemCount(int i);

    public abstract List<FileInfo> getMediaItems(int i, int i2, int i3);

    public abstract String getName();

    public abstract String getOwnerAcc();

    public abstract int getPreItemCount(int i);

    public abstract List<FileInfo> getPreMediaItems(int i, int i2, int i3);

    public abstract int getReceiverCount();

    public abstract ShareInfo getShareInfo();

    public abstract int modifyName(String str);
}
