package com.android.systemui.usb;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.IUsbManager.Stub;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.android.systemui.R;

public class UsbDebuggingActivity extends AlertActivity implements OnClickListener {
    protected CheckBox mAlwaysAllow;
    private UsbDisconnectedReceiver mDisconnectedReceiver;
    protected String mKey;

    private class UsbDisconnectedReceiver extends BroadcastReceiver {
        private final Activity mActivity;

        public UsbDisconnectedReceiver(Activity activity) {
            this.mActivity = activity;
        }

        public void onReceive(Context content, Intent intent) {
            if ("android.hardware.usb.action.USB_STATE".equals(intent.getAction()) && !intent.getBooleanExtra("connected", false)) {
                this.mActivity.finish();
            }
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (SystemProperties.getInt("service.adb.tcp.port", 0) == 0) {
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver(this);
        }
        Intent intent = getIntent();
        String fingerprints = intent.getStringExtra("fingerprints");
        this.mKey = intent.getStringExtra("key");
        if (fingerprints == null || this.mKey == null) {
            finish();
            return;
        }
        AlertParams ap = this.mAlertParams;
        ap.mTitle = getString(R.string.usb_debugging_title);
        ap.mMessage = getString(R.string.usb_debugging_message, new Object[]{fingerprints});
        ap.mPositiveButtonText = getString(17039370);
        ap.mNegativeButtonText = getString(17039360);
        ap.mPositiveButtonListener = this;
        ap.mNegativeButtonListener = this;
        View checkbox = LayoutInflater.from(ap.mContext).inflate(17367089, null);
        this.mAlwaysAllow = (CheckBox) checkbox.findViewById(16909098);
        this.mAlwaysAllow.setText(getString(R.string.usb_debugging_always));
        ap.mView = checkbox;
        setupAlert();
    }

    public void onStart() {
        super.onStart();
        registerReceiver(this.mDisconnectedReceiver, new IntentFilter("android.hardware.usb.action.USB_STATE"));
    }

    protected void onStop() {
        if (this.mDisconnectedReceiver != null) {
            unregisterReceiver(this.mDisconnectedReceiver);
        }
        super.onStop();
    }

    public void onClick(DialogInterface dialog, int which) {
        boolean allow = which == -1;
        boolean isChecked = allow ? this.mAlwaysAllow.isChecked() : false;
        try {
            IUsbManager service = Stub.asInterface(ServiceManager.getService("usb"));
            if (allow) {
                service.allowUsbDebugging(isChecked, this.mKey);
            } else {
                service.denyUsbDebugging();
            }
        } catch (Exception e) {
            Log.e("UsbDebuggingActivity", "Unable to notify Usb service", e);
        }
        finish();
    }
}
