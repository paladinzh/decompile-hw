package com.huawei.systemmanager.rainbow.db.recommend;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.javax.annotation.Nonnull;
import com.huawei.systemmanager.comm.database.gfeature.AbsFeatureView;
import com.huawei.systemmanager.comm.database.gfeature.FeatureToColumn;
import com.huawei.systemmanager.rainbow.comm.meta.AbsBusiness;
import com.huawei.systemmanager.rainbow.comm.meta.AbsConfigItem;
import java.util.List;

public class RecommendTemplateView extends AbsFeatureView {
    private AbsBusiness mBusiness = null;

    private static class ConfigItemToColumnFunc implements Function<AbsConfigItem, FeatureToColumn> {
        private ConfigItemToColumnFunc() {
        }

        public FeatureToColumn apply(@Nonnull AbsConfigItem input) {
            return new FeatureToColumn(input.getColumnlName(), input.getShortFeatureName());
        }
    }

    public RecommendTemplateView(AbsBusiness business) {
        this.mBusiness = business;
    }

    public String getTempViewPrefix() {
        return RecommendConst.RECOMMEND_TMP_PREFIX + this.mBusiness.getBusinessName();
    }

    public String getLinkedRealTablePrefix() {
        return RecommendConst.RECOMMEND_FEATURE_REAL_TABLE_PREFIX;
    }

    public String getQueryViewName() {
        return this.mBusiness.getRecommendViewName();
    }

    public List<FeatureToColumn> getViewColumnFeatureList() {
        return Lists.transform(this.mBusiness.getConfigItemList(), new ConfigItemToColumnFunc());
    }
}
