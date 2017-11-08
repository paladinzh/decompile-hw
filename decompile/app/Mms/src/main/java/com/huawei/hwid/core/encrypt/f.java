package com.huawei.hwid.core.encrypt;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.net.NetUtil;
import com.google.android.gms.common.Scopes;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.p;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/* compiled from: Proguard */
public class f {
    private static final List a = new ArrayList();
    private static String[] b = new String[]{"userid", "password", "siteid", "plmn", "mobilephone", "deviceinfo", "uuid", "deviceid2", "secretdigest", "salt", "emmcid", "secretdigesttype", "clientip", "deviceid", "device_id", "securityphone", "securityemail", "cookie", "devicetype", "useremail", Scopes.EMAIL, "servicetoken", "oldpassword", "newpassword", "thirdtoken", "smsauthcode", "phone", "access_token", "sc", "sso_st", NetUtil.REQ_QUERY_TOEKN, "ac", "pw", "dvid", "pl", "dvid2", "sc", "emid", "sct", "c", "st", "app", "uid"};

    static {
        a();
    }

    private static void a() {
        if (b != null) {
            for (Object add : b) {
                a.add(add);
            }
        }
        a.a("Proguard", "keyList size is " + a.size());
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
        int ceil = (int) Math.ceil(((double) (str.length() * 30)) / 100.0d);
        return a('*', ceil) + str.substring(ceil);
    }

    public static String a(Bundle bundle) {
        if (bundle == null) {
            return "";
        }
        Set<String> keySet = bundle.keySet();
        StringBuffer stringBuffer = new StringBuffer();
        for (String str : keySet) {
            String a;
            Object obj = bundle.get(str);
            String str2 = "";
            if (obj instanceof Bundle) {
                a = a((Bundle) obj);
            } else {
                a = a(obj);
            }
            stringBuffer.append(b(str)).append("=").append(a).append(" ");
        }
        return stringBuffer.toString();
    }

    public static String b(String str) {
        if (a == null || !a.contains(str.toLowerCase(Locale.ENGLISH))) {
            return str;
        }
        a.a("Proguard", "keyList contains " + str.toLowerCase(Locale.ENGLISH));
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
            a.c("Proguard", e.getMessage());
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
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        if (p.a(str)) {
            String[] split = str.split("@");
            if (split.length != 2 || split[0].length() <= 0 || split[1].length() <= 0) {
                return str;
            }
            String str2 = split[0];
            String str3 = split[1];
            if (str2.length() > 6 && p.a(str2, "[0-9]+")) {
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
        } else if (p.c(str)) {
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
}
