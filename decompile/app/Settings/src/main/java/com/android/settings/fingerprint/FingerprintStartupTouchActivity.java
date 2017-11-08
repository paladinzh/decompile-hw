package com.android.settings.fingerprint;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.TextView;
import com.android.settings.SettingsExtUtils;
import com.android.settings.fingerprint.utils.FingerprintUtils;

public class FingerprintStartupTouchActivity extends FingerprintStartupBaseActivity implements OnClickListener {
    private TextView mFuncDesc;
    private TextView mFuncTitle;
    private TextView mNextButton;
    private TextView mPrevButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
            prepareContentView();
            updateContentView();
            setSystemUiVisibilityChangeListener();
            return;
        }
        Log.d("FingerprintStartupTouchActivity", "Current mode is not startup mode.");
        finish();
    }

    private void updateContentView() {
        this.mPrevButton = (TextView) findViewById(2131886328);
        if (this.mPrevButton != null) {
            this.mPrevButton.setVisibility(4);
        }
        this.mNextButton = (TextView) findViewById(2131886329);
        if (this.mNextButton != null) {
            this.mNextButton.setText(2131626195);
            this.mNextButton.setVisibility(0);
            this.mNextButton.setOnClickListener(this);
        }
        this.mFuncTitle = (TextView) findViewById(2131886356);
        if (this.mFuncTitle != null) {
            this.mFuncTitle.setText(2131628679);
        }
        this.mFuncDesc = (TextView) findViewById(2131886638);
        if (this.mFuncDesc != null) {
            this.mFuncDesc.setText(2131628683);
        }
        ScrollView container = (ScrollView) findViewById(2131886639);
        View descView = LayoutInflater.from(this).inflate(2130968810, null);
        if (descView != null) {
            container.addView(descView);
        }
        if (!FingerprintUtils.FP_SHOW_NOTIFICATION_ON) {
            TextView notifyTitle = (TextView) findViewById(2131886671);
            if (notifyTitle != null) {
                notifyTitle.setVisibility(8);
            }
            TextView notifySummary = (TextView) findViewById(2131886672);
            if (notifySummary != null) {
                notifySummary.setVisibility(8);
            }
        }
        if (!FingerprintUtils.HAS_FP_NAVIGATION) {
            TextView browsePicTitle = (TextView) findViewById(2131886674);
            if (browsePicTitle != null) {
                browsePicTitle.setVisibility(8);
            }
            TextView browsePicSummary = (TextView) findViewById(2131886675);
            if (browsePicSummary != null) {
                browsePicSummary.setVisibility(8);
            }
        }
    }

    public void onClick(View v) {
        if (v == this.mNextButton) {
            setResult(2000);
            finish();
        }
    }
}
