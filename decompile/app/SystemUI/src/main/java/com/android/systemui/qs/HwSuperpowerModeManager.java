package com.android.systemui.qs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SecurityCodeCheck;
import com.huawei.keyguard.theme.KeyguardTheme;
import java.util.ArrayList;

public class HwSuperpowerModeManager {
    private ArrayList<ModeChangedCallback> mModeChangedCallbacks = new ArrayList();
    private BroadcastReceiver mModeChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (SecurityCodeCheck.isValidIntentAndAction(intent)) {
                String action = intent.getAction();
                HwLog.i("HwSuperpowerModeManager", "mModeChangedReceiver::action=" + action);
                if ("huawei.intent.action.HWSYSTEMMANAGER_CHANGE_POWERMODE".equals(action)) {
                    if (3 == intent.getIntExtra("power_mode", 0)) {
                        HwSuperpowerModeManager.this.onModeChanged(true);
                        KeyguardTheme.getInst().checkStyle(context, false, false);
                    }
                } else if ("huawei.intent.action.HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE".equals(action) && intent.getIntExtra("shutdomn_limit_powermode", 0) == 0) {
                    HwSuperpowerModeManager.this.onModeChanged(false);
                }
                return;
            }
            HwLog.e("HwSuperpowerModeManager", "mModeChangedReceiver::not valid intent!");
        }
    };

    public interface ModeChangedCallback {
        void onModeChanged(boolean z);
    }

    public void init(Context context) {
        registerReceiver(context);
    }

    public void addCallback(ModeChangedCallback callback) {
        HwLog.i("HwSuperpowerModeManager", "addCallback::callback=" + callback);
        this.mModeChangedCallbacks.add(callback);
        onModeChanged(SystemProperties.getBoolean("sys.super_power_save", false));
    }

    public void removeAllCallbacks() {
        HwLog.i("HwSuperpowerModeManager", "removeAllCallbacks");
        this.mModeChangedCallbacks.clear();
    }

    public void registerReceiver(Context ctx) {
        IntentFilter modeChangedFilter = new IntentFilter();
        modeChangedFilter.addAction("huawei.intent.action.HWSYSTEMMANAGER_CHANGE_POWERMODE");
        modeChangedFilter.addAction("huawei.intent.action.HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE");
        ctx.registerReceiver(this.mModeChangedReceiver, modeChangedFilter);
    }

    private void onModeChanged(boolean modeOn) {
        HwLog.i("HwSuperpowerModeManager", "onModeChanged::modeOn=" + modeOn + ", callbacks=" + this.mModeChangedCallbacks.size());
        for (ModeChangedCallback callback : this.mModeChangedCallbacks) {
            callback.onModeChanged(modeOn);
        }
    }
}
