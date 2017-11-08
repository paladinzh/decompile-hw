package com.huawei.systemmanager.power.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.IntentCompat;
import com.google.android.collect.Maps;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.notification.UserNotifier;
import com.huawei.systemmanager.power.receiver.handle.HandleBootCompleted;
import com.huawei.systemmanager.power.receiver.handle.HandleCloudySyncUnifiedPower;
import com.huawei.systemmanager.power.receiver.handle.HandleExitSuperPowerMode;
import com.huawei.systemmanager.power.receiver.handle.HandleExternalAppAvailable;
import com.huawei.systemmanager.power.receiver.handle.HandleNotifierActions;
import com.huawei.systemmanager.power.receiver.handle.HandlePGCleanPowerConsumeApp;
import com.huawei.systemmanager.power.receiver.handle.HandlePGWastePower;
import com.huawei.systemmanager.power.receiver.handle.HandlePackageAdd;
import com.huawei.systemmanager.power.receiver.handle.HandlePackageRemoved;
import com.huawei.systemmanager.power.receiver.handle.HandlePowerDisconnect;
import com.huawei.systemmanager.power.receiver.handle.HandlePowerModeNotifier;
import com.huawei.systemmanager.power.receiver.handle.HandleStartSuperPowerMode;
import com.huawei.systemmanager.power.receiver.handle.IBroadcastHandler;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;
import java.util.Map;
import java.util.Map.Entry;

public class BootBroadcastReceiver extends HsmBroadcastReceiver {
    private static final String ACTION_CLEAN_POWER_CONSUME_APP = "com.huawei.intent.action.PG_CLEAN_POWER_CONSUME_APP";
    private static final String ACTION_WASTE_POWER_APP = "huawei.intent.action.PG_FOUND_WASTE_POWER_APP";
    private static String INSTALL_PERMISSION = "com.huawei.install.permission";
    private static String TAG = BootBroadcastReceiver.class.getSimpleName();
    private static Map<String, IBroadcastHandler> mBroadcastHandlerMap = Maps.newHashMap();

    static {
        mBroadcastHandlerMap.put("android.intent.action.BOOT_COMPLETED", new HandleBootCompleted());
        mBroadcastHandlerMap.put(ACTION_CLEAN_POWER_CONSUME_APP, new HandlePGCleanPowerConsumeApp());
        mBroadcastHandlerMap.put(ActionConst.INTENT_SHUTDOWN_SUPER_POWER_SAVING_MODE, new HandleExitSuperPowerMode());
        mBroadcastHandlerMap.put(ActionConst.HWSYSTEMMANAGER_START_SUPER_POWERMODE, new HandleStartSuperPowerMode());
        mBroadcastHandlerMap.put(ACTION_WASTE_POWER_APP, new HandlePGWastePower());
        mBroadcastHandlerMap.put("android.intent.action.ACTION_POWER_DISCONNECTED", new HandlePowerDisconnect());
        IBroadcastHandler notifyAction = new HandleNotifierActions();
        mBroadcastHandlerMap.put(UserNotifier.ACTION_NOTIFICATION_DELETED, notifyAction);
        mBroadcastHandlerMap.put(UserNotifier.ACTION_NOTIFICATION_NOT_REMIND, notifyAction);
        mBroadcastHandlerMap.put(UserNotifier.ACTION_NOTIFICATION_CLOSE_APP, notifyAction);
        mBroadcastHandlerMap.put("android.intent.action.PACKAGE_ADDED", new HandlePackageAdd());
        mBroadcastHandlerMap.put("android.intent.action.PACKAGE_REMOVED", new HandlePackageRemoved());
        mBroadcastHandlerMap.put(IntentCompat.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE, new HandleExternalAppAvailable());
        mBroadcastHandlerMap.put("com.huawei.systemmanager.action.unifiledpower", new HandleCloudySyncUnifiedPower());
        IBroadcastHandler powerModeNotifyHandle = new HandlePowerModeNotifier();
        mBroadcastHandlerMap.put(ActionConst.INTENT_SVAE_MODE_NOTIFY, powerModeNotifyHandle);
        mBroadcastHandlerMap.put(ActionConst.INTENT_SUPER_SAVE_MODE_DELETE_NOTIFY, powerModeNotifyHandle);
        mBroadcastHandlerMap.put(ActionConst.INTENT_SAVE_MODE_DELETE_NOTIFY, powerModeNotifyHandle);
        mBroadcastHandlerMap.put(ActionConst.INTENT_SAVE_MODE_CLOSE_NOTIFY, powerModeNotifyHandle);
    }

    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || intent.getAction() == null) {
            HwLog.e(TAG, "Invalid ctx or intent!");
            return;
        }
        HwLog.i(TAG, "BootBroadcastReceiver action =" + intent.getAction());
        sendToBackground(context, intent);
    }

    public void doInBackground(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            HwLog.w(TAG, "doInBackground action is null");
            return;
        }
        for (Entry<String, IBroadcastHandler> entry : mBroadcastHandlerMap.entrySet()) {
            if (action.equals(entry.getKey())) {
                HwLog.v(TAG, "doInBackground handle action: " + action);
                ((IBroadcastHandler) entry.getValue()).handleBroadcast(context, intent);
                return;
            }
        }
        HwLog.w(TAG, "No handler exist for registered action:" + action);
    }
}
