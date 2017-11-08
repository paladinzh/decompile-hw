package com.android.huawei.keyguard.keyguardplus;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;
import java.util.Locale;

public class HwCustKeyguardStatusViewExImpl extends HwCustKeyguardStatusViewEx {
    public boolean isShowFullMonth() {
        if (SystemProperties.getBoolean("ro.config.show_full_month", false) && "el".equals(Locale.getDefault().getLanguage())) {
            return true;
        }
        return false;
    }

    public boolean isShowFrenchCustDate(Context context) {
        if ("fr".equals(Locale.getDefault().getLanguage()) || "en".equals(Locale.getDefault().getLanguage())) {
            if ("true".equals(System.getString(context.getContentResolver(), "is_show_fr_cust_date"))) {
                return true;
            }
        }
        return false;
    }
}
