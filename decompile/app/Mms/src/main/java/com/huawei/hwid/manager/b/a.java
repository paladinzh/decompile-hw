package com.huawei.hwid.manager.b;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.c.a.d;
import com.huawei.hwid.core.c.i;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.manager.g;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/* compiled from: SDKAccountManager */
public final class a implements g {
    private static a a;

    private a() {
    }

    public static synchronized a a(Context context) {
        a aVar;
        synchronized (a.class) {
            if (a == null) {
                a = new a();
                a.b(context);
            }
            aVar = a;
        }
        return aVar;
    }

    private void b(Context context) {
        d.a(context);
    }

    public boolean a(Context context, HwAccount hwAccount) {
        ArrayList c = c(context);
        if (com.huawei.hwid.core.c.d.a(hwAccount)) {
            c = a(c, hwAccount);
            a(context, (List) c);
            com.huawei.hwid.a.a().a(c);
            return true;
        }
        com.huawei.hwid.core.c.b.a.b("SDKAccountManager", "the account is invalid , cannot be added into file");
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ArrayList a(Context context, String str) {
        ArrayList arrayList = new ArrayList();
        ArrayList c = c(context);
        synchronized (c) {
            if (!(p.e(str) || c.isEmpty())) {
                Iterator it = c.iterator();
                while (it.hasNext()) {
                    HwAccount hwAccount = (HwAccount) it.next();
                    if (!(str == null || hwAccount == null || !str.equals(hwAccount.b()))) {
                        arrayList.add(hwAccount);
                    }
                }
                com.huawei.hwid.core.c.b.a.e("SDKAccountManager", "getAccountsByType accountlist size:" + arrayList.size());
                return arrayList;
            }
        }
    }

    public void a(Context context, String str, String str2) {
        ArrayList c = c(context);
        if (c.isEmpty()) {
            com.huawei.hwid.core.c.b.a.b("SDKAccountManager", "there has no account");
        } else if (p.e(str)) {
            com.huawei.hwid.core.c.b.a.b("SDKAccountManager", "accountName is null , can't be deleted from file");
        } else {
            synchronized (c) {
                Collection arrayList = new ArrayList();
                Iterator it = c.iterator();
                while (it.hasNext()) {
                    HwAccount hwAccount = (HwAccount) it.next();
                    if (hwAccount != null && str.equals(hwAccount.a())) {
                        if (!p.e(str2)) {
                            try {
                                if (!p.e(str2)) {
                                    if (!str2.equals(hwAccount.b())) {
                                    }
                                }
                            } catch (Exception e) {
                                com.huawei.hwid.core.c.b.a.d("SDKAccountManager", e.toString());
                            }
                        }
                        arrayList.add(hwAccount);
                    }
                }
                if (!arrayList.isEmpty() && c.containsAll(arrayList)) {
                    c.removeAll(arrayList);
                }
                a(context, (List) c);
                com.huawei.hwid.a.a().a(c);
            }
        }
    }

    public void b(Context context, String str, String str2) {
        com.huawei.hwid.core.c.b.a.b("SDKAccountManager", "invalidateAuthToken  type=" + str);
        ArrayList c = c(context);
        synchronized (c) {
            List<HwAccount> a = a(context, str);
            if (!a.isEmpty()) {
                for (HwAccount hwAccount : a) {
                    if (!(hwAccount == null || p.e(str2) || !str2.equals(hwAccount.f()))) {
                        a(context, hwAccount.a(), str);
                        c.remove(hwAccount);
                    }
                }
            }
            com.huawei.hwid.a.a().a(c);
        }
    }

    public String a(Context context, String str, String str2, String str3) {
        if (p.e(str)) {
            return null;
        }
        HwAccount c = c(context, str, str2);
        if (c == null) {
            return null;
        }
        return c.j().getString(str3);
    }

    public void a(Context context, String str, String str2, String str3, String str4) {
        if (!p.e(str)) {
            HwAccount c = c(context, str, str2);
            if (c != null) {
                Bundle j = c.j();
                if (j.containsKey(str3)) {
                    j.putString(str3, str4);
                    c = c.a(j);
                } else {
                    com.huawei.hwid.core.c.b.a.b("SDKAccountManager", "the Account don't have the key");
                }
                a(context, c);
                return;
            }
            com.huawei.hwid.core.c.b.a.b("SDKAccountManager", "don't find the account");
        }
    }

    public boolean c(Context context, String str) {
        if (com.huawei.hwid.core.c.d.j(context) && com.huawei.hwid.core.c.d.l(context)) {
            Account[] accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
            if (!(accountsByType == null || accountsByType.length <= 0 || p.e(str))) {
                for (Account account : accountsByType) {
                    if (account != null && str.equalsIgnoreCase(account.name)) {
                        return true;
                    }
                }
            }
            return false;
        }
        List<HwAccount> c = c(context);
        if (!(c.isEmpty() || p.e(str))) {
            for (HwAccount hwAccount : c) {
                if (hwAccount != null && str.equalsIgnoreCase(hwAccount.a())) {
                    return true;
                }
            }
        }
        return false;
    }

    public HwAccount c(Context context, String str, String str2) {
        List<HwAccount> c = c(context);
        if (c.isEmpty() || p.e(str)) {
            com.huawei.hwid.core.c.b.a.b("SDKAccountManager", "there has no account");
            return null;
        }
        if ("cloud".equals(str2) || "com.huawei.hwid".equals(str2)) {
            str2 = "";
        }
        synchronized (c) {
            for (HwAccount hwAccount : c) {
                if (a(str, hwAccount, str2)) {
                    return hwAccount;
                }
            }
            return null;
        }
    }

    private boolean a(String str, HwAccount hwAccount, String str2) {
        if (!(str == null || hwAccount == null || !str.equals(hwAccount.a()))) {
            if (!p.e(str2)) {
                if (!p.e(str2)) {
                    if (!str2.equals(hwAccount.b())) {
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean a(Context context, ArrayList arrayList) {
        if (arrayList == null || arrayList.isEmpty()) {
            return false;
        }
        a(context, (List) arrayList);
        com.huawei.hwid.a.a().a(arrayList);
        return true;
    }

    public void b(Context context, String str) {
        com.huawei.hwid.core.c.b.a.b("SDKAccountManager", "removeAllAccounts: type=" + str);
        ArrayList c = c(context);
        synchronized (c) {
            Collection a = a(context, str);
            try {
                if (!c.isEmpty()) {
                    if (!a.isEmpty() && c.containsAll(a)) {
                        c.removeAll(a);
                    }
                }
            } catch (Exception e) {
                com.huawei.hwid.core.c.b.a.d("SDKAccountManager", e.toString());
            }
            a(context, (List) c);
            com.huawei.hwid.a.a().a(c);
        }
    }

    private ArrayList a(ArrayList arrayList, HwAccount hwAccount) {
        if (!com.huawei.hwid.core.c.d.a(hwAccount)) {
            com.huawei.hwid.core.c.b.a.b("SDKAccountManager", "the account is invalid , cannot be added into file");
            return arrayList;
        } else if (arrayList == null || arrayList.isEmpty()) {
            ArrayList arrayList2 = new ArrayList();
            arrayList2.add(hwAccount);
            return arrayList2;
        } else {
            String c = hwAccount.c();
            String a = hwAccount.a();
            String b = hwAccount.b();
            synchronized (arrayList) {
                Collection arrayList3 = new ArrayList();
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    try {
                        HwAccount hwAccount2 = (HwAccount) it.next();
                        if (hwAccount2 != null) {
                            if (!c.equals(hwAccount2.c())) {
                                if (!a.equals(hwAccount2.a())) {
                                }
                            }
                            if (p.e(b) || !b.equals(hwAccount2.b())) {
                                if (!p.e(b)) {
                                }
                            }
                            arrayList3.add(hwAccount2);
                        }
                    } catch (Exception e) {
                        com.huawei.hwid.core.c.b.a.d("SDKAccountManager", e.toString());
                    }
                }
                if (!arrayList3.isEmpty() && arrayList.containsAll(arrayList3)) {
                    arrayList.removeAll(arrayList3);
                }
                arrayList.add(hwAccount);
            }
            return arrayList;
        }
    }

    private void a(Context context, List list) {
        i.a(context, "accounts.xml");
        try {
            b.a(context, "accounts.xml", list, true);
        } catch (Throwable e) {
            com.huawei.hwid.core.c.b.a.c("SDKAccountManager", e.toString(), e);
        }
    }

    private ArrayList c(Context context) {
        ArrayList c = com.huawei.hwid.a.a().c();
        if (c != null && !c.isEmpty()) {
            return c;
        }
        c = b.a("accounts.xml", context, true);
        com.huawei.hwid.a.a().a(c);
        return c;
    }

    public void a(Context context, String str, String str2, AccountManagerCallback accountManagerCallback) {
        a(context, str, str2);
        if (accountManagerCallback != null) {
            accountManagerCallback.run(null);
        }
    }

    public void d(Context context, String str) {
    }
}
