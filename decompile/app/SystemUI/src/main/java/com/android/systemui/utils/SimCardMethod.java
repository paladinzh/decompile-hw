package com.android.systemui.utils;

import android.content.Context;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;

public class SimCardMethod {
    public static final String TAG = SimCardMethod.class.getSimpleName();

    public static boolean hasIccCard(TelephonyManager telePhonyManager, Context context) {
        if (telePhonyManager == null) {
            telePhonyManager = TelephonyManager.from(context);
        }
        if (SystemUiUtil.isMulityCard(context) && (isCardPresent(telePhonyManager, 0) || isCardPresent(telePhonyManager, 1))) {
            return true;
        }
        return telePhonyManager.hasIccCard();
    }

    public static boolean isCardPresent(TelephonyManager telePhonyManager, int slot) {
        int slotState = telePhonyManager.getSimState(slot);
        if (slotState == 2 || slotState == 3 || slotState == 4 || slotState == 5) {
            return true;
        }
        return false;
    }

    public static boolean isCardAbsent(TelephonyManager telePhonyManager, int slot) {
        int slotState = telePhonyManager.getSimState(slot);
        HwLog.i(TAG, "isCardAbsent slot:" + slot + " slotState:" + slotState);
        if (slotState == 1) {
            return true;
        }
        return false;
    }

    public static boolean isCardInactive(int subId) {
        return HwTelephonyManager.getDefault().getSubState((long) subId) == 0;
    }
}
