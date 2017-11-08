package com.huawei.hwid.a.a;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.hwid.a.b;
import com.huawei.hwid.core.d.a.d;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.f;
import com.huawei.hwid.core.datatype.HwAccount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class a implements b {
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
        if (com.huawei.hwid.core.d.b.a(hwAccount)) {
            c = a(c, hwAccount);
            a(context, (List) c);
            com.huawei.hwid.a.a().a(c);
            return true;
        }
        e.b("SDKAccountManager", "the account is invalid , cannot be added into file");
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ArrayList<HwAccount> a(Context context, String str) {
        ArrayList<HwAccount> arrayList = new ArrayList();
        ArrayList c = c(context);
        synchronized (c) {
            if (!(TextUtils.isEmpty(str) || c.isEmpty())) {
                Iterator it = c.iterator();
                while (it.hasNext()) {
                    HwAccount hwAccount = (HwAccount) it.next();
                    if (!(str == null || hwAccount == null || !str.equals(hwAccount.c()))) {
                        arrayList.add(hwAccount);
                    }
                }
                e.e("SDKAccountManager", "getAccountsByType accountlist size:" + arrayList.size());
                return arrayList;
            }
        }
    }

    public void a(Context context, String str, String str2) {
        ArrayList c = c(context);
        if (c.isEmpty()) {
            e.b("SDKAccountManager", "there has no account");
        } else if (TextUtils.isEmpty(str)) {
            e.b("SDKAccountManager", "accountName is null , can't be deleted from file");
        } else {
            synchronized (c) {
                Collection arrayList = new ArrayList();
                Iterator it = c.iterator();
                while (it.hasNext()) {
                    HwAccount hwAccount = (HwAccount) it.next();
                    if (hwAccount != null && str.equals(hwAccount.b())) {
                        if (!TextUtils.isEmpty(str2)) {
                            try {
                                if (!TextUtils.isEmpty(str2)) {
                                    if (!str2.equals(hwAccount.c())) {
                                    }
                                }
                            } catch (Exception e) {
                                e.d("SDKAccountManager", e.getMessage());
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

    public void a(Context context, String str, String str2, String str3) {
        e.b("SDKAccountManager", "invalidateAuthToken  type=" + str2);
        ArrayList c = c(context);
        synchronized (c) {
            List<HwAccount> a = a(context, str2);
            if (!a.isEmpty()) {
                for (HwAccount hwAccount : a) {
                    if (!(hwAccount == null || TextUtils.isEmpty(str3) || !str3.equals(hwAccount.g()))) {
                        a(context, hwAccount.b(), str2);
                        c.remove(hwAccount);
                    }
                }
            }
            com.huawei.hwid.a.a().a(c);
        }
    }

    public void a(Context context, String str, String str2, String str3, String str4) {
        if (!TextUtils.isEmpty(str)) {
            HwAccount b = b(context, str, str2);
            if (b != null) {
                Bundle k = b.k();
                if (k.containsKey(str3)) {
                    k.putString(str3, str4);
                    b = b.a(k);
                } else {
                    e.b("SDKAccountManager", "the Account don't have the key");
                }
                a(context, b);
                com.huawei.hwid.b.a.a(context).a(b);
                return;
            }
            e.b("SDKAccountManager", "don't find the account");
        }
    }

    public HwAccount b(Context context, String str, String str2) {
        List<HwAccount> c = c(context);
        if (c.isEmpty() || TextUtils.isEmpty(str)) {
            e.b("SDKAccountManager", "there has no account");
            return null;
        }
        if ("com.huawei.hwid".equals(str2) || "com.huawei.hwid".equals(str2)) {
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
        if (!(str == null || hwAccount == null || !str.equals(hwAccount.b()))) {
            if (!TextUtils.isEmpty(str2)) {
                if (!TextUtils.isEmpty(str2)) {
                    if (!str2.equals(hwAccount.c())) {
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean a(Context context, ArrayList<HwAccount> arrayList) {
        if (arrayList == null || arrayList.isEmpty()) {
            return false;
        }
        a(context, (List) arrayList);
        com.huawei.hwid.a.a().a((ArrayList) arrayList);
        return true;
    }

    public void b(Context context, String str) {
        e.b("SDKAccountManager", "removeAllAccounts: type=" + str);
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
                e.d("SDKAccountManager", e.getMessage());
            }
            a(context, (List) c);
            com.huawei.hwid.a.a().a(c);
        }
    }

    private ArrayList<HwAccount> a(ArrayList<HwAccount> arrayList, HwAccount hwAccount) {
        if (!com.huawei.hwid.core.d.b.a(hwAccount)) {
            e.b("SDKAccountManager", "the account is invalid , cannot be added into file");
            return arrayList;
        } else if (arrayList == null || arrayList.isEmpty()) {
            ArrayList<HwAccount> arrayList2 = new ArrayList();
            arrayList2.add(hwAccount);
            return arrayList2;
        } else {
            String d = hwAccount.d();
            String b = hwAccount.b();
            Object c = hwAccount.c();
            synchronized (arrayList) {
                Collection arrayList3 = new ArrayList();
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    try {
                        HwAccount hwAccount2 = (HwAccount) it.next();
                        if (hwAccount2 != null) {
                            if (!d.equals(hwAccount2.d())) {
                                if (!b.equals(hwAccount2.b())) {
                                }
                            }
                            if (TextUtils.isEmpty(c) || !c.equals(hwAccount2.c())) {
                                if (!TextUtils.isEmpty(c)) {
                                }
                            }
                            arrayList3.add(hwAccount2);
                        }
                    } catch (Exception e) {
                        e.d("SDKAccountManager", e.getMessage());
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

    private void a(Context context, List<HwAccount> list) {
        f.a(context, "accounts.xml");
        try {
            b.a(context, "accounts.xml", (List) list, true);
        } catch (Throwable e) {
            e.c("SDKAccountManager", e.getMessage(), e);
        }
    }

    private ArrayList<HwAccount> c(Context context) {
        ArrayList<HwAccount> c = com.huawei.hwid.a.a().c();
        if (c != null && !c.isEmpty()) {
            return c;
        }
        ArrayList a = b.a("accounts.xml", context, true);
        com.huawei.hwid.a.a().a(a);
        return a;
    }

    public void b(Context context, String str, String str2, String str3) {
    }
}
