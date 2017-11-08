package com.android.settings.fingerprint;

import android.app.Activity;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.fingerprint.utils.FingerprintUtils;

public class FingerprintSettingsStartCheckoutInfo extends Activity implements OnClickListener {
    private Button mBackButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130968803);
        SettingsExtUtils.hideActionBarInStartupGuide(this);
        this.mBackButton = (Button) findViewById(2131886363);
        this.mBackButton.setOnClickListener(this);
        View accountPreference = (View) findViewById(2131886664).getParent();
        if (!(Utils.isCheckAppExist(this, "com.huawei.hwid") && FingerprintUtils.isHwidSupported(this))) {
            accountPreference.setVisibility(8);
        }
        View applock = findViewById(2131886658);
        View hwAccount = findViewById(2131886662);
        if (UserHandle.myUserId() != 0) {
            if (applock != null) {
                applock.setVisibility(8);
            }
            if (hwAccount != null) {
                hwAccount.setVisibility(8);
            }
        }
    }

    public void onClick(View v) {
        if (v == this.mBackButton) {
            finish();
        }
    }
}
