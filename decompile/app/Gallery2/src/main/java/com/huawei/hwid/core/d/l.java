package com.huawei.hwid.core.d;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.gallery3d.gadget.XmlUtils;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface.GpsMeasureMode;
import com.huawei.hwid.core.c.a;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.c.b;
import com.huawei.hwid.core.encrypt.f;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

public class l {
    private static long a = -1;
    private static String b = "";
    private static String c = "";
    private static String d = "";

    public static String a(Context context) {
        if (-1 == a) {
            a(a.a(context).a("DEVTP", -1));
            if (-1 == a) {
                if (VERSION.SDK_INT <= 22 || context.checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                    if (telephonyManager != null) {
                        a((long) telephonyManager.getPhoneType());
                    }
                }
            }
        }
        e.e("TerminalInfo", "deviceType= " + a);
        if (2 == a) {
            a.a(context).b("DEVTP", 2);
            return GpsMeasureMode.MODE_2_DIMENSIONAL;
        }
        a.a(context).b("DEVTP", 0);
        return "0";
    }

    public static String a(Context context, String str) {
        if (-1 == a) {
            a((long) ((TelephonyManager) context.getSystemService("phone")).getPhoneType());
        }
        e.e("TerminalInfo", "deviceType= " + a);
        if (2 == a) {
            return GpsMeasureMode.MODE_2_DIMENSIONAL;
        }
        if (i(context).equals(str)) {
            return "6";
        }
        return "0";
    }

    private static synchronized void a(long j) {
        synchronized (l.class) {
            a = j;
        }
    }

    public static String a() {
        String str = "";
        str = Build.MODEL;
        if (TextUtils.isEmpty(str)) {
            str = "unknown";
        }
        try {
            e.a("TerminalInfo", "TerminalType is: " + str);
            return URLEncoder.encode(str, XmlUtils.INPUT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.d("TerminalInfo", "in getTerminalType Unsupported encoding exception");
            return str;
        }
    }

    public static String b() {
        String str = "";
        str = Build.MODEL;
        if (TextUtils.isEmpty(str)) {
            str = "unknown";
        }
        e.a("TerminalInfo", "getTerminalTypeWhenXML TerminalType is: " + str);
        return str;
    }

    public static String b(Context context) {
        String c = c(context);
        if (c == null || "NULL".equals(c)) {
            c = i(context);
        }
        e.a("TerminalInfo", "UnitedId= " + f.a(c));
        return c;
    }

    public static String c(Context context) {
        String d = d(context);
        if (!b.f() || b.e() || "NULL".equals(d)) {
            return d;
        }
        return d + "_" + b.d();
    }

    public static String d(Context context) {
        if (TextUtils.isEmpty(b)) {
            a(a.a(context).a("DEVID", ""));
            if (!TextUtils.isEmpty(b)) {
                String c = com.huawei.hwid.core.encrypt.e.c(context, b);
                if (TextUtils.isEmpty(c)) {
                    e.d("TerminalInfo", "cbcDecrypter devid failed!!!");
                } else {
                    a(c);
                    return b;
                }
            }
            if (VERSION.SDK_INT <= 22 || context.checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                if (telephonyManager != null) {
                    a(telephonyManager.getDeviceId());
                }
            }
            if (TextUtils.isEmpty(b) || "unknown".equalsIgnoreCase(b)) {
                return "NULL";
            }
            a.a(context).b("DEVID", com.huawei.hwid.core.encrypt.e.b(context, b));
        }
        return b;
    }

    public static String e(Context context) {
        if (b.f() && !b.e()) {
            return f(context) + "_" + b.d();
        }
        return f(context);
    }

    public static String f(Context context) {
        if (TextUtils.isEmpty(c)) {
            b(a.a(context).a("SUBDEVID", ""));
            if (TextUtils.isEmpty(c)) {
                if (b.b()) {
                    a(d(context));
                    String g = g(context);
                    if (!TextUtils.isEmpty(g) && g.equals(b)) {
                        b(h(context));
                        if (c == null || c.equals(b)) {
                            b("NULL");
                        }
                    } else {
                        b(g);
                    }
                } else {
                    b("NULL");
                }
                if (TextUtils.isEmpty(c) || "unknown".equalsIgnoreCase(c)) {
                    return "NULL";
                }
                a.a(context).b("SUBDEVID", com.huawei.hwid.core.encrypt.e.b(context, c));
            } else {
                b(com.huawei.hwid.core.encrypt.e.c(context, c));
            }
        }
        e.a("TerminalInfo", "getNextDeviceIdOldWay :" + f.a(c));
        return c;
    }

    private static synchronized void a(String str) {
        synchronized (l.class) {
            if (str != null) {
                b = str;
            } else {
                b = "";
            }
        }
    }

    private static synchronized void b(String str) {
        synchronized (l.class) {
            if (str != null) {
                c = str;
            } else {
                c = "";
            }
        }
    }

    public static String g(Context context) {
        String str = "";
        if (b.b()) {
            e.a("TerminalInfo", "multicard device");
            return b.a().a(0);
        } else if (VERSION.SDK_INT > 22 && context.checkSelfPermission("android.permission.READ_PHONE_STATE") != 0) {
            return str;
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (telephonyManager == null) {
                return str;
            }
            return telephonyManager.getDeviceId();
        }
    }

    public static String h(Context context) {
        String str = "";
        if (b.b()) {
            str = b.a().a(1);
        }
        e.a("TerminalInfo", "in getNextDeviceId isMultiSimEnabled:" + b.b() + " nextDeviceId:" + f.a(str));
        return str;
    }

    public static String i(Context context) {
        if (TextUtils.isEmpty(d)) {
            c(a.a(context).a("UUID", ""));
            if (TextUtils.isEmpty(d)) {
                c(UUID.randomUUID().toString());
                if (TextUtils.isEmpty(d)) {
                    return "NULL";
                }
                a.a(context).b("UUID", d);
            }
        }
        e.a("TerminalInfo", "getUUid :" + f.a(d, true));
        return d;
    }

    private static synchronized void c(String str) {
        synchronized (l.class) {
            d = str;
        }
    }

    public static String a(Context context, int i) {
        String str = "";
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (b.b()) {
            CharSequence charSequence;
            com.huawei.hwid.core.d.c.a a = b.a();
            if (i == -999) {
                i = a.a();
            }
            if (5 != a.c(i)) {
                charSequence = str;
            } else {
                charSequence = a.d(i);
                if (TextUtils.isEmpty(charSequence)) {
                    charSequence = a.b(i);
                    if (!TextUtils.isEmpty(charSequence)) {
                        charSequence = charSequence.substring(0, 5);
                    }
                }
            }
            CharSequence charSequence2 = charSequence;
        } else if (5 == telephonyManager.getSimState()) {
            str = telephonyManager.getSimOperator();
            if (TextUtils.isEmpty(str)) {
                str = telephonyManager.getSubscriberId();
                if (!TextUtils.isEmpty(str)) {
                    str = str.substring(0, 5);
                }
            }
        }
        if (TextUtils.isEmpty(str)) {
            str = "00000";
        }
        e.a("TerminalInfo", "getDevicePLMN = " + f.a(str));
        return str;
    }

    public static String c() {
        return VERSION.RELEASE;
    }
}
