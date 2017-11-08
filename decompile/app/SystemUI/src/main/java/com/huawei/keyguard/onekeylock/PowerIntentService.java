package com.huawei.keyguard.onekeylock;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;

public class PowerIntentService extends IntentService {
    public PowerIntentService() {
        super("PowerIntentService");
    }

    protected void onHandleIntent(Intent intent) {
        if (!(ActivityManager.isUserAMonkey() || intent == null || !"com.huawei.keyguard.onekeylock.POWER_OFF".equals(intent.getAction()))) {
            ((PowerManager) getApplicationContext().getSystemService("power")).goToSleep(SystemClock.uptimeMillis());
        }
    }
}
