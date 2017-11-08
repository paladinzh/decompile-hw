package com.android.contacts.hap.sim;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.HwLog;
import java.util.HashMap;

public class SimStateHandlerService extends IntentService {
    private static HashMap<Integer, SimStateServiceHandler> mServiceHandlerMap = null;

    public SimStateHandlerService() {
        super("SimStateHandlerService");
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        if (mServiceHandlerMap == null) {
            mServiceHandlerMap = new HashMap();
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    protected void onHandleIntent(Intent aIntent) {
        int subscription = -1;
        if (aIntent != null) {
            subscription = aIntent.getIntExtra("subscription", -1);
        }
        if (subscription < 0 || subscription > 1) {
            if (HwLog.HWFLOW) {
                HwLog.i("SimStateHandlerService", "Subscription is invalid because subscription is " + subscription);
            }
            return;
        }
        SimStateServiceHandler serviceHandler;
        if (HwLog.HWFLOW) {
            HwLog.i("SimStateHandlerService", "Inside SIM state Service on start command Subscription is:" + subscription);
        }
        if (mServiceHandlerMap.containsKey(Integer.valueOf(subscription))) {
            serviceHandler = (SimStateServiceHandler) mServiceHandlerMap.get(Integer.valueOf(subscription));
        } else {
            Context context = getApplicationContext();
            HandlerThread thread = new HandlerThread("Sim Initialization Thread");
            thread.setPriority(10);
            thread.start();
            serviceHandler = new SimStateServiceHandler(thread.getLooper(), context, subscription, SimFactoryManager.getAccountType(subscription));
            mServiceHandlerMap.put(Integer.valueOf(subscription), serviceHandler);
        }
        if (HwLog.HWDBG) {
            HwLog.d("SimStateHandlerService", "SIM service is created and returing");
        }
        String mSimStateText = null;
        if (aIntent != null) {
            mSimStateText = aIntent.getStringExtra("simstate");
        }
        if (HwLog.HWFLOW) {
            HwLog.i("SimStateHandlerService", "recived SIM STATE inside on start command:" + mSimStateText);
        }
        if (!CommonUtilMethods.isWifiOnlyVersion()) {
            SimFactoryManager.notifySimStateChanged(subscription);
        }
        Message svrMsg = serviceHandler.obtainMessage();
        svrMsg.arg1 = 1;
        Bundle data = new Bundle();
        data.putString("simstate", mSimStateText);
        svrMsg.setData(data);
        serviceHandler.sendMessage(svrMsg);
        if (HwLog.HWDBG) {
            HwLog.d("SimStateHandlerService", "onStart is called and is returning");
        }
    }
}
