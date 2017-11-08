package com.android.rcs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.android.mms.ui.MessageUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcseMmsExt;

public class RcsMMSApp {
    protected BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                HwBackgroundLoader.getInst().postTask(new Runnable() {
                    public void run() {
                        RcsMMSApp.this.initCenterAddress();
                    }
                });
            }
        }
    };
    private Context mContext;

    public RcsMMSApp(Context context) {
        this.mContext = context;
    }

    public void init() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            RcsProfile.init(this.mContext);
            RcseMmsExt.init();
            this.mContext.registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        }
    }

    public void initCenterAddress() {
        String smscAddress1;
        String smsAddressBySubID;
        if (MessageUtils.isMultiSimEnabled()) {
            smscAddress1 = MessageUtils.getSmsAddressBySubID(0);
            smsAddressBySubID = MessageUtils.getSmsAddressBySubID(1);
        } else {
            smscAddress1 = MessageUtils.getSmsAddressBySubID(0);
            smsAddressBySubID = null;
        }
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.putString("sim_center_address_0", smscAddress1);
        editor.putString("sim_center_address_1", smsAddressBySubID);
        editor.apply();
    }

    public void deInit() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            RcsProfile.deInit();
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        }
    }
}
