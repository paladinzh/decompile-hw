package com.android.settings;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ResetSettings extends OptionsMenuFragment {
    private View mContentView;
    private Button mInitiateButton;
    private final OnClickListener mInitiateListener = new OnClickListener() {
        public void onClick(View v) {
            if (!ResetSettings.this.runKeyguardConfirmation(55)) {
                ResetSettings.this.showFinalConfirmation();
            }
        }
    };
    private TextView mNoFingerprint;

    private boolean runKeyguardConfirmation(int request) {
        return new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(request, getActivity().getResources().getText(2131628276));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 55 && resultCode == -1) {
            showFinalConfirmation();
        }
    }

    private void showFinalConfirmation() {
        ((SettingsActivity) getActivity()).startPreferencePanel(ResetSettingsConfirm.class.getName(), null, 2131628276, null, null, 0);
    }

    protected int getMetricsCategory() {
        return 83;
    }

    public void onResume() {
        super.onResume();
        getActivity().setTitle(2131628276);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mContentView = inflater.inflate(2130969052, null);
        showOrHideText();
        this.mInitiateButton = (Button) this.mContentView.findViewById(2131887081);
        this.mInitiateButton.setOnClickListener(this.mInitiateListener);
        checkFingerprint();
        return this.mContentView;
    }

    private void checkFingerprint() {
        this.mNoFingerprint = (TextView) this.mContentView.findViewById(2131887076);
        FingerprintManager fpm = (FingerprintManager) getContext().getSystemService("fingerprint");
        if (fpm != null && !fpm.isHardwareDetected()) {
            Log.d("ResetSettings", "fingerprint hardware isn't present and functional");
            this.mNoFingerprint.setText(2131628651);
        }
    }

    private void showOrHideText() {
        if (this.mContentView != null) {
            TextView resetTitle = (TextView) this.mContentView.findViewById(2131887077);
            TextView findPhone = (TextView) this.mContentView.findViewById(2131887078);
            TextView huaweiId = (TextView) this.mContentView.findViewById(2131887079);
            TextView sdCard = (TextView) this.mContentView.findViewById(2131887080);
            if (!SettingsExtUtils.isGlobalVersion()) {
                if (SdCardLockUtils.isSdCardPresent(getActivity())) {
                    sdCard.setVisibility(0);
                }
                resetTitle.setVisibility(0);
                findPhone.setVisibility(0);
                huaweiId.setVisibility(0);
            } else if (SdCardLockUtils.isSdCardPresent(getActivity())) {
                resetTitle.setVisibility(0);
                sdCard.setVisibility(0);
            }
        }
    }
}
