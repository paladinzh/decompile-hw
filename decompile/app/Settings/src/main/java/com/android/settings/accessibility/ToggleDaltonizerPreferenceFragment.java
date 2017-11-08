package com.android.settings.accessibility;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import android.view.View;
import com.android.settings.ItemUseStat;

public class ToggleDaltonizerPreferenceFragment extends ToggleFeaturePreferenceFragment implements OnPreferenceChangeListener {
    private ListPreference mType;

    protected int getMetricsCategory() {
        return 5;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230723);
        this.mType = (ListPreference) findPreference("type");
        initPreferences();
    }

    protected void onPreferenceToggled(String preferenceKey, boolean enabled) {
        int i;
        Secure.putInt(getContentResolver(), "accessibility_display_daltonizer", Secure.getInt(getContentResolver(), "accessibility_display_daltonizer", -1));
        ContentResolver contentResolver = getContentResolver();
        String str = "accessibility_display_daltonizer_enabled";
        if (enabled) {
            i = 1;
        } else {
            i = 0;
        }
        Secure.putInt(contentResolver, str, i);
        if (enabled) {
            this.mType.setEnabled(true);
        } else {
            this.mType.setEnabled(false);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mType) {
            ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mType, ItemUseStat.KEY_CORRECTION_MODE, (String) newValue);
            Secure.putInt(getContentResolver(), "accessibility_display_daltonizer", Integer.parseInt((String) newValue));
            preference.setSummary((CharSequence) "%s");
        } else if (this.mToggleSwitch == preference) {
            boolean value = ((Boolean) newValue).booleanValue();
            getArguments().putBoolean("checked", value);
            onPreferenceToggled(this.mPreferenceKey, value);
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        }
        return true;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(getString(2131624207));
    }

    private void initPreferences() {
        boolean z = true;
        int iValue = Secure.getInt(getContentResolver(), "accessibility_display_daltonizer", -1);
        String value = Integer.toString(iValue);
        this.mType.setValue(value);
        this.mType.setOnPreferenceChangeListener(this);
        int index = this.mType.findIndexOfValue(value);
        if (iValue == -1) {
            this.mType.setSummary(2131626945);
        } else if (index < 0) {
            this.mType.setSummary(getString(2131624209, new Object[]{getString(2131624146)}));
        }
        ListPreference listPreference = this.mType;
        if (Secure.getInt(getContentResolver(), "accessibility_display_daltonizer_enabled", 0) != 1) {
            z = false;
        }
        listPreference.setEnabled(z);
    }

    protected void onInstallToggleSwitch() {
        boolean z = true;
        this.mToggleSwitch.setKey("daltonizer");
        this.mToggleSwitch.setTitle(2131624207);
        TwoStatePreference twoStatePreference = this.mToggleSwitch;
        if (Secure.getInt(getContentResolver(), "accessibility_display_daltonizer_enabled", 0) != 1) {
            z = false;
        }
        twoStatePreference.setChecked(z);
        this.mToggleSwitch.setOnPreferenceChangeListener(this);
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        return super.onPreferenceTreeClick(preference);
    }
}
