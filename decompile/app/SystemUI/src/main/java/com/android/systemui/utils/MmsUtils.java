package com.android.systemui.utils;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import com.android.mms.service.MmsService;
import fyusion.vislib.BuildConfig;
import java.util.Locale;

public class MmsUtils {
    private static boolean ALWAYS_SHOW_COUNTER = true;
    private static int SUPORT_MMS_7BIT = -1;

    public static final String getMmsCounterText(Context context, CharSequence csText) {
        if (TextUtils.isEmpty(csText)) {
            return BuildConfig.FLAVOR;
        }
        if (SUPORT_MMS_7BIT == -1) {
            int i;
            if (MmsService.getIs7bitEnable(context)) {
                i = 1;
            } else {
                i = 0;
            }
            SUPORT_MMS_7BIT = i;
        }
        if (SUPORT_MMS_7BIT == 1) {
            csText = MmsService.replaceAlphabetForGsm7Bit(context, csText);
        }
        int[] params = SmsMessage.calculateLength(csText, false);
        int msgCount = params[0];
        int remainingInCurrentMessage = params[2];
        String counterText = null;
        if ((!ALWAYS_SHOW_COUNTER || remainingInCurrentMessage <= 0) && msgCount <= 1) {
            if (remainingInCurrentMessage <= 10) {
            }
            return counterText;
        }
        if (ALWAYS_SHOW_COUNTER || msgCount > 1) {
            counterText = String.format(Locale.getDefault(), "%1$d / %2$d", new Object[]{Integer.valueOf(remainingInCurrentMessage), Integer.valueOf(msgCount)});
        } else {
            counterText = String.format(Locale.getDefault(), "%1$d", new Object[]{Integer.valueOf(remainingInCurrentMessage)});
        }
        return counterText;
    }

    public static boolean isMmsNotification(StatusBarNotification sbn) {
        if (sbn != null && "com.android.mms".equals(sbn.getPackageName())) {
            return true;
        }
        return false;
    }
}
