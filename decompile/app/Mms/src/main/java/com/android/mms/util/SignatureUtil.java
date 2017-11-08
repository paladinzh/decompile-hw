package com.android.mms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class SignatureUtil {
    public static String getSignature(Context context, String initString) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean("pref_key_signature", false) ? pref.getString("pref_key_signature_content", initString) : initString;
    }

    public static void putSignature(Context context, String setString) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_key_signature_content", setString).commit();
    }

    public static String deleteNewlineSymbol(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        String s = str.replaceAll("\n", "");
        if (s.trim().length() <= 0) {
            return "";
        }
        char[] array = s.toCharArray();
        int end = array.length - 1;
        while (array[end] == ' ') {
            end--;
        }
        return s.substring(0, end + 1);
    }
}
