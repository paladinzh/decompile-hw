package com.android.contacts.calllog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.sim.SimBootUpService;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimUtility;
import com.android.contacts.util.HwLog;

public class CallLogReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if ("android.intent.action.NEW_VOICEMAIL".equals(intent.getAction())) {
                Intent serviceIntent = new Intent(context, CallLogNotificationsService.class);
                serviceIntent.setAction("com.android.dialer.calllog.UPDATE_VOICEMAIL_NOTIFICATIONS");
                serviceIntent.putExtra("NEW_VOICEMAIL_URI", intent.getData());
                context.startService(serviceIntent);
            } else if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                if (SimFactoryManager.isDualSim()) {
                    if (!SimUtility.isSimHandlingStartedLoaded(0, context)) {
                        SimFactoryManager.clearSimStatePreferences(0);
                        HwLog.i("CallLogReceiver", "Preference Remove SIM_STATE_VALUE called for first sim slot");
                    }
                    if (!SimUtility.isSimHandlingStartedLoaded(1, context)) {
                        SimFactoryManager.clearSimStatePreferences(1);
                        HwLog.i("CallLogReceiver", "Preference Remove SIM_STATE_VALUE called for second sim slot");
                    }
                } else if (!SimUtility.isSimHandlingStartedLoaded(-1, context)) {
                    SimFactoryManager.clearSimStatePreferences(-1);
                    HwLog.i("CallLogReceiver", "Preference Remove SIM_STATE_VALUE called for single sim");
                }
                if (EmuiFeatureManager.isPreLoadingSimContactsEnabled()) {
                    HwLog.i("CallLogReceiver", "not start SimBootUpService any more");
                } else {
                    Intent simServiceIntent = new Intent(context, SimBootUpService.class);
                    simServiceIntent.setAction("phone_boot_up");
                    context.startService(simServiceIntent);
                }
            } else {
                HwLog.w("CallLogReceiver", "onReceive: could not handle: " + intent);
            }
        }
    }
}
