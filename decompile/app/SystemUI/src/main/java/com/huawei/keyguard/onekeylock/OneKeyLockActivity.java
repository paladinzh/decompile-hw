package com.huawei.keyguard.onekeylock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import fyusion.vislib.BuildConfig;

public class OneKeyLockActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!"factory".equalsIgnoreCase(SystemProperties.get("ro.runmode"))) {
            Intent intent = new Intent(this, PowerIntentService.class);
            intent.setAction("com.huawei.keyguard.onekeylock.POWER_OFF");
            startService(intent);
            HwLockScreenReporter.report(this, 172, BuildConfig.FLAVOR);
            finish();
        }
    }
}
