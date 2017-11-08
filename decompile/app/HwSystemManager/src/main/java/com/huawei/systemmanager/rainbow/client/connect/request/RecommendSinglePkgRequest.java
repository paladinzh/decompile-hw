package com.huawei.systemmanager.rainbow.client.connect.request;

import android.content.Context;
import com.google.android.collect.Maps;
import java.util.Map;

public class RecommendSinglePkgRequest extends AbsRecommendRequest {
    private String mPkgName;

    public RecommendSinglePkgRequest(String pkgName) {
        this.mPkgName = pkgName;
    }

    protected Map<String, String> getRequestPkgVerMap(Context ctx) {
        Map<String, String> result = Maps.newHashMap();
        result.put(this.mPkgName, "0");
        return result;
    }
}
