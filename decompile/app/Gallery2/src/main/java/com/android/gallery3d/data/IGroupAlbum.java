package com.android.gallery3d.data;

import com.huawei.gallery.data.AbsGroupData;
import java.util.ArrayList;
import java.util.List;

public interface IGroupAlbum {
    int getBatchSize();

    ArrayList<AbsGroupData> getGroupData();

    List<MediaItem> getMediaItem(int i, int i2, AbsGroupData absGroupData);
}
