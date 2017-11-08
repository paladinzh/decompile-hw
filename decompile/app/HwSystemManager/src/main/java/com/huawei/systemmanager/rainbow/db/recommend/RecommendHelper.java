package com.huawei.systemmanager.rainbow.db.recommend;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.database.gfeature.AbsFeatureView;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderUtils;
import com.huawei.systemmanager.rainbow.comm.meta.AbsBusiness;
import com.huawei.systemmanager.rainbow.comm.meta.CloudMetaMgr;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class RecommendHelper {
    private static final String TAG = RecommendHelper.class.getSimpleName();

    private static class BusinessToViewFunction implements Function<AbsBusiness, AbsFeatureView> {
        private BusinessToViewFunction() {
        }

        public AbsFeatureView apply(AbsBusiness input) {
            return new RecommendTemplateView(input);
        }
    }

    public static List<AbsFeatureView> getRecommendFeatureViews() {
        List<AbsFeatureView> result = Lists.newArrayList(Collections2.transform(CloudMetaMgr.getAllValidBusiness(), new BusinessToViewFunction()));
        result.add(new RecommendPkgVerView());
        return result;
    }

    public static void bulkInsertRecommendData(Context ctx, List<ContentValues> values) {
        try {
            ctx.getContentResolver().bulkInsert(CloudProviderUtils.generateGFeatureUri(RecommendConst.RECOMMEND_FEATURE_REAL_TABLE_PREFIX), (ContentValues[]) values.toArray(new ContentValues[values.size()]));
        } catch (SQLiteException ex) {
            HwLog.e(TAG, "bulkInsertRecommendData catch SQLiteException: " + ex.getMessage());
        } catch (Exception ex2) {
            HwLog.e(TAG, "bulkInsertRecommendData catch Exception: " + ex2.getMessage());
        }
    }
}
