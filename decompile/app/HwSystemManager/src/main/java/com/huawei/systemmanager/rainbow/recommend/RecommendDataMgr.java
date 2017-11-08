package com.huawei.systemmanager.rainbow.recommend;

import android.content.Context;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.javax.annotation.Nonnull;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.systemmanager.rainbow.recommend.base.ConfigurationItem;
import com.huawei.systemmanager.rainbow.recommend.base.DataConstructUtils;
import com.huawei.systemmanager.rainbow.recommend.dataquery.DataQueryFactory;
import com.huawei.systemmanager.rainbow.recommend.dataquery.IConfigItemQuery;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RecommendDataMgr {
    private static final String TAG = RecommendDataMgr.class.getSimpleName();

    private static class ValidConfigItem implements Predicate<ConfigurationItem> {
        private ValidConfigItem() {
        }

        public boolean apply(@Nonnull ConfigurationItem input) {
            return input.valid();
        }
    }

    public static List<String> collectRecommendBIData(Context ctx) {
        final Map<String, List<ConfigurationItem>> config = getDataMap(ctx);
        return Lists.newArrayList(Collections2.transform(config.keySet(), new Function<String, String>() {
            public String apply(String input) {
                StringBuffer buf = new StringBuffer();
                buf.append(input).append("|").append(RecommendDataMgr.configItemsToBiString((List) config.get(input)));
                return buf.toString();
            }
        }));
    }

    public static List<String> collectControlledAppList(Context ctx) {
        List<String> pkgList = Lists.newArrayList(getDataMap(ctx).keySet());
        HwLog.d(TAG, "collectControlledAppList result: " + pkgList);
        return pkgList;
    }

    private static Map<String, List<ConfigurationItem>> getDataMap(Context ctx) {
        Map<String, List<ConfigurationItem>> result = DataConstructUtils.generateEmptyResult();
        for (IConfigItemQuery impl : DataQueryFactory.getQueryImpls()) {
            mergeResult(result, impl.getConfigurationOfItems(ctx));
        }
        return result;
    }

    private static void mergeResult(Map<String, List<ConfigurationItem>> all, Map<String, List<ConfigurationItem>> part) {
        if (part == null) {
            HwLog.w(TAG, "mergeResult input part data is null, ignore this merge!");
            return;
        }
        for (Entry<String, List<ConfigurationItem>> partEntry : part.entrySet()) {
            DataConstructUtils.generateDefaultPackageItemList(all, (String) partEntry.getKey());
            ((List) all.get(partEntry.getKey())).addAll(Collections2.filter((Collection) partEntry.getValue(), new ValidConfigItem()));
        }
    }

    private static String configItemsToBiString(List<ConfigurationItem> items) {
        if (items != null && !items.isEmpty()) {
            return Joiner.on(ConstValues.SEPARATOR_KEYWORDS_EN).join(Collections2.transform(items, new Function<ConfigurationItem, String>() {
                public String apply(@Nonnull ConfigurationItem input) {
                    return input.mConfigItemId + "=" + input.mConfigType;
                }
            }));
        }
        HwLog.w(TAG, "biConfigItems list is invalid");
        return "";
    }
}
