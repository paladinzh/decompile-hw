package com.android.settings.accessibility;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocalePicker.LocaleInfo;
import java.util.List;

public class LocalePreference extends ListPreference {
    public LocalePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LocalePreference(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        List<LocaleInfo> locales = LocalePicker.getAllAssetLocales(context, false);
        int finalSize = locales.size();
        CharSequence[] entries = new CharSequence[(finalSize + 1)];
        CharSequence[] entryValues = new CharSequence[(finalSize + 1)];
        entries[0] = context.getResources().getString(2131625895);
        entryValues[0] = "";
        for (int i = 0; i < finalSize; i++) {
            LocaleInfo info = (LocaleInfo) locales.get(i);
            entries[i + 1] = info.toString();
            entryValues[i + 1] = info.getLocale().toString();
        }
        setEntries(entries);
        setEntryValues(entryValues);
    }
}
