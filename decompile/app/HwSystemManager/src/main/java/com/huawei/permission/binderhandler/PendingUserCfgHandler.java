package com.huawei.permission.binderhandler;

import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.permission.HoldServiceConst;
import com.huawei.permission.PendingUserCfgCache;

public class PendingUserCfgHandler extends HoldServiceBinderHandler {
    private PendingUserCfgCache mCache;

    public PendingUserCfgHandler(PendingUserCfgCache cache) {
        this.mCache = cache;
    }

    public Bundle handleTransact(Bundle params) {
        int code = params.getInt(HoldServiceConst.EXTRA_CODE);
        if (1 != code && 2 != code) {
            return null;
        }
        int uid = params.getInt("uid");
        if (uid == 0) {
            return null;
        }
        int type = params.getInt("permissionType");
        if (type == 0) {
            return null;
        }
        String pkg = params.getString("packageName");
        if (TextUtils.isEmpty(pkg)) {
            return null;
        }
        int operationType = params.getInt(HoldServiceConst.EXTRA_MODE);
        if ((1 != operationType && 2 != operationType) || this.mCache == null) {
            return null;
        }
        if (1 == code) {
            this.mCache.addPendingCfg(uid, pkg, type, operationType);
        } else {
            this.mCache.removePendingCfg(uid, pkg, type, operationType);
        }
        return null;
    }
}
