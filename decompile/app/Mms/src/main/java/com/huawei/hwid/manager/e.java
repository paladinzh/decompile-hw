package com.huawei.hwid.manager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.f;

/* compiled from: AccountTools */
public class e {
    public static HwAccount a(Context context) {
        Account b = b(context);
        if (b == null) {
            return null;
        }
        return new HwAccount().a(a(context, b, "com.huawei.hwid"));
    }

    private static Account a(Context context, String str) {
        try {
            if (TextUtils.isEmpty(str)) {
                return null;
            }
            Account[] accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
            if (accountsByType == null || accountsByType.length == 0) {
                return null;
            }
            for (Account account : accountsByType) {
                if (str.equals(account.name)) {
                    return account;
                }
            }
            return null;
        } catch (Throwable e) {
            a.d("AccountTools", e.toString(), e);
            return null;
        }
    }

    public static Bundle a(Context context, String str, String str2) {
        return a(context, a(context, str), str2);
    }

    private static Bundle a(Context context, Account account, String str) {
        AccountManager accountManager = AccountManager.get(context);
        Bundle a = a(accountManager, account, str);
        if (a.containsKey("authtoken")) {
            a.putAll(a(accountManager, account));
            a.putString("serviceToken", a.getString("authtoken"));
            a.putString("accountName", account.name);
            return a;
        }
        a.d("AccountTools", "request account is not exist");
        return a;
    }

    public static Bundle a(AccountManager accountManager, Account account) {
        Bundle bundle = new Bundle();
        String userData = accountManager.getUserData(account, "userId");
        Object userData2 = accountManager.getUserData(account, "siteId");
        String userData3 = accountManager.getUserData(account, "deviceId");
        String userData4 = accountManager.getUserData(account, "Cookie");
        String userData5 = accountManager.getUserData(account, "deviceType");
        String userData6 = accountManager.getUserData(account, "accountType");
        a.a("AccountTools", "userid:" + f.a(userData) + ", siteid=" + userData2 + ", unitedId=" + f.a(userData3) + ", unitedType=" + userData5 + ", accountType=" + userData6);
        bundle.putString("userId", userData);
        bundle.putString("deviceId", userData3);
        bundle.putString("deviceType", userData5);
        bundle.putString("Cookie", userData4);
        bundle.putString("accountType", userData6);
        if (!TextUtils.isEmpty(userData2)) {
            try {
                bundle.putInt("siteId", Integer.valueOf(userData2).intValue());
            } catch (Throwable e) {
                a.d("AccountTools", "NumberFormatException / " + e.toString(), e);
            }
        }
        return bundle;
    }

    public static Bundle a(AccountManager accountManager, Account account, String str) {
        if (p.e(str) || "com.huawei.hwid".equals(str)) {
            str = "cloud";
        }
        Bundle bundle = new Bundle();
        if (account != null) {
            String peekAuthToken = accountManager.peekAuthToken(account, "cloud");
            a.b("AccountTools", "getAuthToken===> authTokenType:" + str);
            if (!TextUtils.isEmpty(peekAuthToken)) {
                peekAuthToken = d.b(peekAuthToken, str);
                a.b("AccountTools", "getAuthToken, account name:" + f.c(account.name));
                bundle.putString("authAccount", p.c(account.name, accountManager.getUserData(account, "accountType")));
                bundle.putString("accountType", "com.huawei.hwid");
                bundle.putString("authtoken", peekAuthToken);
                return bundle;
            }
        }
        return bundle;
    }

    private static Account b(Context context) {
        Account[] accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
        if (accountsByType != null && accountsByType.length >= 1) {
            return accountsByType[0];
        }
        a.b("AccountTools", "no accounts logined");
        return null;
    }
}
