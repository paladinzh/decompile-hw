package com.huawei.harassmentinterception.util;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

public class Proguard {
    private static final String DIGITAL_REGX = "[0-9]+";
    private static final String DONT_PRAGUARD_CHARS = "{:=@}/#?%\"(),/\\<>| &";
    private static final List<String> KEY_LIST = new ArrayList();
    private static final char PROGUARD_CHAR = '*';
    private static final int PROGUARD_RATE = 30;
    private static final String TAG = "Proguard";
    private static String[] keyArray = new String[]{"password", "mobilephone", "phone", "deviceid", "email"};

    static {
        initKeyList();
    }

    private static void initKeyList() {
        if (keyArray != null) {
            for (String element : keyArray) {
                KEY_LIST.add(element);
            }
        }
        HwLog.d(TAG, "keyList size is " + KEY_LIST.size());
    }

    private static String repeat(char c, int times) {
        StringBuffer sb = new StringBuffer(times);
        for (int i = 0; i < times; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    public static String getProguard(Object o) {
        return getProguard(String.valueOf(o), true);
    }

    public static String getProguard(String info) {
        if (TextUtils.isEmpty(info)) {
            return "";
        }
        int unProguardPos = (int) Math.ceil(((double) (info.length() * 30)) / 100.0d);
        return repeat(PROGUARD_CHAR, unProguardPos) + info.substring(unProguardPos);
    }

    public static String getProguard(Bundle bundle) {
        if (bundle == null) {
            return "";
        }
        Set<String> keys = bundle.keySet();
        StringBuffer sb = new StringBuffer();
        for (String k : keys) {
            Object obj = bundle.get(k);
            String proguardVal = "";
            if (obj instanceof Bundle) {
                proguardVal = getProguard((Bundle) obj);
            } else {
                proguardVal = getProguard(obj);
            }
            sb.append(getKeyProguardStr(k)).append("=").append(proguardVal).append(" ");
        }
        return sb.toString();
    }

    public static String getKeyProguardStr(String srcStr) {
        String result = srcStr;
        if (KEY_LIST == null || !KEY_LIST.contains(srcStr.toLowerCase(Locale.ENGLISH))) {
            return result;
        }
        HwLog.d(TAG, "keyList contains " + srcStr.toLowerCase(Locale.ENGLISH));
        return String.valueOf(PROGUARD_CHAR);
    }

    public static String getProguard(Intent intent) {
        if (intent == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        try {
            if (!TextUtils.isEmpty(intent.getAction())) {
                sb.append("act:" + intent.getAction()).append(" ");
            }
            sb.append(" flag:" + intent.getFlags()).append(" ");
            if (intent.getExtras() != null) {
                sb.append(getProguard(intent.getExtras()));
            }
        } catch (Exception e) {
            HwLog.w(TAG, e.getMessage());
        }
        return sb.toString();
    }

    public static String getProguard(Map map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Entry en : map.entrySet()) {
            sb.append(en.getKey()).append("=").append(getProguard(String.valueOf(en.getValue()))).append(" ");
        }
        return sb.toString();
    }

    public static String getProguard(String content, boolean isLongStr) {
        if (!isLongStr) {
            return getProguard(content);
        }
        if (TextUtils.isEmpty(content)) {
            return "";
        }
        char[] val = content.toCharArray();
        for (int i = 0; i < val.length; i += 2) {
            if (!DONT_PRAGUARD_CHARS.contains(String.valueOf(val[i]))) {
                val[i] = PROGUARD_CHAR;
            }
        }
        return String.valueOf(val);
    }

    public static String getBaseUrl(String fullurl) {
        if (TextUtils.isEmpty(fullurl)) {
            return "";
        }
        try {
            int equalPos = fullurl.indexOf("=");
            if (equalPos > 0) {
                return fullurl.substring(0, equalPos) + getProguard(fullurl, true).substring(equalPos);
            }
            return fullurl;
        } catch (NullPointerException e) {
            HwLog.e(TAG, e.toString());
            return fullurl;
        } catch (Exception e2) {
            HwLog.e(TAG, e2.toString());
            return fullurl;
        }
    }

    public static String handlePhoneEmail(String phoneorEmail) {
        if (TextUtils.isEmpty(phoneorEmail)) {
            return "";
        }
        if (isValidEmail(phoneorEmail)) {
            String[] emails = phoneorEmail.split("@");
            if (emails.length != 2 || emails[0].length() <= 0 || emails[1].length() <= 0) {
                return phoneorEmail;
            }
            String beforeAt = emails[0];
            String afterAt = emails[1];
            if (beforeAt.length() <= 6 || !isValid(beforeAt, DIGITAL_REGX)) {
                if (beforeAt.length() > 8) {
                    return beforeAt.substring(0, beforeAt.length() - 4) + "****" + "@" + afterAt;
                }
                if (beforeAt.length() > 2) {
                    return beforeAt.substring(0, beforeAt.length() - 2) + "**" + "@" + afterAt;
                }
                return generateString("*", beforeAt.length()) + "@" + afterAt;
            } else if (beforeAt.length() > 8) {
                return beforeAt.substring(0, beforeAt.length() - 8) + "****" + beforeAt.substring(beforeAt.length() - 4) + "@" + afterAt;
            } else {
                return generateString("*", beforeAt.length() - 4) + beforeAt.substring(beforeAt.length() - 4) + "@" + afterAt;
            }
        } else if (isValidAllPhoneNumber(phoneorEmail)) {
            int phoneorEmailLen = phoneorEmail.length();
            if (phoneorEmailLen < 5) {
                return phoneorEmail;
            }
            if (phoneorEmailLen < 8) {
                return generateString("*", phoneorEmailLen - 4) + phoneorEmail.substring(phoneorEmailLen - 4);
            }
            return phoneorEmail.substring(0, phoneorEmailLen - 8) + "****" + phoneorEmail.substring(phoneorEmailLen - 4);
        } else if (phoneorEmail.length() < 5) {
            return phoneorEmail;
        } else {
            if (phoneorEmail.length() < 8) {
                return generateString("*", phoneorEmail.length() - 4) + phoneorEmail.substring(phoneorEmail.length() - 4);
            }
            return phoneorEmail.substring(0, phoneorEmail.length() - 8) + "****" + phoneorEmail.substring(phoneorEmail.length() - 4);
        }
    }

    private static String generateString(String str, int num) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < num; i++) {
            buf.append(str);
        }
        return buf.toString();
    }

    private static boolean isValidEmail(String emailString) {
        return isValid(emailString, "^\\s*([A-Za-z0-9_-]+(\\.\\w+)*@(\\w+\\.)+\\w+)\\s*$");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean isValid(String source, String patternStr) {
        if (isEmpty(source) || isEmpty(patternStr) || !Pattern.compile(patternStr).matcher(source).matches()) {
            return false;
        }
        return true;
    }

    private static boolean isEmpty(String str) {
        if (str == null || str.trim().length() < 1) {
            return true;
        }
        return false;
    }

    private static boolean isValidAllPhoneNumber(String phoneNumber) {
        if (Pattern.compile("^1[0-9]{10}$").matcher(phoneNumber).matches()) {
            return true;
        }
        return false;
    }
}
