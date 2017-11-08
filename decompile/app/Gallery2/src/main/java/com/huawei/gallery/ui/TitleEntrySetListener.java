package com.huawei.gallery.ui;

import com.huawei.gallery.data.AbsGroupData;

public interface TitleEntrySetListener extends EntrySetListener {
    AbsGroupData getGroupData(int i);

    Object getTitleObjectIndex(int i);
}
