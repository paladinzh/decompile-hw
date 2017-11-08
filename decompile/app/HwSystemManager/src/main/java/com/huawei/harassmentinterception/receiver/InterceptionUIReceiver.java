package com.huawei.harassmentinterception.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.text.TextUtils;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.engine.HwEngineCaller;
import com.huawei.harassmentinterception.engine.HwEngineCallerManager;
import com.huawei.harassmentinterception.update.UpdateService;
import com.huawei.harassmentinterception.util.PreferenceHelper;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class InterceptionUIReceiver extends HsmBroadcastReceiver {
    private static final String TAG = InterceptionUIReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                HwLog.w(TAG, "onReceive : Invalid  action");
                return;
            }
            HwLog.i(TAG, "onReceive: Action = " + action);
            sendToBackground(context, intent);
        }
    }

    public void doInBackground(Context context, Intent intent) {
        String action = intent.getAction();
        if (ConstValues.ACTION_INTERCEPT_ENGINE_OPEN.equals(action)) {
            handleSwitchOn();
        } else if (ConstValues.ACTION_INTERCEPT_ENGINE_CLOSE.equals(action)) {
            handleSwitchOut();
        } else if (ConstValues.ACTION_ALARM_AUTO_UPDATE.equals(action) && (!intent.getBooleanExtra("android.net.conn.CONNECTIVITY_CHANGE", false) || !PreferenceHelper.isSuccessAutoUpdateInTime(context))) {
            handleAlarmAction(context);
        }
    }

    private void handleSwitchOn() {
        HwEngineCaller caller = HwEngineCallerManager.getInstance().getEngineCaller();
        if (caller == null) {
            caller = new HwEngineCaller(GlobalContext.getContext());
            HwEngineCallerManager.getInstance().setEngineCaller(caller);
        }
        caller.onSwitchIn(0);
    }

    private void handleSwitchOut() {
        HwEngineCaller caller = HwEngineCallerManager.getInstance().getEngineCaller();
        if (caller != null) {
            caller.onSwitchOut(0);
            HwEngineCallerManager.getInstance().setEngineCaller(null);
        }
    }

    private void handleAlarmAction(Context context) {
        if (CustomizeWrapper.shouldEnableIntelligentEngine()) {
            Intent serviceIntent = new Intent(context, UpdateService.class);
            serviceIntent.putExtra(ConstValues.KEY_AUTOUPDATE_FLAG, true);
            context.startServiceAsUser(serviceIntent, UserHandle.OWNER);
            return;
        }
        HwLog.i(TAG, "onReceive: Intelligent engine is not supported on current version");
    }
}
