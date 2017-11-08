package com.huawei.systemmanager.spacecleanner.autoclean;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.UserHandle;
import android.telephony.TelephonyManager;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.service.MainService.HsmService;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.util.HwLog;

public class AutoCleanReceiveServer implements HsmService {
    public static final String TAG = "AutoCleanReceiveServer";
    private final Context mContext;
    private boolean mIsCharging = false;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Utility.checkBroadcast(context, intent)) {
                String action = intent.getAction();
                HwLog.i(AutoCleanReceiveServer.TAG, "receive action:" + action);
                if ("android.intent.action.ACTION_POWER_CONNECTED".equals(action)) {
                    AutoCleanReceiveServer.this.mIsCharging = true;
                    AutoCleanReceiveServer.this.checkToStartCleanService(context);
                } else if ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(action)) {
                    AutoCleanReceiveServer.this.mIsCharging = false;
                } else if (!"android.intent.action.SCREEN_ON".equals(action)) {
                    if ("android.intent.action.SCREEN_OFF".equals(action)) {
                        AutoCleanReceiveServer.this.checkToStartCleanService(context);
                    } else if (ActionConst.INTENT_CHANGE_POWER_MODE.equals(action)) {
                        AutoCleanReceiveServer.this.checkToStartCleanService(context);
                    }
                }
            }
        }
    };

    public AutoCleanReceiveServer(Context ctx) {
        this.mContext = ctx;
    }

    public void init() {
        this.mIsCharging = isPowerCharging();
        if (this.mIsCharging) {
            HwLog.i(TAG, "AutoCleanReceiveServer init, current is charging");
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(ActionConst.INTENT_CHANGE_POWER_MODE);
        this.mContext.registerReceiver(this.mReceiver, filter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    private boolean phoneIsInUse() {
        boolean bInUse = !((TelephonyManager) this.mContext.getSystemService("phone")).isIdle();
        HwLog.i(TAG, "telemamanger state isIdle:" + bInUse);
        return bInUse;
    }

    public void onDestroy() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
    }

    private void checkToStartCleanService(Context ctx) {
        if (phoneIsInUse()) {
            HwLog.i(TAG, "checkToStartCleanService,phone is use state,phone is use,need not start clean service");
        } else if (!this.mIsCharging) {
            HwLog.i(TAG, "checkToStartCleanService, current is not charging, need not start clean service");
        } else if (!AutoCleanConst.getInstance().checkIfScreenOff(ctx)) {
            HwLog.i(TAG, "checkToStartCleanService, current is not screenoff, need not start clean service");
        } else if (AutoCleanConst.getInstance().checkIfSuperPowerMode()) {
            HwLog.i(TAG, "checkToStartCleanService, current is super power mode, need not start clean service");
        } else {
            SpaceStatsUtils.reportAutoCleanServiceTriggedOp();
            Intent intent = new Intent(ctx, AutoCleanService.class);
            intent.setAction(AutoCleanService.ACTION_START_AUTO_CLEAN);
            ctx.startServiceAsUser(intent, UserHandle.OWNER);
        }
    }

    private boolean isPowerCharging() {
        Intent batteryIntent = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (batteryIntent == null) {
            HwLog.i(TAG, "isPowerCharging batteryIntent is null");
            return false;
        }
        int status = batteryIntent.getIntExtra("status", -1);
        int chargePlug = batteryIntent.getIntExtra("plugged", -1);
        HwLog.i(TAG, "isPowerCharging status: " + status + ", plug: " + chargePlug);
        return 2 == status || (chargePlug & 7) != 0;
    }
}
