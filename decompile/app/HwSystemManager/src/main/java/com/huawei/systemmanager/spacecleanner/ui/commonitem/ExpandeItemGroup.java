package com.huawei.systemmanager.spacecleanner.ui.commonitem;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ExpandeItemGroup extends TrashItemGroup {
    private List<ITrashItem> mItemList = Collections.emptyList();

    public ExpandeItemGroup(int trashType, String title, List<ITrashItem> trashItems) {
        this.mTrashType = trashType;
        this.mTitle = title;
        if (trashItems != null) {
            this.mItemList = trashItems;
        }
    }

    public boolean isChecked() {
        boolean result = true;
        for (ITrashItem item : this.mItemList) {
            if (result) {
                result = item.isChecked();
                continue;
            } else {
                result = false;
                continue;
            }
            if (!result) {
                break;
            }
        }
        return result;
    }

    public void toggle() {
        setChecked(!isChecked());
    }

    public void setChecked(boolean checked) {
        for (ITrashItem item : this.mItemList) {
            item.setChecked(checked);
        }
    }

    public boolean isCheckable() {
        return false;
    }

    public int getSize() {
        return this.mItemList.size();
    }

    public long getTrashSize() {
        long size = 0;
        for (ITrashItem item : this.mItemList) {
            size += item.getTrashSize();
        }
        return size;
    }

    public int getTrashCount() {
        int count = 0;
        for (ITrashItem item : this.mItemList) {
            count += item.getTrashCount();
        }
        return count;
    }

    public List<Trash> getUncleanedCheckedTrash() {
        List<Trash> result = Lists.newArrayList();
        for (ITrashItem item : this.mItemList) {
            if (item.isChecked() && (item instanceof CommonTrashItem)) {
                Trash trash = ((CommonTrashItem) item).getTrash();
                if (!trash.isCleaned()) {
                    result.add(trash);
                }
            }
        }
        return result;
    }

    public List<ITrashItem> getTrashs() {
        return this.mItemList;
    }

    public long getTrashSizeCleaned(boolean cleaned) {
        long size = 0;
        for (ITrashItem item : this.mItemList) {
            size += item.getTrashSizeCleaned(cleaned);
        }
        return size;
    }

    public boolean isNoTrash() {
        for (ITrashItem item : this.mItemList) {
            if (!item.isNoTrash()) {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty() {
        return this.mItemList.isEmpty();
    }

    public ITrashItem getItem(int index) {
        return (ITrashItem) this.mItemList.get(index);
    }

    public void refreshContent() {
        if (!isCleaned()) {
            boolean cleaned = true;
            for (ITrashItem item : this.mItemList) {
                item.refreshContent();
                if (!item.isCleaned()) {
                    cleaned = false;
                }
            }
            if (cleaned) {
                setCleaned();
            }
        }
    }

    public Iterator<ITrashItem> iterator() {
        return this.mItemList.iterator();
    }

    public static ExpandeItemGroup create(int type, String title, List<ITrashItem> list) {
        return new ExpandeItemGroup(type, title, list);
    }
}
