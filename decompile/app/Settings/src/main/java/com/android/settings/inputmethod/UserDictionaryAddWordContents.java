package com.android.settings.inputmethod;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.UserDictionary.Words;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import com.android.settings.UserDictionarySettings;
import com.android.settings.Utils;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;

public class UserDictionaryAddWordContents {
    private static final String[] HAS_WORD_PROJECTION = new String[]{"word"};
    private String mLocale;
    private final int mMode;
    private final String mOldShortcut;
    private final String mOldWord;
    private String mSavedShortcut;
    private String mSavedWord;
    private final EditText mShortcutEditText;
    private final EditText mWordEditText;

    public static class LocaleRenderer {
        private final String mDescription;
        private final String mLocaleString;

        public LocaleRenderer(Context context, String localeString) {
            this.mLocaleString = localeString;
            if (localeString == null) {
                this.mDescription = context.getString(2131625804);
            } else if ("".equals(localeString)) {
                this.mDescription = context.getString(2131625803);
            } else {
                this.mDescription = Utils.createLocaleFromString(localeString).getDisplayName();
            }
        }

        public String toString() {
            return this.mDescription;
        }

        public String getLocaleString() {
            return this.mLocaleString;
        }

        public boolean isMoreLanguages() {
            return this.mLocaleString == null;
        }
    }

    UserDictionaryAddWordContents(View view, Bundle args) {
        this.mWordEditText = (EditText) view.findViewById(2131887376);
        this.mShortcutEditText = (EditText) view.findViewById(2131887381);
        String word = args.getString("word");
        if (word != null) {
            this.mWordEditText.setText(word);
            this.mWordEditText.setSelection(this.mWordEditText.getText().length());
        }
        String shortcut = args.getString("shortcut");
        if (!(shortcut == null || this.mShortcutEditText == null)) {
            this.mShortcutEditText.setText(shortcut);
        }
        this.mMode = args.getInt("mode");
        this.mOldWord = args.getString("word");
        this.mOldShortcut = args.getString("shortcut");
        updateLocale(args.getString("locale"));
    }

    UserDictionaryAddWordContents(View view, UserDictionaryAddWordContents oldInstanceToBeEdited) {
        this.mWordEditText = (EditText) view.findViewById(2131887376);
        this.mShortcutEditText = (EditText) view.findViewById(2131887381);
        this.mMode = 0;
        this.mOldWord = oldInstanceToBeEdited.mSavedWord;
        this.mOldShortcut = oldInstanceToBeEdited.mSavedShortcut;
        updateLocale(this.mLocale);
    }

    void updateLocale(String locale) {
        if (locale == null) {
            locale = Locale.getDefault().toString();
        }
        this.mLocale = locale;
    }

    void saveStateIntoBundle(Bundle outState) {
        outState.putString("word", this.mWordEditText.getText().toString());
        outState.putString("originalWord", this.mOldWord);
        if (this.mShortcutEditText != null) {
            outState.putString("shortcut", this.mShortcutEditText.getText().toString());
        }
        if (this.mOldShortcut != null) {
            outState.putString("originalShortcut", this.mOldShortcut);
        }
        outState.putString("locale", this.mLocale);
    }

    void delete(Context context) {
        if (this.mMode == 0 && !TextUtils.isEmpty(this.mOldWord)) {
            UserDictionarySettings.deleteWord(this.mOldWord, this.mOldShortcut, context.getContentResolver());
        }
    }

    int apply(Context context, Bundle outParameters) {
        return apply(context, outParameters, null, null);
    }

    int apply(Context context, Bundle outParameters, String newWord, String newShortcut) {
        Locale locale = null;
        if (outParameters != null) {
            saveStateIntoBundle(outParameters);
        }
        ContentResolver resolver = context.getContentResolver();
        if (this.mMode == 0 && !TextUtils.isEmpty(this.mOldWord)) {
            UserDictionarySettings.deleteWord(this.mOldWord, this.mOldShortcut, resolver);
        }
        if (newWord == null) {
            newWord = this.mWordEditText.getText().toString();
        }
        if (newShortcut == null) {
            if (this.mShortcutEditText == null) {
                newShortcut = null;
            } else {
                String tmpShortcut = this.mShortcutEditText.getText().toString();
                if (TextUtils.isEmpty(tmpShortcut)) {
                    newShortcut = null;
                } else {
                    newShortcut = tmpShortcut;
                }
            }
        }
        if (TextUtils.isEmpty(newWord)) {
            return 1;
        }
        this.mSavedWord = newWord;
        this.mSavedShortcut = newShortcut;
        if (TextUtils.isEmpty(newShortcut) && hasWord(newWord, context)) {
            return 2;
        }
        UserDictionarySettings.deleteWord(newWord, null, resolver);
        if (!TextUtils.isEmpty(newShortcut)) {
            UserDictionarySettings.deleteWord(newWord, newShortcut, resolver);
        }
        if (!TextUtils.isEmpty(this.mLocale)) {
            locale = Utils.createLocaleFromString(this.mLocale);
        }
        Words.addWord(context, newWord, 250, newShortcut, locale);
        return 0;
    }

    private boolean hasWord(String word, Context context) {
        Cursor cursor;
        if ("".equals(this.mLocale)) {
            cursor = context.getContentResolver().query(Words.CONTENT_URI, HAS_WORD_PROJECTION, "word=? AND locale is null", new String[]{word}, null);
        } else {
            cursor = context.getContentResolver().query(Words.CONTENT_URI, HAS_WORD_PROJECTION, "word=? AND locale=?", new String[]{word, this.mLocale}, null);
        }
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return false;
        }
        try {
            boolean z = cursor.getCount() > 0;
            if (cursor != null) {
                cursor.close();
            }
            return z;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static void addLocaleDisplayNameToList(Context context, ArrayList<LocaleRenderer> list, String locale) {
        if (locale != null) {
            list.add(new LocaleRenderer(context, locale));
        }
    }

    public ArrayList<LocaleRenderer> getLocalesList(Activity activity) {
        TreeSet<String> locales = UserDictionaryList.getUserDictionaryLocalesSet(activity);
        if (locales == null) {
            return null;
        }
        locales.remove(this.mLocale);
        String systemLocale = Locale.getDefault().toString();
        locales.remove(systemLocale);
        locales.remove("");
        ArrayList<LocaleRenderer> localesList = new ArrayList();
        addLocaleDisplayNameToList(activity, localesList, this.mLocale);
        if (!systemLocale.equals(this.mLocale)) {
            addLocaleDisplayNameToList(activity, localesList, systemLocale);
        }
        for (String l : locales) {
            addLocaleDisplayNameToList(activity, localesList, l);
        }
        if (!"".equals(this.mLocale)) {
            addLocaleDisplayNameToList(activity, localesList, "");
        }
        localesList.add(new LocaleRenderer(activity, null));
        return localesList;
    }
}
