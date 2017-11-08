package com.huawei.hwid.api.common;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.cloudservice.CloudAccount;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.cloudservice.LoginHandler;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.i;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.e;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.model.http.request.am;
import com.huawei.hwid.manager.f;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* compiled from: CloudAccountImpl */
public class a {
    private static Object b = new Object();
    private static Map c = new HashMap();
    private HwAccount a = new HwAccount();

    public static void a(Context context, String str, Bundle bundle, LoginHandler loginHandler) {
        boolean z = false;
        if (a(context, loginHandler)) {
            Bundle bundle2;
            boolean z2;
            boolean z3;
            com.huawei.hwid.api.common.c.a.a(loginHandler);
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "mHandler is " + loginHandler);
            k(context);
            String d = d(context, str);
            List a = f.a(context).a(context, d);
            if (a == null || a.isEmpty()) {
                new Thread(new b(context)).start();
            }
            if (bundle == null) {
                bundle2 = new Bundle();
                z2 = false;
                z3 = false;
            } else {
                z3 = bundle.getBoolean("popLogin", false);
                z2 = bundle.getBoolean("chooseWindow", false);
                z = bundle.getInt("loginChannel", 0);
                bundle2 = bundle;
            }
            if (z) {
                com.huawei.hwid.core.c.b.a.e("CloudAccountImpl", "getAccountsByType:isSelectAccount=" + z2 + ",isPopLogin=" + z3);
                if (p.e(d)) {
                    d = d.d(context);
                }
                if (b(context, d, loginHandler)) {
                    i(context);
                    String a2 = com.huawei.hwid.api.common.c.a.a(context);
                    if (d.j(context) && b(context)) {
                        c a3 = a(context, e(context), a2);
                        com.huawei.hwid.api.common.c.a.a(a3);
                        com.huawei.hwid.api.common.apkimpl.a.a(context, d, a2, bundle2, loginHandler, a3);
                    } else {
                        com.huawei.hwid.api.common.b.a.a(context, d, z3, z2, a2, a, loginHandler, bundle2);
                    }
                    return;
                }
                return;
            }
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "loginChannel can't be null!");
            loginHandler.onError(new ErrorStatus(12, "loginChannel can't be null!"));
            return;
        }
        com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "getAccountsByType: context or handler is null");
    }

    private static boolean b(Context context, String str, LoginHandler loginHandler) {
        if (context.getPackageName().equals(str)) {
            return true;
        }
        ErrorStatus errorStatus = new ErrorStatus(12, "tokenType is not the same as package name");
        com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "error: " + errorStatus.toString());
        loginHandler.onError(errorStatus);
        return false;
    }

    public static int a(CloudAccount[] cloudAccountArr, String str) {
        if (!(p.e(str) || cloudAccountArr == null || cloudAccountArr.length <= 0)) {
            for (int i = 0; i < cloudAccountArr.length; i++) {
                if (str.equalsIgnoreCase(cloudAccountArr[i].getAccountName())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static CloudAccount[] a(Context context) {
        if (context != null) {
            List a = f.a(context).a(context, d.o(context));
            if (a == null || a.isEmpty()) {
                return new CloudAccount[0];
            }
            CloudAccount[] cloudAccountArr = new CloudAccount[a.size()];
            for (int i = 0; i < cloudAccountArr.length; i++) {
                a aVar = new a();
                aVar.a((HwAccount) a.get(i));
                cloudAccountArr[i] = new CloudAccount(aVar);
            }
            return cloudAccountArr;
        }
        com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "context is null");
        return new CloudAccount[0];
    }

    private void a(HwAccount hwAccount) {
        this.a = hwAccount;
    }

    public static boolean b(Context context) {
        if (context != null) {
            return d.l(context);
        }
        com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "context is null");
        return false;
    }

    private static void i(Context context) {
        j(context);
        if (com.huawei.hwid.api.common.c.a.c()) {
            com.huawei.hwid.core.c.b.a.e("CloudAccountImpl", "begin to init accounts");
            a(context);
            com.huawei.hwid.api.common.c.a.a(false);
            com.huawei.hwid.core.c.b.a.e("CloudAccountImpl", "initData");
            if (p.e(com.huawei.hwid.api.common.c.a.a(context))) {
                List a = f.a(context).a(context, d.o(context));
                if (a != null && !a.isEmpty()) {
                    String a2 = ((HwAccount) a.get(0)).a();
                    com.huawei.hwid.api.common.c.a.a(context, a2);
                    com.huawei.hwid.core.c.b.a.e("CloudAccountImpl", "initData===> mCurrentLoginUserName:" + com.huawei.hwid.core.encrypt.f.c(a2));
                }
            }
        }
    }

    private static synchronized void j(Context context) {
        synchronized (a.class) {
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "synAccountFromApkToSDK");
            if (d.j(context)) {
                if (b(context)) {
                    Account[] accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
                    if (accountsByType != null) {
                        if (accountsByType.length != 0) {
                            List<HwAccount> a = f.a(context).a(context, d.o(context));
                            if (!(a == null || a.isEmpty())) {
                                com.huawei.hwid.core.c.b.a.e("CloudAccountImpl", "sdk has accounts， so need to synchronize accounts");
                                ArrayList arrayList = new ArrayList();
                                for (HwAccount hwAccount : a) {
                                    Object obj;
                                    String a2 = hwAccount.a();
                                    for (Account account : accountsByType) {
                                        if (a2.equals(account.name)) {
                                            obj = 1;
                                            break;
                                        }
                                    }
                                    obj = null;
                                    if (obj != null) {
                                        arrayList.add(hwAccount);
                                    }
                                }
                                if (arrayList.isEmpty()) {
                                    c(context);
                                } else {
                                    f.a(context).a(context, arrayList);
                                    com.huawei.hwid.core.c.b.a.e("CloudAccountImpl", "save accounts size: " + arrayList.size());
                                }
                                com.huawei.hwid.api.common.c.a.a(context, c(context, com.huawei.hwid.api.common.c.a.a(context)));
                            }
                        }
                    }
                    com.huawei.hwid.core.c.b.a.e("CloudAccountImpl", "apk has no account, clear all sdk accounts");
                    c(context);
                }
            }
        }
    }

    private static String c(Context context, String str) {
        List<HwAccount> a = f.a(context).a(context, d.o(context));
        if (a == null || a.isEmpty()) {
            return "";
        }
        for (HwAccount a2 : a) {
            if (a2.a().equals(str)) {
                return str;
            }
        }
        return ((HwAccount) a.get(0)).a();
    }

    public static void c(Context context) {
        if (context != null) {
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "clear all accout data");
            f.a(context).b(context, d.o(context));
            com.huawei.hwid.api.common.c.a.a(context, "");
            return;
        }
        com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "context is null");
    }

    public static void a(Context context, LoginHandler loginHandler, c cVar) {
        if (context != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.huawei.cloudserive.loginSuccess");
            intentFilter.addAction("com.huawei.cloudserive.loginFailed");
            intentFilter.addAction("com.huawei.cloudserive.loginCancel");
            if (c.containsKey("LoginBroadcastReceiver")) {
                e(context, "LoginBroadcastReceiver");
            }
            BroadcastReceiver iVar = new i(context, loginHandler, cVar);
            try {
                context.registerReceiver(iVar, intentFilter);
                a(iVar, "LoginBroadcastReceiver");
            } catch (Exception e) {
                com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "BroadcastReceiver components are not allowed to register to receive intents");
            }
            return;
        }
        com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "context is null");
    }

    private static HwAccount c(Context context, Intent intent) {
        HwAccount hwAccount = new HwAccount();
        if (intent.hasExtra("hwaccount")) {
            return (HwAccount) intent.getParcelableExtra("hwaccount");
        }
        if (intent.hasExtra("accountBundle")) {
            return c(context, intent.getBundleExtra("accountBundle"));
        }
        if (intent.hasExtra("bundle")) {
            return c(context, intent.getBundleExtra("bundle"));
        }
        return hwAccount;
    }

    private static HwAccount c(Context context, Bundle bundle) {
        HwAccount hwAccount = new HwAccount();
        if (bundle != null) {
            String string = bundle.getString("accountName");
            String string2 = bundle.getString("userId");
            String string3 = bundle.getString("deviceId");
            String string4 = bundle.getString("deviceType");
            int i = bundle.getInt("siteId");
            String string5 = bundle.getString("serviceToken");
            String string6 = bundle.getString("accountType");
            String string7 = bundle.getString("Cookie");
            hwAccount.a(string);
            hwAccount.g(string3);
            hwAccount.h(string4);
            hwAccount.a(i);
            hwAccount.e(string5);
            hwAccount.c(string2);
            hwAccount.b(d.o(context));
            hwAccount.f(string6);
            hwAccount.d(string7);
        }
        return hwAccount;
    }

    public static String d(Context context) {
        String str = "";
        if (context == null) {
            return str;
        }
        return com.huawei.hwid.api.common.c.a.a(context);
    }

    public HwAccount a() {
        return this.a;
    }

    public static boolean e(Context context) {
        if (context == null) {
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "context is null");
            return false;
        } else if (d.j(context) && b(context)) {
            return com.huawei.hwid.api.common.apkimpl.a.a(context);
        } else {
            return com.huawei.hwid.api.common.b.a.a(context);
        }
    }

    public static CloudAccount b(Context context, String str) {
        if (context == null) {
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "context is null");
            return null;
        } else if (p.e(str)) {
            com.huawei.hwid.core.c.b.a.e("CloudAccountImpl", "get account by userID failed, the userID is null!");
            return null;
        } else {
            HwAccount c = d.c(context, str);
            if (c == null) {
                com.huawei.hwid.core.c.b.a.e("CloudAccountImpl", "get account by userID failed, there is no matching account!");
                return null;
            }
            a aVar = new a();
            aVar.a(c);
            com.huawei.hwid.core.c.b.a.e("CloudAccountImpl", "get account by userID success!");
            return new CloudAccount(aVar);
        }
    }

    private static synchronized void k(Context context) {
        synchronized (a.class) {
            com.huawei.hwid.core.c.b.a.a(context);
        }
    }

    public static void a(Context context, String str, String str2, String str3, String str4, CloudRequestHandler cloudRequestHandler, Bundle bundle) {
        if (!d(context, cloudRequestHandler)) {
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "checkPassWord: context or cloudRequestHandler is null");
        } else if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2) || TextUtils.isEmpty(str4)) {
            com.huawei.hwid.core.c.b.a.e("CloudAccountImpl", "error: parameter is invalid");
            r0 = new ErrorStatus(12, "parameter is invalid");
            com.huawei.hwid.core.c.b.a.e("CloudAccountImpl", "error: " + r0.toString());
            cloudRequestHandler.onError(r0);
        } else if (!d.a(context)) {
            com.huawei.hwid.core.c.b.a.e("CloudAccountImpl", "error: have no network");
            r0 = new ErrorStatus(5, context.getString(m.a(context, "CS_no_network_content")));
            com.huawei.hwid.core.c.b.a.e("CloudAccountImpl", "error: " + r0.toString());
            cloudRequestHandler.onError(r0);
        } else if (d.f(str3)) {
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "this is third account");
            Bundle bundle2 = new Bundle();
            bundle2.putBoolean("isSuccess", true);
            cloudRequestHandler.onFinish(bundle2);
        } else {
            String str5;
            if (str3 != null) {
                str5 = str3;
            } else {
                str5 = d.b(str);
            }
            com.huawei.hwid.core.model.http.a amVar = new am(context, str, e.d(context, str2), str5, str4);
            amVar.a(context, amVar, str, cloudRequestHandler);
        }
    }

    private static String d(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return d.o(context);
        }
        i.a(context, "tokenType", str);
        return str;
    }

    public static void b(Context context, CloudRequestHandler cloudRequestHandler) {
        if (context != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.huawei.cloudserive.fingerSuccess");
            intentFilter.addAction("com.huawei.cloudserive.fingerCancel");
            if (c.containsKey("FingerBroadcastReceiver")) {
                e(context, "FingerBroadcastReceiver");
            }
            BroadcastReceiver hVar = new h(context, cloudRequestHandler);
            try {
                context.registerReceiver(hVar, intentFilter);
                a(hVar, "FingerBroadcastReceiver");
            } catch (Exception e) {
                com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "BroadcastReceiver components are not allowed to register to receive intents");
            }
            return;
        }
        com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "context is null");
    }

    private static void d(Context context, Intent intent) {
        HwAccount c = c(context, intent);
        if (c != null && d.a(c)) {
            c(context);
            f.a(context).a(context, c);
            com.huawei.hwid.api.common.c.a.a(context, c.a());
        }
    }

    public static void a(Context context, String str, boolean z, CloudRequestHandler cloudRequestHandler, Bundle bundle) {
        if (!d(context, cloudRequestHandler)) {
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "checkHwIDPassword: context or cloudRequestHandler is null");
        } else if (cloudRequestHandler == null) {
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "cloudRequestHandler is null");
        } else if (TextUtils.isEmpty(str)) {
            cloudRequestHandler.onError(new ErrorStatus(12, "the param is invalid"));
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "userId is empty");
        } else if (bundle != null) {
            String str2 = "";
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "checkHwIDPassword");
            CloudAccount b = b(context, str);
            if (b != null) {
                str2 = b.getAccountType();
            } else {
                str2 = bundle.getString("accountType");
                com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "get accountType from bundle");
                if (TextUtils.isEmpty(str2)) {
                    cloudRequestHandler.onError(new ErrorStatus(12, "the param is invalid"));
                    com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "userId  actp is error");
                    return;
                }
            }
            Intent intent = new Intent();
            b(context, cloudRequestHandler);
            intent.putExtra("userId", str);
            intent.putExtra("accountType", str2);
            intent.putExtra("requestTokenType", context.getPackageName());
            intent.putExtra("startway", 3);
            intent.putExtra("use_finger", z);
            intent.putExtra("receive_package", d.d(context));
            intent.putExtras(bundle);
            if (bundle.containsKey("bindOperation")) {
                intent.putExtra("onlyBindPhoneForThird", 1);
                intent.putExtra("startway", 8);
            }
            if (d.b(context, "com.huawei.hwid.FINGER_AUTH")) {
                intent.setAction("com.huawei.hwid.FINGER_AUTH");
                intent.setPackage("com.huawei.hwid");
            } else if (d.a(context, "com.huawei.hwid.UID_AUTH", context.getPackageName())) {
                intent.setAction("com.huawei.hwid.UID_AUTH");
                intent.setPackage(context.getPackageName());
            } else {
                cloudRequestHandler.onError(new ErrorStatus(12, "check pwd activity is null"));
                com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "check pwd activity is null");
                return;
            }
            d.a(context, intent, 0);
        } else {
            cloudRequestHandler.onError(new ErrorStatus(12, "the param is invalid"));
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "bundle is null");
        }
    }

    private static c a(Context context, boolean z, String str) {
        if (z) {
            return new c(context, "105", str);
        }
        return new c(context, "101", str);
    }

    private static boolean a(Context context, LoginHandler loginHandler) {
        if (loginHandler == null) {
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "loginHandler is null");
            return false;
        } else if (context != null) {
            return true;
        } else {
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "context is null");
            loginHandler.onError(new ErrorStatus(12, "context is null"));
            return false;
        }
    }

    private static boolean d(Context context, CloudRequestHandler cloudRequestHandler) {
        if (cloudRequestHandler == null) {
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "requestHandler is null");
            return false;
        } else if (context != null) {
            return true;
        } else {
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "context is null");
            cloudRequestHandler.onError(new ErrorStatus(12, "context is null"));
            return false;
        }
    }

    public static void a(Context context, Bundle bundle) {
        if (context == null || bundle == null || c == null) {
            com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "context, bundle or broadcast is null, can't remove broadcast");
            return;
        }
        boolean z = bundle.getBoolean("LoginBroadcastReceiver", false);
        boolean z2 = bundle.getBoolean("LogoutBroadcastReceiver", false);
        boolean z3 = bundle.getBoolean("FingerBroadcastReceiver", false);
        boolean z4 = bundle.getBoolean("OpenLoginBroadcastReceiver", false);
        boolean z5 = bundle.getBoolean("BindSafePhoneBroadcastReceiver", false);
        if (z) {
            e(context, "LoginBroadcastReceiver");
        }
        if (z2) {
            e(context, "LogoutBroadcastReceiver");
        }
        if (z3) {
            e(context, "FingerBroadcastReceiver");
        }
        if (z4) {
            e(context, "OpenLoginBroadcastReceiver");
        }
        if (z5) {
            e(context, "BindSafePhoneBroadcastReceiver");
        }
    }

    private static synchronized boolean e(Context context, String str) {
        boolean z = false;
        synchronized (a.class) {
            if (c != null) {
                if (c.containsKey(str)) {
                    ArrayList arrayList = (ArrayList) c.get(str);
                    if (!(arrayList == null || arrayList.isEmpty())) {
                        Collection arrayList2 = new ArrayList();
                        Iterator it = arrayList.iterator();
                        while (it.hasNext()) {
                            BroadcastReceiver broadcastReceiver = (BroadcastReceiver) it.next();
                            try {
                                context.unregisterReceiver(broadcastReceiver);
                                arrayList2.add(broadcastReceiver);
                                z = true;
                                com.huawei.hwid.core.c.b.a.b("CloudAccountImpl", "remove " + str + " success!");
                            } catch (Exception e) {
                                com.huawei.hwid.core.c.b.a.c("CloudAccountImpl", e.getMessage());
                            }
                        }
                        if (!arrayList2.isEmpty()) {
                            arrayList.removeAll(arrayList2);
                        }
                        c.put(str, arrayList);
                    }
                }
            }
        }
        return z;
    }

    public static void a(BroadcastReceiver broadcastReceiver, String str) {
        ArrayList arrayList = (ArrayList) c.get(str);
        if (arrayList == null) {
            arrayList = new ArrayList();
        }
        arrayList.add(broadcastReceiver);
        c.put(str, arrayList);
    }
}
