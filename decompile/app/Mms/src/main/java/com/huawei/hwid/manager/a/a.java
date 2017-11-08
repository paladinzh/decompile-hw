package com.huawei.hwid.manager.a;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.i;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.manager.g;
import java.util.ArrayList;
import java.util.List;

/* compiled from: APKAccountManager */
public final class a implements g {
    private static a a;
    private static AccountManager b;

    public static synchronized a a(Context context) {
        a aVar;
        synchronized (a.class) {
            if (a == null) {
                a = new a();
                b(context);
            }
            aVar = a;
        }
        return aVar;
    }

    private a() {
    }

    private static synchronized void b(Context context) {
        synchronized (a.class) {
            b = AccountManager.get(context);
        }
    }

    public boolean a(Context context, HwAccount hwAccount) {
        Account[] accountsByType = b.getAccountsByType("com.huawei.hwid");
        if (accountsByType != null && accountsByType.length > 0) {
            com.huawei.hwid.core.c.b.a.b("APKAccountManager", "arealdy has one account, can't save.");
            return true;
        } else if (!d.a(hwAccount)) {
            return false;
        } else {
            boolean a = a(context, hwAccount, null);
            com.huawei.hwid.core.c.b.a.b("APKAccountManager", "saveUserAccountInfoToDb  result:" + a);
            return a;
        }
    }

    public ArrayList a(Context context, String str) {
        ArrayList arrayList = new ArrayList();
        Account[] accountsByType = b.getAccountsByType(str);
        if (accountsByType != null && accountsByType.length > 0) {
            for (Account account : accountsByType) {
                HwAccount c = c(context, account.name, "cloud");
                if (c != null && d.a(c)) {
                    arrayList.add(c);
                }
            }
        }
        return arrayList;
    }

    public void b(Context context, String str, String str2) {
        b.invalidateAuthToken(str, str2);
    }

    public void a(Context context, String str, String str2, AccountManagerCallback accountManagerCallback) {
        c(context, str);
        d.a(context, true);
        b.removeAccount(a(str), accountManagerCallback, null);
    }

    public void a(Context context, String str, String str2) {
        if (c(context, str)) {
            d.a(context, true);
            b.removeAccount(a(str), null, null);
        }
    }

    public String a(Context context, String str, String str2, String str3) {
        return b.getUserData(a(str), str3);
    }

    public void a(Context context, String str, String str2, String str3, String str4) {
        b.setUserData(a(str), str3, str4);
    }

    public void b(Context context, String str, String str2, String str3) {
        b.setAuthToken(a(str), str2, str3);
    }

    public HwAccount c(Context context, String str, String str2) {
        int i = 0;
        if (p.e(str)) {
            return null;
        }
        int i2;
        if (p.e(str2) || "com.huawei.hwid".equals(str2)) {
            str2 = "cloud";
        }
        Account[] accountsByType = b.getAccountsByType("com.huawei.hwid");
        if (accountsByType != null && accountsByType.length > 0) {
            int length = accountsByType.length;
            for (i2 = i; i2 < length; i2++) {
                if (accountsByType[i2].name.equals(str)) {
                    i2 = 1;
                    break;
                }
            }
            i2 = i;
        } else {
            i2 = i;
        }
        if (i2 == 0) {
            return null;
        }
        HwAccount hwAccount = new HwAccount();
        String a = a(context, str, null, "userId");
        String d = d(context, str, "cloud");
        if (!(TextUtils.isEmpty(str2) || "cloud".equals(str2))) {
            d = d.b(d, str2);
        }
        String a2 = a(context, str, null, "siteId");
        try {
            i = Integer.parseInt(a2);
        } catch (Exception e) {
            com.huawei.hwid.core.c.b.a.a("APKAccountManager", "int getHwAccount siteIdStr:" + a2 + " is invalid");
        }
        a2 = a(context, str, null, "Cookie");
        String a3 = a(context, str, null, "accountType");
        String a4 = a(context, str, null, "deviceType");
        String a5 = a(context, str, null, "deviceId");
        if (p.e(d) || p.e(a)) {
            return null;
        }
        hwAccount.a(str);
        hwAccount.c(a);
        hwAccount.a(i);
        hwAccount.e(d);
        hwAccount.d(a2);
        hwAccount.f(a3);
        hwAccount.b("com.huawei.hwid");
        hwAccount.h(a4);
        hwAccount.g(a5);
        return hwAccount;
    }

    private Account a(String str) {
        if (TextUtils.isEmpty(str)) {
            com.huawei.hwid.core.c.b.a.d("APKAccountManager", "acountName is null");
        }
        return new Account(str, "com.huawei.hwid");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized boolean a(Context context, HwAccount hwAccount, Bundle bundle) {
        String a = hwAccount.a();
        String b = hwAccount.b();
        Account a2 = a(a);
        com.huawei.hwid.core.c.b.a.e("APKAccountManager", "to be saved accounts:" + f.a(hwAccount.toString(), true));
        List<HwAccount> a3 = a(context, "com.huawei.hwid");
        if (!a3.isEmpty()) {
            for (HwAccount hwAccount2 : a3) {
                if (!hwAccount2.a().equals(a)) {
                    if (hwAccount.c().equalsIgnoreCase(hwAccount2.c()) && p.d(a)) {
                        if (p.d(hwAccount2.a())) {
                        }
                    }
                }
            }
        }
        a(a2, "", null);
        b(context, a, "cloud", hwAccount.f());
        a(context, a, b, "userId", hwAccount.c());
        Context context2 = context;
        a(context2, a, b, "siteId", String.valueOf(hwAccount.d()));
        a(context, a, b, "deviceId", hwAccount.h());
        a(context, a, b, "deviceType", hwAccount.i());
        a(context, a, b, "Cookie", hwAccount.e());
        a(context, a, b, "accountType", hwAccount.g());
        return true;
    }

    public String d(Context context, String str, String str2) {
        return b.peekAuthToken(a(str), str2);
    }

    public boolean c(Context context, String str) {
        Account[] accountsByType = b.getAccountsByType("com.huawei.hwid");
        if (!(accountsByType == null || accountsByType.length <= 0 || TextUtils.isEmpty(str))) {
            for (Account account : accountsByType) {
                if (str.equalsIgnoreCase(account.name)) {
                    return true;
                }
            }
        }
        com.huawei.hwid.core.c.b.a.b("APKAccountManager", "isAccountAlreadyLogin " + f.c(str) + "false");
        return false;
    }

    public boolean a(Context context, ArrayList arrayList) {
        return false;
    }

    public void b(Context context, String str) {
        com.huawei.hwid.core.c.b.a.b("APKAccountManager", "removeAllAccounts " + str);
        Account[] accountsByType = b.getAccountsByType("com.huawei.hwid");
        if (accountsByType != null && accountsByType.length > 0) {
            for (Account account : accountsByType) {
                d.a(context, true);
                b.removeAccount(a(account.name), null, null);
            }
        }
    }

    public void d(Context context, String str) {
        if (!TextUtils.isEmpty(str)) {
            i.d(context, str);
            i.f(context, str);
        }
    }

    private boolean a(Account account, String str, Bundle bundle) {
        return b.addAccountExplicitly(account, str, bundle);
    }
}
