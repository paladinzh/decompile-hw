package com.android.settings.inputmethod;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.UserDictionary.Words;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.UserDictionarySettings;
import com.android.settings.Utils;
import com.android.settings.accessibility.AccessibilityExtUtils;
import java.util.Locale;
import java.util.TreeSet;

public class UserDictionaryList extends SettingsPreferenceFragment {
    private String mLocale;

    protected int getMetricsCategory() {
        return 61;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getActivity()));
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        String str;
        String localeFromArguments = null;
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setTitle(2131625786);
        Intent intent = getActivity().getIntent();
        String stringExtra = intent == null ? null : intent.getStringExtra("locale");
        Bundle arguments = getArguments();
        if (arguments != null) {
            localeFromArguments = arguments.getString("locale");
        }
        if (localeFromArguments != null) {
            str = localeFromArguments;
        } else if (stringExtra != null) {
            str = stringExtra;
        } else {
            str = null;
        }
        this.mLocale = str;
    }

    public static TreeSet<String> getUserDictionaryLocalesSet(Context context) {
        Cursor cursor = context.getContentResolver().query(Words.CONTENT_URI, new String[]{"locale"}, null, null, null);
        TreeSet<String> localeSet = new TreeSet();
        if (cursor == null) {
            return null;
        }
        try {
            String locale;
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("locale");
                do {
                    locale = cursor.getString(columnIndex);
                    if (locale == null) {
                        locale = "";
                    }
                    localeSet.add(locale);
                } while (cursor.moveToNext());
            }
            cursor.close();
            InputMethodManager imm = (InputMethodManager) context.getSystemService("input_method");
            for (InputMethodInfo imi : imm.getEnabledInputMethodList()) {
                for (InputMethodSubtype subtype : imm.getEnabledInputMethodSubtypeList(imi, true)) {
                    locale = subtype.getLocale();
                    if (!TextUtils.isEmpty(locale)) {
                        localeSet.add(locale);
                    }
                }
            }
            if (!localeSet.contains(Locale.getDefault().getLanguage().toString())) {
                localeSet.add(Locale.getDefault().toString());
            }
            return localeSet;
        } catch (Throwable th) {
            cursor.close();
        }
    }

    protected void createUserDictSettings(PreferenceGroup userDictGroup) {
        Activity activity = getActivity();
        userDictGroup.removeAll();
        TreeSet<String> localeSet = getUserDictionaryLocalesSet(activity);
        if (this.mLocale != null) {
            localeSet.add(this.mLocale);
        }
        if (localeSet.size() > 1) {
            localeSet.add("");
        }
        if (localeSet.isEmpty()) {
            userDictGroup.addPreference(createUserDictionaryPreference(null, activity));
            return;
        }
        for (String locale : localeSet) {
            userDictGroup.addPreference(createUserDictionaryPreference(locale, activity));
        }
    }

    protected Preference createUserDictionaryPreference(String locale, Activity activity) {
        Preference newPref = new Preference(getPrefContext());
        Intent intent = new Intent("android.settings.USER_DICTIONARY_SETTINGS");
        if (locale == null) {
            newPref.setTitle(Locale.getDefault().getDisplayName());
        } else {
            if ("".equals(locale)) {
                newPref.setTitle(getString(2131625803));
            } else {
                newPref.setTitle(AccessibilityExtUtils.toTitleCase(Utils.createLocaleFromString(locale).getDisplayName()));
            }
            intent.putExtra("locale", locale);
            newPref.getExtras().putString("locale", locale);
        }
        newPref.setIntent(intent);
        newPref.setWidgetLayoutResource(2130968998);
        newPref.setFragment(UserDictionarySettings.class.getName());
        return newPref;
    }

    public void onResume() {
        super.onResume();
        createUserDictSettings(getPreferenceScreen());
    }
}
