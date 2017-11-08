package com.android.contacts.hap.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.telephony.MSimSmsManager;
import android.telephony.MSimTelephonyManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.HwLog;
import java.util.ArrayList;

public class MessageUtils {
    private static final String TAG = MessageUtils.class.getSimpleName();

    public enum Operator {
        CM,
        CU,
        CT,
        UNKNOW
    }

    public static void sendMultipartTextMessage(String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents, int subscription) {
        if (subscription == -1) {
            try {
                SmsManager.getDefault().sendMultipartTextMessage(destinationAddress, scAddress, parts, sentIntents, deliveryIntents);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        MSimSmsManager.getDefault().sendMultipartTextMessage(destinationAddress, scAddress, parts, sentIntents, deliveryIntents, subscription);
    }

    private static boolean isNetWorkVailableForDulSim(int sim_slot) {
        String simOperatorName = MSimTelephonyManager.getDefault().getNetworkOperatorName(sim_slot);
        if (simOperatorName == null || simOperatorName.equals("")) {
            return false;
        }
        return true;
    }

    private static boolean isNetWorkNotAvailable(TelephonyManager tm) {
        if (tm == null) {
            HwLog.w(TAG, "TelephonyManager is null excepiton");
            return false;
        }
        String simOperatorName = tm.getNetworkOperator();
        if (simOperatorName == null || simOperatorName.equals("")) {
            return false;
        }
        return true;
    }

    public static boolean isCMOperator(int soltId) {
        if (covertToOperator(MSimTelephonyManager.getDefault().getSimOperator(soltId)).equals(Operator.CM)) {
            return true;
        }
        return false;
    }

    public static int[] getSlotIdOfOperator(Context context, Operator op) {
        if (Operator.UNKNOW.equals(op)) {
            return new int[0];
        }
        if (SimFactoryManager.isDualSim()) {
            boolean isFirstSimEnabled = isNetWorkVailableForDulSim(0);
            boolean isSecondSimEnabled = isNetWorkVailableForDulSim(1);
            boolean isFirtSlotMatch = false;
            boolean isSecondSlotMatch = false;
            if (isFirstSimEnabled && covertToOperator(MSimTelephonyManager.getDefault().getSimOperator(0)).equals(op)) {
                isFirtSlotMatch = true;
            }
            if (isSecondSimEnabled && covertToOperator(MSimTelephonyManager.getDefault().getSimOperator(1)).equals(op)) {
                isSecondSlotMatch = true;
            }
            if (isFirtSlotMatch && isSecondSlotMatch) {
                return new int[]{0, 1};
            }
            if (isFirtSlotMatch) {
                return new int[]{0};
            } else if (!isSecondSlotMatch) {
                return new int[0];
            } else {
                return new int[]{1};
            }
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        boolean isSimEnabled = isNetWorkNotAvailable(tm);
        if (!covertToOperator(tm.getSimOperator()).equals(op) || !isSimEnabled) {
            return new int[0];
        }
        return new int[]{-1};
    }

    public static int[] getSlotIdOfOperator(Context context, String operatorCode) {
        return getSlotIdOfOperator(context, covertPhonenumberToOperator(operatorCode));
    }

    public static Operator covertToOperator(String operatorCode) {
        if ("46000".equals(operatorCode) || "46007".equals(operatorCode) || "46002".equals(operatorCode)) {
            return Operator.CM;
        }
        if ("46001".equals(operatorCode) || "46006".equals(operatorCode) || "46009".equals(operatorCode)) {
            return Operator.CU;
        }
        if ("46003".equals(operatorCode) || "46005".equals(operatorCode)) {
            return Operator.CT;
        }
        return Operator.UNKNOW;
    }

    private static Operator covertPhonenumberToOperator(String number) {
        if ("10010".equals(number)) {
            return Operator.CU;
        }
        if ("10086".equals(number)) {
            return Operator.CM;
        }
        return Operator.UNKNOW;
    }
}
