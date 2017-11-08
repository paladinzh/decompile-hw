package com.huawei.systemmanager.rainbow.db.recommend;

import android.os.Bundle;
import com.google.android.collect.Maps;
import com.google.common.base.Preconditions;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RecommendQueryOutput {
    private static final String TAG = RecommendQueryOutput.class.getSimpleName();

    public static Map<String, List<RecommendItem>> fromBundle(Bundle bundle) {
        Preconditions.checkNotNull(bundle, "fromBundle: invalid input 'bundle'");
        bundle.setClassLoader(RecommendItem.class.getClassLoader());
        Map<String, List<RecommendItem>> outPut = Maps.newHashMap();
        for (String pkgName : bundle.keySet()) {
            List<RecommendItem> recommendItems = bundle.getParcelableArrayList(pkgName);
            if (recommendItems != null) {
                outPut.put(pkgName, recommendItems);
            }
        }
        return outPut;
    }

    public static Bundle toBundle(Map<String, List<RecommendItem>> queryResult) {
        Preconditions.checkNotNull(queryResult, "toBundle invalid input 'queryResult'");
        Bundle bundle = new Bundle();
        for (Entry<String, List<RecommendItem>> entry : queryResult.entrySet()) {
            bundle.putParcelableList((String) entry.getKey(), (List) entry.getValue());
        }
        HwLog.v(TAG, "toBundle result: " + bundle);
        return bundle;
    }
}
