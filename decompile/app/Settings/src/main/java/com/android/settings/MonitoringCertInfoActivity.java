package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.settingslib.RestrictedLockUtils;

public class MonitoringCertInfoActivity extends Activity implements OnClickListener, OnDismissListener {
    private Dialog mDialog;
    private int mUserId;

    protected void onCreate(Bundle savedStates) {
        int titleId;
        super.onCreate(savedStates);
        this.mUserId = getIntent().getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DevicePolicyManager.class);
        int numberOfCertificates = getIntent().getIntExtra("android.settings.extra.number_of_certificates", 1);
        if (RestrictedLockUtils.getProfileOrDeviceOwner(this, this.mUserId) != null) {
            titleId = 2131689493;
        } else {
            titleId = 2131689490;
        }
        CharSequence title = getResources().getQuantityText(titleId, numberOfCertificates);
        setTitle(title);
        Builder builder = new Builder(this);
        builder.setTitle(title);
        builder.setCancelable(true);
        builder.setPositiveButton(getResources().getQuantityText(2131689493, numberOfCertificates), this);
        builder.setNeutralButton(2131624572, null);
        builder.setOnDismissListener(this);
        if (dpm.getProfileOwnerAsUser(this.mUserId) != null) {
            builder.setMessage(getResources().getQuantityString(2131689492, numberOfCertificates, new Object[]{dpm.getProfileOwnerNameAsUser(this.mUserId)}));
        } else if (dpm.getDeviceOwnerComponentOnCallingUser() != null) {
            builder.setMessage(getResources().getQuantityString(2131689491, numberOfCertificates, new Object[]{dpm.getDeviceOwnerNameOnAnyUser()}));
        } else {
            builder.setIcon(17301624);
            builder.setMessage(2131626432);
        }
        this.mDialog = builder.show();
    }

    public void onClick(DialogInterface dialog, int which) {
        Intent intent = new Intent("com.android.settings.TRUSTED_CREDENTIALS_USER");
        intent.setFlags(335544320);
        intent.putExtra("ARG_SHOW_NEW_FOR_USER", this.mUserId);
        startActivity(intent);
        finish();
    }

    public void onDismiss(DialogInterface dialogInterface) {
        if (!isFinishing()) {
            finish();
        }
    }

    public void onPause() {
        super.onPause();
        if (Utils.isMonkeyRunning() && this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }
}
