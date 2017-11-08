package com.huawei.systemmanager.rainbow.comm.meta.business;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.rainbow.comm.meta.AbsBusiness;
import com.huawei.systemmanager.rainbow.comm.meta.AbsConfigItem;
import com.huawei.systemmanager.rainbow.comm.meta.CloudMetaMgr;
import java.util.List;

public class PermissionBusiness extends AbsBusiness {
    public int getBusinessId() {
        return 6;
    }

    public List<AbsConfigItem> getConfigItemList() {
        List<AbsConfigItem> result = Lists.newArrayList();
        for (int id = 11; id <= 38; id++) {
            AbsConfigItem item = CloudMetaMgr.getItemInstance(id);
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }
}
