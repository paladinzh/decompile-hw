package com.android.systemui.tuner;

import android.app.UiModeManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.NightModeController;
import com.android.systemui.statusbar.policy.NightModeController.Listener;
import com.android.systemui.tuner.TunerService.Tunable;

public class NightModeFragment extends PreferenceFragment implements Tunable, Listener, OnPreferenceChangeListener {
    private static final CharSequence KEY_ADJUST_BRIGHTNESS = "adjust_brightness";
    private static final CharSequence KEY_ADJUST_TINT = "adjust_tint";
    private static final CharSequence KEY_AUTO = "auto";
    private SwitchPreference mAdjustBrightness;
    private SwitchPreference mAdjustTint;
    private SwitchPreference mAutoSwitch;
    private NightModeController mNightModeController;
    private Switch mSwitch;
    private UiModeManager mUiModeManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mNightModeController = new NightModeController(getContext());
        this.mUiModeManager = (UiModeManager) getContext().getSystemService(UiModeManager.class);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.night_mode_settings, container, false);
        ((ViewGroup) view).addView(super.onCreateView(inflater, container, savedInstanceState));
        return view;
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        addPreferencesFromResource(R.xml.night_mode);
        this.mAutoSwitch = (SwitchPreference) findPreference(KEY_AUTO);
        this.mAutoSwitch.setOnPreferenceChangeListener(this);
        this.mAdjustTint = (SwitchPreference) findPreference(KEY_ADJUST_TINT);
        this.mAdjustTint.setOnPreferenceChangeListener(this);
        this.mAdjustBrightness = (SwitchPreference) findPreference(KEY_ADJUST_BRIGHTNESS);
        this.mAdjustBrightness.setOnPreferenceChangeListener(this);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View switchBar = view.findViewById(R.id.switch_bar);
        this.mSwitch = (Switch) switchBar.findViewById(16908352);
        this.mSwitch.setChecked(this.mNightModeController.isEnabled());
        switchBar.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                boolean newState = !NightModeFragment.this.mNightModeController.isEnabled();
                MetricsLogger.action(NightModeFragment.this.getContext(), 309, newState);
                NightModeFragment.this.mNightModeController.setNightMode(newState);
                NightModeFragment.this.mSwitch.setChecked(newState);
            }
        });
    }

    public void onResume() {
        super.onResume();
        MetricsLogger.visibility(getContext(), 308, true);
        this.mNightModeController.addListener(this);
        TunerService.get(getContext()).addTunable((Tunable) this, "brightness_use_twilight", "tuner_night_mode_adjust_tint");
        calculateDisabled();
    }

    public void onPause() {
        super.onPause();
        MetricsLogger.visibility(getContext(), 308, false);
        this.mNightModeController.removeListener(this);
        TunerService.get(getContext()).removeTunable(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 0;
        Boolean value = (Boolean) newValue;
        if (this.mAutoSwitch == preference) {
            MetricsLogger.action(getContext(), 310, value.booleanValue());
            this.mNightModeController.setAuto(value.booleanValue());
        } else if (this.mAdjustTint == preference) {
            MetricsLogger.action(getContext(), 312, value.booleanValue());
            this.mNightModeController.setAdjustTint(value);
            postCalculateDisabled();
        } else if (this.mAdjustBrightness != preference) {
            return false;
        } else {
            MetricsLogger.action(getContext(), 313, value.booleanValue());
            TunerService tunerService = TunerService.get(getContext());
            String str = "brightness_use_twilight";
            if (value.booleanValue()) {
                i = 1;
            }
            tunerService.setValue(str, i);
            postCalculateDisabled();
        }
        return true;
    }

    private void postCalculateDisabled() {
        getView().post(new Runnable() {
            public void run() {
                NightModeFragment.this.calculateDisabled();
            }
        });
    }

    private void calculateDisabled() {
        int i;
        int i2;
        if (this.mAdjustTint.isChecked()) {
            i = 1;
        } else {
            i = 0;
        }
        if (this.mAdjustBrightness.isChecked()) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        if (i + i2 != 1) {
            this.mAdjustTint.setEnabled(true);
            this.mAdjustBrightness.setEnabled(true);
        } else if (this.mAdjustTint.isChecked()) {
            this.mAdjustTint.setEnabled(false);
        } else {
            this.mAdjustBrightness.setEnabled(false);
        }
    }

    public void onTuningChanged(String key, String newValue) {
        boolean z = true;
        SwitchPreference switchPreference;
        if ("brightness_use_twilight".equals(key)) {
            switchPreference = this.mAdjustBrightness;
            if (newValue == null || Integer.parseInt(newValue) == 0) {
                z = false;
            }
            switchPreference.setChecked(z);
        } else if ("tuner_night_mode_adjust_tint".equals(key)) {
            switchPreference = this.mAdjustTint;
            if (newValue != null && Integer.parseInt(newValue) == 0) {
                z = false;
            }
            switchPreference.setChecked(z);
        }
    }

    public void onNightModeChanged() {
        this.mSwitch.setChecked(this.mNightModeController.isEnabled());
    }
}
