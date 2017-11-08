package com.huawei.systemmanager.comm.tools;

import java.util.Locale;

public final class LanguageUtils {
    private static final String ARABIC_LANGUAGE = "ar";
    private static final String BULGARIAN_LANGUAGE = "bg";
    private static final String CHINESE_LANGUAGE = "zh";
    private static final String CN_COUNTRY = "CN";
    private static final String ENGLISH_LANGUAGE = "en";
    private static final String FARSI_LANGUAGE = "fa";
    private static final String GREEK_LANGUAGE = "el";
    private static final String IW_LANGUAGE = "iw";
    private static final String RUSSIA_LANGUAGE = "ru";
    private static final String SG_COUNTRY = "SG";
    private static final String UKRAINIAN_LANGUAGE = "uk";

    private LanguageUtils() {
    }

    public static String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static String getCountry() {
        return Locale.getDefault().getCountry();
    }

    public static boolean isZh() {
        return "zh".equals(getLanguage());
    }

    public static boolean isZhOrEn() {
        String lang = getLanguage();
        return !"zh".equals(lang) ? ENGLISH_LANGUAGE.equals(lang) : true;
    }

    public static boolean isEnLanguage() {
        return ENGLISH_LANGUAGE.equals(getLanguage());
    }

    public static boolean isCNCountry() {
        String country = getCountry();
        return !CN_COUNTRY.equals(country) ? SG_COUNTRY.equals(country) : true;
    }

    public static boolean isRTL() {
        String lang = getLanguage();
        return (ARABIC_LANGUAGE.equals(lang) || FARSI_LANGUAGE.equals(lang)) ? true : IW_LANGUAGE.equals(lang);
    }

    public static boolean isSortByPinyinLanguage() {
        String lang = getLanguage();
        String country = getCountry();
        return ("TW".equals(country) || "HK".equals(country) || "MO".equals(country) || "ja".equals(lang) || "ko".equals(lang)) ? false : true;
    }

    public static boolean isOverlengthLanguage() {
        String lang = getLanguage();
        if (RUSSIA_LANGUAGE.equals(lang) || GREEK_LANGUAGE.equals(lang) || BULGARIAN_LANGUAGE.equals(lang)) {
            return true;
        }
        return UKRAINIAN_LANGUAGE.equals(lang);
    }
}
