package com.huawei.systemmanager.spacecleanner.ui.secondaryui;

import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import java.util.List;

public interface onCallTrashSetListener {
    DataHolder getDataHolder();

    TrashScanHandler getTrashHandler();

    List<Trash> initAndGetData();

    void resetTrashSet();

    void setCleanedOperation(boolean z);
}
