package com.huawei.systemmanager.rainbow.recommend.base;

import com.google.android.collect.Maps;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;

public class DataConstructUtils {
    public static Map<String, List<ConfigurationItem>> generateEmptyResult() {
        return Maps.newHashMap();
    }

    public static void generateDefaultPackageItemList(Map<String, List<ConfigurationItem>> mapData, String pkgName) {
        if (!mapData.containsKey(pkgName)) {
            mapData.put(pkgName, Lists.newArrayList());
        }
    }
}
