package com.huawei.systemmanager.spacecleanner.ui.secondaryui;

import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.tencentadapter.WeChatTypeCons;
import com.huawei.systemmanager.spacecleanner.ui.ListGrid.BaseListGridAdapter;
import com.huawei.systemmanager.spacecleanner.ui.ListGrid.VideoListGridAdapter;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.WeChatDeepItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.LinkedList;
import java.util.List;

public class MediaListGridFragment extends BaseListGridFragment {
    private static final String TAG = "MediaListGridFragment";

    protected BaseListGridAdapter createAdapter(int trashType, int subType) {
        HwLog.i(TAG, "trashType is:   " + trashType + "     subType is:   " + subType);
        if (trashType == 1048576 && WeChatTypeCons.isWeChatVideo(subType)) {
            return new VideoListGridAdapter(this.mActivity, this.mSizeChangeListener);
        }
        return new BaseListGridAdapter(this.mActivity, this.mSizeChangeListener);
    }

    protected List<ITrashItem> createData(int trashType, int index, TrashScanHandler scanHandler) {
        HwLog.i(TAG, "trashType is:   " + trashType + "     index is:   " + index);
        if (trashType == 1048576) {
            return WeChatDeepItem.getListGridSource(scanHandler, index);
        }
        return new LinkedList();
    }
}
