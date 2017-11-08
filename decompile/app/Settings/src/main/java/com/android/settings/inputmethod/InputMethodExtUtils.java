package com.android.settings.inputmethod;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.view.inputmethod.InputMethodInfo;
import com.platform.settings.ext.InputMethodPlatformImp;
import java.util.HashSet;
import java.util.List;

public class InputMethodExtUtils {
    public static boolean isTargetIMEAvailable(List<InputMethodInfo> imes, String currId) {
        return new InputMethodPlatformImp().isTargetIMEAvailable(imes, currId);
    }

    public static void buildPreference(Preference preference, int stringId, int layoutId, int widgetLayoutId) {
        new InputMethodPlatformImp().buildPreference(preference, stringId, layoutId, widgetLayoutId);
    }

    public static boolean arrayContains(String[] array, String value, HashSet<String> matchedLocales) {
        return new InputMethodPlatformImp().arrayContains(array, value, matchedLocales);
    }

    public static String[] getSplitArray(Context context, String key) {
        return new InputMethodPlatformImp().getSplitArray(context, key);
    }

    public static void buildExactlyMatched(HashSet<String> exactlyMatched, String[] whiteLanguages, int subtypeCount, InputMethodInfo imi) {
        new InputMethodPlatformImp().buildExactlyMatched(exactlyMatched, whiteLanguages, subtypeCount, imi);
    }
}
