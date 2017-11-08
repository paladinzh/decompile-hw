package com.huawei.cspcommon.util;

import android.os.SystemProperties;
import java.util.Arrays;
import java.util.Locale;

public class SortUtils {
    private static final boolean TW_DIALPAD_SHOW = SystemProperties.getBoolean("ro.config.tw_dialpad_show", false);

    public static boolean isChinese(char c) {
        return c >= '一' && c <= '龥';
    }

    public static boolean isEnglish(char c) {
        if (c < 'A' || c > 'Z') {
            return c >= 'a' && c <= 'z';
        } else {
            return true;
        }
    }

    public static boolean isZhuyin(char ch) {
        return ch >= '㄀' && ch <= 'ㄯ';
    }

    public static String[][] expandStringArrayCapacity(String[][] datas, int newLen) {
        return (String[][]) Arrays.copyOf(datas, newLen);
    }

    public static String[][] addToStringArray(String[][] array, String[] newData, int arrayLen, int index) {
        if (index > arrayLen - 1) {
            String[][] newArray = expandStringArrayCapacity(array, arrayLen * 2);
            newArray[index] = newData;
            return newArray;
        }
        array[index] = newData;
        return array;
    }

    public static int[] addToIntArray(int[] array, int newData, int arrayLen, int index) {
        if (index > arrayLen - 1) {
            int[] newArray = Arrays.copyOf(array, arrayLen * 2);
            newArray[index] = newData;
            return newArray;
        }
        array[index] = newData;
        return array;
    }

    public static char[] addToCharArray(char[] array, char newData, int arrayLen, int index) {
        if (index > arrayLen - 1) {
            char[] newArray = Arrays.copyOf(array, arrayLen * 2);
            newArray[index] = newData;
            return newArray;
        }
        array[index] = newData;
        return array;
    }

    public static String[] addNewData(String[] addField, String data) {
        if (addField == null) {
            return new String[]{data};
        }
        String[] newField = new String[(addField.length + 1)];
        System.arraycopy(addField, 0, newField, 0, addField.length);
        newField[addField.length] = data;
        return newField;
    }

    public static boolean isTWChineseDialpadShow() {
        String language = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        if (TW_DIALPAD_SHOW && "zh".equals(language) && "TW".equals(country)) {
            return true;
        }
        return false;
    }
}
