package com.huawei.systemmanager.power.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.model.PowerModeControl;
import com.huawei.systemmanager.util.HwLog;

public class OpenInterfaceReceiver extends BroadcastReceiver {
    private static final int DEFAULT_ERROR_VALUE = 100;
    private String TAG = "OpenInterfaceReceiver";
    private Context mContext;

    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                HwLog.i(this.TAG, action);
                if (ActionConst.INTENT_CHANGE_POWER_MODE.equals(action)) {
                    int mode = intent.getIntExtra("power_mode", 100);
                    HwLog.i(this.TAG, "huawei.intent.action.HWSYSTEMMANAGER_CHANGE_POWERMODE Received !");
                    if (4 == mode || 1 == mode) {
                        try {
                            PowerModeControl.getInstance(this.mContext).changePowerMode(mode);
                            HwLog.i(this.TAG, "setPowerModeState success! mode is:" + mode);
                        } catch (IllegalArgumentException e) {
                            HwLog.e(this.TAG, "setPowerModeState failed! ");
                        }
                    }
                }
            }
        }
    }
}
