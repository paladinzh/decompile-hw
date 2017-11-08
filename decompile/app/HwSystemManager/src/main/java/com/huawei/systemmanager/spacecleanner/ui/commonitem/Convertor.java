package com.huawei.systemmanager.spacecleanner.ui.commonitem;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashConst;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.CommonTrashItem.TrashTransFunc;
import java.util.List;
import java.util.Map;

public abstract class Convertor {
    public static List<Trash> covertTrashItem(List<ITrashItem> items) {
        if (HsmCollections.isEmpty(items)) {
            return Lists.newArrayList();
        }
        List<Trash> res = Lists.newArrayListWithCapacity(items.size());
        for (ITrashItem item : items) {
            if (item instanceof CommonTrashItem) {
                Trash trash = ((CommonTrashItem) item).getTrash();
                if (trash != null) {
                    res.add(trash);
                }
            }
        }
        return res;
    }

    public static final TrashItemGroup transToExpandeGroup(Map<Integer, TrashGroup> trashMap, TrashTransFunc<? extends ITrashItem> transFunc) {
        int type = transFunc.getTrashType();
        String title = TrashConst.getTypeTitle(type);
        TrashGroup trashGroup = null;
        if (trashMap != null) {
            trashGroup = (TrashGroup) trashMap.get(Integer.valueOf(type));
        }
        List<Trash> list = Lists.newArrayList();
        if (trashGroup != null) {
            list = trashGroup.getTrashList();
        }
        List<ITrashItem> itemList = Lists.newArrayListWithCapacity(list.size());
        for (Trash trash : list) {
            ITrashItem item = transFunc.apply(trash);
            if (item != null) {
                item.setChecked(trash.isSuggestClean());
                itemList.add(item);
            }
        }
        return ExpandeItemGroup.create(type, title, itemList);
    }

    public static final TrashItemGroup transToExpandeGroup(List<Trash> list, String title, int type, TrashTransFunc<? extends ITrashItem> transFunc) {
        List<ITrashItem> itemList = Lists.newArrayListWithCapacity(list.size());
        for (Trash trash : list) {
            ITrashItem item = transFunc.apply(trash);
            if (item != null) {
                item.setChecked(trash.isSuggestClean());
                itemList.add(item);
            }
        }
        return ExpandeItemGroup.create(type, title, itemList);
    }

    public List<TrashItemGroup> convert(TrashScanHandler scanHandler) {
        return Lists.newArrayList();
    }
}
