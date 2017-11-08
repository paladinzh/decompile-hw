package com.huawei.hwid.core.d;

import android.content.Context;
import android.content.Intent;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.encrypt.f;

public class c {
    public static void a(Context context, Intent intent) {
        if (intent != null && context != null) {
            intent.setAction("com.huawei.cloudserive.loginSuccess");
            e.e("BroadcastUtil", "sendLoginSuccessBroadcast-->context = " + context.getClass().getName() + ", intent = " + f.a(intent));
            context.sendBroadcast(intent);
        }
    }

    public static void b(Context context, Intent intent) {
        if (intent != null && context != null) {
            intent.setAction("com.huawei.cloudserive.loginFailed");
            e.e("BroadcastUtil", "sendLoginFailedBroadcast-->context = " + context.getClass().getName() + ", intent = " + f.a(intent));
            context.sendBroadcast(intent);
        }
    }
}
