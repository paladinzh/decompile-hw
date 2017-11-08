package com.huawei.permission.binderhandler;

import android.content.Context;
import android.os.Bundle;

public abstract class HoldServiceBinderHandler {
    public abstract Bundle handleTransact(Bundle bundle);

    public Bundle handleWithCheckPermission(Context context, Bundle params) {
        if (!ignorePermissionCheck()) {
            context.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        }
        return handleTransact(params);
    }

    protected boolean ignorePermissionCheck() {
        return false;
    }
}
