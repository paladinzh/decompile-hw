package com.android.settings.inputmethod;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.ArrayAdapter;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocalePicker.LocaleInfo;
import com.android.settings.SettingsPreferenceFragment;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class InputMethodAndSubtypeEnabler extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private final HashMap<String, TwoStatePreference> mAutoSelectionPrefsMap = new HashMap();
    private Collator mCollator;
    private boolean mHaveHardKeyboard;
    private InputMethodManager mImm;
    private final HashMap<String, List<Preference>> mInputMethodAndSubtypePrefsMap = new HashMap();
    private List<InputMethodInfo> mInputMethodInfoList;
    private ArrayList<String> mLanguageList = new ArrayList();

    protected int getMetricsCategory() {
        return 60;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mImm = (InputMethodManager) getSystemService("input_method");
        this.mHaveHardKeyboard = getResources().getConfiguration().keyboard == 2;
        String targetImi = getStringExtraFromIntentOrArguments("input_method_id");
        this.mInputMethodInfoList = this.mImm.getInputMethodList();
        this.mCollator = Collator.getInstance();
        ArrayAdapter<LocaleInfo> adapter = LocalePicker.constructAdapter(getActivity());
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            this.mLanguageList.add(((LocaleInfo) adapter.getItem(i)).getLocale().getLanguage());
        }
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
        int imiCount = this.mInputMethodInfoList.size();
        for (int index = 0; index < imiCount; index++) {
            InputMethodInfo imi = (InputMethodInfo) this.mInputMethodInfoList.get(index);
            if (imi.getId().equals(targetImi) || TextUtils.isEmpty(targetImi)) {
                addInputMethodSubtypePreferences(imi, root);
            }
        }
        setPreferenceScreen(root);
    }

    private String getStringExtraFromIntentOrArguments(String name) {
        String str = null;
        String fromIntent = getActivity().getIntent().getStringExtra(name);
        if (fromIntent != null) {
            return fromIntent;
        }
        Bundle arguments = getArguments();
        if (arguments != null) {
            str = arguments.getString(name);
        }
        return str;
    }

    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
        String title = getStringExtraFromIntentOrArguments("android.intent.extra.TITLE");
        if (!TextUtils.isEmpty(title)) {
            getActivity().setTitle(title);
        }
    }

    public void onResume() {
        super.onResume();
        InputMethodSettingValuesWrapper.getInstance(getActivity()).refreshAllInputMethodAndSubtypes();
        InputMethodAndSubtypeUtil.loadInputMethodSubtypeList(this, getContentResolver(), this.mInputMethodInfoList, this.mInputMethodAndSubtypePrefsMap);
        updateAutoSelectionPreferences();
    }

    public void onPause() {
        super.onPause();
        InputMethodAndSubtypeUtil.saveInputMethodSubtypeList(this, getContentResolver(), this.mInputMethodInfoList, this.mHaveHardKeyboard);
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
        if (!(newValue instanceof Boolean)) {
            return true;
        }
        boolean isChecking = ((Boolean) newValue).booleanValue();
        for (String imiId : this.mAutoSelectionPrefsMap.keySet()) {
            if (this.mAutoSelectionPrefsMap.get(imiId) == pref) {
                TwoStatePreference autoSelectionPref = (TwoStatePreference) pref;
                autoSelectionPref.setChecked(isChecking);
                setAutoSelectionSubtypesEnabled(imiId, autoSelectionPref.isChecked());
                return false;
            }
        }
        if (!(pref instanceof InputMethodSubtypePreference)) {
            return true;
        }
        InputMethodSubtypePreference subtypePref = (InputMethodSubtypePreference) pref;
        subtypePref.setChecked(isChecking);
        if (!subtypePref.isChecked()) {
            updateAutoSelectionPreferences();
        }
        return false;
    }

    private void addInputMethodSubtypePreferences(InputMethodInfo imi, PreferenceScreen root) {
        Context context = getActivity();
        String[] whiteLanguages = InputMethodExtUtils.getSplitArray(context, "white_languages");
        String blackLanguages = System.getString(context.getContentResolver(), "black_languages");
        int subtypeCount = imi.getSubtypeCount();
        if (subtypeCount > 1) {
            int index;
            String imiId = imi.getId();
            PreferenceCategory keyboardSettingsCategory = new PreferenceCategory(context);
            keyboardSettingsCategory.setLayoutResource(2130968916);
            root.addPreference(keyboardSettingsCategory);
            keyboardSettingsCategory.setTitle(imi.loadLabel(getPackageManager()));
            keyboardSettingsCategory.setKey(imiId);
            TwoStatePreference autoSelectionPref = new SwitchWithNoTextPreference(context);
            this.mAutoSelectionPrefsMap.put(imiId, autoSelectionPref);
            keyboardSettingsCategory.addPreference(autoSelectionPref);
            autoSelectionPref.setOnPreferenceChangeListener(this);
            PreferenceCategory activeInputMethodsCategory = new PreferenceCategory(context);
            activeInputMethodsCategory.setLayoutResource(2130968916);
            activeInputMethodsCategory.setTitle(2131625818);
            root.addPreference(activeInputMethodsCategory);
            CharSequence autoSubtypeLabel = null;
            ArrayList<Preference> subtypePreferences = new ArrayList();
            HashSet<String> exactlyMatched = new HashSet();
            InputMethodExtUtils.buildExactlyMatched(exactlyMatched, whiteLanguages, subtypeCount, imi);
            for (index = 0; index < subtypeCount; index++) {
                InputMethodSubtype subtype = imi.getSubtypeAt(index);
                if (whiteLanguages == null || InputMethodExtUtils.arrayContains(whiteLanguages, subtype.getLocale(), exactlyMatched)) {
                    String language = subtype.getLocale();
                    if (language != null && language.length() >= 2) {
                        language = language.substring(0, 2);
                    }
                    if (blackLanguages != null && language != null && blackLanguages.contains(language) && !this.mLanguageList.contains(language)) {
                        Log.d("InputMethodAndSubtypeEnabler", "it needs to remove blacklanguage: " + language);
                    } else if (!subtype.overridesImplicitlyEnabledSubtype()) {
                        subtypePreferences.add(new InputMethodSubtypePreference(context, subtype, imi));
                    } else if (autoSubtypeLabel == null) {
                        autoSubtypeLabel = InputMethodAndSubtypeUtil.getSubtypeLocaleNameAsSentence(subtype, context, imi);
                    }
                }
            }
            Collections.sort(subtypePreferences, new Comparator<Preference>() {
                public int compare(Preference lhs, Preference rhs) {
                    if (lhs instanceof InputMethodSubtypePreference) {
                        return ((InputMethodSubtypePreference) lhs).compareTo(rhs, InputMethodAndSubtypeEnabler.this.mCollator);
                    }
                    return lhs.compareTo(rhs);
                }
            });
            int prefCount = subtypePreferences.size();
            for (index = 0; index < prefCount; index++) {
                Preference pref = (Preference) subtypePreferences.get(index);
                activeInputMethodsCategory.addPreference(pref);
                pref.setOnPreferenceChangeListener(this);
                InputMethodAndSubtypeUtil.removeUnnecessaryNonPersistentPreference(pref);
            }
            this.mInputMethodAndSubtypePrefsMap.put(imiId, subtypePreferences);
            if (TextUtils.isEmpty(autoSubtypeLabel)) {
                autoSelectionPref.setTitle(2131625819);
            } else {
                autoSelectionPref.setTitle(autoSubtypeLabel);
            }
        }
    }

    private boolean isNoSubtypesExplicitlySelected(String imiId) {
        for (Preference pref : (List) this.mInputMethodAndSubtypePrefsMap.get(imiId)) {
            if ((pref instanceof TwoStatePreference) && ((TwoStatePreference) pref).isChecked()) {
                return false;
            }
        }
        return true;
    }

    private void setAutoSelectionSubtypesEnabled(String imiId, boolean autoSelectionEnabled) {
        TwoStatePreference autoSelectionPref = (TwoStatePreference) this.mAutoSelectionPrefsMap.get(imiId);
        if (autoSelectionPref != null) {
            autoSelectionPref.setChecked(autoSelectionEnabled);
            for (Preference pref : (List) this.mInputMethodAndSubtypePrefsMap.get(imiId)) {
                if (pref instanceof TwoStatePreference) {
                    boolean z;
                    if (autoSelectionEnabled) {
                        z = false;
                    } else {
                        z = true;
                    }
                    pref.setEnabled(z);
                    if (autoSelectionEnabled) {
                        ((TwoStatePreference) pref).setChecked(false);
                    }
                }
            }
            if (autoSelectionEnabled) {
                InputMethodAndSubtypeUtil.saveInputMethodSubtypeList(this, getContentResolver(), this.mInputMethodInfoList, this.mHaveHardKeyboard);
                updateImplicitlyEnabledSubtypes(imiId, true);
            }
        }
    }

    private void updateImplicitlyEnabledSubtypes(String targetImiId, boolean check) {
        for (InputMethodInfo imi : this.mInputMethodInfoList) {
            String imiId = imi.getId();
            TwoStatePreference autoSelectionPref = (TwoStatePreference) this.mAutoSelectionPrefsMap.get(imiId);
            if (autoSelectionPref != null && autoSelectionPref.isChecked()) {
                if (imiId.equals(targetImiId) || targetImiId == null) {
                    updateImplicitlyEnabledSubtypesOf(imi, check);
                }
            }
        }
    }

    private void updateImplicitlyEnabledSubtypesOf(InputMethodInfo imi, boolean check) {
        String imiId = imi.getId();
        List<Preference> subtypePrefs = (List) this.mInputMethodAndSubtypePrefsMap.get(imiId);
        List<InputMethodSubtype> implicitlyEnabledSubtypes = this.mImm.getEnabledInputMethodSubtypeList(imi, true);
        if (subtypePrefs != null && implicitlyEnabledSubtypes != null) {
            for (Preference pref : subtypePrefs) {
                if (pref instanceof TwoStatePreference) {
                    TwoStatePreference subtypePref = (TwoStatePreference) pref;
                    subtypePref.setChecked(false);
                    if (check) {
                        for (InputMethodSubtype subtype : implicitlyEnabledSubtypes) {
                            if (subtypePref.getKey().equals(imiId + subtype.hashCode())) {
                                subtypePref.setChecked(true);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateAutoSelectionPreferences() {
        for (String imiId : this.mInputMethodAndSubtypePrefsMap.keySet()) {
            setAutoSelectionSubtypesEnabled(imiId, isNoSubtypesExplicitlySelected(imiId));
        }
        updateImplicitlyEnabledSubtypes(null, true);
    }
}
