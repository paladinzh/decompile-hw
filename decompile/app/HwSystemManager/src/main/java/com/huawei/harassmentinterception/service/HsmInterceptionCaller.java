package com.huawei.harassmentinterception.service;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.google.android.collect.Lists;
import com.huawei.harassmentinterception.numbermark.NumberMarkCaller;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.service.CustomCaller;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;

public class HsmInterceptionCaller {
    private static final String TAG = "HsmInterceptionCaller";
    private static final HsmInterceptionCaller mInstance = new HsmInterceptionCaller();
    private Map<String, CustomCaller> callers = HsmCollections.newArrayMap();

    public HsmInterceptionCaller() {
        List<CustomCaller> callerList = Lists.newArrayList();
        callerList.add(new NumberMarkCaller());
        for (CustomCaller caller : callerList) {
            if (TextUtils.isEmpty(caller.getMethodName())) {
                HwLog.e(TAG, new RuntimeException(this + "methodName is empty").getMessage());
            } else if (((CustomCaller) this.callers.put(caller.getMethodName(), caller)) != null) {
                HwLog.e(TAG, new RuntimeException(this + "caller name dupicated!! name:" + caller.getMethodName()).getMessage());
            }
        }
    }

    private Bundle callMethod(Context ctx, String method, Bundle params) {
        if (ctx == null) {
            HwLog.e(TAG, "callMethod ctx is null!");
            return null;
        }
        CustomCaller caller = (CustomCaller) this.callers.get(method);
        if (caller == null) {
            HwLog.e(TAG, "can not found method:" + method);
            return null;
        }
        if (caller.shouldEnforcePermission()) {
            ctx.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        }
        return caller.call(params);
    }

    public static Bundle call(Context applicationContext, String method, Bundle params) {
        return mInstance.callMethod(applicationContext, method, params);
    }
}
