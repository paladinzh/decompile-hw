package com.android.systemui.utils;

import android.content.Context;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberLocationPercent {
    public static String getPercentage(double number, int decimalDigits) {
        return getPercentageInner(number, decimalDigits, null);
    }

    private static String getPercentageInner(double number, int decimalDigits, Locale locale) {
        NumberFormat pnf;
        if (locale == null) {
            pnf = NumberFormat.getPercentInstance();
        } else {
            pnf = NumberFormat.getPercentInstance(locale);
        }
        double pvalue = number / 100.0d;
        pnf.setMinimumFractionDigits(decimalDigits);
        return pnf.format(pvalue);
    }

    public static String getFormatnumberString(int number) {
        return getFormatnumberStringInner(number, null);
    }

    public static String getFormatnumberString(int number, Locale locale) {
        if (locale != null) {
            return getFormatnumberStringInner(number, locale);
        }
        throw new NullPointerException();
    }

    public static String getFormatnumberString(int number, Context context) {
        if (context != null) {
            return getFormatnumberStringInner(number, context.getResources().getConfiguration().locale);
        }
        throw new NullPointerException();
    }

    private static String getFormatnumberStringInner(int number, Locale locale) {
        NumberFormat pnf;
        if (locale == null) {
            pnf = NumberFormat.getIntegerInstance();
        } else {
            pnf = NumberFormat.getIntegerInstance(locale);
        }
        return pnf.format((long) number);
    }

    public static String getFormatnumberString(float number) {
        return getFormatnumberStringInner(number, null);
    }

    public static String getFormatnumberString(float number, Locale locale) {
        if (locale != null) {
            return getFormatnumberStringInner(number, locale);
        }
        throw new NullPointerException();
    }

    private static String getFormatnumberStringInner(float number, Locale locale) {
        NumberFormat pnf;
        if (locale == null) {
            pnf = NumberFormat.getNumberInstance();
        } else {
            pnf = NumberFormat.getNumberInstance(locale);
        }
        return pnf.format((double) number);
    }
}
