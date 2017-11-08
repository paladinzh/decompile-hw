package com.huawei.systemmanager.comm.grule.rules.uid;

import android.os.UserHandle;

public class SystemUIDRule extends APKUidRuleBase {
    boolean uidMatch(int uid) {
        return UserHandle.getAppId(uid) < 10000;
    }
}
