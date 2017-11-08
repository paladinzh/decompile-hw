package com.android.settings.sdencryption;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.Utils;

public class SdEncryptionIntroduction extends Fragment {
    private TextView mBatteryText;
    private View mContentView;
    private LinearLayout mDescriptionLayout;
    private IntentFilter mIntentFilter;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                    int level = intent.getIntExtra("level", 0);
                    int plugged = intent.getIntExtra("plugged", 0);
                    int invalidCharger = intent.getIntExtra("invalid_charger", 0);
                    if (level >= 30) {
                    }
                    if ((plugged & 7) != 0) {
                        if (invalidCharger == 0) {
                        }
                    }
                    if (level < 30) {
                        if (!(SdEncryptionIntroduction.this.mBatteryText == null || SdEncryptionIntroduction.this.mBatteryText.getVisibility() == 0)) {
                            SdEncryptionIntroduction.this.mBatteryText.setVisibility(0);
                        }
                        if (SdEncryptionIntroduction.this.mStartButton != null && SdEncryptionIntroduction.this.mStartButton.isEnabled()) {
                            SdEncryptionIntroduction.this.mStartButton.setEnabled(false);
                        }
                    } else {
                        if (!(SdEncryptionIntroduction.this.mBatteryText == null || SdEncryptionIntroduction.this.mBatteryText.getVisibility() == 8)) {
                            SdEncryptionIntroduction.this.mBatteryText.setVisibility(8);
                        }
                        if (!(SdEncryptionIntroduction.this.mStartButton == null || SdEncryptionIntroduction.this.mStartButton.isEnabled())) {
                            SdEncryptionIntroduction.this.mStartButton.setEnabled(true);
                        }
                    }
                }
            }
        }
    };
    private Button mStartButton;
    private OnClickListener mStartListener = new OnClickListener() {
        public void onClick(View v) {
            ((SdEncryptionSettingsActivity) SdEncryptionIntroduction.this.getActivity()).showProgress();
        }
    };
    private String mState;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        this.mContentView = inflater.inflate(2130969081, container, false);
        this.mContentView.setScrollBarStyle(33554432);
        Utils.prepareCustomPreferencesList(container, this.mContentView, this.mContentView, true);
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.mState = bundle.getString("State");
        }
        if (this.mState == null) {
            return null;
        }
        String str = this.mState;
        if (str.equals("Encrypt")) {
            this.mBatteryText = (TextView) this.mContentView.findViewById(2131886431);
            this.mBatteryText.setText(getString(2131628790, new Object[]{com.android.settingslib.Utils.formatPercentage(30)}));
            this.mDescriptionLayout = (LinearLayout) this.mContentView.findViewById(2131887114);
            this.mDescriptionLayout.setVisibility(0);
            this.mStartButton = (Button) this.mContentView.findViewById(2131887127);
            this.mStartButton.setText(2131628778);
        } else if (str.equals("Decrypt")) {
            this.mBatteryText = (TextView) this.mContentView.findViewById(2131886431);
            this.mBatteryText.setText(getString(2131628796, new Object[]{com.android.settingslib.Utils.formatPercentage(30)}));
            this.mDescriptionLayout = (LinearLayout) this.mContentView.findViewById(2131887121);
            this.mDescriptionLayout.setVisibility(0);
            this.mStartButton = (Button) this.mContentView.findViewById(2131887127);
            this.mStartButton.setText(2131628780);
        } else {
            Log.d("SdEncryptionIntroduction", "received unknow event, just return");
        }
        this.mStartButton.setOnClickListener(this.mStartListener);
        return this.mContentView;
    }

    public void onResume() {
        super.onResume();
        getContext().registerReceiver(this.mIntentReceiver, this.mIntentFilter);
    }

    public void onPause() {
        getContext().unregisterReceiver(this.mIntentReceiver);
        super.onPause();
    }
}
