package com.android.settings.smartcover;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import com.android.settings.HwCustSmartCoverSettingsImpl;

public class SmartCoverKindPreference extends Preference implements OnClickListener {
    private Context mContext;
    private ContentObserver mContextObserver;
    private onListenerSwitchStatus mListenerSwitchStatus;
    private ImageView mNormalImageView;
    private ImageView mSmartImageView;
    private RadioButton mSmartModeRB;
    private RadioButton mSmartNormalRB;
    private boolean misSmartCoverChecked;

    public interface onListenerSwitchStatus {
        void onSmartModeChanged(boolean z);
    }

    public SmartCoverKindPreference(Context context) {
        this(context, null);
        this.mContext = context;
        setLayoutResource(2130969142);
        setSelectable(false);
    }

    public SmartCoverKindPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContextObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                SmartCoverKindPreference.this.updateCheckedStatus();
            }
        };
        this.mContext = context;
    }

    public void setOnSmertCoverChangeListener(onListenerSwitchStatus listenerSwitchStatus) {
        this.mListenerSwitchStatus = listenerSwitchStatus;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        initView(view);
    }

    private void initView(PreferenceViewHolder view) {
        this.mSmartModeRB = (RadioButton) view.findViewById(2131887204);
        this.mSmartNormalRB = (RadioButton) view.findViewById(2131887203);
        this.mSmartImageView = (ImageView) view.findViewById(2131887202);
        this.mNormalImageView = (ImageView) view.findViewById(2131887205);
        if (this.mSmartModeRB != null) {
            this.mSmartModeRB.setOnClickListener(this);
        }
        if (this.mSmartNormalRB != null) {
            this.mSmartNormalRB.setOnClickListener(this);
        }
        if (this.mSmartImageView != null) {
            this.mSmartImageView.setOnClickListener(this);
        }
        if (this.mNormalImageView != null) {
            this.mNormalImageView.setOnClickListener(this);
        }
        this.misSmartCoverChecked = checkIfAllowSmartMode();
        updateCheckedStatus();
        updateRadioBtStatus();
    }

    public void onResume() {
        if (!(this.mContextObserver == null || this.mContext == null)) {
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor(HwCustSmartCoverSettingsImpl.KEY_COVER_ENALBED), true, this.mContextObserver);
        }
        updateCheckedStatus();
    }

    public void onPause() {
        if (this.mContextObserver != null && this.mContext != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mContextObserver);
        }
    }

    private void updateRadioBtStatus() {
        if (this.mContext != null) {
            onCoverModeChanged(Global.getInt(this.mContext.getContentResolver(), HwCustSmartCoverSettingsImpl.KEY_COVER_ENALBED, 1));
        }
    }

    public boolean checkIfAllowSmartMode() {
        if (Global.getInt(this.mContext.getContentResolver(), "cover_mode_checked", 1) == 1) {
            return true;
        }
        return false;
    }

    private void onCoverModeChanged(int coverMode) {
        switch (coverMode) {
            case 0:
                checkedStatus();
                setRadioBtStatus(false);
                return;
            case 1:
                checkedStatus();
                setRadioBtStatus(true);
                return;
            default:
                return;
        }
    }

    private void updateCheckedStatus() {
        boolean getMode = true;
        if (Global.getInt(this.mContext.getContentResolver(), "cover_mode_checked", 1) != 1) {
            getMode = false;
        }
        if (getMode) {
            setSmartCoverMode();
        } else {
            setNormalCoverMode();
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case 2131887202:
            case 2131887204:
                this.misSmartCoverChecked = true;
                setSmartCoverMode();
                this.mListenerSwitchStatus.onSmartModeChanged(true);
                return;
            case 2131887203:
            case 2131887205:
                this.misSmartCoverChecked = false;
                setNormalCoverMode();
                this.mListenerSwitchStatus.onSmartModeChanged(false);
                return;
            default:
                return;
        }
    }

    private void setRadioBtStatus(boolean isEnabledAndClickable) {
        if (this.mSmartModeRB != null) {
            this.mSmartModeRB.setEnabled(isEnabledAndClickable);
            this.mSmartModeRB.setClickable(isEnabledAndClickable);
        }
        if (this.mSmartNormalRB != null) {
            this.mSmartNormalRB.setEnabled(isEnabledAndClickable);
            this.mSmartNormalRB.setClickable(isEnabledAndClickable);
        }
        if (this.mSmartImageView != null) {
            this.mSmartImageView.setClickable(isEnabledAndClickable);
        }
        if (this.mNormalImageView != null) {
            this.mNormalImageView.setClickable(isEnabledAndClickable);
        }
    }

    private void setSmartCoverMode() {
        if (this.mSmartModeRB != null && this.mSmartNormalRB != null) {
            this.mSmartModeRB.setChecked(true);
            this.mSmartNormalRB.setChecked(false);
            saveRadioBtMode(this.misSmartCoverChecked);
        }
    }

    private void setNormalCoverMode() {
        if (this.mSmartModeRB != null && this.mSmartNormalRB != null) {
            this.mSmartModeRB.setChecked(false);
            this.mSmartNormalRB.setChecked(true);
            saveRadioBtMode(this.misSmartCoverChecked);
        }
    }

    private void checkedStatus() {
        if (this.misSmartCoverChecked) {
            setSmartCoverMode();
        } else {
            setNormalCoverMode();
        }
    }

    private void saveRadioBtMode(boolean isChecked) {
        int mode;
        if (isChecked) {
            mode = 1;
        } else {
            mode = 0;
        }
        Global.putInt(this.mContext.getContentResolver(), "cover_mode_checked", mode);
    }
}
