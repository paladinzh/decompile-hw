package com.huawei.systemmanager.spacecleanner.ui.photomanager;

import java.util.List;

public interface onCallPhotoTrashSetListener {
    List<PhotoFolder> getPhotoFolders();

    void startGridSetFragment(String str);
}
