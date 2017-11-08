package com.android.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings.System;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TextView;

public class SignalIconStyle extends Activity implements OnCheckedChangeListener {
    private RadioButton mButtonConcise = null;
    private RadioButton mButtonCriterion = null;
    private TextView mTipText = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130969131);
        setTitle(2131629229);
        this.mTipText = (TextView) findViewById(2131887194);
        this.mButtonCriterion = (RadioButton) findViewById(2131887187);
        this.mButtonConcise = (RadioButton) findViewById(2131887192);
        if (!(this.mButtonCriterion == null || this.mButtonConcise == null)) {
            this.mButtonCriterion.setOnCheckedChangeListener(this);
            this.mButtonConcise.setOnCheckedChangeListener(this);
        }
        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void onResume() {
        super.onResume();
        int signaliconstyle = System.getInt(getContentResolver(), "hw_msim_signal_cluster_style", 1);
        if (signaliconstyle == 1) {
            if (this.mButtonCriterion != null) {
                this.mButtonCriterion.setChecked(true);
            }
        } else if (signaliconstyle == 0 && this.mButtonConcise != null) {
            this.mButtonConcise.setChecked(true);
        }
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        int id = arg0.getId();
        if (id == 2131887187) {
            if (arg1 && this.mButtonConcise != null && this.mTipText != null) {
                this.mButtonConcise.setChecked(false);
                System.putInt(getContentResolver(), "hw_msim_signal_cluster_style", 1);
                this.mTipText.setText(2131629233);
            }
        } else if (id == 2131887192 && arg1 && this.mButtonCriterion != null && this.mTipText != null) {
            this.mButtonCriterion.setChecked(false);
            System.putInt(getContentResolver(), "hw_msim_signal_cluster_style", 0);
            this.mTipText.setText(2131629234);
        }
    }
}
