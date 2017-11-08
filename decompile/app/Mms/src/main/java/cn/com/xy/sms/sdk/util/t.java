package cn.com.xy.sms.sdk.util;

import java.util.Map;

/* compiled from: Unknown */
public final class t {
    private static int a(String str, String str2) {
        int i = 1;
        int i2 = 0;
        switch (str.length()) {
            case 2:
                i = 2;
                break;
            case 3:
                i = 6;
                break;
            case 4:
                i = 24;
                break;
        }
        int i3 = 0;
        while (i2 < str2.length()) {
            i3 += str2.charAt(i2);
            i2++;
        }
        return i3 % i;
    }

    public static String a(String str, String str2, Map<String, String> map) {
        int i = 1;
        int i2 = 0;
        switch (str.length()) {
            case 2:
                i = 2;
                break;
            case 3:
                i = 6;
                break;
            case 4:
                i = 24;
                break;
        }
        int i3 = 0;
        while (i2 < str2.length()) {
            i3 += str2.charAt(i2);
            i2++;
        }
        return (String) map.get(new StringBuilder(String.valueOf(str)).append(i3 % i).toString());
    }
}
