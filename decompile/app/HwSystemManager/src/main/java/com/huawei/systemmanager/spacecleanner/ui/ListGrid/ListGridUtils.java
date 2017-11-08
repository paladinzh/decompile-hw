package com.huawei.systemmanager.spacecleanner.ui.ListGrid;

import com.huawei.systemmanager.spacecleanner.ui.ListGrid.ListGridItem.BaseListGridItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class ListGridUtils {
    private static final String TAG = "ListGridUtils";

    public static List<BaseListGridItem> createContentItem(List<ITrashItem> source) {
        List<BaseListGridItem> result = new ArrayList();
        if (source == null || source.isEmpty()) {
            HwLog.i(TAG, "createContentItem , arg is wrong");
            return result;
        }
        int i;
        int sourceSize = source.size();
        int count = sourceSize / 4;
        HwLog.i(TAG, "createContentItem , count is:  " + count);
        for (i = 0; i < count; i++) {
            ListGridContentItem gridItem = new ListGridContentItem();
            for (int j = 0; j < 4; j++) {
                gridItem.addTrashItem((ITrashItem) source.get((i * 4) + j));
            }
            result.add(gridItem);
        }
        int remainder = sourceSize % 4;
        if (remainder != 0) {
            HwLog.i(TAG, "createContentItem , remainder is:  " + remainder);
            gridItem = new ListGridContentItem();
            for (i = count * 4; i < sourceSize; i++) {
                gridItem.addTrashItem((ITrashItem) source.get(i));
            }
            result.add(gridItem);
        }
        return result;
    }
}
