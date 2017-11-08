package com.huawei.hwid.core.d;

import android.text.TextUtils;
import android.util.Xml;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.encrypt.f;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Pattern;

public class k {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean a(String str, String str2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2) || !Pattern.compile(str2).matcher(str).matches()) {
            return false;
        }
        return true;
    }

    public static boolean a(String str) {
        if (str.endsWith("@inner.up.huawei")) {
            return false;
        }
        return a(str, "^\\s*([A-Za-z0-9_-]+(\\.\\w+)*@(\\w+\\.)+\\w+)\\s*$");
    }

    public static boolean b(String str) {
        if (Pattern.compile("^1[0-9]{10}$").matcher(str).matches()) {
            return true;
        }
        return false;
    }

    public static String c(String str, String str2) {
        if (TextUtils.isEmpty(str) || !b.d(str2)) {
            return str;
        }
        try {
            Xml.newSerializer().text(str);
        } catch (IllegalArgumentException e) {
            String stringBuffer;
            e.c("StringUtil", "IllegalArgumentException / " + e.getMessage());
            e.b("StringUtil", "thirdName: " + f.c(str));
            if ("7".equals(str2)) {
                stringBuffer = new StringBuffer("QQ").append(a()).toString();
            } else if ("4".equals(str2)) {
                stringBuffer = new StringBuffer("Sina").append(a()).toString();
            } else if ("22".equals(str2)) {
                stringBuffer = new StringBuffer("Weixin").append(a()).toString();
            } else {
                stringBuffer = new StringBuffer("ThirdName").append(a()).toString();
            }
            str = stringBuffer;
        } catch (IllegalStateException e2) {
            e.c("StringUtil", "IllegalStateException / " + e2.getMessage());
        } catch (IOException e3) {
            e.c("StringUtil", "IOException / " + e3.getMessage());
        } catch (NullPointerException e4) {
            e.c("StringUtil", "NullPointerException / " + e4.getMessage());
        }
        return str;
    }

    public static String e(String str) {
        int i = 0;
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        char[] toCharArray = str.toCharArray();
        char[] cArr = new char[toCharArray.length];
        int length = toCharArray.length;
        int i2 = 0;
        while (i < length) {
            cArr[i2] = (char) ((char) (toCharArray[i] + 2));
            i2++;
            i++;
        }
        return new String(cArr);
    }

    public static String a() {
        Random secureRandom = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            stringBuilder.append(String.valueOf(secureRandom.nextInt(10)));
        }
        return stringBuilder.toString();
    }

    public static String f(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        if (a(str)) {
            String[] split = str.split("@");
            if (split.length != 2 || split[0].length() <= 0 || split[1].length() <= 0) {
                return str;
            }
            String str2 = split[0];
            String str3 = split[1];
            if (str2.length() > 6 && a(str2, "[0-9]+")) {
                if (str2.length() <= 8) {
                    return a("*", str2.length() - 4) + str2.substring(str2.length() - 4) + "@" + str3;
                }
                return str2.substring(0, str2.length() - 8) + "****" + str2.substring(str2.length() - 4) + "@" + str3;
            } else if (str2.length() > 8) {
                return str2.substring(0, str2.length() - 4) + "****" + "@" + str3;
            } else {
                if (str2.length() <= 2) {
                    return a("*", str2.length()) + "@" + str3;
                }
                return str2.substring(0, str2.length() - 2) + "**" + "@" + str3;
            }
        } else if (b(str)) {
            if (str.length() < 5) {
                return str;
            }
            if (str.length() >= 8) {
                return str.substring(0, str.length() - 8) + "****" + str.substring(str.length() - 4);
            }
            return a("*", str.length() - 4) + str.substring(str.length() - 4);
        } else if (str.length() < 5) {
            return str;
        } else {
            if (str.length() >= 8) {
                return str.substring(0, str.length() - 8) + "****" + str.substring(str.length() - 4);
            }
            return a("*", str.length() - 4) + str.substring(str.length() - 4);
        }
    }

    private static String a(String str, int i) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i2 = 0; i2 < i; i2++) {
            stringBuffer.append(str);
        }
        return stringBuffer.toString();
    }

    public static String a(String str, String[] strArr) {
        return Pattern.compile(strArr[0]).matcher(str).replaceAll(strArr[1]);
    }
}
