package com.android.settings.sim;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.support.v4.app.NotificationCompat.Builder;
import android.telephony.HwTelephonyManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.settings.SecuritySettings;
import com.android.settings.Settings.SimSettingsActivity;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.WirelessSettings;
import com.android.settings.search.Index;
import java.util.List;

public class SimSelectNotification extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
        int numSlots = telephonyManager.getSimCount();
        if (numSlots >= 2 && Utils.isDeviceProvisioned(context)) {
            boolean z;
            cancelNotification(context);
            String simStatus = intent.getStringExtra("ss");
            if ("ABSENT".equals(simStatus)) {
                z = true;
            } else {
                z = "LOADED".equals(simStatus);
            }
            if (z) {
                Log.d("SimSelectNotification", "simstatus = " + simStatus);
                handleSimStateChanged(context, simStatus);
                int i = 0;
                while (i < numSlots) {
                    int state = telephonyManager.getSimState(i);
                    if (state == 1 || state == 5 || state == 0) {
                        i++;
                    } else {
                        Log.d("SimSelectNotification", "All sims not in valid state yet");
                        return;
                    }
                }
                List<SubscriptionInfo> sil = subscriptionManager.getActiveSubscriptionInfoList();
                if (sil == null || sil.size() < 1) {
                    Log.d("SimSelectNotification", "Subscription list is empty");
                    return;
                }
                if (HwTelephonyManager.getDefault().isPlatformSupportVsim()) {
                    String INTENT_KEY_VSIM = "vsim";
                    String INTENT_VALUE_VSIM_RELOAD = "VSIM_RELOAD";
                    if ("VSIM_RELOAD".equals(intent.getStringExtra("vsim"))) {
                        Log.d("SimSelectNotification", "sim state changed by vsim reload");
                        return;
                    }
                }
                subscriptionManager.clearDefaultsForInactiveSubIds();
                boolean dataSelected = SubscriptionManager.isUsableSubIdValue(SubscriptionManager.getDefaultDataSubscriptionId());
                boolean smsSelected = SubscriptionManager.isUsableSubIdValue(SubscriptionManager.getDefaultSmsSubscriptionId());
                if (dataSelected && smsSelected) {
                    Log.d("SimSelectNotification", "Data & SMS default sims are selected. No notification");
                    return;
                } else {
                    createNotification(context);
                    return;
                }
            }
            Log.d("SimSelectNotification", "sim state is not Absent or Loaded");
        }
    }

    private void createNotification(Context context) {
        Resources resources = context.getResources();
        Builder builder = new Builder(context).setSmallIcon(2130838437).setColor(context.getColor(2131427459)).setContentTitle(resources.getString(2131626631)).setContentText(resources.getString(2131626632));
        Intent resultIntent = new Intent(context, SimSettingsActivity.class);
        resultIntent.addFlags(268435456);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, resultIntent, 268435456));
        ((NotificationManager) context.getSystemService("notification")).notify(1, builder.build());
    }

    public static void cancelNotification(Context context) {
        ((NotificationManager) context.getSystemService("notification")).cancel(1);
    }

    private void handleSimStateChanged(Context context, String simStatus) {
        boolean z;
        if (Utils.isChinaTelecomArea() || Utils.isWifiOnly(context) || !Utils.hasPackageInfo(context.getPackageManager(), "com.android.phone")) {
            z = true;
        } else {
            z = SystemProperties.getBoolean("ro.config.hw_hide_lte", false);
        }
        if (!z) {
            Index.getInstance(context).updateFromClassNameResource(WirelessSettings.class.getName(), true, true);
        }
        if ("ABSENT".equals(simStatus)) {
            boolean isChineseVersion = !SettingsExtUtils.isGlobalVersion();
            boolean supportCallEncryption = SystemProperties.getBoolean("persist.sys.cdma_encryption", false);
            if (isChineseVersion && supportCallEncryption) {
                Index.getInstance(context).updateFromClassNameResource(SecuritySettings.class.getName(), true, true);
            }
        }
    }
}
