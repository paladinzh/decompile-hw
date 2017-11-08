package com.huawei.systemmanager.netassistant.netapp.control;

import android.net.HwNetworkPolicyManager;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.utils.PreferenceUtil;

public class NetAppPermissionExcutor {
    private static final String TAG = "NetAppPermissionExcutor";

    public static boolean execute(AppPermissionController... params) {
        HwNetworkPolicyManager networkPolicyManager = HwNetworkPolicyManager.from(GlobalContext.getContext());
        for (int i = 0; i < params.length; i++) {
            if (params[i].getPermission() == 0) {
                if (params[i].getType() == 0) {
                    networkPolicyManager.removeHwUidPolicy(params[i].getUid(), 1);
                } else {
                    networkPolicyManager.removeHwUidPolicy(params[i].getUid(), 2);
                }
                PreferenceUtil.clearNetAppTag(GlobalContext.getContext(), String.valueOf(params[i].getUid()));
            } else if (params[i].getPermission() == 1) {
                if (params[i].getType() == 0) {
                    networkPolicyManager.addHwUidPolicy(params[i].getUid(), 1);
                } else {
                    networkPolicyManager.addHwUidPolicy(params[i].getUid(), 2);
                }
            }
        }
        return true;
    }
}
