package com.huawei.systemmanager.rainbow.db.recommend;

import android.os.Bundle;
import com.huawei.systemmanager.rainbow.db.CloudDBHelper;

public class RecommendCallAssist {
    public static Bundle callQuery(CloudDBHelper dbHelper, String arg, Bundle extras) {
        AbsRecommendQuery query = AbsRecommendQuery.newInstance(RecommendQueryInput.extractQueryType(extras));
        if (query != null) {
            return query.queryRecommendData(dbHelper, extras);
        }
        return null;
    }
}
