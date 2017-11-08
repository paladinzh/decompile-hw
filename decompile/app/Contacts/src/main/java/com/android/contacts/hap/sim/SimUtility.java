package com.android.contacts.hap.sim;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import com.android.contacts.util.HwLog;

public final class SimUtility {
    private SimUtility() {
    }

    public static boolean isSimStateLoaded(int aSlotId, Context aContext) {
        boolean lIsSimStateLoaded = false;
        if (aContext != null && isSimReady(aSlotId)) {
            lIsSimStateLoaded = aContext.registerReceiver(null, new IntentFilter(getIntentActionForStickyBroadcast(aSlotId))) != null;
        }
        if (HwLog.HWDBG) {
            HwLog.d("SimUtility", "lIsSimStateLoaded :: " + lIsSimStateLoaded);
        }
        return lIsSimStateLoaded;
    }

    public static boolean isSimReady(int slotId) {
        boolean lSimReady = SimFactoryManager.getSimState(slotId) == 5;
        if (HwLog.HWDBG) {
            HwLog.d("SimUtility", "Sim State : " + lSimReady);
        }
        return lSimReady;
    }

    public static boolean isSimInBusyState(Context aContext, String aAccountType) {
        SharedPreferences prefs = SimFactoryManager.getSharedPreferences("SimInfoFile", SimFactoryManager.getSlotIdBasedOnAccountType(aAccountType));
        return !prefs.getBoolean("sim_delete_progress", false) ? prefs.getBoolean("sim_copy_contacts_progress", false) : true;
    }

    public static Intent getIntentForStickyBroadcast(int aSlotId) {
        if (HwLog.HWDBG) {
            HwLog.d("SimUtility", "getIntentForStickyBroadcast aSlotId :: " + aSlotId);
        }
        return new Intent(getIntentActionForStickyBroadcast(aSlotId));
    }

    private static String getIntentActionForStickyBroadcast(int aSlotId) {
        if (HwLog.HWDBG) {
            HwLog.d("SimUtility", "getIntentActionForStickyBroadcast aSlotId :: " + aSlotId);
        }
        String lIntentAction = "com.android.huawei.First_Sim_Intent_Loaded";
        if (1 == aSlotId) {
            lIntentAction = "com.android.huawei.Second_Sim_Intent_Loaded";
        }
        if (HwLog.HWDBG) {
            HwLog.d("SimUtility", "lIntentAction :: " + lIntentAction);
        }
        return lIntentAction;
    }

    public static Intent getSimHdlingIntentForStickyBroadcast(int aSlotId) {
        if (HwLog.HWDBG) {
            HwLog.d("SimUtility", "getSimHdlingIntentForStickyBroadcast aSlotId :: " + aSlotId);
        }
        return new Intent(getSimIntentActionForStickyBroadcast(aSlotId));
    }

    private static String getSimIntentActionForStickyBroadcast(int aSlotId) {
        if (HwLog.HWDBG) {
            HwLog.d("SimUtility", "getSimIntentActionForStickyBroadcast aSlotId : " + aSlotId);
        }
        String lIntentAction = "com.android.huawei.First_Sim_Handling_Started";
        if (1 == aSlotId) {
            lIntentAction = "com.android.huawei.Second_Sim_Handling_Started";
        }
        if (HwLog.HWDBG) {
            HwLog.d("SimUtility", "getSimIntentActionForStickyBroadcast IntentAction : " + lIntentAction);
        }
        return lIntentAction;
    }

    public static boolean isSimHandlingStartedLoaded(int aSlotId, Context aContext) {
        boolean lIsSimHandlingStarted = aContext.registerReceiver(null, new IntentFilter(getSimIntentActionForStickyBroadcast(aSlotId))) != null;
        if (HwLog.HWDBG) {
            HwLog.d("SimUtility", "isSimHandlingStartedLoaded for : " + aSlotId + " is " + lIsSimHandlingStarted);
        }
        return lIsSimHandlingStarted;
    }
}
