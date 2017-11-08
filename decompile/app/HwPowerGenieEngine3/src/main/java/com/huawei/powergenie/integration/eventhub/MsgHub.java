package com.huawei.powergenie.integration.eventhub;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.huawei.powergenie.integration.adapter.CommonAdapter;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import com.huawei.powergenie.integration.eventhub.BaseBroadcastReceiver.IHandleMsg;
import java.util.ArrayList;

public class MsgHub extends EventHub implements IHandleMsg {
    private static final boolean DEBUG = (Log.isLoggable("MsgHub", 2));
    private static MsgHub sInstance;
    private boolean mBootCompletedNotified = false;
    private Context mContext;
    private final int mCriticalLowBatteryWarningLevel = 5;
    private final ArrayList<MsgEvent> mFreePool = new ArrayList();
    private boolean mIsSendCriticalLowBattery = false;
    private boolean mIsSendLowBattery = false;
    private boolean mIsStart = false;
    private int mLastBatteryLevel = -1;
    private final int mLowBatteryWarningLevel = CommonAdapter.getPropLowBatteryLevel();
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        private String mLastOptNumeric = "";

        public void onCallStateChanged(int state, String incomingNumber) {
            if (MsgHub.DEBUG) {
                Log.i("MsgHub", "onCallStateChanged state=" + state);
            }
            int evtId = -1;
            switch (state) {
                case NativeAdapter.PLATFORM_QCOM /*0*/:
                    evtId = 323;
                    break;
                case NativeAdapter.PLATFORM_MTK /*1*/:
                case NativeAdapter.PLATFORM_HI /*2*/:
                    evtId = 322;
                    break;
            }
            if (evtId != -1) {
                MsgHub.this.handleMsg(evtId, null);
            }
        }

        public void onServiceStateChanged(ServiceState ss) {
            if (ss != null) {
                if (ss.getState() != 0) {
                    if (MsgHub.DEBUG) {
                        Log.i("MsgHub", "phone service state is not STATE_IN_SERVICE");
                    }
                    return;
                }
                String numeric = ss.getOperatorNumeric();
                if (!(numeric == null || numeric.equals(this.mLastOptNumeric) || numeric.length() < 3)) {
                    int evtId;
                    if ("460".equals(numeric.substring(0, 3))) {
                        evtId = 328;
                        Log.i("MsgHub", "in china opterator");
                    } else {
                        evtId = 329;
                        Log.i("MsgHub", "not in china opterator:" + numeric);
                    }
                    if (evtId != -1) {
                        MsgHub.this.handleMsg(evtId, null);
                    }
                    this.mLastOptNumeric = numeric;
                }
            }
        }
    };

    protected static MsgHub getInstance(Context context) {
        MsgHub msgHub;
        synchronized (MsgHub.class) {
            if (sInstance == null) {
                sInstance = new MsgHub(context);
            }
            msgHub = sInstance;
        }
        return msgHub;
    }

    private MsgHub(Context context) {
        this.mContext = context;
    }

    protected boolean start() {
        if (this.mIsStart) {
            return true;
        }
        this.mIsStart = true;
        ((TelephonyManager) this.mContext.getSystemService("phone")).listen(this.mPhoneStateListener, 33);
        PGBroadcastReceiver.init(this.mContext);
        SysBroadcastReceiver.init(this.mContext);
        return true;
    }

    public void handleMsg(int evtId, Intent intent) {
        String str = null;
        if (DEBUG) {
            Log.i("MsgHub", "msgId:" + evtId + ", action: " + (intent != null ? intent.getAction() : null));
        }
        if (302 == evtId) {
            if (intent == null || !"android.intent.action.LOCKED_BOOT_COMPLETED".equals(intent.getAction()) || (isDeviceProvisioned() && isUserSetupComplete())) {
                String str2 = "MsgHub";
                StringBuilder append = new StringBuilder().append(this.mBootCompletedNotified ? "drop" : "receive").append(" boot completed action: ");
                if (intent != null) {
                    str = intent.getAction();
                }
                Log.i(str2, append.append(str).toString());
                if (!this.mBootCompletedNotified) {
                    this.mBootCompletedNotified = true;
                } else {
                    return;
                }
            }
            Log.i("MsgHub", "first boot pg after recovery ,drop :" + intent.getAction());
            return;
        }
        sendMsg(evtId, intent);
        switch (evtId) {
            case 300:
                makeLockScreenEvent(evtId);
                break;
            case 308:
            case 310:
            case 319:
                makeLowBatteryChangedEvent(evtId, intent);
                break;
        }
    }

    private void makeLockScreenEvent(int evtId) {
        if (evtId == 300 && ((KeyguardManager) this.mContext.getSystemService("keyguard")).inKeyguardRestrictedInputMode()) {
            if (DEBUG) {
                Log.i("MsgHub", "send lock screen front");
            }
            sendMsg(321, null);
        }
    }

    private void makeLowBatteryChangedEvent(int evtId, Intent intent) {
        if (evtId == 308) {
            int level = intent.getIntExtra("level", this.mLowBatteryWarningLevel);
            if (level != this.mLastBatteryLevel) {
                if (level <= this.mLowBatteryWarningLevel) {
                    if (!this.mIsSendLowBattery) {
                        Log.i("MsgHub", "send battery low");
                        sendMsg(319, null);
                    }
                } else if (this.mLastBatteryLevel != -1 && this.mLastBatteryLevel <= this.mLowBatteryWarningLevel) {
                    Log.i("MsgHub", "send battery okay");
                    sendMsg(320, null);
                }
                if (level < 5) {
                    if (!this.mIsSendCriticalLowBattery) {
                        Log.i("MsgHub", "send battery critical");
                        sendMsg(333, null);
                        this.mIsSendCriticalLowBattery = true;
                    }
                } else if (this.mIsSendCriticalLowBattery) {
                    Log.i("MsgHub", "send battery critical back ok");
                    sendMsg(334, null);
                    this.mIsSendCriticalLowBattery = false;
                }
                this.mLastBatteryLevel = level;
                this.mIsSendLowBattery = true;
            }
        } else if (evtId == 310 && this.mLastBatteryLevel <= this.mLowBatteryWarningLevel) {
            Log.i("MsgHub", "send battery okay when power connect");
            sendMsg(320, null);
        }
    }

    private synchronized void sendMsg(int eventId, Intent intent) {
        MsgEvent evt;
        if (this.mFreePool.size() > 0) {
            evt = (MsgEvent) this.mFreePool.remove(0);
            evt.resetAs(eventId, intent);
        } else {
            Log.i("MsgHub", "new MessageEvent ");
            evt = new MsgEvent(eventId, intent);
        }
        dispatchEvent(evt);
        if (3 > this.mFreePool.size()) {
            this.mFreePool.add(evt);
        }
    }

    protected static IHandleMsg getIHandleMsg() {
        return sInstance;
    }

    private boolean isDeviceProvisioned() {
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            return true;
        }
        return false;
    }

    private boolean isUserSetupComplete() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, -2) != 0;
    }
}
