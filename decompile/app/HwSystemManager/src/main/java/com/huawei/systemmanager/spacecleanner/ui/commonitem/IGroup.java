package com.huawei.systemmanager.spacecleanner.ui.commonitem;

import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import java.util.Iterator;
import java.util.List;

public interface IGroup extends Iterable<ITrashItem> {
    ITrashItem getItem(int i);

    int getSize();

    List<ITrashItem> getTrashs();

    List<Trash> getUncleanedCheckedTrash();

    boolean isEmpty();

    boolean isScanFinished();

    Iterator<ITrashItem> iterator();

    void setScanFinished(boolean z);
}
