package com.huawei.g11n.tmr;

import com.huawei.g11n.tmr.datetime.utils.LocaleParam;
import java.util.Date;
import java.util.Locale;

public class DateTmr {
    private static AbstractDateTmrHandle instance = null;

    private static synchronized AbstractDateTmrHandle getInstance() {
        AbstractDateTmrHandle abstractDateTmrHandle;
        synchronized (DateTmr.class) {
            String calLocale = calLocale();
            String calBkLocale = calBkLocale(calLocale);
            if (instance == null) {
                instance = new DateTmrHandle(calLocale, calBkLocale);
            } else if (!instance.getLocale().equals(calLocale)) {
                instance = new DateTmrHandle(calLocale, calBkLocale);
            }
            abstractDateTmrHandle = instance;
        }
        return abstractDateTmrHandle;
    }

    private static String calBkLocale(String str) {
        return !str.equals("en") ? "en" : "zh_hans";
    }

    private static String calLocale() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        if (language.equals("in")) {
            language = "id";
        }
        if (language.equals("iw")) {
            language = "he";
        }
        String str = language.toLowerCase(Locale.ENGLISH) + "_" + locale.getCountry().toUpperCase(Locale.ENGLISH);
        if (language.equalsIgnoreCase("zh")) {
            return "zh_hans";
        }
        if (LocaleParam.isSupport(str)) {
            language = str;
        }
        if (!LocaleParam.isSupport(language)) {
            language = "en";
        }
        return language;
    }

    public static int[] getTime(String str) {
        return getInstance().getTime(str);
    }

    public static Date[] convertDate(String str, long j) {
        return getInstance().convertDate(str, j);
    }
}
