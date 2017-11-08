package com.huawei.systemmanager.rainbow.comm.meta.business;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.rainbow.comm.meta.AbsBusiness;
import com.huawei.systemmanager.rainbow.comm.meta.AbsConfigItem;
import com.huawei.systemmanager.rainbow.comm.meta.CloudMetaMgr;
import java.util.List;

public class NetworkBusiness extends AbsBusiness {
    public int getBusinessId() {
        return 1;
    }

    public List<AbsConfigItem> getConfigItemList() {
        List<AbsConfigItem> result = Lists.newArrayList();
        result.add(CloudMetaMgr.getItemInstance(1));
        result.add(CloudMetaMgr.getItemInstance(2));
        return result;
    }
}
