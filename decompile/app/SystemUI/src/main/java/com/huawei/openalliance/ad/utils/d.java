package com.huawei.openalliance.ad.utils;

import fyusion.vislib.BuildConfig;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* compiled from: Unknown */
public class d {
    private static String a(char c, int i) {
        StringBuffer stringBuffer = new StringBuffer(i);
        for (int i2 = 0; i2 < i; i2++) {
            stringBuffer.append(c);
        }
        return stringBuffer.toString();
    }

    private static String a(String str) {
        try {
            if (!j.a(str)) {
                int ceil = (int) Math.ceil(((double) (str.length() * 30)) / 100.0d);
                str = a('*', ceil) + str.substring(ceil);
            }
        } catch (Throwable e) {
            com.huawei.openalliance.ad.utils.b.d.a("HiAdSDKMyProguard", "get proguard fail", e);
        }
        return str;
    }

    public static String a(String str, boolean z) {
        if (!z) {
            return a(str);
        }
        if (str != null) {
            try {
                if (!str.trim().equals(BuildConfig.FLAVOR)) {
                    char[] toCharArray = str.toCharArray();
                    int i = 0;
                    while (i < toCharArray.length) {
                        if (!("0123456789".contains(String.valueOf(toCharArray[i])) || "{:=@}/#?%\"(),/\\<>| &".contains(String.valueOf(toCharArray[i])))) {
                            toCharArray[i] = '*';
                        }
                        i += 2;
                    }
                    str = String.valueOf(toCharArray);
                    Matcher matcher = Pattern.compile("[0-9]{7,}").matcher(str);
                    while (matcher.find()) {
                        String group = matcher.group();
                        if (group == null) {
                            break;
                        }
                        char[] toCharArray2 = group.toCharArray();
                        for (i = 0; i < toCharArray2.length; i += 2) {
                            if (!"{:=@}/#?%\"(),/\\<>| &".contains(String.valueOf(toCharArray2[i]))) {
                                toCharArray2[i] = '*';
                            }
                        }
                        str = str.replaceAll(group, String.valueOf(toCharArray2));
                    }
                }
            } catch (Throwable e) {
                com.huawei.openalliance.ad.utils.b.d.a("HiAdSDKMyProguard", "get proguard fail", e);
            }
        }
        return str;
    }
}
