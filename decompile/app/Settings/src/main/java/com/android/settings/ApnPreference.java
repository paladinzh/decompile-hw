package com.android.settings;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Telephony.Carriers;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

public class ApnPreference extends Preference implements OnCheckedChangeListener, OnClickListener {
    protected static final Uri APN_SIM1_URI1 = Uri.parse("content://telephony/carriers_sim1");
    protected static final Uri APN_SIM2_URI1 = Uri.parse("content://telephony/carriers_sim2");
    private static CompoundButton mCurrentChecked = null;
    private static String mSelectedKey = null;
    private boolean mProtectFromCheckedChange;
    private RadioButton mRadioButton;
    private boolean mSelectable;
    private int mSlotId;

    public ApnPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mSlotId = 0;
        this.mProtectFromCheckedChange = false;
        this.mSelectable = true;
        SettingsExtUtils.resetPreferenceLayout(this, 2130968623, 2130969000);
    }

    public ApnPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 2130772302);
    }

    public ApnPreference(Context context) {
        this(context, null);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        View widget = view.findViewById(2131886239);
        if (widget != null && (widget instanceof RadioButton)) {
            RadioButton rb = (RadioButton) widget;
            this.mRadioButton = rb;
            if (this.mSelectable) {
                rb.setOnCheckedChangeListener(this);
                boolean isChecked = getKey().equals(mSelectedKey);
                if (isChecked) {
                    mCurrentChecked = rb;
                    mSelectedKey = getKey();
                }
                this.mProtectFromCheckedChange = true;
                rb.setChecked(isChecked);
                this.mProtectFromCheckedChange = false;
                rb.setVisibility(0);
            } else {
                rb.setVisibility(8);
            }
        }
        SettingsExtUtils.setApnPreferenceClickListener(view.itemView, this.mSelectable, this);
    }

    public void setChecked() {
        mSelectedKey = getKey();
    }

    public static void initChecked() {
        mCurrentChecked = null;
        mSelectedKey = null;
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!this.mProtectFromCheckedChange) {
            if (isChecked) {
                if (mCurrentChecked != null) {
                    mCurrentChecked.setChecked(false);
                }
                mCurrentChecked = buttonView;
                mSelectedKey = getKey();
                callChangeListener(mSelectedKey);
            } else {
                mCurrentChecked = null;
                mSelectedKey = null;
            }
        }
    }

    public void onClick(View v) {
        if (v != null) {
            if (!this.mSelectable || v.getId() == 16908312) {
                Context context = getContext();
                if (context != null) {
                    Intent it = new Intent("android.intent.action.EDIT", ContentUris.withAppendedId(getApnUri(this.mSlotId), (long) Integer.parseInt(getKey())));
                    it.putExtra("slotid", this.mSlotId);
                    context.startActivity(it);
                }
                return;
            }
            if (!(this.mRadioButton == null || this.mRadioButton.isChecked())) {
                this.mRadioButton.setChecked(!this.mRadioButton.isChecked());
            }
        }
    }

    public void setSelectable(boolean selectable) {
        this.mSelectable = selectable;
        SettingsExtUtils.setWidgetLayout(this, selectable);
    }

    public void setSlotId(int slotid) {
        this.mSlotId = slotid;
    }

    private Uri getApnUri(int sub) {
        Uri apn_uri = Carriers.CONTENT_URI;
        if (!SystemProperties.getBoolean("ro.config.mtk_platform_apn", false)) {
            return apn_uri;
        }
        if (sub == 0) {
            return APN_SIM1_URI1;
        }
        if (sub == 1) {
            return APN_SIM2_URI1;
        }
        return apn_uri;
    }
}
