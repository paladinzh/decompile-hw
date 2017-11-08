package com.platform.settings.ext;

import android.content.Context;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.view.inputmethod.InputMethodInfo;
import com.android.settings.inputmethod.InputMethodExtAbsBase;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class InputMethodPlatformImp extends InputMethodExtAbsBase {
    public boolean isTargetIMEAvailable(List<InputMethodInfo> imes, String currId) {
        int N = imes == null ? 0 : imes.size();
        if (currId == null) {
            return false;
        }
        for (int i = 0; i < N; i++) {
            if (((InputMethodInfo) imes.get(i)).getId().equals(currId)) {
                return true;
            }
        }
        return false;
    }

    public void buildPreference(Preference preference, int stringId, int layoutId, int widgetLayoutId) {
        if (preference != null) {
            if (stringId > 0) {
                preference.setTitle(stringId);
            }
            if (layoutId > 0) {
                preference.setLayoutResource(layoutId);
            }
            if (widgetLayoutId > 0) {
                preference.setWidgetLayoutResource(widgetLayoutId);
            }
        }
    }

    public boolean arrayContains(String[] array, String value, HashSet<String> matchedLocales) {
        if (matchedLocales == null) {
            return false;
        }
        if (matchedLocales.contains(value.toUpperCase(Locale.getDefault()))) {
            return true;
        }
        int i = 0;
        while (i < array.length) {
            if (!matchedLocales.contains(array[i].toUpperCase(Locale.getDefault())) && !"".equals(value) && array[i].toUpperCase(Locale.getDefault()).startsWith(value.toUpperCase(Locale.getDefault()))) {
                return true;
            }
            i++;
        }
        return false;
    }

    public String[] getSplitArray(Context context, String key) {
        String str = System.getString(context.getContentResolver(), key);
        if (str != null) {
            return str.split(",");
        }
        return null;
    }

    public void buildExactlyMatched(HashSet<String> exactlyMatched, String[] whiteLanguages, int subtypeCount, InputMethodInfo imi) {
        for (int j = 0; j < subtypeCount; j++) {
            String value = imi.getSubtypeAt(j).getLocale();
            if (whiteLanguages != null) {
                int k = 0;
                while (k < whiteLanguages.length) {
                    if (!"".equals(value) && whiteLanguages[k].equalsIgnoreCase(value)) {
                        exactlyMatched.add(value.toUpperCase(Locale.getDefault()));
                    }
                    k++;
                }
            }
        }
    }
}
