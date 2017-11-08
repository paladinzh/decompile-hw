package com.android.settings;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class RadioListPreferenceManager {
    private OnPreferenceChangeListener mOptionChangedListener;
    private OnOptionSelectedListener mOptionSelectedListener;
    private List<RadioListPreference> mPreferences;

    public interface OnOptionSelectedListener {
        boolean isSelectEnabled();

        void onOptionSelected(RadioListPreference radioListPreference, int i);
    }

    private class OptionChangedListener implements OnPreferenceChangeListener {
        private OptionChangedListener() {
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (RadioListPreferenceManager.this.mOptionSelectedListener != null && !RadioListPreferenceManager.this.mOptionSelectedListener.isSelectEnabled()) {
                return false;
            }
            if (preference == null || !(preference instanceof RadioListPreference)) {
                Log.e("RadioListPreferenceManager", "preference is not RadioListPreference!");
                return false;
            }
            int index;
            RadioListPreference changedPref = (RadioListPreference) preference;
            Log.d("RadioListPreferenceManager", "changedPref = " + changedPref.getKey() + ", isChecked = " + changedPref.isChecked() + ", newValue = " + newValue);
            RadioListPreference selectedPref = null;
            int selectedIndex = -1;
            for (index = 0; index < RadioListPreferenceManager.this.mPreferences.size(); index++) {
                RadioListPreference pref = (RadioListPreference) RadioListPreferenceManager.this.mPreferences.get(index);
                if (pref == changedPref && !pref.isChecked()) {
                    Log.d("RadioListPreferenceManager", "option select confirm!");
                    selectedPref = pref;
                    selectedIndex = index;
                }
            }
            if (selectedPref == null) {
                return false;
            }
            if (RadioListPreferenceManager.this.mOptionSelectedListener != null) {
                RadioListPreferenceManager.this.mOptionSelectedListener.onOptionSelected(selectedPref, selectedIndex);
            }
            for (index = 0; index < RadioListPreferenceManager.this.mPreferences.size(); index++) {
                pref = (RadioListPreference) RadioListPreferenceManager.this.mPreferences.get(index);
                if (pref != changedPref && pref.isChecked()) {
                    pref.setChecked(false);
                }
            }
            return true;
        }
    }

    public RadioListPreferenceManager(List<RadioListPreference> radioPrefs) {
        this.mPreferences = radioPrefs;
        if (this.mPreferences == null) {
            this.mPreferences = new ArrayList();
        }
        this.mOptionChangedListener = new OptionChangedListener();
        updateOptionChangedListener();
    }

    private void updateOptionChangedListener() {
        for (RadioListPreference pref : this.mPreferences) {
            pref.setOnPreferenceChangeListener(this.mOptionChangedListener);
        }
    }

    public void setOnOptionSelectedListener(OnOptionSelectedListener listener) {
        this.mOptionSelectedListener = listener;
    }
}
