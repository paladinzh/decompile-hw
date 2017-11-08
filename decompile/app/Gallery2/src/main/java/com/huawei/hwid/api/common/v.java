package com.huawei.hwid.api.common;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.cloudservice.LoginHandler;
import com.huawei.hwid.core.a.b;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.f;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.helper.handler.ErrorStatus;

public class v {
    private static LoginHandler a;
    private static b b;
    private static boolean c = true;
    private static String d;

    public static synchronized void a(LoginHandler loginHandler) {
        synchronized (v.class) {
            a = loginHandler;
            e.b("SDKUtil", "setHandler, mHandler is " + a);
        }
    }

    public static LoginHandler a() {
        e.b("SDKUtil", "getHandler, mHandler is " + a);
        return a;
    }

    public static synchronized void a(b bVar) {
        synchronized (v.class) {
            b = bVar;
            e.b("SDKUtil", "setOpLogItem");
        }
    }

    public static b b() {
        e.b("SDKUtil", "getOpLogItem ");
        return b;
    }

    public static synchronized void a(boolean z) {
        synchronized (v.class) {
            c = z;
            e.b("SDKUtil", "setNeedInit, mNeedInit is " + c);
        }
    }

    public static boolean c() {
        e.b("SDKUtil", "isNeedInit, mNeedInit is " + c);
        return c;
    }

    public static synchronized void a(Context context, String str) {
        synchronized (v.class) {
            d = str;
            f.a(context, "curName", d);
            e.b("SDKUtil", "setCurrentLoginUserName, mCurrentLoginUserName is " + com.huawei.hwid.core.encrypt.f.c(d));
        }
    }

    public static String a(Context context) {
        if (TextUtils.isEmpty(d)) {
            a(context, f.b(context, "curName"));
        }
        e.b("SDKUtil", "getCurrentLoginUserName, mCurrentLoginUserName is " + com.huawei.hwid.core.encrypt.f.c(d));
        return d;
    }

    public static boolean b(Context context) {
        if (context != null) {
            return com.huawei.hwid.core.d.b.k(context);
        }
        e.b("SDKUtil", "context is null");
        return false;
    }

    public static boolean a(Context context, int i) {
        if (context == null) {
            e.b("SDKUtil", "context is null");
            return false;
        } else if (d(context) >= i) {
            return false;
        } else {
            return true;
        }
    }

    static boolean a(Context context, CloudRequestHandler cloudRequestHandler) {
        if (cloudRequestHandler == null) {
            e.b("SDKUtil", "requestHandler is null");
            return false;
        } else if (context != null) {
            return true;
        } else {
            e.b("SDKUtil", "context is null");
            cloudRequestHandler.onError(new ErrorStatus(12, "context is null"));
            return false;
        }
    }

    static HwAccount a(Context context, Bundle bundle) {
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
            String string8 = bundle.getString("loginUserName");
            String string9 = bundle.getString("countryIsoCode");
            hwAccount.b(string);
            hwAccount.h(string3);
            hwAccount.i(string4);
            hwAccount.a(i);
            hwAccount.f(string5);
            hwAccount.d(string2);
            hwAccount.c(com.huawei.hwid.core.d.b.m(context));
            hwAccount.g(string6);
            hwAccount.e(string7);
            hwAccount.j(string8);
            hwAccount.a(string9);
        }
        return hwAccount;
    }

    public static String c(Context context) {
        try {
            String str = context.getPackageManager().getPackageInfo("com.huawei.hwid", 0).versionName;
            e.b("SDKUtil", "versionName " + str);
            return str;
        } catch (NameNotFoundException e) {
            e.d("SDKUtil", "getVersionTag error = " + e.getMessage());
            return "";
        } catch (Exception e2) {
            e.d("SDKUtil", "getVersionTag error" + e2.getMessage());
            return "";
        }
    }

    public static int d(Context context) {
        try {
            int i = context.getPackageManager().getPackageInfo("com.huawei.hwid", 0).versionCode;
            e.b("SDKUtil", "versionCode " + i);
            return i;
        } catch (NameNotFoundException e) {
            e.d("SDKUtil", "getVersionTag error" + e.getMessage());
            return 0;
        } catch (Exception e2) {
            e.d("SDKUtil", "getVersionTag error" + e2.getMessage());
            return 0;
        }
    }
}
