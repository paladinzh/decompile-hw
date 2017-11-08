package com.huawei.hwid.core.c;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.c.c;

/* compiled from: SimCardInfoUtil */
public class n {
    public static String a(Context context) {
        if (!d.a(context, (int) MsgUrlService.RESULT_NOT_IMPL)) {
            return null;
        }
        Object b;
        String a;
        if (c.b()) {
            b = b();
            a = a();
        } else {
            b = c(context);
            a = b(context);
        }
        if (TextUtils.isEmpty(b) || a == null || a.length() < 3) {
            return null;
        }
        return a(a.substring(0, 3), b);
    }

    private static String a(String str, String str2) {
        if (str == null) {
            return null;
        }
        if (str.equals("460")) {
            a.b("SimCardInfoUtil", "this card maybe china");
            String str3 = "";
            if (str2.length() > 11) {
                str2 = str2.substring(str2.length() - 11);
            }
            if (!str2.matches("[0-9]{1,}")) {
                return null;
            }
            a.b("SimCardInfoUtil", "phoneNumber is valid");
            return str2;
        }
        a.b("SimCardInfoUtil", "this card maybe oversea");
        return null;
    }

    private static String b(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager != null) {
            return telephonyManager.getSubscriberId();
        }
        return null;
    }

    private static String a() {
        com.huawei.hwid.core.c.c.a a = c.a();
        return a.b(a.a());
    }

    private static String c(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager != null) {
            return telephonyManager.getLine1Number();
        }
        return null;
    }

    private static String b() {
        com.huawei.hwid.core.c.c.a a = c.a();
        return a.e(a.a());
    }
}
