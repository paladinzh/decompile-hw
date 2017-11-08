package com.huawei.openalliance.ad.utils;

import java.util.List;

/* compiled from: Unknown */
public class j {
    public static String a(String str, String str2, String str3) {
        if (str == null) {
            return null;
        }
        int length = str3.length() + 2;
        int indexOf = str.indexOf("<" + str2);
        if (-1 != indexOf) {
            int indexOf2 = str.indexOf(str3 + "=\"", indexOf);
            if (-1 == indexOf2) {
                indexOf2 = str.indexOf(str3 + "='", indexOf);
                indexOf = -1 == indexOf2 ? -1 : str.indexOf("'", indexOf2 + length);
            } else {
                indexOf = str.indexOf("\"", indexOf2 + length);
            }
            if (-1 != indexOf) {
                return str.substring(indexOf2 + length, indexOf);
            }
        }
        return null;
    }

    public static String a(List<String> list, String str) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!(list == null || list.isEmpty())) {
            Object obj = 1;
            for (String str2 : list) {
                if (obj == null) {
                    stringBuilder.append(str);
                }
                stringBuilder.append(str2);
                obj = null;
            }
        }
        return stringBuilder.toString();
    }

    public static String a(List<String> list, String str, String str2) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!(list == null || list.isEmpty())) {
            Object obj = 1;
            for (String str3 : list) {
                if (obj == null) {
                    stringBuilder.append(str);
                }
                stringBuilder.append(str2);
                stringBuilder.append(str3);
                stringBuilder.append(str2);
                obj = null;
            }
        }
        return stringBuilder.toString();
    }

    public static boolean a(String str) {
        return str == null || str.trim().length() == 0;
    }
}
