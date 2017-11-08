package com.android.settings.tts;

import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.EngineInfo;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TtsEngines;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Checkable;
import com.android.internal.app.LocaleStore.LocaleInfo;
import com.android.settings.SeekBarPreference;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.localepicker.LocaleListHelper;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.tts.TtsEnginePreference.RadioButtonGroupState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Set;

public class TextToSpeechSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener, RadioButtonGroupState, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> indexables = new ArrayList();
            SearchIndexableResource indexable = new SearchIndexableResource(context);
            indexable.xmlResId = 2131230917;
            indexables.add(indexable);
            return indexables;
        }

        public List<String> getNonIndexableKeys(Context context) {
            return new ArrayList();
        }
    };
    private List<String> mAvailableStrLocals;
    private Checkable mCurrentChecked;
    private Locale mCurrentDefaultLocale;
    private String mCurrentEngine;
    private int mDefaultPitch = 100;
    private SeekBarPreference mDefaultPitchPref;
    private int mDefaultRate = 100;
    private SeekBarPreference mDefaultRatePref;
    private PreferenceCategory mEnginePreferenceCategory;
    private Preference mEngineStatus;
    private TtsEngines mEnginesHelper = null;
    Handler mHandler = new Handler();
    private final OnInitListener mInitListener = new OnInitListener() {
        public void onInit(int status) {
            TextToSpeechSettings.this.onInitEngine(status);
        }
    };
    private Preference mPlayExample;
    private String mPreviousEngine;
    private Preference mResetSpeechPitch;
    private Preference mResetSpeechRate;
    private String mSampleText = null;
    private TextToSpeech mTts = null;
    private final OnInitListener mUpdateListener = new OnInitListener() {
        public void onInit(int status) {
            TextToSpeechSettings.this.onUpdateEngine(status);
        }
    };

    protected int getMetricsCategory() {
        return 94;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230917);
        getActivity().setVolumeControlStream(3);
        this.mPlayExample = findPreference("tts_play_example");
        this.mPlayExample.setOnPreferenceClickListener(this);
        this.mPlayExample.setEnabled(false);
        this.mResetSpeechRate = findPreference("reset_speech_rate");
        this.mResetSpeechRate.setOnPreferenceClickListener(this);
        this.mResetSpeechPitch = findPreference("reset_speech_pitch");
        this.mResetSpeechPitch.setOnPreferenceClickListener(this);
        this.mEnginePreferenceCategory = (PreferenceCategory) findPreference("tts_engine_preference_section");
        this.mDefaultPitchPref = (SeekBarPreference) findPreference("tts_default_pitch");
        this.mDefaultRatePref = (SeekBarPreference) findPreference("tts_default_rate");
        this.mEngineStatus = findPreference("tts_status");
        updateEngineStatus(2131624049);
        this.mTts = new TextToSpeech(getActivity().getApplicationContext(), this.mInitListener);
        this.mEnginesHelper = new TtsEngines(getActivity().getApplicationContext());
        setTtsUtteranceProgressListener();
        initSettings();
        setRetainInstance(true);
    }

    public void onResume() {
        super.onResume();
        if (this.mTts != null && this.mCurrentDefaultLocale != null) {
            Locale ttsDefaultLocale = this.mTts.getDefaultLanguage();
            if (!(this.mCurrentDefaultLocale == null || this.mCurrentDefaultLocale.equals(ttsDefaultLocale))) {
                updateWidgetState(false);
                checkDefaultLocale();
            }
        }
    }

    private void setTtsUtteranceProgressListener() {
        if (this.mTts != null) {
            this.mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                public void onStart(String utteranceId) {
                }

                public void onDone(String utteranceId) {
                }

                public void onError(String utteranceId) {
                    Log.e("TextToSpeechSettings", "Error while trying to synthesize sample text");
                }
            });
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mTts != null) {
            this.mTts.shutdown();
            this.mTts = null;
        }
    }

    private void initSettings() {
        ContentResolver resolver = getContentResolver();
        this.mDefaultRate = Secure.getInt(resolver, "tts_default_rate", 100);
        this.mDefaultPitch = Secure.getInt(resolver, "tts_default_pitch", 100);
        this.mDefaultRatePref.setProgress(getSeekBarProgressFromValue("tts_default_rate", this.mDefaultRate));
        this.mDefaultRatePref.setOnPreferenceChangeListener(this);
        this.mDefaultRatePref.setMax(getSeekBarProgressFromValue("tts_default_rate", 600));
        this.mDefaultPitchPref.setProgress(getSeekBarProgressFromValue("tts_default_pitch", this.mDefaultPitch));
        this.mDefaultPitchPref.setOnPreferenceChangeListener(this);
        this.mDefaultPitchPref.setMax(getSeekBarProgressFromValue("tts_default_pitch", 400));
        if (this.mTts != null) {
            this.mCurrentEngine = this.mTts.getCurrentEngine();
            this.mTts.setSpeechRate(((float) this.mDefaultRate) / 100.0f);
            this.mTts.setPitch(((float) this.mDefaultPitch) / 100.0f);
        }
        if (getActivity() instanceof SettingsActivity) {
            SettingsActivity activity = (SettingsActivity) getActivity();
            this.mEnginePreferenceCategory.removeAll();
            List<EngineInfo> engines = this.mEnginesHelper.getEngines();
            boolean isCN = ((LocaleInfo) new LocaleListHelper().getFeedsList().get(0)).toString().startsWith("zh");
            PackageManager mPME = getActivity().getPackageManager();
            for (EngineInfo engine : engines) {
                TtsEnginePreference enginePref = new TtsEnginePreference(getPrefContext(), engine, this, activity);
                if (SystemProperties.getBoolean("ro.talkback.chn_enable", true)) {
                    if (Utils.isChinaArea() && Utils.hasPackageInfo(mPME, "com.iflytek.speechsuite") && Utils.hasPackageInfo(mPME, "com.svox.pico")) {
                        if (engine.name.equals("com.iflytek.speechsuite") && !isCN) {
                            enginePref.setEnabled(false);
                        }
                        if (engine.name.equals("com.svox.pico") && isCN) {
                            enginePref.setEnabled(false);
                        }
                    }
                    this.mEnginePreferenceCategory.addPreference(enginePref);
                } else if (!engine.name.equals("com.iflytek.speechsuite")) {
                    this.mEnginePreferenceCategory.addPreference(enginePref);
                }
            }
            checkVoiceData(this.mCurrentEngine);
            return;
        }
        throw new IllegalStateException("TextToSpeechSettings used outside a Settings");
    }

    private int getValueFromSeekBarProgress(String preferenceKey, int progress) {
        if (preferenceKey.equals("tts_default_rate")) {
            return progress + 10;
        }
        if (preferenceKey.equals("tts_default_pitch")) {
            return progress + 25;
        }
        return progress;
    }

    private int getSeekBarProgressFromValue(String preferenceKey, int value) {
        if (preferenceKey.equals("tts_default_rate")) {
            return value - 10;
        }
        if (preferenceKey.equals("tts_default_pitch")) {
            return value - 25;
        }
        return value;
    }

    public void onInitEngine(int status) {
        if (status == 0) {
            checkDefaultLocale();
        } else {
            updateWidgetState(false);
        }
    }

    private void checkDefaultLocale() {
        if (this.mTts != null) {
            Locale defaultLocale = this.mTts.getDefaultLanguage();
            if (defaultLocale == null) {
                Log.e("TextToSpeechSettings", "Failed to get default language from engine " + this.mCurrentEngine);
                updateWidgetState(false);
                updateEngineStatus(2131624048);
                return;
            }
            Locale oldDefaultLocale = this.mCurrentDefaultLocale;
            this.mCurrentDefaultLocale = this.mEnginesHelper.parseLocaleString(defaultLocale.toString());
            if (!Objects.equals(oldDefaultLocale, this.mCurrentDefaultLocale)) {
                this.mSampleText = null;
            }
            int defaultAvailable = this.mTts.setLanguage(defaultLocale);
            if (evaluateDefaultLocale() && this.mSampleText == null) {
                updateSampleTextWithDelay(1000);
            }
        }
    }

    private boolean evaluateDefaultLocale() {
        if (this.mCurrentDefaultLocale == null || this.mAvailableStrLocals == null) {
            return false;
        }
        boolean notInAvailableLangauges = true;
        try {
            String defaultLocaleStr = this.mCurrentDefaultLocale.getISO3Language();
            if (!TextUtils.isEmpty(this.mCurrentDefaultLocale.getISO3Country())) {
                defaultLocaleStr = defaultLocaleStr + "-" + this.mCurrentDefaultLocale.getISO3Country();
            }
            if (!TextUtils.isEmpty(this.mCurrentDefaultLocale.getVariant())) {
                defaultLocaleStr = defaultLocaleStr + "-" + this.mCurrentDefaultLocale.getVariant();
            }
            for (String loc : this.mAvailableStrLocals) {
                if (loc.equalsIgnoreCase(defaultLocaleStr)) {
                    notInAvailableLangauges = false;
                    break;
                }
            }
            int defaultAvailable = this.mTts.setLanguage(this.mCurrentDefaultLocale);
            if (defaultAvailable == -2 || defaultAvailable == -1 || notInAvailableLangauges) {
                updateEngineStatus(2131624048);
                updateWidgetState(false);
                return false;
            }
            if (isNetworkRequiredForSynthesis()) {
                updateEngineStatus(2131624047);
            } else {
                updateEngineStatus(2131624046);
            }
            updateWidgetState(true);
            return true;
        } catch (MissingResourceException e) {
            updateEngineStatus(2131624048);
            updateWidgetState(false);
            return false;
        }
    }

    private void updateSampleTextWithDelay(long delay) {
        this.mPlayExample.setEnabled(false);
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                if (TextToSpeechSettings.this.isAdded() && TextToSpeechSettings.this.mTts != null) {
                    TextToSpeechSettings.this.getSampleText();
                }
            }
        }, delay);
    }

    private void getSampleText() {
        String currentEngine = this.mTts.getCurrentEngine();
        if (TextUtils.isEmpty(currentEngine)) {
            currentEngine = this.mTts.getDefaultEngine();
        }
        Intent intent = new Intent("android.speech.tts.engine.GET_SAMPLE_TEXT");
        intent.putExtra("language", this.mCurrentDefaultLocale.getLanguage());
        intent.putExtra("country", this.mCurrentDefaultLocale.getCountry());
        intent.putExtra("variant", this.mCurrentDefaultLocale.getVariant());
        intent.setPackage(currentEngine);
        try {
            startActivityForResult(intent, 1983);
        } catch (ActivityNotFoundException e) {
            Log.e("TextToSpeechSettings", "Failed to get sample text, no activity found for " + intent + ")");
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1983) {
            if (isAdded()) {
                onSampleTextReceived(resultCode, data);
            }
        } else if (requestCode == 1977) {
            onVoiceDataIntegrityCheckDone(data);
        }
    }

    private String getDefaultSampleString() {
        if (!(this.mTts == null || this.mTts.getLanguage() == null)) {
            try {
                String currentLang = this.mTts.getLanguage().getISO3Language();
                String[] strings = getActivity().getResources().getStringArray(2131361825);
                String[] langs = getActivity().getResources().getStringArray(2131361826);
                for (int i = 0; i < strings.length; i++) {
                    if (langs[i].equals(currentLang)) {
                        return strings[i];
                    }
                }
            } catch (MissingResourceException e) {
            }
        }
        return getString(2131624044);
    }

    private boolean isNetworkRequiredForSynthesis() {
        boolean z = false;
        Set<String> features = this.mTts.getFeatures(this.mCurrentDefaultLocale);
        if (features == null) {
            return false;
        }
        if (features.contains("networkTts") && !features.contains("embeddedTts")) {
            z = true;
        }
        return z;
    }

    private void onSampleTextReceived(int resultCode, Intent data) {
        String sample = getDefaultSampleString();
        if (!(resultCode != 0 || data == null || data == null || data.getStringExtra("sampleText") == null)) {
            sample = data.getStringExtra("sampleText");
        }
        this.mSampleText = sample;
        if (this.mSampleText != null) {
            updateWidgetState(true);
        } else {
            Log.e("TextToSpeechSettings", "Did not have a sample string for the requested language. Using default");
        }
    }

    private void speakSampleText() {
        boolean networkRequired = isNetworkRequiredForSynthesis();
        if (!networkRequired || (networkRequired && this.mTts.isLanguageAvailable(this.mCurrentDefaultLocale) >= 0)) {
            HashMap<String, String> params = new HashMap();
            params.put("utteranceId", "Sample");
            this.mTts.speak(this.mSampleText, 0, params);
            return;
        }
        Log.w("TextToSpeechSettings", "Network required for sample synthesis for requested language");
        displayNetworkAlert();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if ("tts_default_rate".equals(preference.getKey())) {
            updateSpeechRate(((Integer) objValue).intValue());
        } else if ("tts_default_pitch".equals(preference.getKey())) {
            updateSpeechPitchValue(((Integer) objValue).intValue());
        }
        return true;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference == this.mPlayExample) {
            speakSampleText();
            return true;
        } else if (preference == this.mResetSpeechRate) {
            int speechRateSeekbarProgress = getSeekBarProgressFromValue("tts_default_rate", 100);
            this.mDefaultRatePref.setProgress(speechRateSeekbarProgress);
            updateSpeechRate(speechRateSeekbarProgress);
            return true;
        } else if (preference != this.mResetSpeechPitch) {
            return false;
        } else {
            int pitchSeekbarProgress = getSeekBarProgressFromValue("tts_default_pitch", 100);
            this.mDefaultPitchPref.setProgress(pitchSeekbarProgress);
            updateSpeechPitchValue(pitchSeekbarProgress);
            return true;
        }
    }

    private void updateSpeechRate(int speechRateSeekBarProgress) {
        this.mDefaultRate = getValueFromSeekBarProgress("tts_default_rate", speechRateSeekBarProgress);
        try {
            Secure.putInt(getContentResolver(), "tts_default_rate", this.mDefaultRate);
            if (this.mTts != null) {
                this.mTts.setSpeechRate(((float) this.mDefaultRate) / 100.0f);
            }
        } catch (NumberFormatException e) {
            Log.e("TextToSpeechSettings", "could not persist default TTS rate setting", e);
        }
    }

    private void updateSpeechPitchValue(int speechPitchSeekBarProgress) {
        this.mDefaultPitch = getValueFromSeekBarProgress("tts_default_pitch", speechPitchSeekBarProgress);
        try {
            Secure.putInt(getContentResolver(), "tts_default_pitch", this.mDefaultPitch);
            if (this.mTts != null) {
                this.mTts.setPitch(((float) this.mDefaultPitch) / 100.0f);
            }
        } catch (NumberFormatException e) {
            Log.e("TextToSpeechSettings", "could not persist default TTS pitch setting", e);
        }
    }

    private void updateWidgetState(boolean enable) {
        this.mPlayExample.setEnabled(enable);
        this.mDefaultRatePref.setEnabled(enable);
        this.mEngineStatus.setEnabled(enable);
    }

    private void updateEngineStatus(int resourceId) {
        Locale locale = this.mCurrentDefaultLocale;
        if (locale == null) {
            locale = Locale.getDefault();
        }
        this.mEngineStatus.setSummary(getString(resourceId, new Object[]{locale.getDisplayName()}));
    }

    private void displayNetworkAlert() {
        Builder builder = new Builder(getActivity());
        builder.setTitle(17039380).setMessage(getActivity().getString(2131624043)).setCancelable(false).setPositiveButton(17039370, null);
        builder.create().show();
    }

    private void updateDefaultEngine(String engine) {
        updateWidgetState(false);
        updateEngineStatus(2131624049);
        this.mPreviousEngine = this.mTts.getCurrentEngine();
        if (this.mTts != null) {
            try {
                this.mTts.shutdown();
                this.mTts = null;
            } catch (Exception e) {
                Log.e("TextToSpeechSettings", "Error shutting down TTS engine" + e);
            }
        }
        this.mTts = new TextToSpeech(getActivity().getApplicationContext(), this.mUpdateListener, engine);
        setTtsUtteranceProgressListener();
    }

    public void onUpdateEngine(int status) {
        if (status == 0) {
            checkVoiceData(this.mTts.getCurrentEngine());
            return;
        }
        if (this.mPreviousEngine != null) {
            this.mTts = new TextToSpeech(getActivity().getApplicationContext(), this.mInitListener, this.mPreviousEngine);
            setTtsUtteranceProgressListener();
        }
        this.mPreviousEngine = null;
    }

    private void checkVoiceData(String engine) {
        Intent intent = new Intent("android.speech.tts.engine.CHECK_TTS_DATA");
        intent.setPackage(engine);
        try {
            startActivityForResult(intent, 1977);
        } catch (ActivityNotFoundException e) {
            Log.e("TextToSpeechSettings", "Failed to check TTS data, no activity found for " + intent + ")");
        }
    }

    private void onVoiceDataIntegrityCheckDone(Intent data) {
        String engine = this.mTts.getCurrentEngine();
        if (engine == null) {
            Log.e("TextToSpeechSettings", "Voice data check complete, but no engine bound");
        } else if (data == null) {
            Log.e("TextToSpeechSettings", "Engine failed voice data integrity check (null return)" + this.mTts.getCurrentEngine());
        } else {
            Secure.putString(getContentResolver(), "tts_default_synth", engine);
            this.mAvailableStrLocals = data.getStringArrayListExtra("availableVoices");
            if (this.mAvailableStrLocals == null) {
                Log.e("TextToSpeechSettings", "Voice data check complete, but no available voices found");
                this.mAvailableStrLocals = new ArrayList();
            }
            if (evaluateDefaultLocale()) {
                updateSampleTextWithDelay(1000);
            }
            int engineCount = this.mEnginePreferenceCategory.getPreferenceCount();
            for (int i = 0; i < engineCount; i++) {
                Preference p = this.mEnginePreferenceCategory.getPreference(i);
                if (p instanceof TtsEnginePreference) {
                    TtsEnginePreference enginePref = (TtsEnginePreference) p;
                    if (enginePref.getKey().equals(engine)) {
                        enginePref.setVoiceDataDetails(data);
                        break;
                    }
                }
            }
        }
    }

    public Checkable getCurrentChecked() {
        return this.mCurrentChecked;
    }

    public String getCurrentKey() {
        return this.mCurrentEngine;
    }

    public void setCurrentChecked(Checkable current) {
        this.mCurrentChecked = current;
    }

    public void setCurrentKey(String key) {
        this.mCurrentEngine = key;
        updateDefaultEngine(this.mCurrentEngine);
    }
}
