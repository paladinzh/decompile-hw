package com.android.settings;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.AttributeSet;
import com.huawei.android.hardware.usb.UsbManagerEx;

public class HisuitePreference extends CustomDialogPreference implements OnClickListener {
    public HisuitePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onPrepareDialogBuilder(Builder builder, OnClickListener listener) {
        super.onPrepareDialogBuilder(builder, listener);
        builder.setPositiveButton(17039370, listener);
        builder.setMessage(2131627842);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            UsbManagerEx.clearUsbHDBKeys();
        }
    }
}
