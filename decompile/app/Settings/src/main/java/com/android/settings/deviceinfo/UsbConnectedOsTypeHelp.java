package com.android.settings.deviceinfo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.android.settings.HwAnimationReflection;

public class UsbConnectedOsTypeHelp extends Activity {
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.hardware.usb.action.USB_STATE".equals(intent.getAction()) && !intent.getBooleanExtra("connected", false)) {
                UsbConnectedOsTypeHelp.this.finish();
            }
        }
    };

    public void finish() {
        super.finish();
        new HwAnimationReflection(this).overrideTransition(2);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_STATE");
        registerReceiver(this.mIntentReceiver, filter);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onResume() {
        super.onResume();
        initView();
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.mIntentReceiver);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void initView() {
        String intentStr = getIntent().getStringExtra("usb_connected_os_type");
        if ("usb_connected_windows_help".equals(intentStr)) {
            setTitle(getString(2131627807));
            setContentView(2130969230);
        } else if ("usb_connected_mac_help".equals(intentStr)) {
            setTitle(getString(2131627808));
            setContentView(2130969229);
        } else if ("usb_connected_linux_help".equals(intentStr)) {
            setTitle(getString(2131627809));
            setContentView(2130969228);
        }
    }
}
