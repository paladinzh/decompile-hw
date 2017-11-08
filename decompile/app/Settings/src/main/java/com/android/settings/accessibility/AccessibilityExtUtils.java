package com.android.settings.accessibility;

public class AccessibilityExtUtils {
    public static String toTitleCase(String s) {
        if (s == null) {
            return null;
        }
        if (s.length() == 0) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
