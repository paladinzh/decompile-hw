package com.huawei.thermal.eventhub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import com.huawei.thermal.event.MsgEvent;
import java.util.ArrayList;

public class MsgReceiver extends BroadcastReceiver {
    private static final boolean DEBUG;
    private EventListener mCallback = null;
    private Context mContext;
    private final ArrayList<MsgEvent> mFreePool = new ArrayList();
    private boolean mIsStart = false;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            if (MsgReceiver.DEBUG) {
                Log.i("MsgReceiver", "onCallStateChanged state=" + state);
            }
            int evtId = -1;
            switch (state) {
                case NativeAdapter.PLATFORM_QCOM /*0*/:
                    evtId = 207;
                    break;
                case NativeAdapter.PLATFORM_MTK /*1*/:
                case NativeAdapter.PLATFORM_HI /*2*/:
                    evtId = 206;
                    break;
            }
            if (evtId != -1) {
                MsgReceiver.this.dispatchMessage(evtId, null);
            }
        }
    };

    static {
        boolean z = false;
        if (Log.isLoggable("MsgReceiver", 2)) {
            z = true;
        }
        DEBUG = z;
    }

    public MsgReceiver(Context context, EventListener callback) {
        this.mContext = context;
        this.mCallback = callback;
    }

    public boolean start() {
        this.mIsStart = true;
        ((TelephonyManager) this.mContext.getSystemService("phone")).listen(this.mPhoneStateListener, 33);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.USER_PRESENT");
        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("huawei.intent.action.POWER_MODE_CHANGED_ACTION");
        filter.addAction("android.hardware.usb.action.USB_STATE");
        IntentFilter definedFilter = new IntentFilter();
        definedFilter.addAction("com.android.vrservice.glass");
        definedFilter.addAction("huawei.intent.action.BATTERY_QUICK_CHARGE");
        this.mContext.getApplicationContext().registerReceiverAsUser(this, UserHandle.ALL, filter, null, null);
        this.mContext.getApplicationContext().registerReceiverAsUser(this, UserHandle.ALL, definedFilter, "com.huawei.powergenie.receiverPermission", null);
        return true;
    }

    public void onReceive(Context context, Intent intent) {
        int evtId;
        String action = intent.getAction();
        Log.i("MsgReceiver", "intent action: " + action);
        if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
            evtId = 202;
        } else if ("android.intent.action.SCREEN_ON".equals(action)) {
            evtId = 200;
        } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
            evtId = 20004;
        } else if ("android.intent.action.ACTION_POWER_CONNECTED".equals(action)) {
            evtId = 204;
        } else if ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(action)) {
            evtId = 205;
        } else if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
            evtId = 203;
        } else if ("com.android.vrservice.glass".equals(action)) {
            evtId = 209;
        } else if ("huawei.intent.action.BATTERY_QUICK_CHARGE".equals(action)) {
            evtId = 210;
        } else if ("huawei.intent.action.POWER_MODE_CHANGED_ACTION".equals(action)) {
            evtId = 208;
        } else if ("android.hardware.usb.action.USB_STATE".equals(action)) {
            evtId = 215;
        } else {
            Log.w("MsgReceiver", "Receive unknown message event! intent:" + intent);
            return;
        }
        dispatchMessage(evtId, intent);
    }

    private synchronized void dispatchMessage(int eventId, Intent intent) {
        if (this.mIsStart) {
            MsgEvent evt;
            if (this.mFreePool.size() <= 0) {
                if (DEBUG) {
                    Log.i("MsgReceiver", "new MessageEvent ");
                }
                evt = new MsgEvent(eventId, intent);
            } else {
                evt = (MsgEvent) this.mFreePool.remove(0);
                if (evt == null) {
                    evt = new MsgEvent(eventId, intent);
                } else {
                    evt.resetAs(eventId, intent);
                }
            }
            if (DEBUG) {
                Log.v("MsgReceiver", "new MsgEvent:" + evt);
            }
            if (this.mCallback != null) {
                this.mCallback.handleEvent(evt);
            }
            if (3 > this.mFreePool.size()) {
                this.mFreePool.add(evt);
            }
        } else {
            Log.w("MsgReceiver", "message receiver is not running!");
        }
    }
}
