package com.huawei.systemmanager.optimize;

import android.os.Bundle;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.power.provider.SmartProvider;
import com.huawei.systemmanager.power.provider.SmartProviderHelper;
import com.huawei.systemmanager.service.CustomCaller;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Map;

public class FressListPowerGenieCaller extends CustomCaller {
    private static String CALL_METHOD_GET_FREE_LIST_POWERGENIE = "hsm_get_freeze_list";
    private static String GET_FREE_PROTECT_LIST_KEY = "freeze_list_type";
    private static final String TAG = "FressListPowerGenieCaller";

    public String getMethodName() {
        return CALL_METHOD_GET_FREE_LIST_POWERGENIE;
    }

    public Bundle call(Bundle params) {
        if (params == null) {
            HwLog.w(TAG, "params is null");
            return Bundle.EMPTY;
        }
        String type = params.getString(GET_FREE_PROTECT_LIST_KEY);
        HwLog.i(TAG, "type= " + type);
        Map<String, ArrayList<String>> res = SmartProviderHelper.getProtectAppFromDbForPowerGenie(GlobalContext.getContext(), type, null);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(SmartProvider.CALL_METHOD_PROTECT_KEY, (ArrayList) res.get(SmartProvider.CALL_METHOD_PROTECT_KEY));
        bundle.putStringArrayList(SmartProvider.CALL_METHOD_UNPROTECT_KEY, (ArrayList) res.get(SmartProvider.CALL_METHOD_UNPROTECT_KEY));
        return bundle;
    }
}
