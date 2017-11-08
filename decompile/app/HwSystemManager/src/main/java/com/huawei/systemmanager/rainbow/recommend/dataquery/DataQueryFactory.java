package com.huawei.systemmanager.rainbow.recommend.dataquery;

import com.google.common.collect.Lists;
import java.util.List;

public class DataQueryFactory {
    public static List<IConfigItemQuery> getQueryImpls() {
        List<IConfigItemQuery> result = Lists.newArrayList();
        result.add(new NetworkQueryImpl());
        result.add(new BootStartupQueryImpl());
        result.add(new AddViewQueryImpl());
        result.add(new PermissionQueryImpl());
        return result;
    }
}
