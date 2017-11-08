package com.huawei.hwsystemmanager;

import android.content.Context;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;

public class HwCustSystemManagerUtils {
    public static final String HW_SHOW_4_5G_FOR_MCC = "hw_show_4_5G_for_mcc";
    public static final String HW_SHOW_LTE = "hw_show_lte";
    private static final String LOG_TAG = "HwCustSystemManagerUtils";

    public static boolean isMccChange4G(String configEntry, Context context, int subId) {
        String mccmnc;
        if (subId == -1) {
            mccmnc = TelephonyManager.from(context).getSimOperator();
        } else {
            mccmnc = TelephonyManager.from(context).getSimOperator(subId);
        }
        return parseConfig(context, configEntry, mccmnc);
    }

    public static boolean isMccChange4G(String configEntry, Context context) {
        int mSwitchDualCardSlot = 0;
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            try {
                mSwitchDualCardSlot = TelephonyManagerEx.getDefault4GSlotId();
            } catch (NoExtAPIException e) {
                Log.v(LOG_TAG, "TelephonyManagerEx.getDefault4GSlotId()->NoExtAPIException!");
            }
        }
        return parseConfig(context, configEntry, TelephonyManager.from(context).getSimOperatorNumericForPhone(mSwitchDualCardSlot));
    }

    private static boolean parseConfig(Context context, String configEntry, String mccmnc) {
        if (context == null) {
            return false;
        }
        String configString = System.getString(context.getContentResolver(), configEntry);
        if (TextUtils.isEmpty(configString)) {
            return false;
        }
        if ("ALL".equals(configString)) {
            return true;
        }
        boolean result = false;
        if (!TextUtils.isEmpty(mccmnc)) {
            String[] custValues = configString.trim().split(SqlMarker.SQL_END);
            int size = custValues.length;
            int i = 0;
            while (i < size) {
                if (mccmnc.startsWith(custValues[i]) || mccmnc.equalsIgnoreCase(custValues[i])) {
                    result = true;
                    break;
                }
                i++;
            }
        }
        return result;
    }
}
