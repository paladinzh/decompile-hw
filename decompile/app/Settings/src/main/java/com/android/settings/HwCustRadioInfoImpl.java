package com.android.settings;

import android.content.Context;
import android.os.SystemProperties;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class HwCustRadioInfoImpl extends HwCustRadioInfo {
    private TextView dnsCheckState;
    private Button dnsCheckToggleButton;
    private Button oemInfoButton;
    private Spinner preferredNetworkType;
    private Button radioPowerButton;
    private Button refreshSmscButton;
    private EditText smsc;
    private TextView smscLabel;
    private Button updateSmscButton;

    public HwCustRadioInfoImpl(Context context) {
        super(context);
    }

    public void hideUselessMenu(RadioInfo activity) {
        if (!isShowExtraMenu()) {
            this.dnsCheckState = (TextView) activity.findViewById(2131887062);
            this.smscLabel = (TextView) activity.findViewById(2131887057);
            this.smsc = (EditText) activity.findViewById(2131887060);
            this.radioPowerButton = (Button) activity.findViewById(2131887055);
            this.dnsCheckToggleButton = (Button) activity.findViewById(2131887061);
            this.updateSmscButton = (Button) activity.findViewById(2131887058);
            this.refreshSmscButton = (Button) activity.findViewById(2131887059);
            this.oemInfoButton = (Button) activity.findViewById(2131887063);
            this.preferredNetworkType = (Spinner) activity.findViewById(2131887036);
            this.dnsCheckState.setVisibility(8);
            this.smscLabel.setVisibility(8);
            this.smsc.setVisibility(8);
            this.radioPowerButton.setVisibility(8);
            this.dnsCheckToggleButton.setVisibility(8);
            this.updateSmscButton.setVisibility(8);
            this.refreshSmscButton.setVisibility(8);
            this.oemInfoButton.setVisibility(8);
            this.preferredNetworkType.setVisibility(8);
        }
    }

    private boolean isShowExtraMenu() {
        return SystemProperties.getBoolean("ro.config.isShowMenu", false);
    }
}
