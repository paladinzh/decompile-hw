package com.android.settings.deviceinfo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.settings.HwAnimationReflection;
import com.android.settings.SettingsExtUtils;

public class UsbConnectedHelp extends Activity {
    private Context mContext;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.hardware.usb.action.USB_STATE".equals(intent.getAction()) && !intent.getBooleanExtra("connected", false)) {
                UsbConnectedHelp.this.finish();
            }
        }
    };
    private Intent mTypeIntent;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
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

    public void finish() {
        super.finish();
        new HwAnimationReflection(this).overrideTransition(2);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void initView() {
        String intentStr = getIntent().getStringExtra("usb_connected_help_type");
        if ("usb_connected_ptp_help".equals(intentStr)) {
            setContentView(2130969234);
        } else if ("usb_connected_mtp_help".equals(intentStr)) {
            setContentView(2130969231);
            handleOsTypeHelp();
        } else if ("usb_connected_storage_help".equals(intentStr)) {
            setContentView(2130969235);
        }
    }

    private void handleOsTypeHelp() {
        this.mTypeIntent = new Intent(this, UsbConnectedOsTypeHelp.class);
        this.mTypeIntent.setFlags(268435456);
        findViewById(2131887338).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                UsbConnectedHelp.this.mTypeIntent.putExtra("usb_connected_os_type", "usb_connected_windows_help");
                UsbConnectedHelp.this.startActivity(UsbConnectedHelp.this.mTypeIntent);
                SettingsExtUtils.setAnimationReflection(UsbConnectedHelp.this.mContext);
            }
        });
        findViewById(2131887340).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                UsbConnectedHelp.this.mTypeIntent.putExtra("usb_connected_os_type", "usb_connected_mac_help");
                UsbConnectedHelp.this.startActivity(UsbConnectedHelp.this.mTypeIntent);
                SettingsExtUtils.setAnimationReflection(UsbConnectedHelp.this.mContext);
            }
        });
        findViewById(2131887342).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                UsbConnectedHelp.this.mTypeIntent.putExtra("usb_connected_os_type", "usb_connected_linux_help");
                UsbConnectedHelp.this.startActivity(UsbConnectedHelp.this.mTypeIntent);
                SettingsExtUtils.setAnimationReflection(UsbConnectedHelp.this.mContext);
            }
        });
    }
}
