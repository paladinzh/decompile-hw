package com.huawei.hwid.core.c;

import android.text.TextUtils;
import android.util.Xml;
import cn.com.xy.sms.util.ParseMeizuManager;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.encrypt.f;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

/* compiled from: StringUtil */
public class p {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean a(String str, String str2) {
        if (e(str) || e(str2) || !Pattern.compile(str2).matcher(str).matches()) {
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
        if (Pattern.compile("[^a-zA-Z0-9-_.@]").matcher(str).find()) {
            return false;
        }
        return true;
    }

    public static boolean c(String str) {
        if (Pattern.compile("^1[0-9]{10}$").matcher(str).matches()) {
            return true;
        }
        return false;
    }

    public static boolean d(String str) {
        if (str != null) {
            return a(str, "^[0-9]{0,128}$");
        }
        return false;
    }

    public static boolean e(String str) {
        if (str != null && str.trim().length() >= 1) {
            return false;
        }
        return true;
    }

    public static boolean b(String str, String str2) {
        if (str == null && str2 == null) {
            return true;
        }
        if (str == null || str2 == null) {
            if (str != null) {
                return str.trim().length() == 0;
            } else {
                if (str2.trim().length() == 0) {
                    return true;
                }
            }
        } else if (str.equals(str2)) {
            return true;
        }
    }

    public static boolean a(String... strArr) {
        if (strArr != null && strArr.length > 0) {
            for (String str : strArr) {
                if (!TextUtils.isEmpty(str)) {
                    int length = str.length();
                    for (int i = 0; i < length; i++) {
                        char charAt = str.charAt(i);
                        if ('!' > charAt || charAt > '~') {
                            return false;
                        }
                    }
                    continue;
                }
            }
        }
        return true;
    }

    public static String c(String str, String str2) {
        if (TextUtils.isEmpty(str) || !d.f(str2)) {
            return str;
        }
        try {
            Xml.newSerializer().text(str);
        } catch (IllegalArgumentException e) {
            String stringBuffer;
            a.c("StringUtil", "IllegalArgumentException / " + e.toString());
            a.b("StringUtil", "thirdName: " + f.c(str));
            if ("7".equals(str2)) {
                stringBuffer = new StringBuffer("QQ").append(a()).toString();
            } else if ("4".equals(str2)) {
                stringBuffer = new StringBuffer("Sina").append(a()).toString();
            } else if (ParseMeizuManager.SMS_FLOW_THREE.equals(str2)) {
                stringBuffer = new StringBuffer("Weixin").append(a()).toString();
            } else {
                stringBuffer = new StringBuffer("ThirdName").append(a()).toString();
            }
            str = stringBuffer;
        } catch (IllegalStateException e2) {
            a.c("StringUtil", "IllegalStateException / " + e2.toString());
        } catch (IOException e3) {
            a.c("StringUtil", "IOException / " + e3.toString());
        } catch (NullPointerException e4) {
            a.c("StringUtil", "NullPointerException / " + e4.toString());
        }
        return str;
    }

    public static boolean d(String str, String str2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return false;
        }
        a.b("StringUtil", "accountName: " + f.c(str));
        if (str2.toLowerCase(Locale.ENGLISH).contains(str.toLowerCase(Locale.ENGLISH))) {
            return true;
        }
        StringBuffer stringBuffer = new StringBuffer();
        int length = str.length();
        char[] toCharArray = str.toCharArray();
        while (true) {
            length--;
            if (length <= -1) {
                break;
            }
            stringBuffer.append(toCharArray[length]);
        }
        String stringBuffer2 = stringBuffer.toString();
        a.b("StringUtil", "mirrorAccountName: " + f.c(stringBuffer2));
        return str2.equalsIgnoreCase(stringBuffer2);
    }

    public static String g(String str) {
        int i = 0;
        if (e(str)) {
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
}
