package com.android.settings.inputmethod;

import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.hardware.input.InputDeviceIdentifier;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.hardware.input.KeyboardLayout;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.speech.tts.TtsEngines;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.view.InputDevice;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.view.textservice.SpellCheckerInfo;
import android.view.textservice.TextServicesManager;
import com.android.internal.app.LocaleHelper;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocaleStore;
import com.android.settings.ItemUseStat;
import com.android.settings.Settings.KeyboardLayoutPickerActivity;
import com.android.settings.SubSettings;
import com.android.settings.UserDictionarySettings;
import com.android.settings.Utils;
import com.android.settings.VoiceInputOutputSettings;
import com.android.settings.accessibility.AccessibilityExtUtils;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.inputmethod.KeyboardLayoutDialogFragment.OnSetupKeyboardLayoutsListener;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class InputMethodAndLanguageSettings extends InputMethodAndLanguageSettingsHwBase implements OnPreferenceChangeListener, InputDeviceListener, OnSetupKeyboardLayoutsListener, Indexable, OnSavePreferenceListener {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            SearchIndexableRaw indexable;
            List<SearchIndexableRaw> indexables = new ArrayList();
            String screenTitle = context.getString(2131625746);
            if (context.getAssets().getLocales().length > 1) {
                String localeNames = InputMethodAndLanguageSettings.getLocaleNames(context);
                indexable = new SearchIndexableRaw(context);
                indexable.key = "phone_language";
                indexable.title = context.getString(2131625749);
                indexable.summaryOn = localeNames;
                indexable.summaryOff = localeNames;
                indexable.screenTitle = screenTitle;
                indexables.add(indexable);
            }
            indexable = new SearchIndexableRaw(context);
            indexable.key = "spellcheckers_settings";
            indexable.title = context.getString(2131626420);
            indexable.screenTitle = screenTitle;
            indexable.keywords = context.getString(2131626655);
            indexables.add(indexable);
            InputMethodAndLanguageSettings.addInputMethodSearchIndex(indexables, context);
            InputMethodAndLanguageSettings.addUserDictionarySearchIndex(indexables, context);
            indexable = new SearchIndexableRaw(context);
            indexable.key = "keyboard_settings";
            indexable.title = context.getString(2131625748);
            indexable.screenTitle = screenTitle;
            indexable.keywords = context.getString(2131626669);
            indexables.add(indexable);
            InputMethodSettingValuesWrapper immValues = InputMethodSettingValuesWrapper.getInstance(context);
            immValues.refreshAllInputMethodAndSubtypes();
            String currImeName = immValues.getCurrentInputMethodName(context).toString();
            indexable = new SearchIndexableRaw(context);
            indexable.key = "current_input_method";
            indexable.title = context.getString(2131625810);
            indexable.summaryOn = currImeName;
            indexable.summaryOff = currImeName;
            indexable.screenTitle = screenTitle;
            indexables.add(indexable);
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService("input_method");
            List<InputMethodInfo> inputMethods = immValues.getInputMethodList();
            int inputMethodCount = inputMethods == null ? 0 : inputMethods.size();
            for (int i = 0; i < inputMethodCount; i++) {
                InputMethodInfo inputMethod = (InputMethodInfo) inputMethods.get(i);
                String summary = InputMethodAndSubtypeUtil.getSubtypeLocaleNameListAsSentence(inputMethodManager.getEnabledInputMethodSubtypeList(inputMethod, true), context, inputMethod);
                ServiceInfo serviceInfo = inputMethod.getServiceInfo();
                ComponentName componentName = new ComponentName(serviceInfo.packageName, serviceInfo.name);
                indexable = new SearchIndexableRaw(context);
                indexable.key = componentName.flattenToString();
                indexable.title = inputMethod.loadLabel(context.getPackageManager()).toString();
                indexable.summaryOn = summary;
                indexable.summaryOff = summary;
                indexable.screenTitle = screenTitle;
                indexables.add(indexable);
            }
            if (!new TtsEngines(context).getEngines().isEmpty()) {
                indexable = new SearchIndexableRaw(context);
                indexable.key = "tts_settings";
                indexable.title = context.getString(2131624029);
                indexable.screenTitle = screenTitle;
                indexable.keywords = context.getString(2131626657);
                indexables.add(indexable);
            }
            indexable = new SearchIndexableRaw(context);
            indexable.key = "pointer_speed";
            indexable.title = context.getString(2131625777);
            indexable.screenTitle = screenTitle;
            indexables.add(indexable);
            if (InputMethodAndLanguageSettings.haveInputDeviceWithVibrator()) {
                indexable = new SearchIndexableRaw(context);
                indexable.key = "vibrate_input_devices";
                indexable.title = context.getString(2131625779);
                indexable.summaryOn = context.getString(2131625780);
                indexable.summaryOff = context.getString(2131625780);
                indexable.screenTitle = screenTitle;
                indexables.add(indexable);
            }
            indexable = new SearchIndexableRaw(context);
            indexable.title = screenTitle;
            indexable.screenTitle = screenTitle;
            indexables.add(indexable);
            return indexables;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private int mDefaultInputMethodSelectorVisibility = 0;
    private DevicePolicyManager mDpm;
    private Handler mHandler;
    private PreferenceCategory mHardKeyboardCategory;
    private final ArrayList<PreferenceScreen> mHardKeyboardPreferenceList = new ArrayList();
    private InputManager mIm;
    private InputMethodManager mImm;
    private final ArrayList<InputMethodPreference> mInputMethodPreferenceList = new ArrayList();
    private InputMethodSettingValuesWrapper mInputMethodSettingValues;
    private Intent mIntentWaitingForResult;
    private PreferenceCategory mKeyboardSettingsCategory;
    private Preference mLangPref;
    private Preference mLanguagePref;
    private SettingsObserver mSettingsObserver;
    private boolean mShowsOnlyFullImeAndKeyboardList;

    private class SettingsObserver extends ContentObserver {
        private Context mContext;

        public SettingsObserver(Handler handler, Context context) {
            super(handler);
            this.mContext = context;
        }

        public void onChange(boolean selfChange) {
            InputMethodAndLanguageSettings.this.updateCurrentImeName();
        }

        public void resume() {
            ContentResolver cr = this.mContext.getContentResolver();
            cr.registerContentObserver(Secure.getUriFor("default_input_method"), false, this);
            cr.registerContentObserver(Secure.getUriFor("selected_input_method_subtype"), false, this);
        }

        public void pause() {
            this.mContext.getContentResolver().unregisterContentObserver(this);
        }
    }

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                this.mSummaryLoader.setSummary(this, InputMethodAndLanguageSettings.getLocaleNames(this.mContext));
            }
        }
    }

    protected int getMetricsCategory() {
        return 57;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230804);
        Activity activity = getActivity();
        this.mImm = (InputMethodManager) getSystemService("input_method");
        this.mInputMethodSettingValues = InputMethodSettingValuesWrapper.getInstance(activity);
        try {
            this.mDefaultInputMethodSelectorVisibility = Integer.valueOf(getString(2131624343)).intValue();
        } catch (NumberFormatException e) {
        }
        this.mLangPref = findPreference("phone_language");
        if (activity.getAssets().getLocales().length == 1) {
            getPreferenceScreen().removePreference(this.mLangPref);
        }
        new VoiceInputOutputSettings(this).onCreate();
        this.mHardKeyboardCategory = (PreferenceCategory) findPreference("hard_keyboard");
        this.mKeyboardSettingsCategory = (PreferenceCategory) findPreference("keyboard_settings_category");
        this.mAdvancedCategory = (PreferenceCategory) findPreference("advanced_category");
        this.mVibratePreference = findPreference("vibrate_input_devices");
        initSecureIMEPreference();
        Intent startingIntent = activity.getIntent();
        this.mShowsOnlyFullImeAndKeyboardList = "android.settings.INPUT_METHOD_SETTINGS".equals(startingIntent.getAction());
        if (this.mShowsOnlyFullImeAndKeyboardList) {
            getPreferenceScreen().removeAll();
            getPreferenceScreen().addPreference(this.mHardKeyboardCategory);
            this.mKeyboardSettingsCategory.removeAll();
            getPreferenceScreen().addPreference(this.mKeyboardSettingsCategory);
        }
        this.mIm = (InputManager) activity.getSystemService("input");
        updateInputDevices();
        Preference spellChecker = findPreference("spellcheckers_settings");
        if (spellChecker != null) {
            InputMethodAndSubtypeUtil.removeUnnecessaryNonPersistentPreference(spellChecker);
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setClass(activity, SubSettings.class);
            intent.putExtra(":settings:show_fragment", SpellCheckersSettings.class.getName());
            intent.putExtra(":settings:show_fragment_title_resid", 2131626420);
            spellChecker.setIntent(intent);
        }
        this.mHandler = new Handler();
        this.mSettingsObserver = new SettingsObserver(this.mHandler, activity);
        this.mDpm = (DevicePolicyManager) getActivity().getSystemService("device_policy");
        InputDeviceIdentifier identifier = (InputDeviceIdentifier) startingIntent.getParcelableExtra("input_device_identifier");
        if (this.mShowsOnlyFullImeAndKeyboardList && identifier != null) {
            showKeyboardLayoutDialog(identifier);
        }
    }

    private void updateUserDictionaryPreference(Preference userDictionaryPreference) {
        int i = 0;
        if (userDictionaryPreference != null) {
            final TreeSet<String> localeSet = UserDictionaryList.getUserDictionaryLocalesSet(getActivity());
            boolean disableDictionary = true;
            String[] checkingPackageArray = "com.google.android.inputmethod.latin;com.android.inputmethod.latin".split(";");
            int length = checkingPackageArray.length;
            while (i < length) {
                try {
                    if (getPackageManager().getApplicationInfo(checkingPackageArray[i], 0) != null) {
                        disableDictionary = false;
                        break;
                    }
                    i++;
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if (localeSet != null && !disableDictionary) {
                userDictionaryPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference arg0) {
                        Class<? extends Fragment> targetFragment;
                        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(InputMethodAndLanguageSettings.this.getActivity(), arg0);
                        Bundle extras = new Bundle();
                        if (localeSet.size() <= 1) {
                            if (!localeSet.isEmpty()) {
                                extras.putString("locale", (String) localeSet.first());
                            }
                            targetFragment = UserDictionarySettings.class;
                        } else {
                            targetFragment = UserDictionaryList.class;
                        }
                        InputMethodAndLanguageSettings.this.startFragment(InputMethodAndLanguageSettings.this, targetFragment.getCanonicalName(), -1, -1, extras);
                        return true;
                    }
                });
            } else if (this.mAdvancedCategory != null) {
                this.mAdvancedCategory.removePreference(userDictionaryPreference);
            }
        }
    }

    public void onResume() {
        super.onResume();
        getActivity().setTitle(2131625746);
        this.mSettingsObserver.resume();
        this.mIm.registerInputDeviceListener(this, null);
        Preference spellChecker = findPreference("spellcheckers_settings");
        if (spellChecker != null) {
            TextServicesManager tsm = (TextServicesManager) getSystemService("textservices");
            if (tsm.isSpellCheckerEnabled()) {
                SpellCheckerInfo sci = tsm.getCurrentSpellChecker();
                if (sci != null) {
                    spellChecker.setSummary(sci.loadLabel(getPackageManager()));
                } else {
                    spellChecker.setSummary(2131627181);
                }
            } else {
                spellChecker.setSummary(2131626852);
            }
        }
        if (!this.mShowsOnlyFullImeAndKeyboardList) {
            if (this.mLanguagePref != null) {
                this.mLanguagePref.setSummary(getLocaleNames(getActivity()));
            }
            updateUserDictionaryPreference(findPreference("key_user_dictionary_settings"));
        }
        if (this.mLangPref != null) {
            LocaleList mLocaleList = LocalePicker.getLocales();
            if (!TextUtils.isEmpty(getLanguageString(mLocaleList))) {
                this.mLangPref.setSummary(getLanguageString(mLocaleList));
            }
        }
        updateInputDevices();
        this.mInputMethodSettingValues.refreshAllInputMethodAndSubtypes();
        updateInputMethodPreferenceViews();
        ensureSpellcheckersEntrance();
    }

    private String getLanguageString(LocaleList mLocaleList) {
        if (mLocaleList == null) {
            return null;
        }
        StringBuilder languageName = new StringBuilder();
        if (mLocaleList.size() > 1) {
            for (int i = 0; i < mLocaleList.size() - 1; i++) {
                languageName = languageName.append(LocaleStore.getLocaleInfo(mLocaleList.get(i)).getFullNameInUiLanguage()).append(getResources().getString(2131628608));
            }
            return languageName.subSequence(0, languageName.length() - 1).toString() + " " + getActivity().getString(2131628607) + " " + LocaleStore.getLocaleInfo(mLocaleList.get(mLocaleList.size() - 1)).getFullNameInUiLanguage();
        }
        languageName.append(LocaleStore.getLocaleInfo(mLocaleList.get(0)).getFullNameInUiLanguage());
        return languageName.toString();
    }

    public void onPause() {
        super.onPause();
        this.mIm.unregisterInputDeviceListener(this);
        this.mSettingsObserver.pause();
        InputMethodAndSubtypeUtil.saveInputMethodSubtypeList(this, getContentResolver(), this.mInputMethodSettingValues.getInputMethodList(), !this.mHardKeyboardPreferenceList.isEmpty());
    }

    public void onDestroy() {
        super.onDestroy();
        synchronized (this.mInputMethodPreferenceList) {
            for (InputMethodPreference pref : this.mInputMethodPreferenceList) {
                if (pref != null) {
                    pref.dismiss();
                }
            }
        }
    }

    public void onInputDeviceAdded(int deviceId) {
        updateInputDevices();
    }

    public void onInputDeviceChanged(int deviceId) {
        updateInputDevices();
    }

    public void onInputDeviceRemoved(int deviceId) {
        updateInputDevices();
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        int i = 0;
        if (Utils.isMonkeyRunning()) {
            return false;
        }
        if (preference instanceof PreferenceScreen) {
            if (preference.getFragment() != null) {
                ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
            } else if ("current_input_method".equals(preference.getKey())) {
                ((InputMethodManager) getSystemService("input_method")).showInputMethodPicker(false);
            }
        } else if (preference instanceof TwoStatePreference) {
            Preference chkPref = (TwoStatePreference) preference;
            if (this.mAdvancedCategory != null && chkPref == this.mAdvancedCategory.findPreference("vibrate_input_devices")) {
                ContentResolver contentResolver = getContentResolver();
                String str = "vibrate_input_devices";
                if (chkPref.isChecked()) {
                    i = 1;
                }
                System.putInt(contentResolver, str, i);
                return true;
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private static String getLocaleNames(Context context) {
        LocaleList locales = LocalePicker.getLocales();
        Locale displayLocale = Locale.getDefault();
        return LocaleHelper.toSentenceCase(LocaleHelper.getDisplayLocaleList(locales, displayLocale, 2), displayLocale);
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        return super.onPreferenceChange(preference, value);
    }

    private void updateInputMethodPreferenceViews() {
        if (this.mKeyboardSettingsCategory != null && getActivity() != null) {
            synchronized (this.mInputMethodPreferenceList) {
                InputMethodPreference pref;
                List<InputMethodInfo> imis;
                for (InputMethodPreference pref2 : this.mInputMethodPreferenceList) {
                    this.mKeyboardSettingsCategory.removePreference(pref2);
                }
                this.mInputMethodPreferenceList.clear();
                List<String> permittedList = this.mDpm.getPermittedInputMethodsForCurrentUser();
                Context context = getPrefContext();
                if (this.mShowsOnlyFullImeAndKeyboardList) {
                    imis = this.mInputMethodSettingValues.getInputMethodList();
                } else {
                    imis = this.mImm.getEnabledInputMethodList();
                }
                int N = imis == null ? 0 : imis.size();
                int i = 0;
                while (i < N) {
                    boolean contains;
                    InputMethodInfo imi = (InputMethodInfo) imis.get(i);
                    if (permittedList != null) {
                        contains = permittedList.contains(imi.getPackageName());
                    } else {
                        contains = true;
                    }
                    pref2 = new InputMethodPreference(context, imi, this.mShowsOnlyFullImeAndKeyboardList, contains, this);
                    if (getActivity() == null) {
                        return;
                    } else {
                        this.mInputMethodPreferenceList.add(pref2);
                        i++;
                    }
                }
                final Collator collator = Collator.getInstance();
                Collections.sort(this.mInputMethodPreferenceList, new Comparator<InputMethodPreference>() {
                    public int compare(InputMethodPreference lhs, InputMethodPreference rhs) {
                        return lhs.compareTo(rhs, collator);
                    }
                });
                for (i = 0; i < N; i++) {
                    pref2 = (InputMethodPreference) this.mInputMethodPreferenceList.get(i);
                    this.mKeyboardSettingsCategory.addPreference(pref2);
                    InputMethodAndSubtypeUtil.removeUnnecessaryNonPersistentPreference(pref2);
                    pref2.updatePreferenceViews();
                }
                updateCurrentImeName();
                InputMethodAndSubtypeUtil.loadInputMethodSubtypeList(this, getContentResolver(), this.mInputMethodSettingValues.getInputMethodList(), null);
            }
        }
    }

    public void onSaveInputMethodPreference(InputMethodPreference pref) {
        InputMethodInfo imi = pref.getInputMethodInfo();
        if (!pref.isChecked()) {
            saveEnabledSubtypesOf(imi);
        }
        InputMethodAndSubtypeUtil.saveInputMethodSubtypeList(this, getContentResolver(), this.mImm.getInputMethodList(), getResources().getConfiguration().keyboard == 2);
        this.mInputMethodSettingValues.refreshAllInputMethodAndSubtypes();
        if (pref.isChecked()) {
            restorePreviouslyEnabledSubtypesOf(imi);
        }
        for (InputMethodPreference p : this.mInputMethodPreferenceList) {
            p.updatePreferenceViews();
        }
    }

    private void saveEnabledSubtypesOf(InputMethodInfo imi) {
        HashSet<String> enabledSubtypeIdSet = new HashSet();
        for (InputMethodSubtype subtype : this.mImm.getEnabledInputMethodSubtypeList(imi, true)) {
            enabledSubtypeIdSet.add(Integer.toString(subtype.hashCode()));
        }
        HashMap<String, HashSet<String>> imeToEnabledSubtypeIdsMap = loadPreviouslyEnabledSubtypeIdsMap();
        imeToEnabledSubtypeIdsMap.put(imi.getId(), enabledSubtypeIdSet);
        savePreviouslyEnabledSubtypeIdsMap(imeToEnabledSubtypeIdsMap);
    }

    private void restorePreviouslyEnabledSubtypesOf(InputMethodInfo imi) {
        HashMap<String, HashSet<String>> imeToEnabledSubtypeIdsMap = loadPreviouslyEnabledSubtypeIdsMap();
        String imiId = imi.getId();
        HashSet<String> enabledSubtypeIdSet = (HashSet) imeToEnabledSubtypeIdsMap.remove(imiId);
        if (enabledSubtypeIdSet != null) {
            savePreviouslyEnabledSubtypeIdsMap(imeToEnabledSubtypeIdsMap);
            InputMethodAndSubtypeUtil.enableInputMethodSubtypesOf(getContentResolver(), imiId, enabledSubtypeIdSet);
        }
    }

    private HashMap<String, HashSet<String>> loadPreviouslyEnabledSubtypeIdsMap() {
        return InputMethodAndSubtypeUtil.parseInputMethodsAndSubtypesString(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("previously_enabled_subtypes", null));
    }

    private void savePreviouslyEnabledSubtypeIdsMap(HashMap<String, HashSet<String>> subtypesMap) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.edit().putString("previously_enabled_subtypes", InputMethodAndSubtypeUtil.buildInputMethodsAndSubtypesString(subtypesMap)).apply();
    }

    private void updateCurrentImeName() {
        Context context = getActivity();
        if (context != null && this.mImm != null) {
            Preference curPref = getPreferenceScreen().findPreference("current_input_method");
            if (curPref != null) {
                String strCurIme = AccessibilityExtUtils.toTitleCase(this.mInputMethodSettingValues.getCurrentInputMethodName(context).toString());
                CharSequence curIme = strCurIme.subSequence(0, strCurIme.length());
                if (!TextUtils.isEmpty(curIme)) {
                    synchronized (this) {
                        curPref.setSummary(curIme);
                    }
                }
            }
        }
    }

    private void updateInputDevices() {
        updateHardKeyboards();
        updateGameControllers();
    }

    private void updateHardKeyboards() {
        if (this.mHardKeyboardCategory != null) {
            int i;
            this.mHardKeyboardPreferenceList.clear();
            int[] devices = InputDevice.getDeviceIds();
            for (int device : devices) {
                InputDevice device2 = InputDevice.getDevice(device);
                if (!(device2 == null || device2.isVirtual() || !device2.isFullKeyboard())) {
                    final InputDeviceIdentifier identifier = device2.getIdentifier();
                    String keyboardLayoutDescriptor = this.mIm.getCurrentKeyboardLayoutForInputDevice(identifier);
                    KeyboardLayout keyboardLayout = keyboardLayoutDescriptor != null ? this.mIm.getKeyboardLayout(keyboardLayoutDescriptor) : null;
                    PreferenceScreen pref = new PreferenceScreen(getPrefContext(), null);
                    pref.setTitle(device2.getName());
                    if (keyboardLayout != null) {
                        pref.setSummary(keyboardLayout.toString());
                    } else {
                        pref.setSummary(2131625784);
                    }
                    pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {
                            InputMethodAndLanguageSettings.this.showKeyboardLayoutDialog(identifier);
                            return true;
                        }
                    });
                    this.mHardKeyboardPreferenceList.add(pref);
                }
            }
            if (this.mHardKeyboardPreferenceList.isEmpty()) {
                getPreferenceScreen().removePreference(this.mHardKeyboardCategory);
            } else {
                Preference pref2;
                int i2 = this.mHardKeyboardCategory.getPreferenceCount();
                while (true) {
                    i = i2 - 1;
                    if (i2 <= 0) {
                        break;
                    }
                    pref2 = this.mHardKeyboardCategory.getPreference(i);
                    if (pref2.getOrder() < 1000) {
                        this.mHardKeyboardCategory.removePreference(pref2);
                    }
                    i2 = i;
                }
                Collections.sort(this.mHardKeyboardPreferenceList);
                int count = this.mHardKeyboardPreferenceList.size();
                for (i = 0; i < count; i++) {
                    pref2 = (Preference) this.mHardKeyboardPreferenceList.get(i);
                    pref2.setOrder(i);
                    this.mHardKeyboardCategory.addPreference(pref2);
                }
                getPreferenceScreen().addPreference(this.mHardKeyboardCategory);
            }
        }
    }

    private void showKeyboardLayoutDialog(InputDeviceIdentifier inputDeviceIdentifier) {
        if (((KeyboardLayoutDialogFragment) getFragmentManager().findFragmentByTag("keyboardLayout")) == null) {
            KeyboardLayoutDialogFragment fragment = new KeyboardLayoutDialogFragment(inputDeviceIdentifier);
            fragment.setTargetFragment(this, 0);
            fragment.show(getActivity().getFragmentManager(), "keyboardLayout");
        }
    }

    public void onSetupKeyboardLayouts(InputDeviceIdentifier inputDeviceIdentifier) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClass(getActivity(), KeyboardLayoutPickerActivity.class);
        intent.putExtra("input_device_identifier", inputDeviceIdentifier);
        this.mIntentWaitingForResult = intent;
        startActivityForResult(intent, 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.mIntentWaitingForResult != null) {
            InputDeviceIdentifier inputDeviceIdentifier = (InputDeviceIdentifier) this.mIntentWaitingForResult.getParcelableExtra("input_device_identifier");
            this.mIntentWaitingForResult = null;
            showKeyboardLayoutDialog(inputDeviceIdentifier);
        }
    }

    private void updateGameControllers() {
        boolean z = true;
        if (this.mAdvancedCategory != null) {
            if (haveInputDeviceWithVibrator()) {
                this.mAdvancedCategory.addPreference(this.mVibratePreference);
                TwoStatePreference chkPref = this.mVibratePreference;
                if (System.getInt(getContentResolver(), "vibrate_input_devices", 1) <= 0) {
                    z = false;
                }
                chkPref.setChecked(z);
            } else {
                this.mAdvancedCategory.removePreference(this.mVibratePreference);
            }
        }
    }

    private static boolean haveInputDeviceWithVibrator() {
        int[] devices = InputDevice.getDeviceIds();
        for (int device : devices) {
            InputDevice device2 = InputDevice.getDevice(device);
            if (device2 != null && !device2.isVirtual() && device2.getVibrator().hasVibrator()) {
                return true;
            }
        }
        return false;
    }

    public static void addInputMethodSearchIndex(List<SearchIndexableRaw> indexables, Context context) {
        if (context != null && indexables != null) {
            SearchIndexableRaw indexable;
            String screenTitle = context.getString(2131625746);
            if (Utils.hasIntentService(context.getPackageManager(), "com.huawei.secime.SoftKeyboard")) {
                indexable = new SearchIndexableRaw(context);
                indexable.key = "secure_input_switcher";
                indexable.title = context.getString(2131628184);
                indexable.summaryOn = context.getString(2131628185);
                indexable.summaryOff = context.getString(2131628185);
                indexable.screenTitle = screenTitle;
                indexables.add(indexable);
            }
            indexable = new SearchIndexableRaw(context);
            indexable.key = "virtual_keyboard";
            indexable.title = context.getString(2131625765);
            indexable.screenTitle = screenTitle;
            indexables.add(indexable);
            indexable = new SearchIndexableRaw(context);
            indexable.key = "physical_keyboard";
            indexable.title = context.getString(2131625769);
            indexable.screenTitle = screenTitle;
            indexables.add(indexable);
        }
    }

    public static void addUserDictionarySearchIndex(List<SearchIndexableRaw> indexables, Context context) {
        int i = 0;
        boolean disableDictionary = true;
        String[] checkingPackageArray = "com.google.android.inputmethod.latin;com.android.inputmethod.latin".split(";");
        int length = checkingPackageArray.length;
        while (i < length) {
            try {
                if (context.getPackageManager().getApplicationInfo(checkingPackageArray[i], 0) != null) {
                    disableDictionary = false;
                    break;
                }
                i++;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (!disableDictionary && UserDictionaryList.getUserDictionaryLocalesSet(context) != null) {
            SearchIndexableRaw indexable = new SearchIndexableRaw(context);
            indexable.key = "user_dict_settings";
            indexable.title = context.getString(2131625786);
            indexable.screenTitle = context.getString(2131625746);
            indexables.add(indexable);
        }
    }
}
