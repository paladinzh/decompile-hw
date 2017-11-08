package com.huawei.mms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageUtils;
import com.huawei.cspcommon.MLog;

public class HwSIMCardChangedHelper {
    public static boolean checkSimWasReplaced(Context context, int subID) {
        int status;
        if (MessageUtils.isMultiSimEnabled()) {
            status = MessageUtils.getIccCardStatus(subID);
        } else {
            subID = -1;
            status = MessageUtils.getIccCardStatus();
        }
        if (2 == status) {
            MLog.v("HwSIMCardChangedHelper", "sim card is not insert, return false");
            return false;
        }
        String strNewSimNo = getNowSimNO(context, subID);
        String strOldSimNo = getPrevSimNO(context, subID);
        strNewSimNo = MessageUtils.encode(strNewSimNo);
        if (strOldSimNo.equals(strNewSimNo)) {
            return false;
        }
        restoreSmsCenterNumberBySubID(context, subID);
        saveSimNoInFile(context, strNewSimNo, subID);
        return true;
    }

    private static String getPrevSimNO(Context context, int subID) {
        String simSerialNumber = "";
        SharedPreferences prefs = context.getSharedPreferences("simNO", 0);
        switch (subID) {
            case -1:
                return prefs.getString("prevNO", "");
            case 0:
                return prefs.getString("prevNO_card1", "");
            case 1:
                return prefs.getString("prevNO_card2", "");
            default:
                return simSerialNumber;
        }
    }

    private static String getNowSimNO(Context context, int subID) {
        String simSerialNumber = null;
        if (!MessageUtils.isMultiSimEnabled()) {
            return ((TelephonyManager) context.getSystemService("phone")).getSubscriberId();
        }
        try {
            return MSimTelephonyManager.from(context).getSubscriberId(subID);
        } catch (Exception e) {
            MLog.e("HwSIMCardChangedHelper", "get the multi-sim serial number exception!");
            return simSerialNumber;
        }
    }

    private static void saveSimNoInFile(Context context, String strSimNo, int subID) {
        Editor editor = context.getSharedPreferences("simNO", 0).edit();
        switch (subID) {
            case -1:
                editor.putString("prevNO", strSimNo);
                break;
            case 0:
                editor.putString("prevNO_card1", strSimNo);
                break;
            case 1:
                editor.putString("prevNO_card2", strSimNo);
                break;
        }
        editor.putString("prevNO", strSimNo);
        editor.apply();
    }

    private static void restoreSmsCenterNumberBySubID(Context context, int subID) {
        String str = null;
        try {
            str = MessageUtils.getSmsAddressBySubID(subID);
        } catch (Exception e) {
            MLog.e("HwSIMCardChangedHelper", "get the sms getSmscAddr exception");
        }
        if (TextUtils.isEmpty(str) && MmsConfig.getSMSCAddress() != null) {
            str = MmsConfig.getSMSCAddress();
        }
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        switch (subID) {
            case -1:
                editor.putString("sms_center_number", str);
                break;
            case 0:
                editor.putString("pref_key_simuim1_message_center", str);
                break;
            case 1:
                editor.putString("pref_key_simuim2_message_center", str);
                break;
        }
        editor.commit();
    }

    public static void checkSimWasReplacedForSmscNumber(Context context, int subID) {
        if (!MessageUtils.isMultiSimEnabled()) {
            subID = -1;
        }
        String strNewSimNo = getNowSimNO(context, subID);
        String strOldSimNo = getPrevSimNO(context, subID);
        strNewSimNo = MessageUtils.encode(strNewSimNo);
        if (!strOldSimNo.equals(strNewSimNo)) {
            cleanSmscNumberSharedPreferences(context, subID);
            saveSimNoInFile(context, strNewSimNo, subID);
        }
    }

    private static void cleanSmscNumberSharedPreferences(Context context, int subId) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if (MessageUtils.isMultiSimEnabled()) {
            if (subId == 1) {
                editor.putString("pref_key_simuim2_message_center", null);
            } else {
                editor.putString("pref_key_simuim1_message_center", null);
            }
            editor.putString("sim_center_address_" + subId, null);
        } else {
            editor.putString("sms_center_number", null);
            editor.putString("sim_center_address_0", null);
        }
        editor.commit();
    }
}
