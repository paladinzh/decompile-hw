package com.huawei.systemmanager.rainbow.comm.meta.business;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.rainbow.comm.meta.AbsBusiness;
import com.huawei.systemmanager.rainbow.comm.meta.AbsConfigItem;
import com.huawei.systemmanager.rainbow.comm.meta.CloudMetaMgr;
import java.util.List;

public class NotificationBusiness extends AbsBusiness {
    public int getBusinessId() {
        return 2;
    }

    public List<AbsConfigItem> getConfigItemList() {
        List<AbsConfigItem> result = Lists.newArrayList();
        result.add(CloudMetaMgr.getItemInstance(3));
        return result;
    }
}
