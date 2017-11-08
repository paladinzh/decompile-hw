package com.android.settings.fingerprint;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.settings.SettingsExtUtils;
import com.huawei.cust.HwCustUtils;

public class FingerprintSettingsStartTouchInfo extends Activity implements OnClickListener {
    private Button mBackButton;
    private HwCustFingerprintSettingsStartTouchInfo mHwCustFingerprintSettingsStartTouchInfo;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHwCustFingerprintSettingsStartTouchInfo = (HwCustFingerprintSettingsStartTouchInfo) HwCustUtils.createObj(HwCustFingerprintSettingsStartTouchInfo.class, new Object[0]);
        if (this.mHwCustFingerprintSettingsStartTouchInfo != null) {
            setContentView(this.mHwCustFingerprintSettingsStartTouchInfo.getCustLayout(2130968804));
        } else {
            setContentView(2130968804);
        }
        SettingsExtUtils.hideActionBarInStartupGuide(this);
        this.mBackButton = (Button) findViewById(2131886363);
        this.mBackButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v == this.mBackButton) {
            finish();
        }
    }
}
