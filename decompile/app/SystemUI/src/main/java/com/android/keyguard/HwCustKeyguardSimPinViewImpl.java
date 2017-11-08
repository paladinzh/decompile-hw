package com.android.keyguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.widget.TextView;

public class HwCustKeyguardSimPinViewImpl extends HwCustKeyguardSimPinView {
    private static final String ACTION_SIM_ICCID_READY = "android.intent.action.ACTION_SIM_ICCID_READY";
    private Context context;
    private boolean mEnabled = "true".equals(SystemProperties.get("ro.config.iccid_language", "false"));
    private KeyguardSimPinView mKeyguardSimPinView;
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.LOCALE_CHANGED".equals(action) || HwCustKeyguardSimPinViewImpl.ACTION_SIM_ICCID_READY.equals(action)) {
                HwCustKeyguardSimPinViewImpl.this.mKeyguardSimPinView.resetState();
                TextView cancel = (TextView) HwCustKeyguardSimPinViewImpl.this.mKeyguardSimPinView.findViewById(R$id.cancel);
                if (cancel != null) {
                    cancel.setText(HwCustKeyguardSimPinViewImpl.this.mKeyguardSimPinView.getResources().getString(17039360));
                    HwCustKeyguardSimPinViewImpl.this.mKeyguardSimPinView.invalidate();
                }
            }
        }
    };

    public HwCustKeyguardSimPinViewImpl(Context context, KeyguardSimPinView lKeyguardSimPinView) {
        super(context, lKeyguardSimPinView);
        this.context = context;
        this.mKeyguardSimPinView = lKeyguardSimPinView;
    }

    public void registerReceiver() {
        if (this.mEnabled) {
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction("android.intent.action.LOCALE_CHANGED");
            iFilter.addAction(ACTION_SIM_ICCID_READY);
            this.context.registerReceiver(this.mReceiver, iFilter);
        }
    }

    public void unregisterReceiver() {
        if (this.mEnabled && this.mReceiver != null) {
            this.context.unregisterReceiver(this.mReceiver);
        }
    }
}
