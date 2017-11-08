package com.huawei.thermal;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.thermal.event.Event;
import com.huawei.thermal.event.MsgEvent;
import com.huawei.thermal.policy.Action;
import com.huawei.thermal.policy.BatteryAction;
import com.huawei.thermal.policy.PolicyDispatcher;

public final class LowBatteryManager implements InputListener {
    private static final boolean DEBUG;
    private final Context mContext;
    private final int mCriticalLowBatteryWarningLevel = 5;
    private boolean mIsSendCriticalLowBattery = false;
    private int mLastBatteryLevel = -1;
    private Object mLock = new Object();
    private final int mLowBatteryWarningLevel = SystemProperties.getInt("ro.config.pg_lowbatterylevel", 10);
    private final PolicyDispatcher mPolicy;
    private final TContext mTContext;

    static {
        boolean z = false;
        if (Log.isLoggable("LowBatteryManager", 2)) {
            z = true;
        }
        DEBUG = z;
    }

    public LowBatteryManager(TContext tcontext) {
        this.mContext = tcontext.getContext();
        this.mTContext = tcontext;
        this.mPolicy = PolicyDispatcher.getInstance(tcontext);
    }

    public void onInputEvent(Event evt) {
        if (evt instanceof MsgEvent) {
            onInputMsgEvent((MsgEvent) evt);
        } else {
            Log.i("LowBatteryManager", "Only handle msg event!");
        }
    }

    private void onInputMsgEvent(MsgEvent evt) {
        int evtId = evt.getEventId();
        if (DEBUG) {
            Log.d("LowBatteryManager", "onInputMsgEvent : " + evt);
        }
        if (evtId == 203) {
            handleLowBatteryChangedEvent(evtId, evt.getIntent());
        }
    }

    private void handleLowBatteryChangedEvent(int evtId, Intent intent) {
        int level = intent.getIntExtra("level", this.mLowBatteryWarningLevel);
        if (DEBUG) {
            Log.d("LowBatteryManager", "level : " + level + " mLastBatteryLevel :" + this.mLastBatteryLevel);
        }
        if (level != this.mLastBatteryLevel) {
            Action batteryAction = null;
            if (level >= 5) {
                if (this.mIsSendCriticalLowBattery) {
                    Log.i("LowBatteryManager", "send battery critical back ok");
                    batteryAction = new BatteryAction(level, "battery_critical", "0");
                    this.mIsSendCriticalLowBattery = false;
                }
            } else if (!this.mIsSendCriticalLowBattery) {
                Log.i("LowBatteryManager", "send battery critical");
                batteryAction = new BatteryAction(level, "battery_critical", "1");
                this.mIsSendCriticalLowBattery = true;
            }
            this.mLastBatteryLevel = level;
            if (batteryAction != null) {
                Log.i("LowBatteryManager", "batteryAction string: " + batteryAction.toString());
                this.mPolicy.dispatchPolicy(batteryAction);
            }
        }
    }
}
