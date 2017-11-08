package com.android.settings.inputmethod;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.EditText;
import com.android.settings.InstrumentedFragment;

public class UserDictionaryAddWordFragmentHwBase extends InstrumentedFragment {
    protected UserDictionaryAddWordContents mContents;
    protected Editor mEditor;
    protected SharedPreferences mSharedPref;
    protected EditText mShortcutEditText;
    protected EditText mWordEditText;

    protected int getMetricsCategory() {
        return 100000;
    }

    public void onResume() {
        super.onResume();
        String str = null;
        if (getActivity() != null) {
            this.mSharedPref = getActivity().getSharedPreferences("personal_dictionary", 0);
            str = this.mSharedPref.getString("country", null);
            this.mEditor = this.mSharedPref.edit();
            this.mEditor.putString("country", null);
            this.mEditor.commit();
        }
        if (str != null) {
            this.mContents.updateLocale(str);
        }
    }
}
