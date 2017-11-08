package com.huawei.systemmanager.customize;

import android.content.Context;
import com.huawei.systemmanager.comm.misc.GlobalContext;

public class AbroadUtils {
    public static final int REGION_TYPE_ABROAD = 1;
    public static final int REGION_TYPE_NORMAL = 0;

    @Deprecated
    public static boolean isAbroad() {
        return isAbroad(GlobalContext.getContext());
    }

    public static boolean isAbroad(Context ctx) {
        return CustomizeManager.getInstance().getFeatureIntConfig(ctx, "region_type", 6, 1) != 0;
    }
}
