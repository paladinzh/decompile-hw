package com.android.dialer.util;

import android.content.Context;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.hap.utils.MessageUtils;
import com.android.contacts.hap.utils.MessageUtils.Operator;
import com.android.contacts.util.HwLog;

public class TelecomUtil {
    private static boolean sWarningLogged = false;

    public static boolean hasReadWriteVoicemailPermissions(Context context) {
        if (isDefaultDialer(context)) {
            return true;
        }
        if (hasPermission(context, "com.android.voicemail.permission.READ_VOICEMAIL")) {
            return hasPermission(context, "com.android.voicemail.permission.WRITE_VOICEMAIL");
        }
        return false;
    }

    private static boolean hasPermission(Context context, String permission) {
        return context.checkSelfPermission(permission) == 0;
    }

    public static boolean isDefaultDialer(Context context) {
        boolean result = TextUtils.equals(context.getPackageName(), getTelecomManager(context).getDefaultDialerPackage());
        if (result) {
            sWarningLogged = false;
        } else if (!sWarningLogged) {
            HwLog.w("TelecomUtil", "Dialer is not currently set to be default dialer");
            sWarningLogged = true;
        }
        return result;
    }

    private static TelecomManager getTelecomManager(Context context) {
        return (TelecomManager) context.getSystemService("telecom");
    }

    public static boolean isOperatorCM(Context context) {
        if (!MultiUsersUtils.isCurrentUserOwner()) {
            return false;
        }
        int length = MessageUtils.getSlotIdOfOperator(context, Operator.CM).length;
        HwLog.i("TelecomUtil", "isOperatorCM length : " + length);
        if (length == 0) {
            return false;
        }
        return true;
    }
}
