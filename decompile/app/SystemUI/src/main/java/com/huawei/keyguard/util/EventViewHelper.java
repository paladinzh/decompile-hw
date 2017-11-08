package com.huawei.keyguard.util;

import android.content.Context;
import com.android.keyguard.R$plurals;
import com.android.keyguard.R$string;
import fyusion.vislib.BuildConfig;

public class EventViewHelper {
    public static String getMissCallAccessibilityDescription(Context context, int num, boolean canOpen) {
        String description = BuildConfig.FLAVOR;
        if (num <= 99 && num > 0) {
            description = context.getResources().getQuantityString(R$plurals.missed_call_tip, num, new Object[]{Integer.valueOf(num)});
        } else if (num > 99) {
            description = context.getResources().getString(R$string.missed_call_99tip);
        }
        if (!canOpen) {
            return description;
        }
        return (description + " ") + context.getResources().getString(R$string.slide_open_accessibility_description);
    }

    public static String getNewMmsAccessibilityDescription(Context context, int num, boolean canOpen) {
        String description = BuildConfig.FLAVOR;
        if (num <= 99 && num > 0) {
            description = context.getResources().getQuantityString(R$plurals.missed_message_tip, num, new Object[]{Integer.valueOf(num)});
        } else if (num > 99) {
            description = context.getResources().getString(R$string.missed_message_99tip);
        }
        if (!canOpen) {
            return description;
        }
        return (description + " ") + context.getResources().getString(R$string.slide_open_accessibility_description);
    }
}
