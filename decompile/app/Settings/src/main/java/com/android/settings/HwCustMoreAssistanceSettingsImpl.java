package com.android.settings;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;

public class HwCustMoreAssistanceSettingsImpl extends HwCustMoreAssistanceSettings implements OnPreferenceChangeListener {
    private static final String DB_GLOVE_FILE_NODE = "glove_file_node";
    private static final int GLOVE_MODE_STATUS_OFF = 0;
    private static final int GLOVE_MODE_STATUS_ON = 1;
    private static final String KEY_GLOVE_MODE_SETTING = "finger_glove_button_settings";
    private Context mContext;
    private SwitchPreference mGloveModePreference;

    public HwCustMoreAssistanceSettingsImpl(MoreAssistanceSettings moreAssistanceSettings) {
        super(moreAssistanceSettings);
    }

    public void updateCustPreference(Context context) {
        this.mContext = context;
        PreferenceScreen root = this.mMoreAssistanceSettings.getPreferenceScreen();
        this.mMoreAssistanceSettings.getPreferenceManager().inflateFromResource(context, 2131230816, root);
        this.mGloveModePreference = (SwitchPreference) root.findPreference(KEY_GLOVE_MODE_SETTING);
        if (isSupportGloveButton()) {
            this.mGloveModePreference.setOnPreferenceChangeListener(this);
            this.mGloveModePreference.setPersistent(false);
            updateGloveModetatus();
            return;
        }
        root.removePreference(this.mGloveModePreference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mGloveModePreference) {
            setGloveMode(((Boolean) newValue).booleanValue());
        }
        return true;
    }

    private boolean isGloveModeOn() {
        boolean z = true;
        if (this.mContext == null) {
            return false;
        }
        if (1 != System.getInt(this.mContext.getContentResolver(), DB_GLOVE_FILE_NODE, 0)) {
            z = false;
        }
        return z;
    }

    private boolean isSupportGloveButton() {
        if (SystemProperties.getInt("ro.config.hw_glovemode_enabled", 0) == 1) {
            return true;
        }
        return false;
    }

    private void updateGloveModetatus() {
        if (this.mGloveModePreference != null) {
            this.mGloveModePreference.setChecked(isGloveModeOn());
        }
    }

    public void setGloveMode(boolean isGloveMode) {
        if (this.mContext != null) {
            System.putInt(this.mContext.getContentResolver(), DB_GLOVE_FILE_NODE, isGloveMode ? 1 : 0);
        }
    }
}
