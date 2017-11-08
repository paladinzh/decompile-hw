package com.android.systemui.usb;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.IUsbManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import com.android.systemui.R;
import com.android.systemui.utils.HwLog;
import com.huawei.android.hardware.usb.UsbManagerEx;
import com.huawei.android.util.NoExtAPIException;
import fyusion.vislib.BuildConfig;

public class HwUsbDebuggingActivity extends UsbDebuggingActivity {
    private boolean mIsHDBModel = false;

    protected void setupAlert() {
        Intent intent = getIntent();
        String model = BuildConfig.FLAVOR;
        if (intent != null) {
            model = intent.getStringExtra("hdb");
        }
        setUsbDialogPosition();
        this.mIsHDBModel = model != null ? model.equals("hdb") : false;
        if (this.mIsHDBModel) {
            this.mAlertParams.mTitle = getString(R.string.hdb_debugging_title);
            this.mAlertParams.mMessage = getString(R.string.hdb_debugging_message);
            this.mAlwaysAllow.setText(getString(R.string.hdb_debugging_always));
        }
        this.mAlertParams.mIconId = 17302322;
        this.mAlwaysAllow.setTextSize(1, 12.0f);
        this.mAlwaysAllow.setTextColor(getResources().getColor(R.color.usb_text_color));
        super.setupAlert();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setUsbDialogPosition();
    }

    private void setUsbDialogPosition() {
        int i = 17;
        LayoutParams lp = getWindow().getAttributes();
        if ("tablet".equals(SystemProperties.get("ro.build.characteristics", BuildConfig.FLAVOR))) {
            lp.gravity = 17;
        } else {
            if (1 == getResources().getConfiguration().orientation) {
                i = 80;
            }
            lp.gravity = i;
        }
        getWindow().setAttributes(lp);
    }

    public void onClick(DialogInterface dialog, int which) {
        boolean allow = which == -1;
        boolean isChecked = allow ? this.mAlwaysAllow.isChecked() : false;
        IUsbManager service = Stub.asInterface(ServiceManager.getService("usb"));
        if (allow) {
            processAllow(service, isChecked);
        } else {
            processDeny(service);
        }
        finish();
    }

    private void processAllow(IUsbManager service, boolean alwaysAllow) {
        HwLog.i("HwUsbDebuggingActivity", "processAllow:alwaysAllow=" + alwaysAllow + ", IsHDBModel=" + this.mIsHDBModel);
        if (this.mIsHDBModel) {
            try {
                UsbManagerEx.allowUsbHDB(alwaysAllow, this.mKey);
            } catch (NoExtAPIException e) {
                Log.e("HwUsbDebuggingActivity", "Unable to notify Hdb service", e);
            }
            return;
        }
        try {
            service.allowUsbDebugging(alwaysAllow, this.mKey);
        } catch (RemoteException e2) {
            HwLog.e("HwUsbDebuggingActivity", "Unable to notify Usb service", e2);
        }
    }

    private void processDeny(IUsbManager service) {
        HwLog.i("HwUsbDebuggingActivity", "processDeny:IsHDBModel=" + this.mIsHDBModel);
        if (this.mIsHDBModel) {
            try {
                UsbManagerEx.denyUsbHDB();
            } catch (NoExtAPIException e) {
                HwLog.e("HwUsbDebuggingActivity", "Unable to notify Hdb service", e);
            }
            return;
        }
        try {
            service.denyUsbDebugging();
        } catch (RemoteException e2) {
            HwLog.e("HwUsbDebuggingActivity", "Unable to notify Usb service", e2);
        }
    }
}
