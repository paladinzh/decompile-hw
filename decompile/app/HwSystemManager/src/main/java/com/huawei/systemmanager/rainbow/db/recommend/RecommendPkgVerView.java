package com.huawei.systemmanager.rainbow.db.recommend;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.database.gfeature.AbsFeatureView;
import com.huawei.systemmanager.comm.database.gfeature.FeatureToColumn;
import java.util.List;

public class RecommendPkgVerView extends AbsFeatureView {
    public static final String VIEW_NAME = "Recommend_PackageVersion_View";

    public String getTempViewPrefix() {
        return "Recommend_TMP_PV";
    }

    public String getLinkedRealTablePrefix() {
        return RecommendConst.RECOMMEND_FEATURE_REAL_TABLE_PREFIX;
    }

    public String getQueryViewName() {
        return VIEW_NAME;
    }

    public List<FeatureToColumn> getViewColumnFeatureList() {
        List<FeatureToColumn> result = Lists.newArrayList();
        result.add(new FeatureToColumn(RecommendConst.RECOMMEND_FEATURE_PV_VIEW_COL_KEY, RecommendConst.RECOMMEND_FEATURE_PV_STORE_KEY, "0"));
        return result;
    }
}
