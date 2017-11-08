package com.huawei.hwid.c;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.c.b.c;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.j;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.datatype.n;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.model.http.request.k;
import com.huawei.hwid.manager.e;

/* compiled from: VipCommonUtils */
public class a {
    public static void a(Context context) {
        if (!c(context)) {
            return;
        }
        if (TextUtils.isEmpty(com.huawei.hwid.c.a.a.a(context, "deviceVipUserId"))) {
            com.huawei.hwid.c.b.a.a(context.getApplicationContext(), null);
        } else {
            com.huawei.hwid.core.c.b.a.a("VipCommonUtils", "aready query Success, not need query more!");
        }
    }

    public static void a(Context context, String str, String str2, CloudRequestHandler cloudRequestHandler) {
        if (!c(context)) {
            return;
        }
        if (str == null || str.length() == 0) {
            com.huawei.hwid.core.c.b.a.d("VipCommonUtils", "in startGetUserInfoRequest userId is empty");
            if (cloudRequestHandler != null) {
                cloudRequestHandler.onError(new ErrorStatus(13, "in requestCurUserVIPRightIfNeed, userId is empty"));
            }
            return;
        }
        c.a(context.getApplicationContext(), str, str2, cloudRequestHandler);
    }

    public static boolean b(Context context) {
        boolean z = false;
        if (!c(context)) {
            return false;
        }
        CharSequence b = com.huawei.hwid.c.a.a.b(context);
        if (TextUtils.isEmpty(b) || ThemeUtil.SET_NULL_STR.equals(b)) {
            z = true;
        }
        return z;
    }

    public static void a(Context context, Bundle bundle) {
        if (!c(context)) {
            return;
        }
        if (bundle != null) {
            com.huawei.hwid.core.c.b.a.a("VipCommonUtils", "enter deposeVipWhenSuccess bundle:" + f.a(bundle));
            String string = bundle.getString("userId");
            String string2 = bundle.getString("userName");
            int i = bundle.getInt("rightsID");
            String string3 = bundle.getString("vipExpiredDate");
            HwAccount a = e.a(context);
            if (string == null || a == null || !string.equals(a.c())) {
                string = "";
                if (a != null) {
                    string = a.a();
                }
                com.huawei.hwid.core.c.b.a.b("VipCommonUtils", "cur login request User " + f.c(string2) + " is not logined user:" + f.c(string));
                return;
            }
            com.huawei.hwid.c.a.a.a(context);
            com.huawei.hwid.c.a.a.a(context, string, i, string3);
            CloudRequestHandler bVar = new b();
            if (i < 1) {
                if (bundle.getBoolean("isVipRequest", false)) {
                    q.a(context, string, "131");
                }
                a(context.getApplicationContext());
                c.a(context.getApplicationContext(), string, string2, bVar);
                bVar.a();
                return;
            }
            if (b(context)) {
                com.huawei.hwid.c.b.a.a(context.getApplicationContext(), null);
            }
            k.a(context.getApplicationContext(), i, bVar);
            bVar.a();
            com.huawei.hwid.core.c.b.a.b("VipCommonUtils", "vipQueryWaithandler.waitForLock() ");
            return;
        }
        com.huawei.hwid.core.c.b.a.b("VipCommonUtils", "when call deposeVipWhenLoginSuccess bundle is null");
    }

    public static void b(Context context, Bundle bundle) {
        if (c(context)) {
            HwAccount a = e.a(context);
            String str = "";
            if (!(a == null || a.c() == null)) {
                str = a.c();
            }
            CharSequence c = com.huawei.hwid.c.a.a.c(context);
            if (a != null) {
                if (str.equals(c)) {
                    com.huawei.hwid.core.c.b.a.a("VipCommonUtils", "login account equal with vip database, need not do anything");
                } else {
                    com.huawei.hwid.core.c.b.a.a("VipCommonUtils", "account is not equal current vip User, maybe some error!");
                    com.huawei.hwid.c.a.a.a(context);
                    if (a.a() == null) {
                        com.huawei.hwid.core.c.b.a.a("VipCommonUtils", "account name is empty, cannot send requestCurUserVIPRightIfNeed");
                    } else {
                        a(context, str, a.a(), null);
                    }
                }
            } else if (!TextUtils.isEmpty(c)) {
                com.huawei.hwid.core.c.b.a.a("VipCommonUtils", "account is logout, clear vip database");
                com.huawei.hwid.c.a.a.a(context);
            }
        }
    }

    public static boolean c(Context context) {
        if (context == null) {
            com.huawei.hwid.core.c.b.a.b("VipCommonUtils", "when call isNeedDeposeVip context is null!!");
            return false;
        } else if (!"com.huawei.hwid".equals(context.getPackageName())) {
            com.huawei.hwid.core.c.b.a.b("VipCommonUtils", "only hwid need depose vip");
            return false;
        } else if (n.g() || a()) {
            HwAccount a = e.a(context);
            if (a == null) {
                com.huawei.hwid.core.c.b.a.a("VipCommonUtils", " no loging account");
            } else if (d.f(a.g())) {
                com.huawei.hwid.core.c.b.a.b("VipCommonUtils", "third account not support vip!");
                return false;
            }
            return true;
        } else {
            com.huawei.hwid.core.c.b.a.b("VipCommonUtils", "device config is not support vip");
            return false;
        }
    }

    public static boolean a() {
        String str = "";
        String str2 = "";
        String str3;
        try {
            Object a = j.a("android.os.SystemProperties", "get", new Class[]{String.class}, new Object[]{"ro.product.locale.language"});
            Object a2 = j.a("android.os.SystemProperties", "get", new Class[]{String.class}, new Object[]{"ro.product.locale.region"});
            if (a != null) {
                str = (String) a;
            }
            if (a2 == null) {
                str3 = str2;
            } else {
                str3 = (String) a2;
            }
        } catch (Exception e) {
            String str4 = str;
            com.huawei.hwid.core.c.b.a.c("VipCommonUtils", e.getMessage());
            str3 = str2;
            str = str4;
        }
        if ("zh".equalsIgnoreCase(str) && "cn".equalsIgnoreCase(r1)) {
            return true;
        }
        return false;
    }
}
