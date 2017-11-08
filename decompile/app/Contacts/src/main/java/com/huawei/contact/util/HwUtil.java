package com.huawei.contact.util;

import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.contacts.util.HwLog;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;

public class HwUtil {
    public static boolean isOffhook(String packageName) {
        try {
            ITelephony phone = Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) {
                return phone.isOffhook(packageName);
            }
        } catch (RemoteException e) {
            HwLog.w("HwUtil", "phone.isOffhook() failed", e);
        }
        return false;
    }

    public static boolean isIdle(String packageName) {
        try {
            ITelephony phone = Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) {
                return phone.isIdle(packageName);
            }
        } catch (RemoteException e) {
            HwLog.w("HwUtil", "phone.isIdle() failed", e);
        }
        return true;
    }
}
