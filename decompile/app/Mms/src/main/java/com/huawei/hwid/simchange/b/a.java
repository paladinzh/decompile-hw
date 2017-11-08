package com.huawei.hwid.simchange.b;

import android.content.Context;
import android.content.Intent;
import com.huawei.hwid.core.encrypt.f;

/* compiled from: SimChangeBroadcastUtil */
public class a {
    public static void b(Context context, String str) {
        Intent intent = new Intent("com.huawei.hwid.ACTION_ACCOUNT_UNLOCK");
        intent.putExtra("userId", str);
        com.huawei.hwid.core.c.b.a.b("SimChangeBroadcastUtil", "sendAccountUnBlockedBroadcast-->context = " + context.getClass().getName() + ", intent = " + f.a(intent));
        context.sendBroadcast(intent);
    }
}
