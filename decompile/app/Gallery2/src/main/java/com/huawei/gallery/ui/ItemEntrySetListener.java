package com.huawei.gallery.ui;

import com.android.gallery3d.data.MediaItem;

public interface ItemEntrySetListener extends EntrySetListener {
    Object getItemObjectIndex(int i);

    MediaItem getMediaItem(int i);

    void requestNoneActiveEntry();

    void updateSourceRange(int i, int i2);

    void updateUIRange(int i, int i2);
}
