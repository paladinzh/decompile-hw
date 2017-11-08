package com.android.settings.inputmethod;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocalePicker.LocaleSelectionListener;
import java.util.Locale;

public class UserDictionaryLocalePicker extends LocalePicker implements LocaleSelectionListener {
    private Editor mEditor;
    private SharedPreferences mSharedPref;

    public UserDictionaryLocalePicker() {
        setLocaleSelectionListener(this);
    }

    public void onLocaleSelected(Locale locale) {
        if (getActivity() != null && locale != null) {
            this.mSharedPref = getActivity().getApplicationContext().getSharedPreferences("personal_dictionary", 0);
            this.mEditor = this.mSharedPref.edit();
            this.mEditor.putString("country", locale.toString());
            this.mEditor.commit();
            getActivity().onBackPressed();
        }
    }
}
