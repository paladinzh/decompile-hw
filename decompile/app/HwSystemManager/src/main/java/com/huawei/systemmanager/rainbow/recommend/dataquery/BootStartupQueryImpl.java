package com.huawei.systemmanager.rainbow.recommend.dataquery;

import android.content.Context;
import com.huawei.systemmanager.rainbow.recommend.base.ConfigurationItem;
import com.huawei.systemmanager.rainbow.recommend.base.DataConstructUtils;
import java.util.List;
import java.util.Map;

class BootStartupQueryImpl implements IConfigItemQuery {
    BootStartupQueryImpl() {
    }

    public Map<String, List<ConfigurationItem>> getConfigurationOfItems(Context ctx) {
        return DataConstructUtils.generateEmptyResult();
    }
}
