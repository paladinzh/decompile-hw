package com.huawei.systemmanager.rainbow.comm.meta;

import java.util.List;

public abstract class AbsBusiness {
    public abstract int getBusinessId();

    public abstract List<AbsConfigItem> getConfigItemList();

    public String getBusinessName() {
        return CloudMetaMgr.getBusinessName(getBusinessId());
    }

    public String getRecommendViewName() {
        return "Recommend_" + CloudMetaMgr.getBusinessViewKey(getBusinessId()) + "_View";
    }
}
