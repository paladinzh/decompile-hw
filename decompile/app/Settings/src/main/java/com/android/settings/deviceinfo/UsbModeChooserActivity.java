package com.android.settings.deviceinfo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.UserManager;

public class UsbModeChooserActivity extends Activity {
    private String[] mFunctions;
    private boolean mIsUnlocked;
    private UsbManager mUsbManager;

    protected void onCreate(Bundle savedInstanceState) {
        CharSequence[] items;
        this.mIsUnlocked = getBaseContext().registerReceiver(null, new IntentFilter("android.hardware.usb.action.USB_STATE")).getBooleanExtra("unlocked", false);
        super.onCreate(savedInstanceState);
        this.mUsbManager = (UsbManager) getSystemService("usb");
        if (((UserManager) getSystemService("user")).hasUserRestriction("no_usb_file_transfer")) {
            items = new CharSequence[]{getText(2131626994), getText(2131627002)};
            this.mFunctions = new String[]{null, "midi"};
        } else {
            items = new CharSequence[]{getText(2131626994), getText(2131626998), getText(2131627000), getText(2131627002)};
            this.mFunctions = new String[]{null, "mtp", "ptp", "midi"};
        }
        Builder builder = new Builder(this);
        builder.setTitle(2131627004);
        builder.setSingleChoiceItems(items, getCurrentFunction(), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (!ActivityManager.isUserAMonkey()) {
                    UsbModeChooserActivity.this.setCurrentFunction(which);
                }
                dialog.dismiss();
                UsbModeChooserActivity.this.finish();
            }
        });
        builder.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                UsbModeChooserActivity.this.finish();
            }
        });
        builder.setNegativeButton(2131624572, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                UsbModeChooserActivity.this.finish();
            }
        });
        builder.create().show();
    }

    private int getCurrentFunction() {
        if (!this.mIsUnlocked) {
            return 0;
        }
        for (int i = 1; i < this.mFunctions.length; i++) {
            if (this.mUsbManager.isFunctionEnabled(this.mFunctions[i])) {
                return i;
            }
        }
        return 0;
    }

    private void setCurrentFunction(int which) {
        if (which == 0) {
            this.mUsbManager.setCurrentFunction(null);
            this.mUsbManager.setUsbDataUnlocked(false);
            return;
        }
        this.mUsbManager.setCurrentFunction(this.mFunctions[which]);
        this.mUsbManager.setUsbDataUnlocked(true);
    }
}
