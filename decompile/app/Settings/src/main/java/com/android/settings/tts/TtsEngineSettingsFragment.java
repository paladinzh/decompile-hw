package com.android.settings.tts;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TtsEngines;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.settings.SettingsPreferenceFragment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class TtsEngineSettingsFragment extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener {
    private Intent mEngineSettingsIntent;
    private Preference mEngineSettingsPreference;
    private TtsEngines mEnginesHelper;
    private Preference mInstallVoicesPreference;
    private final BroadcastReceiver mLanguagesChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.speech.tts.engine.TTS_DATA_INSTALLED".equals(intent.getAction())) {
                TtsEngineSettingsFragment.this.checkTtsData();
            }
        }
    };
    private ListPreference mLocalePreference;
    private int mSelectedLocaleIndex = -1;
    private TextToSpeech mTts;
    private final OnInitListener mTtsInitListener = new OnInitListener() {
        public void onInit(int status) {
            if (status != 0) {
                TtsEngineSettingsFragment.this.finishFragment();
            } else {
                TtsEngineSettingsFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        TtsEngineSettingsFragment.this.mLocalePreference.setEnabled(true);
                    }
                });
            }
        }
    };
    private Intent mVoiceDataDetails;

    protected int getMetricsCategory() {
        return 93;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230916);
        this.mEnginesHelper = new TtsEngines(getActivity());
        PreferenceScreen root = getPreferenceScreen();
        this.mLocalePreference = (ListPreference) root.findPreference("tts_default_lang");
        this.mLocalePreference.setOnPreferenceChangeListener(this);
        this.mEngineSettingsPreference = root.findPreference("tts_engine_settings");
        this.mEngineSettingsPreference.setOnPreferenceClickListener(this);
        this.mInstallVoicesPreference = root.findPreference("tts_install_data");
        this.mInstallVoicesPreference.setOnPreferenceClickListener(this);
        root.setTitle(getEngineLabel());
        root.setKey(getEngineName());
        this.mEngineSettingsPreference.setTitle(getResources().getString(2131624050, new Object[]{getEngineLabel()}));
        this.mEngineSettingsIntent = this.mEnginesHelper.getSettingsIntent(getEngineName());
        if (this.mEngineSettingsIntent == null) {
            this.mEngineSettingsPreference.setEnabled(false);
        }
        this.mInstallVoicesPreference.setEnabled(false);
        if (savedInstanceState == null) {
            this.mLocalePreference.setEnabled(false);
            this.mLocalePreference.setEntries(new CharSequence[0]);
            this.mLocalePreference.setEntryValues(new CharSequence[0]);
        } else {
            String charSequence;
            boolean z;
            CharSequence[] entries = savedInstanceState.getCharSequenceArray("locale_entries");
            CharSequence[] entryValues = savedInstanceState.getCharSequenceArray("locale_entry_values");
            CharSequence value = savedInstanceState.getCharSequence("locale_value");
            this.mLocalePreference.setEntries(entries);
            this.mLocalePreference.setEntryValues(entryValues);
            ListPreference listPreference = this.mLocalePreference;
            if (value != null) {
                charSequence = value.toString();
            } else {
                charSequence = null;
            }
            listPreference.setValue(charSequence);
            ListPreference listPreference2 = this.mLocalePreference;
            if (entries.length > 0) {
                z = true;
            } else {
                z = false;
            }
            listPreference2.setEnabled(z);
        }
        this.mVoiceDataDetails = (Intent) getArguments().getParcelable("voices");
        this.mTts = new TextToSpeech(getActivity().getApplicationContext(), this.mTtsInitListener, getEngineName());
        updateVoiceDetails(this.mVoiceDataDetails);
        checkTtsData();
        getActivity().registerReceiver(this.mLanguagesChangedReceiver, new IntentFilter("android.speech.tts.engine.TTS_DATA_INSTALLED"));
    }

    public void onDestroy() {
        getActivity().unregisterReceiver(this.mLanguagesChangedReceiver);
        this.mTts.shutdown();
        super.onDestroy();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequenceArray("locale_entries", this.mLocalePreference.getEntries());
        outState.putCharSequenceArray("locale_entry_values", this.mLocalePreference.getEntryValues());
        outState.putCharSequence("locale_value", this.mLocalePreference.getValue());
    }

    private final void checkTtsData() {
        Intent intent = new Intent("android.speech.tts.engine.CHECK_TTS_DATA");
        intent.setPackage(getEngineName());
        try {
            startActivityForResult(intent, 1977);
        } catch (ActivityNotFoundException e) {
            Log.e("TtsEngineSettings", "Failed to check TTS data, no activity found for " + intent + ")");
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1977) {
            return;
        }
        if (resultCode != 0) {
            updateVoiceDetails(data);
        } else {
            Log.e("TtsEngineSettings", "CheckVoiceData activity failed");
        }
    }

    private void updateVoiceDetails(Intent data) {
        if (data == null) {
            this.mInstallVoicesPreference.setEnabled(false);
            this.mLocalePreference.setEnabled(false);
            Log.e("TtsEngineSettings", "Engine failed voice data integrity check (null return)" + this.mTts.getCurrentEngine());
            return;
        }
        this.mVoiceDataDetails = data;
        ArrayList<String> available = this.mVoiceDataDetails.getStringArrayListExtra("availableVoices");
        ArrayList<String> unavailable = this.mVoiceDataDetails.getStringArrayListExtra("unavailableVoices");
        if (unavailable == null || unavailable.size() <= 0) {
            this.mInstallVoicesPreference.setEnabled(false);
        } else {
            this.mInstallVoicesPreference.setEnabled(true);
        }
        if (available == null) {
            Log.e("TtsEngineSettings", "TTS data check failed (available == null).");
            this.mLocalePreference.setEnabled(false);
            return;
        }
        updateDefaultLocalePref(available);
    }

    private void updateDefaultLocalePref(ArrayList<String> availableLangs) {
        if (availableLangs == null || availableLangs.size() == 0) {
            this.mLocalePreference.setEnabled(false);
            return;
        }
        int i;
        Object currentLocale = null;
        if (!this.mEnginesHelper.isLocaleSetToDefaultForEngine(getEngineName())) {
            currentLocale = this.mEnginesHelper.getLocalePrefForEngine(getEngineName());
        }
        ArrayList<Pair<String, Locale>> entryPairs = new ArrayList(availableLangs.size());
        for (i = 0; i < availableLangs.size(); i++) {
            Locale locale = this.mEnginesHelper.parseLocaleString((String) availableLangs.get(i));
            if (locale != null) {
                entryPairs.add(new Pair(locale.getDisplayName(), locale));
            }
        }
        Collections.sort(entryPairs, new Comparator<Pair<String, Locale>>() {
            public int compare(Pair<String, Locale> lhs, Pair<String, Locale> rhs) {
                return ((String) lhs.first).compareToIgnoreCase((String) rhs.first);
            }
        });
        this.mSelectedLocaleIndex = 0;
        CharSequence[] entries = new CharSequence[(availableLangs.size() + 1)];
        CharSequence[] entryValues = new CharSequence[(availableLangs.size() + 1)];
        entries[0] = getActivity().getString(2131624035);
        entryValues[0] = "";
        i = 1;
        for (Pair<String, Locale> entry : entryPairs) {
            if (((Locale) entry.second).equals(currentLocale)) {
                this.mSelectedLocaleIndex = i;
            }
            entries[i] = (CharSequence) entry.first;
            int i2 = i + 1;
            entryValues[i] = ((Locale) entry.second).toString();
            i = i2;
        }
        this.mLocalePreference.setEntries(entries);
        this.mLocalePreference.setEntryValues(entryValues);
        this.mLocalePreference.setEnabled(true);
        setLocalePreference(this.mSelectedLocaleIndex);
    }

    private void setLocalePreference(int index) {
        if (index < 0) {
            this.mLocalePreference.setValue("");
            this.mLocalePreference.setSummary(2131624036);
            return;
        }
        this.mLocalePreference.setValueIndex(index);
        this.mLocalePreference.setSummary(this.mLocalePreference.getEntries()[index]);
    }

    private void installVoiceData() {
        if (!TextUtils.isEmpty(getEngineName())) {
            Intent intent = new Intent("android.speech.tts.engine.INSTALL_TTS_DATA");
            intent.setPackage(getEngineName());
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e("TtsEngineSettings", "Failed to install TTS data, no acitivty found for " + intent + ")");
            }
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference == this.mInstallVoicesPreference) {
            installVoiceData();
            return true;
        } else if (preference != this.mEngineSettingsPreference) {
            return false;
        } else {
            startActivity(this.mEngineSettingsIntent);
            return true;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference != this.mLocalePreference) {
            return false;
        }
        String localeString = (String) newValue;
        updateLanguageTo(!TextUtils.isEmpty(localeString) ? this.mEnginesHelper.parseLocaleString(localeString) : null);
        return true;
    }

    private void updateLanguageTo(Locale locale) {
        int selectedLocaleIndex = -1;
        String localeString = locale != null ? locale.toString() : "";
        for (int i = 0; i < this.mLocalePreference.getEntryValues().length; i++) {
            if (localeString.equalsIgnoreCase(this.mLocalePreference.getEntryValues()[i].toString())) {
                selectedLocaleIndex = i;
                break;
            }
        }
        if (selectedLocaleIndex == -1) {
            Log.w("TtsEngineSettings", "updateLanguageTo called with unknown locale argument");
            return;
        }
        this.mLocalePreference.setSummary(this.mLocalePreference.getEntries()[selectedLocaleIndex]);
        this.mSelectedLocaleIndex = selectedLocaleIndex;
        this.mEnginesHelper.updateLocalePrefForEngine(getEngineName(), locale);
        if (getEngineName().equals(this.mTts.getCurrentEngine())) {
            TextToSpeech textToSpeech = this.mTts;
            if (locale == null) {
                locale = Locale.getDefault();
            }
            textToSpeech.setLanguage(locale);
        }
    }

    private String getEngineName() {
        return getArguments().getString("name");
    }

    private String getEngineLabel() {
        return getArguments().getString("label");
    }
}
