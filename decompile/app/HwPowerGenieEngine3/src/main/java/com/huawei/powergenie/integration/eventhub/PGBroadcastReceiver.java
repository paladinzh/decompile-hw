package com.huawei.powergenie.integration.eventhub;

import android.content.Context;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.util.Log;
import java.util.HashMap;
import java.util.Map.Entry;

public final class PGBroadcastReceiver extends BaseBroadcastReceiver {
    private static BaseBroadcastReceiver mBaseBroadcastReceiver;
    private static final HashMap<String, Integer> mMsgActionsIds = new HashMap<String, Integer>() {
        {
            put("huawei.intent.action.POWER_MODE_CHANGED_ACTION", Integer.valueOf(350));
            put("huawei.intent.action.PG_EXTREME_MODE_ENABLE_ACTION", Integer.valueOf(361));
            put("huawei.intent.action.PG_PENDING_ALARM_ACTION", Integer.valueOf(356));
            put("android.scroff.ctrlsocket.status", Integer.valueOf(357));
            put("huawei.intent.action.PG_UPDATE_CONFIG_ACTION", Integer.valueOf(358));
            put("com.huawei.intent.action.QUERY_BT_ACTIVE_APPS_RESULT", Integer.valueOf(360));
            put("huawei.intent.action.USER_DEVICE_STATE_CHANGED", Integer.valueOf(362));
            put("com.android.vrservice.glass", Integer.valueOf(363));
            put("huawei.intent.action.BATTERY_QUICK_CHARGE", Integer.valueOf(364));
            put("android.ctrlsocket.all.allowed", Integer.valueOf(365));
        }
    };

    protected Integer getMsgId(String action) {
        if (action != null) {
            return (Integer) mMsgActionsIds.get(action);
        }
        return null;
    }

    protected static void init(Context context) {
        if (mBaseBroadcastReceiver != null) {
            Log.i("MsgHub", "PG Receiver has been inited.");
            return;
        }
        IntentFilter filter = new IntentFilter();
        for (Entry entry : mMsgActionsIds.entrySet()) {
            filter.addAction((String) entry.getKey());
        }
        mBaseBroadcastReceiver = new PGBroadcastReceiver();
        context.registerReceiverAsUser(mBaseBroadcastReceiver, UserHandle.ALL, filter, "com.huawei.powergenie.receiverPermission", null);
    }
}
