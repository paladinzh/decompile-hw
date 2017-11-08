package com.huawei.contact.util;

import android.content.ContentResolver;
import android.provider.Settings.System;

public class SettingsWrapper {
    public static String getString(ContentResolver cr, String name) {
        return System.getString(cr, name);
    }

    public static int getInt(ContentResolver cr, String name, int def) {
        return System.getInt(cr, name, def);
    }

    public static boolean putString(ContentResolver cr, String name, String value) {
        return System.putString(cr, name, value);
    }
}
