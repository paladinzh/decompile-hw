package com.huawei.systemmanager.adblock.ui.model;

import com.huawei.systemmanager.adblock.comm.AdBlock;
import com.huawei.systemmanager.adblock.ui.model.AdModelImpl.IDataListener;
import java.util.List;

public interface IAdModel {
    void cancelLoad();

    void enableAdBlock(boolean z, List<AdBlock> list);

    void loadAdBlocks(IDataListener iDataListener);
}
