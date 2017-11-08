package com.android.systemui.floattask;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.UserHandle;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;

public class FloatTask {
    private static ServiceConnection mFloatConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            HwLog.i("FloatTask", "onServiceDisconnected user=" + UserSwitchUtils.getCurrentUser());
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            HwLog.i("FloatTask", "onServiceConnected user=" + UserSwitchUtils.getCurrentUser());
        }
    };
    private boolean mBindFlag;
    private Context mContext;
    private BroadcastReceiver mFloatReceiver = new FloatViewReceiver();
    private boolean mFloatReceiverRegistered = false;
    private BroadcastReceiver mUserSwitcvhedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HwLog.i("FloatTask", "onReceive:" + intent);
            if (intent != null && "android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                FloatTask.this.onUserSwitch();
            }
        }
    };

    class FloatViewReceiver extends BroadcastReceiver {
        FloatViewReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                HwLog.i("FloatTask", "onReceive:" + intent);
                String action = intent.getAction();
                if (action != null) {
                    if ("com.huawei.android.floatTasks.on".equals(action)) {
                        FloatTask.this.bindFloatService();
                    } else if ("com.huawei.android.floatTasks.off".equals(action)) {
                        FloatTask.this.unBindFloatService();
                    }
                }
            }
        }
    }

    public FloatTask(Context context) {
        this.mContext = context;
    }

    private void bindFloatService() {
        HwLog.i("FloatTask", "bindService user=" + UserSwitchUtils.getCurrentUser());
        if (this.mBindFlag) {
            HwLog.w("FloatTask", "FloatService has binded");
            return;
        }
        Intent intent = new Intent();
        intent.setAction("com.huawei.android.FloatTask.Service");
        intent.setClassName("com.huawei.android.FloatTasks", "com.huawei.android.FloatTasks.FloatTasksService");
        this.mContext.bindServiceAsUser(intent, mFloatConnection, 1, new UserHandle(UserSwitchUtils.getCurrentUser()));
        this.mBindFlag = true;
    }

    private void unBindFloatService() {
        if (this.mBindFlag) {
            this.mContext.unbindService(mFloatConnection);
            this.mBindFlag = false;
        }
    }

    public void init() {
        HwLog.i("FloatTask", "init:" + this.mFloatReceiverRegistered);
        registerUserSwitchedReceiver();
        registerFloatReceiver();
    }

    private void registerFloatReceiver() {
        unregisterFloatReceiver();
        unBindFloatService();
        this.mFloatReceiverRegistered = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.android.floatTasks.on");
        filter.addAction("com.huawei.android.floatTasks.off");
        this.mContext.registerReceiverAsUser(this.mFloatReceiver, new UserHandle(UserSwitchUtils.getCurrentUser()), filter, "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM", null);
        Intent intent = new Intent("com.huawei.android.floatTasks.userswitch.restart");
        intent.setPackage("com.huawei.android.FloatTasks");
        this.mContext.sendBroadcastAsUser(intent, new UserHandle(UserSwitchUtils.getCurrentUser()));
    }

    private void unregisterFloatReceiver() {
        if (this.mFloatReceiverRegistered) {
            this.mContext.unregisterReceiver(this.mFloatReceiver);
        }
    }

    public void onUserSwitch() {
        registerFloatReceiver();
    }

    private void registerUserSwitchedReceiver() {
        HwLog.i("FloatTask", "registerUserSwitchedReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiverAsUser(this.mUserSwitcvhedReceiver, UserHandle.OWNER, filter, null, null);
    }
}
