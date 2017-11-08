package com.android.settings.inputmethod;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.textservice.SpellCheckerInfo;
import android.view.textservice.TextServicesManager;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class InputMethodAndLanguageSettingsHwBase extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    protected PreferenceCategory mAdvancedCategory;
    protected SpellCheckerInfo[] mEnabledScis;
    private PreferenceCategory mSecureIMEPreference;
    private SwitchPreference mSecureIMESwither;
    protected TextServicesManager mTsm;
    protected Preference mVibratePreference;

    protected int getMetricsCategory() {
        return 100000;
    }

    protected void ensureSpellcheckersEntrance() {
        this.mTsm = (TextServicesManager) getSystemService("textservices");
        this.mEnabledScis = this.mTsm.getEnabledSpellCheckers();
        if (this.mEnabledScis == null || this.mEnabledScis.length <= 0) {
            removePreference("advanced_category", "spellcheckers_settings");
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        return super.onPreferenceTreeClick(preference);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    protected void initSecureIMEPreference() {
        this.mSecureIMEPreference = (PreferenceCategory) findPreference("secure_ime_prefence");
        PreferenceScreen prefScreen = getPreferenceScreen();
        if (Utils.hasIntentService(getPackageManager(), "com.huawei.secime.SoftKeyboard")) {
            boolean bSecureOn = Secure.getInt(getContentResolver(), "secure_keyboard", 1) == 1;
            this.mSecureIMESwither = (SwitchPreference) findPreference("secure_input_switcher");
            if (this.mSecureIMESwither != null) {
                this.mSecureIMESwither.setOnPreferenceChangeListener(this);
                Log.d("InputMethodAndLanguageSettingsHwBase", "initSecureIMEPreference secure_on = " + bSecureOn);
                this.mSecureIMESwither.setChecked(bSecureOn);
                return;
            }
            return;
        }
        Log.d("InputMethodAndLanguageSettingsHwBase", "SUCURE ime NOT FOUND!");
        if (prefScreen != null) {
            prefScreen.removePreference(this.mSecureIMEPreference);
        }
        this.mSecureIMEPreference = null;
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        if (this.mSecureIMESwither == null || preference == null || preference != this.mSecureIMESwither) {
            return false;
        }
        int i;
        boolean beChecked = ((Boolean) value).booleanValue();
        ContentResolver contentResolver = getContentResolver();
        String str = "secure_keyboard";
        if (beChecked) {
            i = 1;
        } else {
            i = 0;
        }
        Secure.putInt(contentResolver, str, i);
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, value);
        return true;
    }
}
