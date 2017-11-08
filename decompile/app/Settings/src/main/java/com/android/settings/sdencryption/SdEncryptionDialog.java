package com.android.settings.sdencryption;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.android.settings.Settings.UserSettingsActivity;

public class SdEncryptionDialog extends AlertActivity implements OnClickListener {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertParams ap = this.mAlertParams;
        ap.mTitle = getString(2131627350);
        ap.mMessage = getString(2131628818);
        ap.mPositiveButtonText = getString(2131628816);
        ap.mNegativeButtonText = getString(2131625657);
        ap.mPositiveButtonListener = this;
        ap.mNegativeButtonListener = this;
        getWindow().setCloseOnTouchOutside(false);
        getWindow().setGravity(80);
        getWindow().setType(2003);
        setupAlert();
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                finish();
                return;
            case -1:
                startActivity(new Intent(this, UserSettingsActivity.class));
                finish();
                return;
            default:
                return;
        }
    }
}
