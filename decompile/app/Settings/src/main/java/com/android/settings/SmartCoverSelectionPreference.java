package com.android.settings;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;

public class SmartCoverSelectionPreference extends Preference implements OnClickListener {
    private ImageView mCommonCoverImage;
    private RadioButton mCommonCoverRadioButton;
    private Context mContext;
    private int mCurrentCoverType;
    private boolean mIsSelectioinEnabled;
    private ImageView mWindowCoverImage;
    private RadioButton mWindowCoverRadioButton;

    public SmartCoverSelectionPreference(Context context) {
        this(context, null);
    }

    public SmartCoverSelectionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mIsSelectioinEnabled = true;
        this.mCurrentCoverType = 0;
        this.mContext = context;
        setLayoutResource(2130969143);
    }

    private int getCurrentCoverType() {
        if (this.mContext != null) {
            return Global.getInt(this.mContext.getContentResolver(), "cover_type", 0);
        }
        return 0;
    }

    private boolean isSmartCoverEnabled() {
        boolean z = true;
        if (this.mContext == null) {
            return true;
        }
        if (1 != Global.getInt(this.mContext.getContentResolver(), HwCustSmartCoverSettingsImpl.KEY_COVER_ENALBED, 1)) {
            z = false;
        }
        return z;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        boolean z = true;
        super.onBindViewHolder(view);
        this.mWindowCoverImage = (ImageView) view.findViewById(2131886856);
        this.mWindowCoverImage.setOnClickListener(this);
        this.mCommonCoverImage = (ImageView) view.findViewById(2131886857);
        this.mCommonCoverImage.setOnClickListener(this);
        if (this.mContext != null) {
            this.mCurrentCoverType = getCurrentCoverType();
            this.mIsSelectioinEnabled = isSmartCoverEnabled();
        }
        this.mWindowCoverRadioButton = (RadioButton) view.findViewById(2131886858);
        this.mWindowCoverRadioButton.setEnabled(this.mIsSelectioinEnabled);
        this.mWindowCoverRadioButton.setChecked(this.mCurrentCoverType == 0);
        this.mWindowCoverRadioButton.setOnClickListener(this);
        this.mCommonCoverRadioButton = (RadioButton) view.findViewById(2131886859);
        this.mCommonCoverRadioButton.setEnabled(this.mIsSelectioinEnabled);
        RadioButton radioButton = this.mCommonCoverRadioButton;
        if (this.mCurrentCoverType != 1) {
            z = false;
        }
        radioButton.setChecked(z);
        this.mCommonCoverRadioButton.setOnClickListener(this);
    }

    public void onClick(View view) {
        if (this.mIsSelectioinEnabled) {
            if (view == this.mWindowCoverImage || view == this.mWindowCoverRadioButton) {
                setCoverType(0);
                ItemUseStat.getInstance().handleClick(this.mContext, 2, "cover_type", 0);
            } else if (view == this.mCommonCoverImage || view == this.mCommonCoverRadioButton) {
                setCoverType(1);
                ItemUseStat.getInstance().handleClick(this.mContext, 2, "cover_type", 1);
            }
        }
    }

    public void setSelectionEnabled(boolean enabled) {
        this.mIsSelectioinEnabled = enabled;
        notifyChanged();
    }

    private void setCoverType(int type) {
        boolean z = true;
        if (!(this.mWindowCoverRadioButton == null || this.mCommonCoverRadioButton == null)) {
            boolean z2;
            RadioButton radioButton = this.mWindowCoverRadioButton;
            if (type == 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            radioButton.setChecked(z2);
            RadioButton radioButton2 = this.mCommonCoverRadioButton;
            if (type != 1) {
                z = false;
            }
            radioButton2.setChecked(z);
        }
        if (this.mContext != null) {
            Global.putInt(this.mContext.getContentResolver(), "cover_type", type);
        } else {
            Log.e("SmartCoverSelectionPreference", "Context is not been created yet.");
        }
    }
}
