package com.android.deskclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.android.util.Config;
import com.android.util.HwLog;
import com.android.util.Utils;

public class DeskClockSecurityActivity extends AlarmsMainActivity {
    private BroadcastReceiver mScreenOnOffReceiver = null;

    protected void onCreate(Bundle savedInstanceState) {
        HwLog.i("DeskClockSecurityActivity", "onCreate");
        setScreenOrientation();
        super.onCreate(savedInstanceState);
        registerScreenOnOffListener();
        this.isFromOtherAPP = true;
        getWindow().addFlags(524288);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        HwLog.i("DeskClockSecurityActivity", "onSaveInstanceState");
        outState.putInt("lock_enter_tab", Config.clockTabIndex());
    }

    public void setScreenOrientation() {
        if (Utils.isLockScreenSupportLand()) {
            HwLog.i("DeskClockSecurityActivity", "support land");
            setRequestedOrientation(2);
            return;
        }
        setRequestedOrientation(5);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mScreenOnOffReceiver != null) {
            unregisterReceiver(this.mScreenOnOffReceiver);
            this.mScreenOnOffReceiver = null;
        }
    }

    private void registerScreenOnOffListener() {
        if (this.mScreenOnOffReceiver == null) {
            this.mScreenOnOffReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (intent != null && intent.getAction() != null) {
                        String action = intent.getAction();
                        if (action != null && action.equals("android.intent.action.SCREEN_OFF")) {
                            DeskClockSecurityActivity.this.finish();
                        }
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction("android.intent.action.SCREEN_OFF");
            registerReceiver(this.mScreenOnOffReceiver, iFilter);
        }
    }
}
