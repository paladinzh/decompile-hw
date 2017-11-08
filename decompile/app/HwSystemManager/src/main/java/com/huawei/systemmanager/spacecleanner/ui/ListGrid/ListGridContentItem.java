package com.huawei.systemmanager.spacecleanner.ui.ListGrid;

import com.huawei.systemmanager.spacecleanner.ui.ListGrid.ListGridItem.SimpleListGridItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import java.util.LinkedList;

public class ListGridContentItem extends SimpleListGridItem {
    private LinkedList<ITrashItem> list = new LinkedList();

    public int getType() {
        return 1;
    }

    public ITrashItem getTrashItem(int index) {
        if (index >= this.list.size()) {
            return null;
        }
        return (ITrashItem) this.list.get(index);
    }

    public void addTrashItem(ITrashItem index) {
        this.list.add(index);
    }

    public int getCount() {
        return this.list.size();
    }

    public void setChecked(boolean checked) {
        for (ITrashItem item : this.list) {
            item.setChecked(checked);
        }
    }
}
