package com.avast.android.sdk.engine.obfuscated;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.avast.android.sdk.engine.EngineConfig;
import com.avast.android.sdk.internal.c;
import com.avast.android.sdk.internal.c.a;
import com.avast.android.sdk.internal.h;
import com.huawei.permissionmanager.utils.ShareCfg;

/* compiled from: Unknown */
public class bg {
    public static void a(Context context, EngineConfig engineConfig) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        Intent intent = new Intent();
        intent.setComponent(c.a(a.VPS_UPDATE_SERVICE));
        PendingIntent service = PendingIntent.getService(context.getApplicationContext(), 667788, intent, ShareCfg.PERMISSION_MODIFY_CALENDAR);
        if (engineConfig.isAutomaticUpdateEnabled()) {
            alarmManager.set(1, h.a(context.getApplicationContext()).a() + 14400000, service);
            return;
        }
        alarmManager.cancel(service);
        service.cancel();
    }
}
