package com.huawei.systemmanager.preventmode;

import android.os.ServiceManager;
import com.huawei.cspcommon.MLog;
import com.huawei.systemmanager.preventmode.IHoldPreventService.Stub;

public class HwPreventModeHelper {
    public static boolean isBlackListNumInPreventMode(String number) {
        boolean isBlackNum = false;
        try {
            IHoldPreventService stud = Stub.asInterface(ServiceManager.getService("com.huawei.systemmanager.preventmode.PreventModeService"));
            if (stud != null) {
                isBlackNum = stud.isPrevent(number, false);
            }
        } catch (Exception e) {
            MLog.e("HwPreventModeHelper", "calling the method isPrevent exception!");
        }
        return isBlackNum;
    }
}
