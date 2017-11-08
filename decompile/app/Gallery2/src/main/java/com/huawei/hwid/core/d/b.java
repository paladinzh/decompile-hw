package com.huawei.hwid.core.d;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import com.android.gallery3d.gadget.XmlUtils;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface.GpsMeasureMode;
import com.huawei.android.os.BuildEx;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.b.a;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class b {
    public static String a(byte[] bArr) {
        int i = 0;
        if (bArr == null) {
            return null;
        }
        if (bArr.length == 0) {
            return "";
        }
        char[] cArr = new char[(bArr.length * 2)];
        char[] cArr2 = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        while (i < bArr.length) {
            byte b = bArr[i];
            cArr[i * 2] = (char) cArr2[(b & 240) >> 4];
            cArr[(i * 2) + 1] = (char) cArr2[b & 15];
            i++;
        }
        return new String(cArr);
    }

    public static byte[] a(String str) {
        byte[] bArr = new byte[(str.length() / 2)];
        try {
            byte[] bytes = str.getBytes(XmlUtils.INPUT_ENCODING);
            for (int i = 0; i < bArr.length; i++) {
                bArr[i] = (byte) ((byte) (((byte) (Byte.decode("0x" + new String(new byte[]{(byte) bytes[i * 2]}, XmlUtils.INPUT_ENCODING)).byteValue() << 4)) ^ Byte.decode("0x" + new String(new byte[]{(byte) bytes[(i * 2) + 1]}, XmlUtils.INPUT_ENCODING)).byteValue()));
            }
        } catch (UnsupportedEncodingException e) {
            e.d("BaseUtil", "hexString2ByteArray UnsupportedEncodingException");
        }
        return bArr;
    }

    public static boolean a(Context context) {
        e.b("BaseUtil", "enter networkIsAvaiable");
        if (context == null) {
            return false;
        }
        Object systemService = context.getSystemService("connectivity");
        if (systemService != null) {
            NetworkInfo[] allNetworkInfo = ((ConnectivityManager) systemService).getAllNetworkInfo();
            if (allNetworkInfo == null || allNetworkInfo.length == 0) {
                e.b("BaseUtil", "NetworkInfo is null,so networkIsAvaiable is unaviable");
                return false;
            }
            for (int i = 0; i < allNetworkInfo.length; i++) {
                if (allNetworkInfo[i] != null) {
                    State state = allNetworkInfo[i].getState();
                    e.b("BaseUtil", "NetworkInfo  state " + i + state);
                    if (state == State.CONNECTED) {
                        e.b("BaseUtil", "NetworkInfo  state " + i + state + "is CONNECTED");
                        return true;
                    }
                }
            }
            e.b("BaseUtil", "NetworkInfo  state is unaviable");
            return false;
        }
        e.b("BaseUtil", "connectivity is null,so networkIsAvaiable is unaviable");
        return false;
    }

    public static int b(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivityManager == null) {
            return 0;
        }
        NetworkInfo[] allNetworkInfo = connectivityManager.getAllNetworkInfo();
        if (allNetworkInfo == null) {
            return 0;
        }
        for (int i = 0; i < allNetworkInfo.length; i++) {
            if (allNetworkInfo[i].getState() == State.CONNECTED) {
                return allNetworkInfo[i].getType();
            }
        }
        return 0;
    }

    public static String c(Context context) {
        String[] strArr = new String[]{"Unknown", "Unknown"};
        if (context.getPackageManager().checkPermission("android.permission.ACCESS_NETWORK_STATE", context.getPackageName()) == 0) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(1);
                if (networkInfo != null && networkInfo.getState() == State.CONNECTED) {
                    strArr[0] = "Wi-Fi";
                    return strArr[0];
                }
                NetworkInfo networkInfo2 = connectivityManager.getNetworkInfo(0);
                if (networkInfo2 == null || networkInfo2.getState() != State.CONNECTED) {
                    return strArr[0];
                }
                strArr[0] = "2G/3G/4G";
                strArr[1] = networkInfo2.getSubtypeName();
                return strArr[0] + strArr[1];
            }
            strArr[0] = "Unknown";
            return strArr[0];
        }
        strArr[0] = "Unknown";
        return strArr[0];
    }

    public static String a() {
        return f("yyyyMMddHHmmssSSS");
    }

    @SuppressLint({"SimpleDateFormat"})
    public static String a(String str, String str2, String str3) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2) || TextUtils.isEmpty(str3)) {
            return "";
        }
        try {
            return new SimpleDateFormat(str3).format(new SimpleDateFormat(str2).parse(str));
        } catch (Throwable e) {
            e.b("BaseUtil", e.getMessage(), e);
            return "";
        }
    }

    public static String d(Context context) {
        return context.getPackageName();
    }

    public static String f(Context context) {
        String toLowerCase = context.getResources().getConfiguration().locale.getCountry().toLowerCase(Locale.getDefault());
        e.b("BaseUtil", "countryStr:" + toLowerCase);
        return toLowerCase;
    }

    public static int a(Context context, String str) {
        HwAccount b = a.a(context).b();
        if (b == null) {
            b = a.a(context).c();
        }
        if (b == null) {
            return 0;
        }
        return b.e();
    }

    public static boolean a(HwAccount hwAccount) {
        if (hwAccount == null) {
            return false;
        }
        if (!TextUtils.isEmpty(hwAccount.b()) && !TextUtils.isEmpty(hwAccount.d()) && !TextUtils.isEmpty(hwAccount.c()) && !TextUtils.isEmpty(hwAccount.g())) {
            return true;
        }
        e.a("BaseUtil", "addHwAccount is invalid:" + f.a(hwAccount.toString(), true));
        return false;
    }

    public static boolean h(Context context) {
        return context.getPackageName().equals("com.huawei.hwid");
    }

    public static boolean a(Context context, String str, boolean z) {
        Object b = f.b(context, str);
        if (TextUtils.isEmpty(b)) {
            return z;
        }
        try {
            return Boolean.parseBoolean(b);
        } catch (Exception e) {
            return z;
        }
    }

    public static boolean b(Context context, String str) {
        return a(context, str, "com.huawei.hwid");
    }

    public static boolean a(Context context, String str, String str2) {
        List list = null;
        boolean z = false;
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(str);
        intent.setPackage(str2);
        if (packageManager != null) {
            list = packageManager.queryIntentActivities(intent, 0);
        }
        if (list == null) {
            e.b("BaseUtil", "action " + str + " in HwID is no exist");
            return false;
        }
        if (!list.isEmpty()) {
            z = true;
        }
        return z;
    }

    public static boolean c(Context context, String str) {
        List list = null;
        boolean z = false;
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(str);
        intent.setPackage("com.huawei.hwid");
        if (packageManager != null) {
            list = packageManager.queryIntentServices(intent, 0);
        }
        if (list == null) {
            e.b("BaseUtil", "action " + str + "in HwID is no exist");
            return false;
        }
        if (!list.isEmpty()) {
            z = true;
        }
        return z;
    }

    public static byte[] c(String str) {
        if (TextUtils.isEmpty(str)) {
            e.d("BaseUtil", "getUTF8Bytes, str is empty");
            return new byte[0];
        }
        try {
            return str.getBytes(XmlUtils.INPUT_ENCODING);
        } catch (Throwable e) {
            e.d("BaseUtil", "getBytes error", e);
            return new byte[0];
        }
    }

    public static boolean i(Context context) {
        List list = null;
        if (context == null) {
            return true;
        }
        com.huawei.hwid.a a = com.huawei.hwid.a.a();
        if (a.b() != null) {
            list = a.b();
        } else {
            try {
                list = j(context);
                a.a(list);
            } catch (NullPointerException e) {
                e.d("BaseUtil", "NullPointerException");
            } catch (Exception e2) {
                e.d("BaseUtil", "Exception");
            }
        }
        return list == null || !list.contains(context.getPackageName());
    }

    public static List<String> j(Context context) {
        String str = "package";
        List<String> arrayList = new ArrayList();
        XmlResourceParser xml = context.getResources().getXml(j.b(context, "usesdk_packagename"));
        if (xml == null) {
            return arrayList;
        }
        try {
            for (int eventType = xml.getEventType(); 1 != eventType; eventType = xml.next()) {
                String name = xml.getName();
                if (eventType == 2 && str.equals(name)) {
                    arrayList.add(xml.nextText());
                }
            }
        } catch (Throwable e) {
            e.d("BaseUtil", "Parser xml exception: XmlPullParserException:" + e.getMessage(), e);
        } catch (Throwable e2) {
            e.d("BaseUtil", "Parser xml exception: IOException:" + e2.getMessage(), e2);
        }
        return arrayList;
    }

    public static boolean k(Context context) {
        boolean z = false;
        try {
            if (context.getPackageManager().getApplicationInfo("com.huawei.hwid", 128) != null) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static String l(Context context) {
        try {
            String str = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            e.b("BaseUtil", "versionName " + str);
            return str;
        } catch (Throwable e) {
            e.d("BaseUtil", "getVersionTag error", e);
            return "";
        }
    }

    public static void a(Context context, Intent intent, int i) {
        if (context == null || intent == null) {
            e.d("BaseUtil", "context or intent is null.");
            return;
        }
        if (!(context instanceof Activity)) {
            intent.setFlags((268435456 | i) | 67108864);
            e.e("BaseUtil", "not send Activity");
        } else if (i != 0) {
            intent.setFlags(i);
        }
        try {
            e.e("BaseUtil", "startActivity-->context = " + context.getClass().getName() + ", intent = " + f.a(intent));
            context.startActivity(intent);
        } catch (Exception e) {
            e.d("BaseUtil", "can not start activity:" + e.getMessage());
        }
    }

    public static boolean d(String str) {
        e.b("BaseUtil", "accountType" + str);
        if (TextUtils.isEmpty(str) || str.equals("0") || str.equals("1") || str.equals(GpsMeasureMode.MODE_2_DIMENSIONAL)) {
            return false;
        }
        return true;
    }

    public static HwAccount d(Context context, String str) {
        if (context == null) {
            e.b("BaseUtil", "context is null");
            return null;
        } else if (TextUtils.isEmpty(str)) {
            e.e("BaseUtil", "get account by userID failed, the userID is null!");
            return null;
        } else {
            List<HwAccount> a = com.huawei.hwid.a.a.a(context).a(context, m(context));
            if (!(a == null || a.isEmpty())) {
                for (HwAccount hwAccount : a) {
                    if (hwAccount != null && str.equalsIgnoreCase(hwAccount.d())) {
                        e.e("BaseUtil", "get account by userID success!");
                        return hwAccount;
                    }
                }
            }
            e.e("BaseUtil", "get account by userID failed, there is no matching account!");
            return null;
        }
    }

    public static String m(Context context) {
        Object b = f.b(context, "tokenType");
        if (!TextUtils.isEmpty(b)) {
            return b;
        }
        String d = d(context);
        f.a(context, "tokenType", d);
        return d;
    }

    public static String n(Context context) {
        String str = null;
        if (context == null) {
            return "";
        }
        HwAccount hwAccount;
        e.a("BaseUtil", "TokenType" + f.a(m(context)));
        List a = com.huawei.hwid.a.a.a(context).a(context, m(context));
        if (a != null && a.size() > 0) {
            hwAccount = (HwAccount) a.get(0);
        } else {
            hwAccount = null;
        }
        if (hwAccount != null) {
            str = hwAccount.b();
        }
        return str;
    }

    public static void a(Context context, String str, String str2, CloudRequestHandler cloudRequestHandler) {
        e.b("BaseUtil", "do getUserInfoReq in BaseUtil");
        if (context == null || cloudRequestHandler == null) {
            e.b("BaseUtil", "context or requestHandler is null");
        } else if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            r1 = new ErrorStatus(1002, "userId  or queryRangeFlag is null");
            e.e("BaseUtil", "error: " + r1.toString());
            cloudRequestHandler.onError(r1);
        } else {
            HwAccount d = d(context, str);
            if (d != null) {
                if (a(context, "getUserInfo", 30000)) {
                    String b = d.b();
                    com.huawei.hwid.core.b.a.a aVar = new com.huawei.hwid.core.b.a.a.a(context, str, str2, null);
                    aVar.a(context, aVar, b, cloudRequestHandler);
                } else {
                    r1 = new ErrorStatus(25, "Too many recent requests have been made and last request hasn't callback");
                    e.b("BaseUtil", "error: " + r1.toString());
                    cloudRequestHandler.onError(r1);
                }
                return;
            }
            r1 = new ErrorStatus(13, "no account by userId");
            e.b("BaseUtil", "error: " + r1.toString());
            cloudRequestHandler.onError(r1);
        }
    }

    public static boolean a(Context context, String str, int i) {
        if ("com.huawei.hwid".equals(context.getPackageName())) {
            return true;
        }
        long a = com.huawei.hwid.a.a().a(str);
        long currentTimeMillis = System.currentTimeMillis();
        boolean b = com.huawei.hwid.a.a().b(str);
        int c = com.huawei.hwid.a.a().c(str);
        if (!(0 == a || 0 == currentTimeMillis)) {
            if ((((long) i) >= currentTimeMillis - a ? 1 : null) != null) {
                if (b) {
                    a(str, currentTimeMillis, c);
                    e.b("BaseUtil", "request has call back");
                    return true;
                } else if (c >= 5) {
                    e.b("BaseUtil", "return error, key: " + str + " curTime: " + currentTimeMillis + " lastTime: " + a + " requestNum: " + c);
                    return false;
                } else {
                    a(str, currentTimeMillis, c);
                    e.b("BaseUtil", "request number is " + c);
                    return true;
                }
            }
        }
        a(str, currentTimeMillis, c);
        e.b("BaseUtil", "last time or current time is zero, interval time is enough");
        return true;
    }

    private static void a(String str, long j, int i) {
        e.b("BaseUtil", "setStartRequestFlag key: " + str + " curTime: " + j + " requestNum: " + i);
        if (0 != j) {
            com.huawei.hwid.a.a().a(str, j);
        }
        com.huawei.hwid.a.a().a(str, false);
        com.huawei.hwid.a.a().a(str, i + 1);
    }

    public static void e(String str) {
        e.b("BaseUtil", str + " set true");
        int c = com.huawei.hwid.a.a().c(str);
        if (c > 0) {
            com.huawei.hwid.a.a().a(str, c - 1);
        }
        com.huawei.hwid.a.a().a(str, true);
    }

    public static boolean o(Context context) {
        Object b = b();
        int i;
        try {
            Object a = g.a("android.os.SystemProperties", "getInt", new Class[]{String.class, Integer.TYPE}, new Object[]{"ro.build.hw_emui_api_level", Integer.valueOf(0)});
            if (a == null) {
                i = 0;
            } else {
                i = ((Integer) a).intValue();
            }
        } catch (Exception e) {
            e.c("BaseUtil", e.getMessage());
            i = 0;
        }
        if (!TextUtils.isEmpty(b)) {
            b = b.toLowerCase(Locale.ENGLISH);
        }
        if (!TextUtils.isEmpty(b) && b.contains("3.0") && r0 == 0) {
            return true;
        }
        return false;
    }

    public static String b() {
        String str = "";
        try {
            Object a = g.a("android.os.SystemProperties", "get", new Class[]{String.class, String.class}, new Object[]{"ro.build.version.emui", ""});
            if (a == null) {
                return str;
            }
            return (String) a;
        } catch (Exception e) {
            e.c("BaseUtil", e.getMessage());
            return str;
        }
    }

    public static String f(String str) {
        return new SimpleDateFormat(str, Locale.getDefault()).format(new Date(System.currentTimeMillis()));
    }

    public static boolean g(String str) {
        try {
            Class.forName(str);
            return true;
        } catch (ClassNotFoundException e) {
            e.d("isExsit", "The class is not existing: " + str);
            return false;
        }
    }

    public static String c() {
        return "B778D57C1D7C80E09C1FFDD68A2BCF74";
    }

    public static int d() {
        int i;
        try {
            Object a = g.a("android.os.UserHandle", "myUserId", null, null);
            if (a == null) {
                i = -1;
            } else {
                i = ((Integer) a).intValue();
            }
        } catch (Exception e) {
            e.c("BaseUtil", e.getMessage());
            i = -1;
        }
        e.b("BaseUtil", "getAndroidSystemUserId =" + i);
        return i;
    }

    public static boolean e() {
        return d() == 0;
    }

    public static boolean f() {
        return VERSION.SDK_INT > 22;
    }

    public static boolean g() {
        if (!g("com.huawei.android.os.BuildEx") || BuildEx.VERSION.EMUI_SDK_INT < 9) {
            return false;
        }
        e.b("BaseUtil", "BuildEx.VERSION.EMUI_SDK_INT = " + BuildEx.VERSION.EMUI_SDK_INT);
        return true;
    }

    public static boolean h() {
        if (!g("com.huawei.android.os.BuildEx") || BuildEx.VERSION.EMUI_SDK_INT != 11) {
            return false;
        }
        e.b("BaseUtil", "BuildEx.VERSION.EMUI_SDK_INT = " + BuildEx.VERSION.EMUI_SDK_INT);
        return true;
    }

    public static void a(Activity activity, boolean z) {
        if (VERSION.SDK_INT > 18) {
            Window window = activity.getWindow();
            LayoutParams attributes = window.getAttributes();
            if (z) {
                attributes.flags |= 67108864;
            } else {
                attributes.flags &= -67108865;
            }
            window.setAttributes(attributes);
        }
    }

    public static boolean a(Activity activity, Boolean bool) {
        try {
            Window window = activity.getWindow();
            e.b("BaseUtil", "setHwFloating");
            g.a(window.getClass(), window, "setHwFloating", new Class[]{Boolean.TYPE}, new Object[]{bool});
            return true;
        } catch (Exception e) {
            e.d("BaseUtil", e.getMessage());
            return false;
        }
    }

    public static Builder e(Context context, String str) {
        View inflate;
        e.b("BaseUtil", "createAppsDialog");
        LayoutInflater from = LayoutInflater.from(context);
        if (o(context)) {
            inflate = from.inflate(j.d(context, "cs_permission_list_3"), null);
        } else {
            inflate = from.inflate(j.d(context, "cs_permission_list"), null);
        }
        if (inflate != null) {
            TextView textView = (TextView) inflate.findViewById(j.e(context, "list_permission"));
            TextView textView2 = (TextView) inflate.findViewById(j.e(context, "text_exiting_apps"));
            if (textView2 != null) {
                textView2.setText(j.a(context, "CS_permission_warning_tip"));
            }
            if (textView != null) {
                String[] split = str.split(";");
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < split.length; i++) {
                    if (i != split.length - 1) {
                        stringBuilder.append(String.valueOf(i + 1)).append(". ").append(split[i]).append("\n");
                    } else {
                        stringBuilder.append(String.valueOf(i + 1)).append(". ").append(split[i]);
                    }
                }
                textView.setText(stringBuilder.toString());
            }
        }
        return new Builder(context, m.a(context)).setView(inflate);
    }

    public static void a(Activity activity) {
        activity.getWindow().setFlags(67108864, 67108864);
        a(activity, Boolean.valueOf(true));
    }

    public static String p(Context context) {
        String str = "";
        if ("com.huawei.hwid".equals(context.getPackageName())) {
            return "HwID_" + l(context);
        }
        return "HwID_SDK_" + "2.4.0.300";
    }
}
