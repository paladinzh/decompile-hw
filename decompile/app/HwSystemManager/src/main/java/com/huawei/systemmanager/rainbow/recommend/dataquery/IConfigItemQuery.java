package com.huawei.systemmanager.rainbow.recommend.dataquery;

import android.content.Context;
import com.huawei.systemmanager.rainbow.recommend.base.ConfigurationItem;
import java.util.List;
import java.util.Map;

public interface IConfigItemQuery {
    Map<String, List<ConfigurationItem>> getConfigurationOfItems(Context context);
}
