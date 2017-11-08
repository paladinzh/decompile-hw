package com.android.settings.wifi.cmcc;

import android.os.Bundle;
import android.provider.Settings.System;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import java.util.Timer;
import java.util.TimerTask;

public class CmccWarningDialog extends AlertActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CmccWarningDialog", "warringCmccDialog is called");
        BuildDialog();
        setupAlert();
        new Timer().schedule(new TimerTask() {
            public void run() {
                Log.d("CmccWarningDialog", "warringCmccDialog is canceled");
                CmccWarningDialog.this.finish();
            }
        }, 5000);
    }

    private void BuildDialog() {
        AlertParams p = this.mAlertParams;
        View view = LayoutInflater.from(this).inflate(2130969270, null);
        ((CheckBox) view.findViewById(2131887481)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d("CmccWarningDialog", "checkBox.isChecked, make DB_WIFI_CMCC_CONNECTED_REMIND to 0 ");
                    System.putInt(CmccWarningDialog.this.getContentResolver(), "wifi_cmcc_connected_remind", 0);
                }
            }
        });
        p.mView = view;
        p.mTitle = getString(2131627791);
        p.mCancelable = false;
        p.mPositiveButtonText = getString(17039370);
    }
}
