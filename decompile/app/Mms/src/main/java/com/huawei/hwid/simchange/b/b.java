package com.huawei.hwid.simchange.b;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.System;
import android.text.TextUtils;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.encrypt.e;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.simchange.a.g;
import com.huawei.hwid.simchange.a.h;

/* compiled from: SimChangeUtil */
public class b {
    public static void a(Context context, String str, String str2) {
        new Thread(new c(context, str, str2)).start();
    }

    public static String a(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accountsByType = accountManager.getAccountsByType("com.huawei.hwid");
        if (accountsByType != null && accountsByType.length > 0) {
            Account account = accountsByType[0];
            a.a("SimChangeUtil", "accountName is:" + f.c(account.name));
            Object userData = accountManager.getUserData(account, "accountStatus");
            if (!TextUtils.isEmpty(userData)) {
                return userData;
            }
        }
        return "noaccount";
    }

    public static void c(Context context, String str, String str2) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = new Account(str, "com.huawei.hwid");
        accountManager.setUserData(account, "accountStatus", str2);
        if (com.huawei.hwid.simchange.a.f.a()) {
            String a = com.huawei.hwid.simchange.a.f.a(context, 0);
            String a2 = com.huawei.hwid.simchange.a.f.a(context, 1);
            a.b("SimChangeUtil", "saveSimChangeInfo Multi imsiFirst: " + a + " imsiOldFirst: ");
            accountManager.setUserData(account, "imsiFirst", e.b(context, a));
            accountManager.setUserData(account, "imsiSecond", e.b(context, a2));
            return;
        }
        a = com.huawei.hwid.simchange.a.f.a(context, 0);
        a.b("SimChangeUtil", "saveSimChangeInfo Single imsiSim: " + a + " imsiOldFirst");
        accountManager.setUserData(account, "imsiFirst", e.b(context, a));
        accountManager.setUserData(account, "imsiSecond", "");
    }

    public static void a(Activity activity, String str, int i) {
        String str2 = "";
        if (d.b((Context) activity, "com.huawei.hwid.FINGER_AUTH")) {
            a.b("SimChangeUtil", "check pwd ACTION_FINGER_AUTH");
            str2 = "com.huawei.hwid.FINGER_AUTH";
        } else if (d.a((Context) activity, "com.huawei.hwid.UID_AUTH", "com.huawei.hwid")) {
            str2 = "com.huawei.hwid.UID_AUTH";
        } else {
            a.b("SimChangeUtil", "check pwd activity is null");
            return;
        }
        Intent intent = new Intent(str2);
        intent.setPackage("com.huawei.hwid");
        intent.putExtra("IS_FROM_SIMCHANGE", true);
        intent.putExtra("userId", str);
        intent.putExtra("startway", 5);
        intent.putExtra("requestTokenType", "com.huawei.hwid");
        activity.startActivityForResult(intent, i);
    }

    public static boolean a() {
        if (!d.g() && -1 != g.a().b()) {
            a.b("SimChangeUtil", "isSkyToneOpen No_apiLevelHigher22 getVSimSubId true");
            return true;
        } else if (!d.g() || !h.a().b()) {
            return false;
        } else {
            a.b("SimChangeUtil", "isSkyToneOpen isVSimEnabled true");
            return true;
        }
    }

    public static boolean d(Context context) {
        if (com.huawei.hwid.simchange.a.f.a()) {
            CharSequence a = com.huawei.hwid.simchange.a.f.a(context, 0);
            CharSequence a2 = com.huawei.hwid.simchange.a.f.a(context, 1);
            if ((5 == com.huawei.hwid.simchange.a.f.b(context, 0) && !TextUtils.isEmpty(a)) || 1 == com.huawei.hwid.simchange.a.f.b(context, 0)) {
                if (5 != com.huawei.hwid.simchange.a.f.b(context, 1) || TextUtils.isEmpty(a2)) {
                    if (1 == com.huawei.hwid.simchange.a.f.b(context, 1)) {
                        return true;
                    }
                }
                return true;
            }
            return false;
        }
        return (5 == com.huawei.hwid.simchange.a.f.b(context, 0) && !TextUtils.isEmpty(com.huawei.hwid.simchange.a.f.a(context, 0))) || 1 == com.huawei.hwid.simchange.a.f.b(context, 0);
    }

    public static boolean e(Context context) {
        if (1 != System.getInt(context.getContentResolver(), "airplane_mode_on", 0)) {
            return false;
        }
        return true;
    }

    public static boolean f(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accountsByType = accountManager.getAccountsByType("com.huawei.hwid");
        if (accountsByType == null || accountsByType.length <= 0 || !d.f(accountManager.getUserData(accountsByType[0], "accountType"))) {
            return false;
        }
        a.b("SimChangeUtil", "isThirdAccount true");
        return true;
    }

    public static boolean g(Context context) {
        return (e(context) || !d(context) || a() || f(context)) ? false : true;
    }
}
