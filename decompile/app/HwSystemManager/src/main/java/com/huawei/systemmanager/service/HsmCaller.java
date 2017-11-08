package com.huawei.systemmanager.service;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.harassmentinterception.numbermark.NumberMarkCaller;
import com.huawei.harassmentinterception.service.HarassGetIntellState;
import com.huawei.harassmentinterception.service.HarassSetIntellState;
import com.huawei.permissionmanager.utils.PermissionInfoCaller;
import com.huawei.systemmanager.adblock.background.NotifyAdUpdateCaller;
import com.huawei.systemmanager.antivirus.cache.CheckIsVirusApkCaller;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.optimize.AntiMalCaller;
import com.huawei.systemmanager.optimize.FressListPowerGenieCaller;
import com.huawei.systemmanager.optimize.MemoryUsedCaller;
import com.huawei.systemmanager.optimize.UnifiedPowerAppsCaller;
import com.huawei.systemmanager.securitythreats.background.CheckUninstallApkCaller;
import com.huawei.systemmanager.securitythreats.background.NotifyInstallVirusCaller;
import com.huawei.systemmanager.startupmgr.service.GetAppAutoStartupStateCaller;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;

public class HsmCaller {
    private static final String TAG = "HsmCaller";
    private static final HsmCaller mInstance = new HsmCaller();
    private Map<String, CustomCaller> callers = HsmCollections.newArrayMap();

    private HsmCaller() {
        List<CustomCaller> callerList = Lists.newArrayList();
        callerList.add(new MemoryUsedCaller());
        callerList.add(new UnifiedPowerAppsCaller());
        callerList.add(new FressListPowerGenieCaller());
        callerList.add(new PermissionInfoCaller());
        callerList.add(new CheckUninstallApkCaller());
        callerList.add(new NotifyInstallVirusCaller());
        callerList.add(new NumberMarkCaller());
        callerList.add(new HarassGetIntellState());
        callerList.add(new HarassSetIntellState());
        callerList.add(new NotifyAdUpdateCaller());
        callerList.add(new CheckIsVirusApkCaller());
        callerList.add(new AntiMalCaller());
        callerList.add(new GetAppAutoStartupStateCaller());
        for (CustomCaller caller : callerList) {
            if (TextUtils.isEmpty(caller.getMethodName())) {
                throw new RuntimeException(this + "methodName is empty");
            } else if (((CustomCaller) this.callers.put(caller.getMethodName(), caller)) != null) {
                throw new RuntimeException(this + "caller name dupicated!! name:" + caller.getMethodName());
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

    public static Bundle call(Context ctx, String method, Bundle params) {
        return mInstance.callMethod(ctx, method, params);
    }
}
