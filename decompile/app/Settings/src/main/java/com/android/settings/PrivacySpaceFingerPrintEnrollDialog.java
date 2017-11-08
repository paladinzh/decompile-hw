package com.android.settings;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;

public final class PrivacySpaceFingerPrintEnrollDialog extends AlertActivity implements OnClickListener {
    private PrivacySpaceSettingsHelper mHelper;
    private byte[] mToken;
    private int mUserId;
    private int mUserType;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int quality = intent.getIntExtra("lockscreen.password_type", -1);
        this.mToken = intent.getByteArrayExtra("hw_auth_token");
        this.mUserId = intent.getIntExtra("android.intent.extra.USER", UserHandle.myUserId());
        this.mUserType = intent.getIntExtra("hidden_space_main_user_type", -1);
        this.mHelper = new PrivacySpaceSettingsHelper((Activity) this);
        setResult(0);
        if (!buildDialog(quality)) {
            finish();
        }
        getWindow().setGravity(80);
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                finish();
                return;
            case -1:
                this.mHelper.launcheFingerEnrollActivity(this.mToken, this.mUserId, this.mUserType);
                return;
            default:
                return;
        }
    }

    private boolean buildDialog(int quality) {
        if (this.mToken == null) {
            return false;
        }
        String currentLockType;
        if (quality == 131072 || quality == 196608) {
            currentLockType = getString(2131624735);
        } else if (quality < 262144) {
            return false;
        } else {
            currentLockType = getString(2131624737);
        }
        AlertParams p = this.mAlertParams;
        p.mTitle = getString(2131627686);
        p.mMessage = getString(2131627689, new Object[]{currentLockType});
        p.mPositiveButtonText = getString(2131627687);
        p.mNegativeButtonText = getString(2131627688);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonListener = this;
        setupAlert();
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 401) {
            setResult(-1);
            finish();
        }
    }
}
