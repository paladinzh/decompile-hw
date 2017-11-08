package com.android.mms.ui;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import com.android.internal.app.AlertController;
import com.android.internal.app.AlertController.AlertParams;
import com.google.android.gms.R;
import com.huawei.mms.ui.HwBaseActivity;

public class WarnOfStorageLimitsActivity extends HwBaseActivity implements DialogInterface, OnClickListener {
    protected AlertController mAlert;
    protected AlertParams mAlertParams;

    protected void onCreate(Bundle savedInstanceState) {
        int themeID = getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
        if (themeID != 0) {
            setTheme(themeID);
        } else {
            setTheme(16974972);
        }
        super.onCreate(savedInstanceState);
        this.mAlert = new AlertController(this, this, getWindow());
        this.mAlertParams = new AlertParams(this);
        AlertParams p = this.mAlertParams;
        p.mMessage = getString(R.string.storage_limits_message);
        p.mPositiveButtonText = getString(R.string.storage_limits_setting);
        p.mNegativeButtonText = getString(R.string.storage_limits_setting_dismiss);
        p.mPositiveButtonListener = this;
        setupAlert();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            startActivity(new Intent(this, MessagingPreferenceActivity.class));
        }
        dialog.dismiss();
        finish();
    }

    public void cancel() {
        finish();
    }

    public void dismiss() {
        if (!isFinishing()) {
            finish();
        }
    }

    protected void setupAlert() {
        this.mAlertParams.apply(this.mAlert);
        this.mAlert.installContent();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mAlert.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mAlert.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}
