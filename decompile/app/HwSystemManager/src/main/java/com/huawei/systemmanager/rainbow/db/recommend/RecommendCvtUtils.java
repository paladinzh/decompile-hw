package com.huawei.systemmanager.rainbow.db.recommend;

import android.content.ContentValues;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.database.gfeature.GFeatureCvt;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class RecommendCvtUtils {
    private static final String TAG = RecommendCvtUtils.class.getSimpleName();

    public static String cvtRecommendStoreValue(int configType, int percentage) {
        return Joiner.on("_").join(Integer.valueOf(configType), Integer.valueOf(percentage), new Object[0]);
    }

    public static int cvtStoreValueToConfigType(String value) throws RecommendParamException {
        try {
            return Integer.parseInt(value.split("_")[0]);
        } catch (Exception ex) {
            HwLog.e(TAG, "cvtStoreValueToConfigType catch exception: " + ex.getMessage());
            throw new RecommendParamException("cvtStoreValueToConfigType invalid value: " + value);
        }
    }

    public static int cvtStoreValueToPercentage(String value) throws RecommendParamException {
        try {
            return Integer.parseInt(value.split("_")[1]);
        } catch (Exception ex) {
            HwLog.e(TAG, "cvtStoreValueToPercentage catch exception: " + ex.getMessage());
            throw new RecommendParamException("cvtStoreValueToConfigType invalid value: " + value);
        }
    }

    public static List<ContentValues> cvtCloudDataToContentValues(String pkgName, List<String> recDataList, String version) {
        List<ContentValues> result = Lists.newArrayList();
        for (String oneItem : recDataList) {
            try {
                String[] cloudRecData = oneItem.split("\\|");
                ContentValues cv = GFeatureCvt.cvtToStdContentValue(pkgName, cloudRecData[0], cloudRecData[1] + "_" + cloudRecData[2]);
                if (cv != null) {
                    result.add(cv);
                }
            } catch (Exception ex) {
                HwLog.e(TAG, "cvtCloudDataToContentValues catch Exception: " + ex.getMessage());
            }
        }
        if (result.isEmpty()) {
            HwLog.w(TAG, "cvtCloudDataToContentValues empty recommend data added!");
        } else {
            result.add(GFeatureCvt.cvtToStdContentValue(pkgName, RecommendConst.RECOMMEND_FEATURE_PV_STORE_KEY, version));
        }
        return result;
    }
}
