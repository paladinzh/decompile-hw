package com.huawei.cspcommon.util;

import android.net.Uri.Builder;
import android.text.TextUtils;
import com.android.contacts.model.account.AccountWithDataSet;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SortUtils {
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

    public static boolean isContainChinese(String name) {
        for (char c : name.toCharArray()) {
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    public static void buildAccountTypeString(Builder uriBuilder, List<AccountWithDataSet> accountList) {
        if (accountList != null && accountList.size() > 0) {
            StringBuilder typeBuilder = new StringBuilder();
            for (AccountWithDataSet account : accountList) {
                if (TextUtils.isEmpty(account.name)) {
                    typeBuilder.append("account_name\u0002");
                } else {
                    typeBuilder.append("account_name\u0002").append(account.name);
                }
                typeBuilder.append("\u0001");
                if (TextUtils.isEmpty(account.type)) {
                    typeBuilder.append("account_type\u0002");
                } else {
                    typeBuilder.append("account_type\u0002").append(account.type);
                }
                typeBuilder.append("\u0001");
                if (TextUtils.isEmpty(account.dataSet)) {
                    typeBuilder.append("data_set\u0002");
                } else {
                    typeBuilder.append("data_set\u0002").append(account.dataSet);
                }
                typeBuilder.append("\u0001");
            }
            if (typeBuilder.length() > 0) {
                typeBuilder.setLength(typeBuilder.length() - 1);
                uriBuilder.appendQueryParameter("include_accounts", typeBuilder.toString());
            }
        }
    }

    public static boolean isTWChineseDialpadShow() {
        String language = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        if ("zh".equals(language) && "TW".equals(country)) {
            return true;
        }
        return false;
    }
}
