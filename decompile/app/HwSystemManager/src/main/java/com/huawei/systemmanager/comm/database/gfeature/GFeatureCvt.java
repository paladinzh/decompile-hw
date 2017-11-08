package com.huawei.systemmanager.comm.database.gfeature;

import android.content.ContentValues;
import com.google.common.base.Strings;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class GFeatureCvt {
    private static final String TAG = GFeatureCvt.class.getSimpleName();

    public static ContentValues cvtToStdContentValue(String pkgName, String featureName, String featureValue) {
        if (Strings.isNullOrEmpty(pkgName) || Strings.isNullOrEmpty(featureName) || Strings.isNullOrEmpty(featureValue)) {
            return null;
        }
        ContentValues cv = new ContentValues();
        cv.put("packageName", pkgName);
        cv.put(GFeatureTable.COL_FEATURE_NAME, featureName);
        cv.put(GFeatureTable.COL_FEATURE_VALUE, featureValue);
        return cv;
    }

    public static boolean isStdContentValue(ContentValues cv) {
        boolean result;
        if (cv.containsKey("packageName") && cv.containsKey(GFeatureTable.COL_FEATURE_NAME)) {
            result = cv.containsKey(GFeatureTable.COL_FEATURE_VALUE);
        } else {
            result = false;
        }
        if (!result) {
            HwLog.e(TAG, "isStdContentValue false: " + cv);
        }
        return result;
    }

    public static boolean isStdContentValue(ContentValues[] cvs) {
        for (ContentValues cv : cvs) {
            if (!isStdContentValue(cv)) {
                return false;
            }
        }
        return true;
    }

    public static List<ContentValues> getDefaultContentValues(List<ContentValues> src, List<ContentValues> defs) {
        List<ContentValues> result = null;
        if (src != null && src.size() > 0 && defs != null && defs.size() > 0) {
            result = new ArrayList();
            for (ContentValues v : defs) {
                if (!isStdContentValue(v)) {
                    return null;
                }
                boolean contants = false;
                for (ContentValues va : src) {
                    if (isStdContentValue(va)) {
                        if (va.getAsString("packageName").equals(v.getAsString("packageName")) && va.getAsString(GFeatureTable.COL_FEATURE_NAME).equals(v.getAsString(GFeatureTable.COL_FEATURE_NAME))) {
                            contants = true;
                            break;
                        }
                    } else {
                        return null;
                    }
                }
                if (!contants) {
                    result.add(new ContentValues(v));
                }
            }
        }
        return result;
    }
}
