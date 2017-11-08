package com.huawei.hwid.core.c;

import android.content.Context;
import android.content.Intent;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.encrypt.f;

/* compiled from: BroadcastUtil */
public class e {
    public static void a(Context context, Intent intent) {
        if (intent != null && context != null) {
            intent.setAction("com.huawei.cloudserive.loginSuccess");
            a.e("BroadcastUtil", "sendLoginSuccessBroadcast-->context = " + context.getClass().getName() + ", intent = " + f.a(intent));
            context.sendBroadcast(intent);
        }
    }

    public static void a(Context context) {
        Intent intent = new Intent();
        intent.setFlags(32);
        intent.setAction("com.huawei.hwid.loginSuccess.anonymous");
        a.e("BroadcastUtil", "sendLoginAnonymousSuccessBroadcast-->context = " + context.getClass().getName() + ", intent = " + f.a(intent));
        context.sendBroadcast(intent);
    }

    public static void b(Context context, Intent intent) {
        if (intent != null && context != null) {
            intent.setAction("com.huawei.cloudserive.loginCancel");
            a.e("BroadcastUtil", "sendLoginCancelBroadcast-->context = " + context.getClass().getName() + ", intent = " + f.a(intent));
            context.sendBroadcast(intent);
        }
    }

    public static void c(Context context, Intent intent) {
        if (intent != null && context != null) {
            intent.setAction("com.huawei.cloudserive.loginFailed");
            a.e("BroadcastUtil", "sendLoginFailedBroadcast-->context = " + context.getClass().getName() + ", intent = " + f.a(intent));
            context.sendBroadcast(intent);
        }
    }

    public static void a(Context context, String str, String str2) {
        Intent intent = new Intent();
        intent.setFlags(32);
        intent.putExtra("userId", str);
        intent.putExtra("removedAccountName", str2);
        intent.setAction("com.huawei.hwid.ACTION_REMOVE_ACCOUNT");
        a.e("BroadcastUtil", "sendAccountRemoveBroadcast-->context = " + context.getClass().getName() + ", intent = " + f.a(intent));
        context.sendBroadcast(intent, "com.huawei.hwid.permission.ACCESS");
    }

    public static void d(Context context, Intent intent) {
        if (intent != null && context != null) {
            intent.setAction("com.huawei.cloudserive.fingerCancel");
            a.e("BroadcastUtil", "sendFingerCancelBroadcast-->context = " + context.getClass().getName() + ", intent = " + f.a(intent));
            context.sendBroadcast(intent);
        }
    }

    public static void e(Context context, Intent intent) {
        if (intent != null && context != null) {
            intent.setAction("com.huawei.cloudserive.fingerSuccess");
            a.e("BroadcastUtil", "sendFingerSuccessBroadcast-->context = " + context.getClass().getName() + ", intent = " + f.a(intent));
            context.sendBroadcast(intent);
        }
    }
}
