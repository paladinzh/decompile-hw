package com.android.contacts.compatibility;

import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;

public class NumberLocationCache {
    private static int DEFAULT_CACHE_SIZE = 100;
    private static Map<String, String> map = new HashMap(DEFAULT_CACHE_SIZE);
    private static Map<String, String> sMapDefault = new HashMap(DEFAULT_CACHE_SIZE);

    public static String getLocation(String number) {
        String str;
        synchronized (map) {
            str = (String) map.get(number);
        }
        return str;
    }

    public static void put(String number, String location) {
        if (!TextUtils.isEmpty(number) && location != null) {
            synchronized (map) {
                if (map.get(number) == null) {
                    map.put(number, location);
                }
            }
        }
    }

    public static void putDefault(String aCountryIso, String aLocation) {
        if (!TextUtils.isEmpty(aCountryIso) && aLocation != null) {
            synchronized (sMapDefault) {
                sMapDefault.put(aCountryIso, aLocation);
            }
        }
    }

    public static String getDefaultLocation(String countryIso) {
        String str;
        synchronized (sMapDefault) {
            str = (String) sMapDefault.get(countryIso);
        }
        return str;
    }

    public static void clear() {
        synchronized (map) {
            map.clear();
        }
    }

    public static void clearLocation() {
        synchronized (sMapDefault) {
            sMapDefault.clear();
        }
    }
}
