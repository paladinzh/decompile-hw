package com.android.settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.util.Jlog;
import android.util.Log;
import java.util.Objects;

public class FallbackHome extends Activity {
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            FallbackHome.this.maybeFinish();
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            FallbackHome.this.maybeFinish();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Jlog.d(82, "com.android.settings", "");
        if (Global.getInt(getContentResolver(), "device_provisioned", 0) == 0) {
            setTheme(16973834);
        }
        tryToDisableStartupWizard();
        registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.USER_UNLOCKED"));
        maybeFinish();
    }

    private void tryToDisableStartupWizard() {
        try {
            if (isInternal() || !isSUWEnabled()) {
                disableOverseaComponent();
            } else {
                disableInternalComponent();
            }
        } catch (Exception e) {
            Log.w("FallbackHome", "disable StartupWizard Component failed");
        }
    }

    private void disableInternalComponent() {
        if (isComponentEnabled("com.huawei.hwstartupguide", "com.huawei.hwstartupguide.LanguageSelectActivity")) {
            disableComponent("com.huawei.hwstartupguide", "com.huawei.hwstartupguide.LanguageSelectActivity");
        }
    }

    private void disableOverseaComponent() {
        if (isComponentEnabled("com.huawei.hwstartupguide", "com.huawei.hwstartupguide.LanguageSelectActivity")) {
            disableComponent("com.huawei.hwstartupguide", "com.huawei.hwstartupguide.WizardActivity");
            disableComponent("com.huawei.hwstartupguide", "com.huawei.hwstartupguide.PartnerReceiver");
        }
    }

    private void disableComponent(String packageName, String name) {
        getApplicationContext().getPackageManager().setComponentEnabledSetting(new ComponentName(packageName, name), 2, 0);
    }

    private boolean isComponentEnabled(String packageName, String name) {
        return 2 != getApplicationContext().getPackageManager().getComponentEnabledSetting(new ComponentName(packageName, name));
    }

    private boolean isSUWEnabled() {
        return "OPTIONAL".equalsIgnoreCase(SystemProperties.get("ro.setupwizard.mode"));
    }

    private boolean isInternal() {
        return "zh".equals(SystemProperties.get("ro.product.locale.language")) ? "CN".equals(SystemProperties.get("ro.product.locale.region")) : false;
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.mReceiver);
        Jlog.d(81, "com.android.settings", "");
    }

    private void maybeFinish() {
        if (((UserManager) getSystemService(UserManager.class)).isUserUnlocked()) {
            Intent homeIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME");
            ResolveInfo homeInfo = getPackageManager().resolveActivity(homeIntent, 0);
            if (Objects.equals(getPackageName(), homeInfo.activityInfo.packageName)) {
                Log.d("FallbackHome", "User unlocked but no home; let's hope someone enables one soon?");
                this.mHandler.sendEmptyMessageDelayed(0, 500);
                return;
            }
            Log.d("FallbackHome", "User unlocked and real home found; let's go!");
            homeIntent.addFlags(270532608);
            homeIntent.setPackage(homeInfo.activityInfo.packageName);
            try {
                startActivity(homeIntent);
            } catch (Exception ex) {
                Log.e("FallbackHome", "maybeFinish()-->ex : " + ex);
            }
            finish();
        }
    }
}
