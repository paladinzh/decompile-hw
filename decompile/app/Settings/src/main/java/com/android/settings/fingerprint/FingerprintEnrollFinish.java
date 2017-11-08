package com.android.settings.fingerprint;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FingerprintEnrollFinish extends FingerprintEnrollBase {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130968791);
        setHeaderText(2131624668);
        Button addButton = (Button) findViewById(2131886625);
        if (((FingerprintManager) getSystemService("fingerprint")).getEnrolledFingerprints(this.mUserId).size() >= getResources().getInteger(17694880)) {
            addButton.setVisibility(4);
        } else {
            addButton.setOnClickListener(this);
        }
    }

    protected void onNextButtonClick() {
        setResult(1);
        finish();
    }

    public void onClick(View v) {
        if (v.getId() == 2131886625) {
            Intent intent = getEnrollingIntent();
            intent.addFlags(33554432);
            startActivity(intent);
            finish();
        }
        super.onClick(v);
    }

    protected int getMetricsCategory() {
        return 242;
    }
}
