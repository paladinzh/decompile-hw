package com.android.settings;

import android.content.Context;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;

public class SettingNavigationBarPositionPreference extends Preference {
    private Context context;
    private boolean[] mIsChecked = new boolean[]{this.mLeftIsChecked, this.mMiddleIsChecked, this.mRightIsChecked};
    protected boolean mLeftIsChecked = false;
    protected RadioButton mLeftRadioButton;
    protected LinearLayout mLeftTitleArea;
    protected boolean mMiddleIsChecked = false;
    protected RadioButton mMiddleRadioButton;
    protected LinearLayout mMiddleTitleArea;
    protected OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case 2131886952:
                    if (!SettingNavigationBarPositionPreference.this.isChecked(0)) {
                        SettingNavigationBarPositionPreference.this.setChecked(true, 0);
                        SettingNavigationBarPositionPreference.this.setChecked(false, 1);
                        SettingNavigationBarPositionPreference.this.setChecked(false, 2);
                        System.putInt(SettingNavigationBarPositionPreference.this.context.getContentResolver(), "virtual_key_position", 0);
                        return;
                    }
                    return;
                case 2131886954:
                    if (SettingNavigationBarPositionPreference.this.isChecked(0)) {
                        SettingNavigationBarPositionPreference.this.setChecked(true, 0);
                        SettingNavigationBarPositionPreference.this.setChecked(false, 1);
                        SettingNavigationBarPositionPreference.this.setChecked(false, 2);
                        System.putInt(SettingNavigationBarPositionPreference.this.context.getContentResolver(), "virtual_key_position", 0);
                        return;
                    }
                    return;
                case 2131886955:
                    if (!SettingNavigationBarPositionPreference.this.isChecked(1)) {
                        SettingNavigationBarPositionPreference.this.setChecked(true, 1);
                        SettingNavigationBarPositionPreference.this.setChecked(false, 0);
                        SettingNavigationBarPositionPreference.this.setChecked(false, 2);
                        System.putInt(SettingNavigationBarPositionPreference.this.context.getContentResolver(), "virtual_key_position", 1);
                        return;
                    }
                    return;
                case 2131886956:
                    if (SettingNavigationBarPositionPreference.this.isChecked(1)) {
                        SettingNavigationBarPositionPreference.this.setChecked(true, 1);
                        SettingNavigationBarPositionPreference.this.setChecked(false, 0);
                        SettingNavigationBarPositionPreference.this.setChecked(false, 2);
                        System.putInt(SettingNavigationBarPositionPreference.this.context.getContentResolver(), "virtual_key_position", 1);
                        return;
                    }
                    return;
                case 2131886957:
                    if (!SettingNavigationBarPositionPreference.this.isChecked(2)) {
                        SettingNavigationBarPositionPreference.this.setChecked(true, 2);
                        SettingNavigationBarPositionPreference.this.setChecked(false, 0);
                        SettingNavigationBarPositionPreference.this.setChecked(false, 1);
                        System.putInt(SettingNavigationBarPositionPreference.this.context.getContentResolver(), "virtual_key_position", 2);
                        return;
                    }
                    return;
                case 2131886959:
                    if (SettingNavigationBarPositionPreference.this.isChecked(2)) {
                        SettingNavigationBarPositionPreference.this.setChecked(true, 2);
                        SettingNavigationBarPositionPreference.this.setChecked(false, 0);
                        SettingNavigationBarPositionPreference.this.setChecked(false, 1);
                        System.putInt(SettingNavigationBarPositionPreference.this.context.getContentResolver(), "virtual_key_position", 2);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private RadioButton[] mRadioButtons = new RadioButton[3];
    protected boolean mRightIsChecked = false;
    protected RadioButton mRightRadioButton;
    protected LinearLayout mRightTitleArea;
    private LinearLayout[] mTitleAreas = new LinearLayout[3];

    public SettingNavigationBarPositionPreference(Context context) {
        super(context);
        this.context = context;
        setLayoutResource(2130968988);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mLeftRadioButton = (RadioButton) view.findViewById(2131886954);
        this.mRadioButtons[0] = this.mLeftRadioButton;
        this.mMiddleRadioButton = (RadioButton) view.findViewById(2131886956);
        this.mRadioButtons[1] = this.mMiddleRadioButton;
        this.mRightRadioButton = (RadioButton) view.findViewById(2131886959);
        this.mRadioButtons[2] = this.mRightRadioButton;
        this.mLeftTitleArea = (LinearLayout) view.findViewById(2131886952);
        this.mTitleAreas[0] = this.mLeftTitleArea;
        this.mMiddleTitleArea = (LinearLayout) view.findViewById(2131886955);
        this.mTitleAreas[1] = this.mMiddleTitleArea;
        this.mRightTitleArea = (LinearLayout) view.findViewById(2131886957);
        this.mTitleAreas[2] = this.mRightTitleArea;
        for (int i = 0; i < this.mRadioButtons.length; i++) {
            this.mRadioButtons[i].setChecked(this.mIsChecked[i]);
            this.mRadioButtons[i].setOnClickListener(this.mOnClickListener);
            this.mTitleAreas[i].setOnClickListener(this.mOnClickListener);
        }
    }

    public void initRadioButton(int whitchRadioButton) {
        for (int i = 0; i < this.mIsChecked.length; i++) {
            if (whitchRadioButton == i) {
                this.mIsChecked[i] = true;
            } else {
                this.mIsChecked[i] = false;
            }
        }
        notifyChanged();
    }

    public void setChecked(boolean checked, int whitchRadioButton) {
        if (this.mIsChecked[whitchRadioButton] != checked) {
            this.mIsChecked[whitchRadioButton] = checked;
            if (this.mRadioButtons[whitchRadioButton] != null) {
                this.mRadioButtons[whitchRadioButton].setChecked(checked);
            }
        }
    }

    public boolean isChecked(int whitchRadioButton) {
        if (this.mRadioButtons[whitchRadioButton] != null) {
            return this.mRadioButtons[whitchRadioButton].isChecked();
        }
        return false;
    }
}
