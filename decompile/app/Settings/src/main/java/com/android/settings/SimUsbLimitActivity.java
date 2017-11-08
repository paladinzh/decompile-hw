package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;

public class SimUsbLimitActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(17170445);
        showDialog(1);
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new Builder(this).setTitle(2131626521).setMessage(2131628027).setPositiveButton(17039370, null).setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        SimUsbLimitActivity.this.finish();
                    }
                }).create();
            default:
                return super.onCreateDialog(id);
        }
    }
}
