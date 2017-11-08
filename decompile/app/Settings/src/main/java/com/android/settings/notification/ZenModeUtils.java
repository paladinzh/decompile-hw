package com.android.settings.notification;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.support.v7.preference.ListPreference;
import android.util.Log;

public class ZenModeUtils {
    public static int setSelectedValue(ListPreference preference, String value) {
        if (preference == null || value == null) {
            return -1;
        }
        CharSequence[] values = preference.getEntryValues();
        int index = -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i].toString().equals(value)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            preference.setSummary(preference.getEntries()[index]);
            preference.setValue(value);
        }
        return index;
    }

    public static String getDefaultRuleName(Context context, String ruleName) {
        String defaultRuleName = ruleName;
        int ruleNameId = context.getResources().getIdentifier(ruleName, "string", "android");
        Log.d("ZenModeRuleSetting", "ruleNameId: " + ruleNameId);
        if (ruleNameId <= 0) {
            return defaultRuleName;
        }
        try {
            defaultRuleName = context.getResources().getString(ruleNameId);
            Log.d("ZenModeRuleSetting", "ruleName: " + defaultRuleName);
            return defaultRuleName;
        } catch (NotFoundException e) {
            Log.e("ZenModeRuleSetting", "ruleName is not found");
            return defaultRuleName;
        }
    }
}
