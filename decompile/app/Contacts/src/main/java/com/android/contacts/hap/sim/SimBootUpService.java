package com.android.contacts.hap.sim;

import android.content.Context;
import android.content.Intent;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.util.IntentServiceWithWakeLock;
import com.android.contacts.util.HwLog;

public class SimBootUpService extends IntentServiceWithWakeLock {
    public SimBootUpService() {
        super("SimBootUpService");
        setIntentRedelivery(true);
    }

    protected void doWakefulWork(Intent intent) {
        if (!"phone_boot_up".equals(intent.getAction())) {
            return;
        }
        if (EmuiFeatureManager.isPreLoadingSimContactsEnabled()) {
            if (HwLog.HWFLOW) {
                HwLog.i("SimBootUpService", "not hide the SIM contacts any more");
            }
            return;
        }
        Context context = getApplicationContext();
        if (SimFactoryManager.isDualSim()) {
            if (needToHideSimContacts(0)) {
                if (HwLog.HWDBG) {
                    HwLog.d("SimBootUpService", "mark as deleted for First Slot");
                }
                SimDatabaseHelper.markSimContactsAsDeleted(context, "com.android.huawei.sim");
            }
            if (needToHideSimContacts(1)) {
                if (HwLog.HWDBG) {
                    HwLog.d("SimBootUpService", "mark as deleted for Second slot");
                }
                SimDatabaseHelper.markSimContactsAsDeleted(context, "com.android.huawei.secondsim");
            }
        } else if (needToHideSimContacts(-1)) {
            if (HwLog.HWDBG) {
                HwLog.d("SimBootUpService", "mark as deleted for Single Sim");
            }
            SimDatabaseHelper.markSimContactsAsDeleted(context, "com.android.huawei.sim");
        }
    }

    private boolean needToHideSimContacts(int aSlotId) {
        String simState = "";
        simState = SimFactoryManager.getSharedPreferences("SimInfoFile", aSlotId).getString("sim_state_value", "");
        if (!SimUtility.isSimReady(aSlotId)) {
            return true;
        }
        if ("LOADED".equals(simState) || "NOT_READY".equals(simState) || "LOCKED".equals(simState)) {
            return false;
        }
        return true;
    }
}
