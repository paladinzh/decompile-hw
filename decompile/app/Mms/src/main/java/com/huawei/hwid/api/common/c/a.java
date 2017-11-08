package com.huawei.hwid.api.common.c;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.cloudservice.LoginHandler;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.c.i;
import com.huawei.hwid.core.encrypt.f;

/* compiled from: SDKUtil */
public class a {
    private static LoginHandler a;
    private static c b;
    private static boolean c = true;
    private static String d;

    public static synchronized void a(LoginHandler loginHandler) {
        synchronized (a.class) {
            a = loginHandler;
            com.huawei.hwid.core.c.b.a.b("SDKUtil", "setHandler, mHandler is " + a);
        }
    }

    public static LoginHandler a() {
        com.huawei.hwid.core.c.b.a.b("SDKUtil", "getHandler, mHandler is " + a);
        return a;
    }

    public static synchronized void a(c cVar) {
        synchronized (a.class) {
            b = cVar;
            com.huawei.hwid.core.c.b.a.b("SDKUtil", "setOpLogItem");
        }
    }

    public static c b() {
        com.huawei.hwid.core.c.b.a.b("SDKUtil", "getOpLogItem ");
        return b;
    }

    public static synchronized void a(boolean z) {
        synchronized (a.class) {
            c = z;
            com.huawei.hwid.core.c.b.a.b("SDKUtil", "setNeedInit, mNeedInit is " + c);
        }
    }

    public static boolean c() {
        com.huawei.hwid.core.c.b.a.b("SDKUtil", "isNeedInit, mNeedInit is " + c);
        return c;
    }

    public static synchronized void a(Context context, String str) {
        synchronized (a.class) {
            d = str;
            i.a(context, "curName", d);
            com.huawei.hwid.core.c.b.a.b("SDKUtil", "setCurrentLoginUserName, mCurrentLoginUserName is " + f.c(d));
        }
    }

    public static String a(Context context) {
        if (TextUtils.isEmpty(d)) {
            a(context, i.b(context, "curName"));
        }
        com.huawei.hwid.core.c.b.a.b("SDKUtil", "getCurrentLoginUserName, mCurrentLoginUserName is " + f.c(d));
        return d;
    }
}
