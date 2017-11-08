package com.huawei.harassmentinterception.numbermark;

import android.os.Bundle;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.service.CustomCaller;
import com.huawei.systemmanager.util.HwLog;

public class NumberMarkCaller extends CustomCaller {
    private static final String TAG = "NumberMarkCaller";

    public String getMethodName() {
        return "reportNumberMark";
    }

    public Bundle call(Bundle params) {
        if (Utility.isOwner()) {
            return HsmNumberMarkerManager.getInstance(GlobalContext.getContext()).doReport(params);
        }
        HwLog.e(TAG, "current is not owner!");
        return null;
    }
}
