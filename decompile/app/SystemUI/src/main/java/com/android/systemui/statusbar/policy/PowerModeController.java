package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.systemui.observer.ObserverItem.OnChangeListener;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.utils.HwLog;
import java.util.ArrayList;
import java.util.List;

public class PowerModeController {
    private List<CallBack> mCallBacks = new ArrayList();
    private OnChangeListener mChangeListener = new OnChangeListener() {
        public void onChange(Object value) {
            PowerModeController.this.mPowerSave = ((Integer) SystemUIObserver.get(20)).intValue() == 4;
            for (CallBack cb : PowerModeController.this.mCallBacks) {
                cb.onSaveChanged(PowerModeController.this.mPowerSave);
            }
        }
    };
    private Context mContext;
    private boolean mInited = false;
    private boolean mPowerSave = false;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean lastSupperPowerSave = PowerModeController.this.mSupperPowerSave;
            if ("huawei.intent.action.HWSYSTEMMANAGER_CHANGE_POWERMODE".equals(intent.getAction())) {
                if (intent.getIntExtra("power_mode", 0) == 3) {
                    PowerModeController.this.mSupperPowerSave = true;
                }
            } else if ("huawei.intent.action.HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE".equals(intent.getAction()) && intent.getIntExtra("shutdomn_limit_powermode", 0) == 0) {
                PowerModeController.this.mSupperPowerSave = false;
            }
            HwLog.i("PowerModeController", "onReceive:" + intent + ", old=" + lastSupperPowerSave + ", new=" + PowerModeController.this.mSupperPowerSave + ", powersave=" + PowerModeController.this.mPowerSave);
            if (lastSupperPowerSave != PowerModeController.this.mSupperPowerSave) {
                for (CallBack cb : PowerModeController.this.mCallBacks) {
                    cb.onSupperPowerSaveChanged(PowerModeController.this.mSupperPowerSave);
                }
            }
        }
    };
    private boolean mSupperPowerSave = false;

    public interface CallBack {
        void onSaveChanged(boolean z);

        void onSupperPowerSaveChanged(boolean z);
    }

    public void init(Context context) {
        this.mContext = context;
        this.mInited = true;
        this.mPowerSave = ((Integer) SystemUIObserver.get(20)).intValue() == 4;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("huawei.intent.action.HWSYSTEMMANAGER_CHANGE_POWERMODE");
        intentFilter.addAction("huawei.intent.action.HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        SystemUIObserver.getObserver(20).addOnChangeListener(this.mChangeListener);
    }

    public void release() {
        this.mCallBacks.clear();
        if (this.mInited) {
            this.mContext.unregisterReceiver(this.mReceiver);
            SystemUIObserver.getObserver(20).removeOnChangeListener(this.mChangeListener);
        }
        this.mInited = false;
    }

    public void register(CallBack callBack) {
        if (!this.mCallBacks.contains(callBack)) {
            this.mCallBacks.add(callBack);
        }
    }

    public void unRegister(CallBack callBack) {
        this.mCallBacks.remove(callBack);
    }

    public boolean isPowerSave() {
        return this.mPowerSave;
    }

    public boolean isSupperPowerSave() {
        return this.mSupperPowerSave;
    }
}
