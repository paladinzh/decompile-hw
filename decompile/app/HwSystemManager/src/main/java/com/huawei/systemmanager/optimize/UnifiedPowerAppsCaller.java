package com.huawei.systemmanager.optimize;

import android.content.ContentValues;
import android.os.Bundle;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.model.UnifiedPowerAppControl;
import com.huawei.systemmanager.power.provider.SmartProvider;
import com.huawei.systemmanager.power.provider.SmartProviderHelper;
import com.huawei.systemmanager.service.CustomCaller;
import com.huawei.systemmanager.util.HwLog;

public class UnifiedPowerAppsCaller extends CustomCaller {
    private static String CALL_METHOD_MODIFY_UNIFIEDPOWERAPPS = "hsm_modify_unifiedpowerapps";
    private static final String TAG = "UnifiedPowerAppsCaller";

    public String getMethodName() {
        return CALL_METHOD_MODIFY_UNIFIEDPOWERAPPS;
    }

    public Bundle call(Bundle params) {
        if (params == null) {
            HwLog.w(TAG, "params is null");
            return null;
        }
        String pkgname = params.getString("pkg_name");
        int flag = params.getInt(ApplicationConstant.UNIFIED_POWER_APP_CHECK);
        HwLog.i(TAG, "pkgname=" + pkgname + " flag =" + flag + " method =" + CALL_METHOD_MODIFY_UNIFIEDPOWERAPPS);
        ContentValues values = new ContentValues();
        String[] selectionArgs = new String[]{pkgname};
        String selection = "pkg_name=?";
        if (1 == flag) {
            values.put(ApplicationConstant.UNIFIED_POWER_APP_CHECK, Integer.valueOf(1));
        } else {
            values.put(ApplicationConstant.UNIFIED_POWER_APP_CHECK, Integer.valueOf(0));
        }
        GlobalContext.getContext().getContentResolver().update(SmartProvider.UNIFIED_POWER_APP_RUI, values, selection, selectionArgs);
        values.clear();
        if (1 == flag) {
            UnifiedPowerAppControl.getInstance(GlobalContext.getContext()).addAppToFWKForDOZEAndAppStandby(pkgname);
        } else {
            UnifiedPowerAppControl.getInstance(GlobalContext.getContext()).removeAppToFWKForDOZEAndAppStandby(pkgname);
        }
        if (SmartProviderHelper.getUnifiedPowerAppChangedStatusByPKG(GlobalContext.getContext(), pkgname) == 0) {
            SmartProviderHelper.updateUnifiedPowerAppForChangedColumns(pkgname, true, GlobalContext.getContext());
            HwLog.i(TAG, "modify pkg change status,pkgname=" + pkgname);
        } else {
            HwLog.i(TAG, "pkgname=" + pkgname + " is changed ago");
        }
        return null;
    }
}
