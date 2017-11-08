package com.huawei.hwid.api.common.b;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.cloudservice.CloudAccount;
import com.huawei.cloudservice.LoginHandler;
import com.huawei.hwid.core.c.b;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.manager.AccountManagerActivity;
import com.huawei.hwid.ui.common.login.LoginActivity;
import com.huawei.hwid.ui.common.login.StartUpGuideLoginForAPPActivity;
import java.util.List;

/* compiled from: SDKCloudAccountImpl */
public class a {
    public static void a(Context context, String str, boolean z, boolean z2, String str2, List list, LoginHandler loginHandler, Bundle bundle) {
        com.huawei.hwid.core.a.a.a(str);
        com.huawei.hwid.api.common.a.a(context, loginHandler, null);
        b.a(context, str, str2, bundle);
        if (list == null || list.isEmpty()) {
            a(context, str, z, bundle);
            return;
        }
        com.huawei.hwid.core.c.b.a.e("SDKCloudAccountImpl", "getAccountsByType, size:" + list.size() + ", curName:" + f.c(str2));
        a(context, str, z, z2, str2, loginHandler);
    }

    private static void a(Context context, String str, boolean z, boolean z2, String str2, LoginHandler loginHandler) {
        boolean c = b.c(context, str);
        Intent intent = new Intent();
        intent.putExtra("popLogin", z);
        intent.putExtra("requestTokenType", str);
        intent.putExtra("accountName", com.huawei.hwid.api.common.a.d(context));
        intent.setClass(context, AccountManagerActivity.class);
        if (z2 || c) {
            d.a(context, intent, 0);
            return;
        }
        CloudAccount[] a = com.huawei.hwid.api.common.a.a(context);
        loginHandler.onLogin(a, com.huawei.hwid.api.common.a.a(a, str2));
        Bundle bundle = new Bundle();
        bundle.putBoolean("LoginBroadcastReceiver", true);
        com.huawei.hwid.api.common.a.a(context, bundle);
    }

    private static void a(Context context, String str, boolean z, Bundle bundle) {
        Intent intent = new Intent();
        if (bundle != null) {
            intent.putExtra("bundle", bundle);
        }
        intent.putExtra("requestTokenType", str);
        intent.putExtra("popLogin", z);
        if (d.j(context)) {
            intent.setClass(context, StartUpGuideLoginForAPPActivity.class);
            d.a(context, intent, 0);
            return;
        }
        intent.setClass(context, LoginActivity.class);
        d.a(context, intent, 1048576);
    }

    public static boolean a(Context context) {
        List a = com.huawei.hwid.manager.f.a(context).a(context, d.d(context));
        if (a == null || a.isEmpty()) {
            return false;
        }
        return true;
    }
}
