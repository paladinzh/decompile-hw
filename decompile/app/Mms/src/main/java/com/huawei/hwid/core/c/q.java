package com.huawei.hwid.core.c;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import com.huawei.hwid.core.a.d;
import com.huawei.hwid.core.b.a;
import com.huawei.hwid.core.c.c.c;
import com.huawei.hwid.core.encrypt.e;
import com.huawei.hwid.core.encrypt.f;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.UUID;

/* compiled from: TerminalInfo */
public class q {
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
        com.huawei.hwid.core.c.b.a.e("TerminalInfo", "deviceType= " + a);
        if (2 == a) {
            a.a(context).b("DEVTP", 2);
            return "2";
        }
        a.a(context).b("DEVTP", 0);
        return "0";
    }

    public static String a(Context context, String str) {
        if (-1 == a) {
            a((long) ((TelephonyManager) context.getSystemService("phone")).getPhoneType());
        }
        com.huawei.hwid.core.c.b.a.e("TerminalInfo", "deviceType= " + a);
        if (2 == a) {
            return "2";
        }
        if (i(context).equals(str)) {
            return "6";
        }
        return "0";
    }

    private static synchronized void a(long j) {
        synchronized (q.class) {
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
            com.huawei.hwid.core.c.b.a.a("TerminalInfo", "TerminalType is: " + str);
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            com.huawei.hwid.core.c.b.a.d("TerminalInfo", "in getTerminalType Unsupported encoding exception");
            return str;
        }
    }

    public static String b() {
        String str = "";
        str = Build.MODEL;
        if (TextUtils.isEmpty(str)) {
            str = "unknown";
        }
        com.huawei.hwid.core.c.b.a.a("TerminalInfo", "getTerminalTypeWhenXML TerminalType is: " + str);
        return str;
    }

    public static String b(Context context) {
        String c = c(context);
        if (c == null || "NULL".equals(c)) {
            c = i(context);
        }
        com.huawei.hwid.core.c.b.a.a("TerminalInfo", "UnitedId= " + f.a(c));
        return c;
    }

    public static String c(Context context) {
        String d = d(context);
        if (!d.g() || d.f() || "NULL".equals(d)) {
            return d;
        }
        return d + "_" + d.e();
    }

    public static String d(Context context) {
        if (TextUtils.isEmpty(b)) {
            a(a.a(context).a("DEVID", ""));
            if (TextUtils.isEmpty(b)) {
                if (VERSION.SDK_INT <= 22 || context.checkSelfPermission("android.permission.READ_PHONE_STATE") == 0) {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                    if (telephonyManager != null) {
                        a(telephonyManager.getDeviceId());
                    }
                }
                if (TextUtils.isEmpty(b) || "unknown".equalsIgnoreCase(b)) {
                    return "NULL";
                }
                a.a(context).b("DEVID", e.b(context, b));
            } else {
                a(e.c(context, b));
            }
        }
        return b;
    }

    public static String e(Context context) {
        if (d.g() && !d.f()) {
            return f(context) + "_" + d.e();
        }
        return f(context);
    }

    public static String f(Context context) {
        if (TextUtils.isEmpty(c)) {
            b(a.a(context).a("SUBDEVID", ""));
            if (TextUtils.isEmpty(c)) {
                if (c.b()) {
                    a(d(context));
                    String g = g(context);
                    if (b.equals(g)) {
                        b(h(context));
                        if (c.equals(b)) {
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
                a.a(context).b("SUBDEVID", e.b(context, c));
            } else {
                b(e.c(context, c));
            }
        }
        com.huawei.hwid.core.c.b.a.a("TerminalInfo", "getNextDeviceIdOldWay :" + f.a(c));
        return c;
    }

    private static synchronized void a(String str) {
        synchronized (q.class) {
            b = str;
        }
    }

    private static synchronized void b(String str) {
        synchronized (q.class) {
            c = str;
        }
    }

    public static String g(Context context) {
        String str = "";
        if (c.b()) {
            com.huawei.hwid.core.c.b.a.a("TerminalInfo", "multicard device");
            return c.a().a(0);
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
        if (c.b()) {
            str = c.a().a(1);
        }
        com.huawei.hwid.core.c.b.a.a("TerminalInfo", "in getNextDeviceId isMultiSimEnabled:" + c.b() + " nextDeviceId:" + f.a(str));
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
        com.huawei.hwid.core.c.b.a.a("TerminalInfo", "getUUid :" + f.a(d, true));
        return d;
    }

    private static synchronized void c(String str) {
        synchronized (q.class) {
            d = str;
        }
    }

    public static String j(Context context) {
        String str = "";
        str = b();
        com.huawei.hwid.core.c.b.a.a("TerminalInfo", "The deviceName is : " + f.a(str));
        return str;
    }

    public static String a(Context context, int i) {
        String str = "";
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (c.b()) {
            CharSequence charSequence;
            com.huawei.hwid.core.c.c.a a = c.a();
            if (i == MsgUrlService.RESULT_NOT_IMPL) {
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
        com.huawei.hwid.core.c.b.a.a("TerminalInfo", "getDevicePLMN = " + f.a(str));
        return str;
    }

    public static String c() {
        return VERSION.RELEASE;
    }

    public static String d() {
        String e = e();
        if (TextUtils.isEmpty(e)) {
            com.huawei.hwid.core.c.b.a.b("TerminalInfo", "call getEMMCIDUseFrameWork return empty!!, read it directory");
            e = h();
            if (TextUtils.isEmpty(e)) {
                com.huawei.hwid.core.c.b.a.b("TerminalInfo", "call getEmmcIDDirect also return empty!!");
            }
        }
        return e;
    }

    public static String e() {
        if (-1 != j.a("com.huawei.attestation.HwAttestationManager", "DEVICE_ID_TYPE_EMMC", -1)) {
            try {
                Object b = j.b("com.huawei.attestation.HwAttestationManager", "getDeviceID", new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(j.a("com.huawei.attestation.HwAttestationManager", "DEVICE_ID_TYPE_EMMC", -1))});
                if (b != null) {
                    return new String((byte[]) b, "UTF-8");
                }
            } catch (Exception e) {
                com.huawei.hwid.core.c.b.a.d("TerminalInfo", e.toString());
            }
            return "";
        }
        com.huawei.hwid.core.c.b.a.d("TerminalInfo", "call get typeEMMC failed");
        return "";
    }

    private static String h() {
        FileInputStream fileInputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        InputStream fileInputStream2;
        Throwable e;
        String str;
        InputStreamReader inputStreamReader2;
        BufferedReader bufferedReader2;
        InputStreamReader inputStreamReader3;
        BufferedReader bufferedReader3;
        Reader reader;
        InputStream inputStream;
        Reader reader2;
        boolean z = false;
        BufferedReader bufferedReader4 = null;
        com.huawei.hwid.core.c.b.a.a("TerminalInfo", "enter HwAttestationManager::readEmmcID()");
        try {
            fileInputStream = new FileInputStream("/sys/block/mmcblk0/device/type");
            try {
                inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                try {
                    bufferedReader = new BufferedReader(inputStreamReader);
                    try {
                        Object readLine = bufferedReader.readLine();
                        if (readLine != null) {
                            if (!TextUtils.isEmpty(readLine)) {
                                z = readLine.toLowerCase(Locale.ENGLISH).contentEquals("mmc");
                            }
                        }
                        if (z) {
                            String str2 = "/sys/block/mmcblk0/device/";
                            try {
                                fileInputStream2 = new FileInputStream(str2 + "cid");
                            } catch (FileNotFoundException e2) {
                                e = e2;
                                str = str2;
                                inputStreamReader2 = inputStreamReader;
                                bufferedReader2 = bufferedReader;
                                inputStreamReader3 = inputStreamReader2;
                                try {
                                    com.huawei.hwid.core.c.b.a.d("TerminalInfo", e.toString(), e);
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (Throwable e3) {
                                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3.toString(), e3);
                                        }
                                    }
                                    if (inputStreamReader3 != null) {
                                        try {
                                            inputStreamReader3.close();
                                        } catch (Throwable e32) {
                                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32.toString(), e32);
                                        }
                                    }
                                    if (bufferedReader2 != null) {
                                        try {
                                            bufferedReader2.close();
                                        } catch (Throwable e322) {
                                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e322.toString(), e322);
                                        }
                                    }
                                    if (bufferedReader4 != null) {
                                        try {
                                            bufferedReader4.close();
                                        } catch (Throwable e3222) {
                                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222.toString(), e3222);
                                        }
                                    }
                                    return str;
                                } catch (Throwable th) {
                                    e3222 = th;
                                    bufferedReader3 = bufferedReader2;
                                    inputStreamReader = inputStreamReader3;
                                    bufferedReader = bufferedReader3;
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (Throwable e4) {
                                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e4.toString(), e4);
                                        }
                                    }
                                    if (inputStreamReader != null) {
                                        try {
                                            inputStreamReader.close();
                                        } catch (Throwable e5) {
                                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e5.toString(), e5);
                                        }
                                    }
                                    if (bufferedReader != null) {
                                        try {
                                            bufferedReader.close();
                                        } catch (Throwable e52) {
                                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e52.toString(), e52);
                                        }
                                    }
                                    if (bufferedReader4 != null) {
                                        try {
                                            bufferedReader4.close();
                                        } catch (Throwable e6) {
                                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e6.toString(), e6);
                                        }
                                    }
                                    throw e3222;
                                }
                            } catch (NullPointerException e7) {
                                e3222 = e7;
                                str = str2;
                                try {
                                    com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222.toString(), e3222);
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (Throwable e32222) {
                                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222.toString(), e32222);
                                        }
                                    }
                                    if (inputStreamReader != null) {
                                        try {
                                            inputStreamReader.close();
                                        } catch (Throwable e322222) {
                                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e322222.toString(), e322222);
                                        }
                                    }
                                    if (bufferedReader != null) {
                                        try {
                                            bufferedReader.close();
                                        } catch (Throwable e3222222) {
                                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222.toString(), e3222222);
                                        }
                                    }
                                    if (bufferedReader4 != null) {
                                        try {
                                            bufferedReader4.close();
                                        } catch (Throwable e32222222) {
                                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222.toString(), e32222222);
                                        }
                                    }
                                    return str;
                                } catch (Throwable th2) {
                                    e32222222 = th2;
                                    if (fileInputStream != null) {
                                        fileInputStream.close();
                                    }
                                    if (inputStreamReader != null) {
                                        inputStreamReader.close();
                                    }
                                    if (bufferedReader != null) {
                                        bufferedReader.close();
                                    }
                                    if (bufferedReader4 != null) {
                                        bufferedReader4.close();
                                    }
                                    throw e32222222;
                                }
                            } catch (IOException e8) {
                                e32222222 = e8;
                                str = str2;
                                com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222.toString(), e32222222);
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (Throwable e322222222) {
                                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e322222222.toString(), e322222222);
                                    }
                                }
                                if (inputStreamReader != null) {
                                    try {
                                        inputStreamReader.close();
                                    } catch (Throwable e3222222222) {
                                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222.toString(), e3222222222);
                                    }
                                }
                                if (bufferedReader != null) {
                                    try {
                                        bufferedReader.close();
                                    } catch (Throwable e32222222222) {
                                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222.toString(), e32222222222);
                                    }
                                }
                                if (bufferedReader4 != null) {
                                    try {
                                        bufferedReader4.close();
                                    } catch (Throwable e322222222222) {
                                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e322222222222.toString(), e322222222222);
                                    }
                                }
                                return str;
                            } catch (Exception e9) {
                                e322222222222 = e9;
                                str = str2;
                                com.huawei.hwid.core.c.b.a.d("TerminalInfo", e322222222222.toString(), e322222222222);
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (Throwable e3222222222222) {
                                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222222.toString(), e3222222222222);
                                    }
                                }
                                if (inputStreamReader != null) {
                                    try {
                                        inputStreamReader.close();
                                    } catch (Throwable e32222222222222) {
                                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222.toString(), e32222222222222);
                                    }
                                }
                                if (bufferedReader != null) {
                                    try {
                                        bufferedReader.close();
                                    } catch (Throwable e322222222222222) {
                                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e322222222222222.toString(), e322222222222222);
                                    }
                                }
                                if (bufferedReader4 != null) {
                                    try {
                                        bufferedReader4.close();
                                    } catch (Throwable e3222222222222222) {
                                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222222222.toString(), e3222222222222222);
                                    }
                                }
                                return str;
                            }
                            try {
                                Reader inputStreamReader4 = new InputStreamReader(fileInputStream2, "UTF-8");
                                try {
                                    bufferedReader2 = new BufferedReader(inputStreamReader4);
                                    try {
                                        String readLine2 = bufferedReader2.readLine();
                                        bufferedReader3 = bufferedReader2;
                                        reader = inputStreamReader4;
                                        inputStream = fileInputStream2;
                                        str = readLine2;
                                        bufferedReader4 = bufferedReader3;
                                    } catch (FileNotFoundException e10) {
                                        e3222222222222222 = e10;
                                        bufferedReader4 = bufferedReader2;
                                        bufferedReader2 = bufferedReader;
                                        reader2 = inputStreamReader4;
                                        inputStream = fileInputStream2;
                                        str = str2;
                                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222222222.toString(), e3222222222222222);
                                        if (fileInputStream != null) {
                                            fileInputStream.close();
                                        }
                                        if (inputStreamReader3 != null) {
                                            inputStreamReader3.close();
                                        }
                                        if (bufferedReader2 != null) {
                                            bufferedReader2.close();
                                        }
                                        if (bufferedReader4 != null) {
                                            bufferedReader4.close();
                                        }
                                        return str;
                                    } catch (NullPointerException e11) {
                                        e3222222222222222 = e11;
                                        bufferedReader4 = bufferedReader2;
                                        reader = inputStreamReader4;
                                        inputStream = fileInputStream2;
                                        str = str2;
                                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222222222.toString(), e3222222222222222);
                                        if (fileInputStream != null) {
                                            fileInputStream.close();
                                        }
                                        if (inputStreamReader != null) {
                                            inputStreamReader.close();
                                        }
                                        if (bufferedReader != null) {
                                            bufferedReader.close();
                                        }
                                        if (bufferedReader4 != null) {
                                            bufferedReader4.close();
                                        }
                                        return str;
                                    } catch (IOException e12) {
                                        e3222222222222222 = e12;
                                        bufferedReader4 = bufferedReader2;
                                        reader = inputStreamReader4;
                                        inputStream = fileInputStream2;
                                        str = str2;
                                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222222222.toString(), e3222222222222222);
                                        if (fileInputStream != null) {
                                            fileInputStream.close();
                                        }
                                        if (inputStreamReader != null) {
                                            inputStreamReader.close();
                                        }
                                        if (bufferedReader != null) {
                                            bufferedReader.close();
                                        }
                                        if (bufferedReader4 != null) {
                                            bufferedReader4.close();
                                        }
                                        return str;
                                    } catch (Exception e13) {
                                        e3222222222222222 = e13;
                                        bufferedReader4 = bufferedReader2;
                                        reader = inputStreamReader4;
                                        inputStream = fileInputStream2;
                                        str = str2;
                                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222222222.toString(), e3222222222222222);
                                        if (fileInputStream != null) {
                                            fileInputStream.close();
                                        }
                                        if (inputStreamReader != null) {
                                            inputStreamReader.close();
                                        }
                                        if (bufferedReader != null) {
                                            bufferedReader.close();
                                        }
                                        if (bufferedReader4 != null) {
                                            bufferedReader4.close();
                                        }
                                        return str;
                                    } catch (Throwable th3) {
                                        e3222222222222222 = th3;
                                        bufferedReader4 = bufferedReader2;
                                        reader = inputStreamReader4;
                                        inputStream = fileInputStream2;
                                        if (fileInputStream != null) {
                                            fileInputStream.close();
                                        }
                                        if (inputStreamReader != null) {
                                            inputStreamReader.close();
                                        }
                                        if (bufferedReader != null) {
                                            bufferedReader.close();
                                        }
                                        if (bufferedReader4 != null) {
                                            bufferedReader4.close();
                                        }
                                        throw e3222222222222222;
                                    }
                                } catch (FileNotFoundException e14) {
                                    e3222222222222222 = e14;
                                    bufferedReader2 = bufferedReader;
                                    reader2 = inputStreamReader4;
                                    inputStream = fileInputStream2;
                                    str = str2;
                                    com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222222222.toString(), e3222222222222222);
                                    if (fileInputStream != null) {
                                        fileInputStream.close();
                                    }
                                    if (inputStreamReader3 != null) {
                                        inputStreamReader3.close();
                                    }
                                    if (bufferedReader2 != null) {
                                        bufferedReader2.close();
                                    }
                                    if (bufferedReader4 != null) {
                                        bufferedReader4.close();
                                    }
                                    return str;
                                } catch (NullPointerException e15) {
                                    e3222222222222222 = e15;
                                    reader = inputStreamReader4;
                                    inputStream = fileInputStream2;
                                    str = str2;
                                    com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222222222.toString(), e3222222222222222);
                                    if (fileInputStream != null) {
                                        fileInputStream.close();
                                    }
                                    if (inputStreamReader != null) {
                                        inputStreamReader.close();
                                    }
                                    if (bufferedReader != null) {
                                        bufferedReader.close();
                                    }
                                    if (bufferedReader4 != null) {
                                        bufferedReader4.close();
                                    }
                                    return str;
                                } catch (IOException e16) {
                                    e3222222222222222 = e16;
                                    reader = inputStreamReader4;
                                    inputStream = fileInputStream2;
                                    str = str2;
                                    com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222222222.toString(), e3222222222222222);
                                    if (fileInputStream != null) {
                                        fileInputStream.close();
                                    }
                                    if (inputStreamReader != null) {
                                        inputStreamReader.close();
                                    }
                                    if (bufferedReader != null) {
                                        bufferedReader.close();
                                    }
                                    if (bufferedReader4 != null) {
                                        bufferedReader4.close();
                                    }
                                    return str;
                                } catch (Exception e17) {
                                    e3222222222222222 = e17;
                                    reader = inputStreamReader4;
                                    inputStream = fileInputStream2;
                                    str = str2;
                                    com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222222222.toString(), e3222222222222222);
                                    if (fileInputStream != null) {
                                        fileInputStream.close();
                                    }
                                    if (inputStreamReader != null) {
                                        inputStreamReader.close();
                                    }
                                    if (bufferedReader != null) {
                                        bufferedReader.close();
                                    }
                                    if (bufferedReader4 != null) {
                                        bufferedReader4.close();
                                    }
                                    return str;
                                } catch (Throwable th4) {
                                    e3222222222222222 = th4;
                                    reader = inputStreamReader4;
                                    inputStream = fileInputStream2;
                                    if (fileInputStream != null) {
                                        fileInputStream.close();
                                    }
                                    if (inputStreamReader != null) {
                                        inputStreamReader.close();
                                    }
                                    if (bufferedReader != null) {
                                        bufferedReader.close();
                                    }
                                    if (bufferedReader4 != null) {
                                        bufferedReader4.close();
                                    }
                                    throw e3222222222222222;
                                }
                            } catch (FileNotFoundException e18) {
                                e3222222222222222 = e18;
                                inputStream = fileInputStream2;
                                str = str2;
                                bufferedReader3 = bufferedReader;
                                inputStreamReader3 = inputStreamReader;
                                bufferedReader2 = bufferedReader3;
                                com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222222222.toString(), e3222222222222222);
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                                if (inputStreamReader3 != null) {
                                    inputStreamReader3.close();
                                }
                                if (bufferedReader2 != null) {
                                    bufferedReader2.close();
                                }
                                if (bufferedReader4 != null) {
                                    bufferedReader4.close();
                                }
                                return str;
                            } catch (NullPointerException e19) {
                                e3222222222222222 = e19;
                                inputStream = fileInputStream2;
                                str = str2;
                                com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222222222.toString(), e3222222222222222);
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                                if (inputStreamReader != null) {
                                    inputStreamReader.close();
                                }
                                if (bufferedReader != null) {
                                    bufferedReader.close();
                                }
                                if (bufferedReader4 != null) {
                                    bufferedReader4.close();
                                }
                                return str;
                            } catch (IOException e20) {
                                e3222222222222222 = e20;
                                inputStream = fileInputStream2;
                                str = str2;
                                com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222222222.toString(), e3222222222222222);
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                                if (inputStreamReader != null) {
                                    inputStreamReader.close();
                                }
                                if (bufferedReader != null) {
                                    bufferedReader.close();
                                }
                                if (bufferedReader4 != null) {
                                    bufferedReader4.close();
                                }
                                return str;
                            } catch (Exception e21) {
                                e3222222222222222 = e21;
                                inputStream = fileInputStream2;
                                str = str2;
                                com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222222222.toString(), e3222222222222222);
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                                if (inputStreamReader != null) {
                                    inputStreamReader.close();
                                }
                                if (bufferedReader != null) {
                                    bufferedReader.close();
                                }
                                if (bufferedReader4 != null) {
                                    bufferedReader4.close();
                                }
                                return str;
                            } catch (Throwable th5) {
                                e3222222222222222 = th5;
                                inputStream = fileInputStream2;
                                if (fileInputStream != null) {
                                    fileInputStream.close();
                                }
                                if (inputStreamReader != null) {
                                    inputStreamReader.close();
                                }
                                if (bufferedReader != null) {
                                    bufferedReader.close();
                                }
                                if (bufferedReader4 != null) {
                                    bufferedReader4.close();
                                }
                                throw e3222222222222222;
                            }
                        }
                        str = null;
                        try {
                            com.huawei.hwid.core.c.b.a.b("TerminalInfo", "emmcId=" + f.a(str + ""));
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (Throwable e32222222222222222) {
                                    com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222.toString(), e32222222222222222);
                                }
                            }
                            if (inputStreamReader != null) {
                                try {
                                    inputStreamReader.close();
                                } catch (Throwable e322222222222222222) {
                                    com.huawei.hwid.core.c.b.a.d("TerminalInfo", e322222222222222222.toString(), e322222222222222222);
                                }
                            }
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (Throwable e3222222222222222222) {
                                    com.huawei.hwid.core.c.b.a.d("TerminalInfo", e3222222222222222222.toString(), e3222222222222222222);
                                }
                            }
                            if (bufferedReader4 != null) {
                                try {
                                    bufferedReader4.close();
                                } catch (Throwable e32222222222222222222) {
                                    com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                                }
                            }
                        } catch (FileNotFoundException e22) {
                            e32222222222222222222 = e22;
                            bufferedReader3 = bufferedReader;
                            inputStreamReader3 = inputStreamReader;
                            bufferedReader2 = bufferedReader3;
                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            if (inputStreamReader3 != null) {
                                inputStreamReader3.close();
                            }
                            if (bufferedReader2 != null) {
                                bufferedReader2.close();
                            }
                            if (bufferedReader4 != null) {
                                bufferedReader4.close();
                            }
                            return str;
                        } catch (NullPointerException e23) {
                            e32222222222222222222 = e23;
                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            if (inputStreamReader != null) {
                                inputStreamReader.close();
                            }
                            if (bufferedReader != null) {
                                bufferedReader.close();
                            }
                            if (bufferedReader4 != null) {
                                bufferedReader4.close();
                            }
                            return str;
                        } catch (IOException e24) {
                            e32222222222222222222 = e24;
                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            if (inputStreamReader != null) {
                                inputStreamReader.close();
                            }
                            if (bufferedReader != null) {
                                bufferedReader.close();
                            }
                            if (bufferedReader4 != null) {
                                bufferedReader4.close();
                            }
                            return str;
                        } catch (Exception e25) {
                            e32222222222222222222 = e25;
                            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            if (inputStreamReader != null) {
                                inputStreamReader.close();
                            }
                            if (bufferedReader != null) {
                                bufferedReader.close();
                            }
                            if (bufferedReader4 != null) {
                                bufferedReader4.close();
                            }
                            return str;
                        }
                    } catch (FileNotFoundException e26) {
                        e32222222222222222222 = e26;
                        str = null;
                        inputStreamReader2 = inputStreamReader;
                        bufferedReader2 = bufferedReader;
                        inputStreamReader3 = inputStreamReader2;
                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        if (inputStreamReader3 != null) {
                            inputStreamReader3.close();
                        }
                        if (bufferedReader2 != null) {
                            bufferedReader2.close();
                        }
                        if (bufferedReader4 != null) {
                            bufferedReader4.close();
                        }
                        return str;
                    } catch (NullPointerException e27) {
                        e32222222222222222222 = e27;
                        str = null;
                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        if (inputStreamReader != null) {
                            inputStreamReader.close();
                        }
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        if (bufferedReader4 != null) {
                            bufferedReader4.close();
                        }
                        return str;
                    } catch (IOException e28) {
                        e32222222222222222222 = e28;
                        str = null;
                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        if (inputStreamReader != null) {
                            inputStreamReader.close();
                        }
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        if (bufferedReader4 != null) {
                            bufferedReader4.close();
                        }
                        return str;
                    } catch (Exception e29) {
                        e32222222222222222222 = e29;
                        str = null;
                        com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        if (inputStreamReader != null) {
                            inputStreamReader.close();
                        }
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        if (bufferedReader4 != null) {
                            bufferedReader4.close();
                        }
                        return str;
                    }
                } catch (FileNotFoundException e30) {
                    e32222222222222222222 = e30;
                    inputStreamReader3 = inputStreamReader;
                    str = null;
                    bufferedReader2 = null;
                    com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (inputStreamReader3 != null) {
                        inputStreamReader3.close();
                    }
                    if (bufferedReader2 != null) {
                        bufferedReader2.close();
                    }
                    if (bufferedReader4 != null) {
                        bufferedReader4.close();
                    }
                    return str;
                } catch (NullPointerException e31) {
                    e32222222222222222222 = e31;
                    bufferedReader = null;
                    str = null;
                    com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (bufferedReader4 != null) {
                        bufferedReader4.close();
                    }
                    return str;
                } catch (IOException e33) {
                    e32222222222222222222 = e33;
                    bufferedReader = null;
                    str = null;
                    com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (bufferedReader4 != null) {
                        bufferedReader4.close();
                    }
                    return str;
                } catch (Exception e34) {
                    e32222222222222222222 = e34;
                    bufferedReader = null;
                    str = null;
                    com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (bufferedReader4 != null) {
                        bufferedReader4.close();
                    }
                    return str;
                } catch (Throwable th6) {
                    e32222222222222222222 = th6;
                    bufferedReader = null;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (bufferedReader4 != null) {
                        bufferedReader4.close();
                    }
                    throw e32222222222222222222;
                }
            } catch (FileNotFoundException e35) {
                e32222222222222222222 = e35;
                bufferedReader2 = null;
                inputStreamReader3 = null;
                str = null;
                com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader3 != null) {
                    inputStreamReader3.close();
                }
                if (bufferedReader2 != null) {
                    bufferedReader2.close();
                }
                if (bufferedReader4 != null) {
                    bufferedReader4.close();
                }
                return str;
            } catch (NullPointerException e36) {
                e32222222222222222222 = e36;
                bufferedReader = null;
                inputStreamReader = null;
                str = null;
                com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (bufferedReader4 != null) {
                    bufferedReader4.close();
                }
                return str;
            } catch (IOException e37) {
                e32222222222222222222 = e37;
                bufferedReader = null;
                inputStreamReader = null;
                str = null;
                com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (bufferedReader4 != null) {
                    bufferedReader4.close();
                }
                return str;
            } catch (Exception e38) {
                e32222222222222222222 = e38;
                bufferedReader = null;
                inputStreamReader = null;
                str = null;
                com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (bufferedReader4 != null) {
                    bufferedReader4.close();
                }
                return str;
            } catch (Throwable th7) {
                e32222222222222222222 = th7;
                bufferedReader = null;
                inputStreamReader = null;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (bufferedReader4 != null) {
                    bufferedReader4.close();
                }
                throw e32222222222222222222;
            }
        } catch (FileNotFoundException e39) {
            e32222222222222222222 = e39;
            bufferedReader2 = null;
            inputStreamReader3 = null;
            fileInputStream = null;
            str = null;
            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (inputStreamReader3 != null) {
                inputStreamReader3.close();
            }
            if (bufferedReader2 != null) {
                bufferedReader2.close();
            }
            if (bufferedReader4 != null) {
                bufferedReader4.close();
            }
            return str;
        } catch (NullPointerException e40) {
            e32222222222222222222 = e40;
            bufferedReader = null;
            inputStreamReader = null;
            fileInputStream = null;
            str = null;
            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedReader4 != null) {
                bufferedReader4.close();
            }
            return str;
        } catch (IOException e41) {
            e32222222222222222222 = e41;
            bufferedReader = null;
            inputStreamReader = null;
            fileInputStream = null;
            str = null;
            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedReader4 != null) {
                bufferedReader4.close();
            }
            return str;
        } catch (Exception e42) {
            e32222222222222222222 = e42;
            bufferedReader = null;
            inputStreamReader = null;
            fileInputStream = null;
            str = null;
            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e32222222222222222222.toString(), e32222222222222222222);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedReader4 != null) {
                bufferedReader4.close();
            }
            return str;
        } catch (Throwable th8) {
            e32222222222222222222 = th8;
            bufferedReader = null;
            inputStreamReader = null;
            fileInputStream = null;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedReader4 != null) {
                bufferedReader4.close();
            }
            throw e32222222222222222222;
        }
        return str;
    }

    public static byte[] a(String str, String str2) {
        if (-1 != j.a("com.huawei.attestation.HwAttestationManager", "KEY_INDEX_HWCLOUD", -1)) {
            if (-1 != j.a("com.huawei.attestation.HwAttestationManager", "DEVICE_ID_TYPE_EMMC", -1)) {
                byte[] bArr;
                try {
                    byte[] bytes = str2.getBytes("UTF-8");
                    Class cls = Class.forName("com.huawei.attestation.HwAttestationManager");
                    Object a = j.a(cls, cls.newInstance(), "getAttestationSignature", new Class[]{Integer.TYPE, Integer.TYPE, String.class, byte[].class}, new Object[]{Integer.valueOf(r0), Integer.valueOf(r2), str, bytes});
                    if (a == null) {
                        bArr = null;
                    } else {
                        bArr = (byte[]) a;
                    }
                } catch (Exception e) {
                    com.huawei.hwid.core.c.b.a.b("TerminalInfo", e.toString());
                    bArr = null;
                }
                if (bArr == null || bArr.length == 0) {
                    com.huawei.hwid.core.c.b.a.d("TerminalInfo", "call DeviceAttestationManager::getAttestionSignature cause err:" + f());
                }
                return bArr;
            }
            com.huawei.hwid.core.c.b.a.d("TerminalInfo", "get DEVICE_ID_TYPE_EMMC failed");
            return new byte[0];
        }
        com.huawei.hwid.core.c.b.a.d("TerminalInfo", "get KEY_INDEX_HWCLOUD failed");
        return new byte[0];
    }

    public static String f() {
        Object b;
        try {
            b = j.b("com.huawei.attestation.HwAttestationManager", "getLastError", null, null);
        } catch (Exception e) {
            com.huawei.hwid.core.c.b.a.d("TerminalInfo", e.toString());
            b = null;
        }
        return String.valueOf(b);
    }

    public static String g() {
        String str = "";
        try {
            return (String) j.b("com.huawei.attestation.HwAttestationManager", "getPublickKey", new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(1)});
        } catch (Exception e) {
            com.huawei.hwid.core.c.b.a.b("TerminalInfo", e.toString());
            return str;
        }
    }

    public static void a(Context context, String str, String str2) {
        String g = g();
        if (g == null) {
            g = "";
        }
        com.huawei.hwid.core.c.b.a.a("TerminalInfo", "sign base:" + f.a(g) + " len:" + g.length());
        com.huawei.hwid.core.a.c cVar = new com.huawei.hwid.core.a.c(context, str2);
        cVar.b(str);
        cVar.d(g);
        d.a(cVar, context);
    }
}
