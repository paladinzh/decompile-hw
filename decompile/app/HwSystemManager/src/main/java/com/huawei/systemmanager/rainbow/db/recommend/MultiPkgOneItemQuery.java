package com.huawei.systemmanager.rainbow.db.recommend;

import android.os.Bundle;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class MultiPkgOneItemQuery extends AbsRecommendQuery {
    private static final String TAG = MultiPkgOneItemQuery.class.getSimpleName();

    protected List<Integer> getItemIdList(Bundle bundle) {
        try {
            return Lists.newArrayList(Integer.valueOf(RecommendQueryInput.extractItemIdFromBundle(1, bundle)));
        } catch (RecommendParamException ex) {
            HwLog.e(TAG, "getItemIdList catch RecommendParamException: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } catch (Exception ex2) {
            HwLog.e(TAG, "getItemIdList catch Exception: " + ex2.getMessage());
            ex2.printStackTrace();
            return null;
        }
    }

    protected List<String> getRangeOfPackages(Bundle bundle) {
        try {
            return RecommendQueryInput.extractPkgListFromBundle(1, bundle);
        } catch (RecommendParamException ex) {
            HwLog.e(TAG, "getRangeOfPackages catch RecommendParamException: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } catch (Exception ex2) {
            HwLog.e(TAG, "getRangeOfPackages catch Exception: " + ex2.getMessage());
            ex2.printStackTrace();
            return null;
        }
    }
}
