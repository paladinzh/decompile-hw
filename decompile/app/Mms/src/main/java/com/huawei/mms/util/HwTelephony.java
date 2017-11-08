package com.huawei.mms.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.MSimTelephonyManager;
import com.android.mms.MmsApp;
import com.android.mms.ui.MessageUtils;
import com.huawei.cspcommon.MLog;

public class HwTelephony {
    private static HwTelephony sHwPhone;
    private MSimTelephonyManager mMultyPhone;

    public interface HwSimStateListener {
        void onSimStateChanged(int i);

        void onSimStateChanged(int i, int i2);
    }

    private static class HwSimStateReceiver extends BroadcastReceiver {
        private HwSimStateListener mListener;

        HwSimStateReceiver(HwSimStateListener l) {
            this.mListener = l;
        }

        private void handleMultySim(Intent intent) {
            String state = intent.getStringExtra("ss");
            int subId = MessageUtils.getSimIdFromIntent(intent, -1);
            MLog.d("HwTelephony", "handleMultySim with sub" + subId + "; state " + state);
            this.mListener.onSimStateChanged(HwTelephony.getSimIntState(state), subId);
        }

        private void handleSingleSim(Intent intent) {
            String state = intent.getStringExtra("ss");
            MLog.d("HwTelephony", "handleSingeSim with state " + state);
            this.mListener.onSimStateChanged(HwTelephony.getSimIntState(state));
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                MLog.d("HwTelephony", "HwSimStateReceiver Receive Invalide Intent");
                return;
            }
            String action = intent.getAction();
            if (!HwTelephony.isSupportMultiSim()) {
                handleSingleSim(intent);
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                handleMultySim(intent);
            }
        }
    }

    private static final int getSimIntState(String strState) {
        if ("LOADED".equals(strState) || "READY".equals(strState)) {
            return 5;
        }
        if ("ABSENT".equals(strState)) {
            return 1;
        }
        return 0;
    }

    public static void init(Context context) {
        sHwPhone = new HwTelephony(context);
    }

    public static HwTelephony getDefault() {
        if (sHwPhone == null) {
            init(MmsApp.getApplication().getApplicationContext());
        }
        return sHwPhone;
    }

    public static boolean isSupportMultiSim() {
        return MessageUtils.isMultiSimEnabled();
    }

    private HwTelephony(Context context) {
        try {
            this.mMultyPhone = new MSimTelephonyManager(context);
        } catch (Exception e) {
            MLog.e("HwTelephony", "new MSimTelephonyManager occur file not find exception");
        }
    }

    public String getSimOperator() {
        return this.mMultyPhone != null ? this.mMultyPhone.getSimOperator() : "";
    }

    public static BroadcastReceiver registeSimChange(Context context, HwSimStateListener l) {
        MLog.v("HwTelephony", "Registe ACTION_SIM_STATE_CHANGED");
        HwSimStateReceiver receiver = new HwSimStateReceiver(l);
        IntentFilter filter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        filter.addAction("android.intent.action.AIRPLANE_MODE");
        context.registerReceiver(receiver, filter);
        return receiver;
    }
}
