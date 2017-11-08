package com.huawei.hwid.core.encrypt;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.hwid.core.d.b.e;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class f {
    private static final List<String> a = new ArrayList();
    private static String[] b = new String[]{"userid", "password", "siteid", "plmn", "mobilephone", "deviceinfo", "uuid", "deviceid2", "secretdigest", "salt", "emmcid", "secretdigesttype", "clientip", "deviceid", "device_id", "securityphone", "securityemail", "cookie", "devicetype", "useremail", "email", "servicetoken", "oldpassword", "newpassword", "thirdtoken", "smsauthcode", "phone", "access_token", "sc", "sso_st", "token", "ac", "pw", "dvid", "pl", "dvid2", "sc", "emid", "sct", "c", "st", "app", "uid", "imsi"};

    static {
        a();
    }

    private static void a() {
        if (b != null) {
            for (Object add : b) {
                a.add(add);
            }
        }
        e.a("Proguard", "keyList size is " + a.size());
    }

    private static String a(char c, int i) {
        StringBuffer stringBuffer = new StringBuffer(i);
        for (int i2 = 0; i2 < i; i2++) {
            stringBuffer.append(c);
        }
        return stringBuffer.toString();
    }

    public static String a(Object obj) {
        return a(String.valueOf(obj), true);
    }

    public static String a(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        int ceil = (int) Math.ceil(((double) (str.length() * 50)) / 100.0d);
        return a('*', ceil) + str.substring(ceil);
    }

    public static String a(String str, Object obj) {
        String str2 = "";
        if (str == null || obj == null) {
            return str2;
        }
        String b = b(str);
        String valueOf = String.valueOf(obj);
        if (!String.valueOf('*').equals(b)) {
            str2 = valueOf;
        } else if (TextUtils.isEmpty(valueOf)) {
            str2 = valueOf;
        } else {
            String str3 = "";
            str2 = a(String.valueOf('*'), valueOf.length());
            if (valueOf.length() > 4) {
                str3 = valueOf.substring(valueOf.length() - 2);
                str2 = str2.substring(0, valueOf.length() - 2);
            }
            str2 = str2 + str3;
        }
        return b + "=" + str2;
    }

    private static String a(String str, int i) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i2 = 0; i2 < i; i2++) {
            stringBuffer.append(str);
        }
        return stringBuffer.toString();
    }

    public static String a(Bundle bundle) {
        if (bundle == null) {
            return "";
        }
        Set<String> keySet = bundle.keySet();
        StringBuffer stringBuffer = new StringBuffer();
        for (String str : keySet) {
            Object obj = bundle.get(str);
            String str2 = "";
            if (obj instanceof Bundle) {
                obj = a((Bundle) obj);
            } else {
                obj = a(obj);
            }
            stringBuffer.append(a(str, obj)).append(" ");
        }
        return stringBuffer.toString();
    }

    public static String b(String str) {
        if (a == null || !a.contains(str.toLowerCase(Locale.ENGLISH))) {
            return str;
        }
        e.a("Proguard", "keyList contains " + str.toLowerCase(Locale.ENGLISH));
        return String.valueOf('*');
    }

    public static String a(Intent intent) {
        if (intent == null) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        try {
            if (!TextUtils.isEmpty(intent.getAction())) {
                stringBuffer.append("act:" + intent.getAction()).append(" ");
            }
            stringBuffer.append(" flag:" + intent.getFlags()).append(" ");
            if (intent.getExtras() != null) {
                stringBuffer.append(a(intent.getExtras()));
            }
        } catch (Exception e) {
            e.c("Proguard", e.getMessage());
        }
        return stringBuffer.toString();
    }

    public static String a(String str, boolean z) {
        if (!z) {
            return a(str);
        }
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        char[] toCharArray = str.toCharArray();
        for (int i = 0; i < toCharArray.length; i += 2) {
            if (!"{:=@}/#?%\"(),/\\<>| &".contains(String.valueOf(toCharArray[i]))) {
                toCharArray[i] = '*';
            }
        }
        return String.valueOf(toCharArray);
    }

    public static String c(String str) {
        return "ACCOUNT_NAME";
    }
}
