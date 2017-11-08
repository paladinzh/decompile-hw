package com.huawei.hwid.core.c;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.c.c;
import com.huawei.hwid.core.constants.HwAccountConstants;
import com.huawei.hwid.core.datatype.AgreementVersion;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.datatype.UserAccountInfo;
import com.huawei.hwid.core.encrypt.g;
import com.huawei.hwid.manager.f;
import com.huawei.hwid.ui.common.j;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/* compiled from: BaseUtil */
public class d {
    public static final boolean a = (VERSION.SDK_INT >= 11);
    private static List b = new ArrayList();
    private static List c = new ArrayList();

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
            byte[] bytes = str.getBytes("UTF-8");
            for (int i = 0; i < bArr.length; i++) {
                bArr[i] = (byte) ((byte) (((byte) (Byte.decode("0x" + new String(new byte[]{(byte) bytes[i * 2]}, "UTF-8")).byteValue() << 4)) ^ Byte.decode("0x" + new String(new byte[]{(byte) bytes[(i * 2) + 1]}, "UTF-8")).byteValue()));
            }
        } catch (UnsupportedEncodingException e) {
            a.d("BaseUtil", "hexString2ByteArray UnsupportedEncodingException");
        }
        return bArr;
    }

    public static boolean a(Context context, int i) {
        int c;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (c.b()) {
            com.huawei.hwid.core.c.c.a a = c.a();
            if (i == MsgUrlService.RESULT_NOT_IMPL) {
                i = a.a();
            }
            c = a.c(i);
        } else if (telephonyManager == null) {
            c = -1;
        } else {
            c = telephonyManager.getSimState();
        }
        if (c != 5) {
            return false;
        }
        return true;
    }

    public static boolean a(Context context) {
        a.b("BaseUtil", "enter networkIsAvaiable");
        if (context == null) {
            return false;
        }
        Object systemService = context.getSystemService("connectivity");
        if (systemService != null) {
            NetworkInfo[] allNetworkInfo = ((ConnectivityManager) systemService).getAllNetworkInfo();
            if (allNetworkInfo == null || allNetworkInfo.length == 0) {
                a.b("BaseUtil", "NetworkInfo is null,so networkIsAvaiable is unaviable");
                return false;
            }
            for (int i = 0; i < allNetworkInfo.length; i++) {
                if (allNetworkInfo[i] != null) {
                    State state = allNetworkInfo[i].getState();
                    a.b("BaseUtil", "NetworkInfo  state " + i + state);
                    if (state == State.CONNECTED) {
                        a.b("BaseUtil", "NetworkInfo  state " + i + state + "is CONNECTED");
                        return true;
                    }
                }
            }
            a.b("BaseUtil", "NetworkInfo  state is unaviable");
            return false;
        }
        a.b("BaseUtil", "connectivity is null,so networkIsAvaiable is unaviable");
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
        return i("yyyyMMddHHmmssSSS");
    }

    public static String a(String str, String str2, String str3) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2) || TextUtils.isEmpty(str3)) {
            return "";
        }
        try {
            return new SimpleDateFormat(str3).format(new SimpleDateFormat(str2).parse(str));
        } catch (Throwable e) {
            a.b("BaseUtil", e.toString(), e);
            return "";
        }
    }

    public static String d(Context context) {
        return context.getPackageName();
    }

    public static String e(Context context) {
        String toLowerCase = context.getResources().getConfiguration().locale.getLanguage().toLowerCase(Locale.getDefault());
        a.b("BaseUtil", "languageStr:" + toLowerCase);
        return toLowerCase;
    }

    public static String f(Context context) {
        String toLowerCase = context.getResources().getConfiguration().locale.getCountry().toLowerCase(Locale.getDefault());
        a.b("BaseUtil", "countryStr:" + toLowerCase);
        return toLowerCase;
    }

    public static String g(Context context) {
        return e(context) + "-" + f(context).toUpperCase(Locale.getDefault());
    }

    public static String b(String str) {
        if (str == null) {
            return "0";
        }
        if (str.startsWith("+")) {
            str = str.replace("+", "00");
        }
        String str2 = "0";
        if (str.contains("@")) {
            str2 = "1";
        }
        if (p.d(str)) {
            str2 = "2";
        }
        return str2;
    }

    public static String a(String str, String str2) {
        if (!StringUtils.MPLUG86.equals(str)) {
            return str2;
        }
        if (!TextUtils.isEmpty(str2) && str2.contains("+")) {
            str2 = str2.replace("+", "00");
        }
        if (str.contains("+")) {
            str = str.replace("+", "00");
        }
        if (TextUtils.isEmpty(str2) || !str2.startsWith(str)) {
            return str2;
        }
        return str2.replaceFirst(str, "");
    }

    public static String c(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        if (str.startsWith("00")) {
            return "+" + str.substring(2);
        }
        return str;
    }

    public static String d(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        if (str.startsWith("+")) {
            return str.replace("+", "00");
        }
        return str;
    }

    public static int a(Context context, String str) {
        HwAccount c = f.a(context).c(context, str, null);
        if (c == null) {
            return 0;
        }
        return c.d();
    }

    public static String b(String str, String str2) {
        StringBuffer stringBuffer = new StringBuffer();
        if (!(TextUtils.isEmpty(str) || TextUtils.isEmpty(str2))) {
            if ("cloud".equals(str2)) {
                stringBuffer.append(str);
            } else if (str.length() >= 20) {
                String substring = str.substring(0, 20);
                stringBuffer.append(substring).append(g.a(str + ":" + str2));
            }
        }
        return stringBuffer.toString();
    }

    public static boolean a(HwAccount hwAccount) {
        if (hwAccount == null) {
            return false;
        }
        if (!p.e(hwAccount.a()) && !p.e(hwAccount.c()) && !p.e(hwAccount.b()) && !p.e(hwAccount.f())) {
            return true;
        }
        a.a("BaseUtil", "addHwAccount is invalid:" + com.huawei.hwid.core.encrypt.f.a(hwAccount.toString(), true));
        return false;
    }

    public static HwAccount a(String str, String str2, String str3, String str4, int i, String str5, String str6, String str7, String str8) {
        HwAccount hwAccount = new HwAccount();
        hwAccount.a(str);
        hwAccount.b(str2);
        hwAccount.e(str3);
        hwAccount.c(str4);
        hwAccount.a(i);
        hwAccount.g(str6);
        hwAccount.h(str7);
        hwAccount.d(str5);
        hwAccount.f(str8);
        return hwAccount;
    }

    public static boolean h(Context context) {
        return context.getPackageName().equals("com.huawei.hwid");
    }

    public static void a(Context context, boolean z) {
        a(context, "isInnerRemoveAccount", z);
    }

    public static void b(Context context, boolean z) {
        a(context, "isSendBroadcast", z);
    }

    public static void a(Context context, String str, boolean z) {
        i.a(context, str, String.valueOf(z));
    }

    public static boolean b(Context context, String str, boolean z) {
        String b = i.b(context, str);
        if (p.e(b)) {
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
            a.b("BaseUtil", "action " + str + " in HwID is no exist");
            return false;
        }
        if (!list.isEmpty()) {
            z = true;
        }
        return z;
    }

    public static byte[] e(String str) {
        if (TextUtils.isEmpty(str)) {
            a.d("BaseUtil", "getUTF8Bytes, str is empty");
            return new byte[0];
        }
        try {
            return str.getBytes("UTF-8");
        } catch (Throwable e) {
            a.d("BaseUtil", "getBytes error", e);
            return new byte[0];
        }
    }

    public static String i(Context context) {
        return context.getFilesDir().getPath() + "/privaces/";
    }

    public static boolean j(Context context) {
        List list = null;
        if (context == null) {
            return true;
        }
        com.huawei.hwid.a a = com.huawei.hwid.a.a();
        if (a.b() != null) {
            list = a.b();
        } else {
            try {
                list = k(context);
                a.a(list);
            } catch (NullPointerException e) {
                a.d("BaseUtil", "NullPointerException");
            } catch (Exception e2) {
                a.d("BaseUtil", "Exception");
            }
        }
        return list == null || !list.contains(context.getPackageName());
    }

    public static List k(Context context) {
        String str = "package";
        List arrayList = new ArrayList();
        XmlResourceParser xml = context.getResources().getXml(m.b(context, "usesdk_packagename"));
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
            a.d("BaseUtil", "Parser xml exception: XmlPullParserException:" + e.toString(), e);
        } catch (Throwable e2) {
            a.d("BaseUtil", "Parser xml exception: IOException:" + e2.toString(), e2);
        }
        return arrayList;
    }

    public static boolean l(Context context) {
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

    public static void a(Context context, Intent intent, int i) {
        if (context == null || intent == null) {
            a.d("BaseUtil", "context or intent is null.");
            return;
        }
        if (!(context instanceof Activity)) {
            intent.setFlags((268435456 | i) | 67108864);
            a.e("BaseUtil", "not send Activity");
        } else if (i != 0) {
            intent.setFlags(i);
        }
        try {
            a.e("BaseUtil", "startActivity-->context = " + context.getClass().getName() + ", intent = " + com.huawei.hwid.core.encrypt.f.a(intent));
            context.startActivity(intent);
        } catch (Exception e) {
            a.d("BaseUtil", "can not start activity:" + e.toString());
        }
    }

    public static boolean a(PackageManager packageManager, String str, String str2) {
        return packageManager.checkPermission(str, str2) == 0;
    }

    public static boolean f(String str) {
        a.b("BaseUtil", "accountType" + str);
        if (TextUtils.isEmpty(str) || str.equals("0") || str.equals("1") || str.equals("2")) {
            return false;
        }
        return true;
    }

    public static void m(Context context) {
        Intent intent = new Intent("com.huawei.vip.ACTION_VIP_STARTUP_GUIDE");
        intent.setPackage("com.huawei.hwid");
        a.b("BaseUtil", "cancelAlarm," + intent.getAction());
        ((AlarmManager) context.getSystemService("alarm")).cancel(PendingIntent.getBroadcast(context, 0, intent, 134217728));
    }

    public static void b(Context context, int i) {
        ((NotificationManager) context.getSystemService("notification")).cancel(i);
    }

    public static boolean b() {
        String language = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        if ("zh".equalsIgnoreCase(language) && "CN".equalsIgnoreCase(country)) {
            return true;
        }
        return false;
    }

    public static boolean n(Context context) {
        Account[] accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
        if (accountsByType == null || accountsByType.length == 0) {
            return false;
        }
        return true;
    }

    public static HwAccount c(Context context, String str) {
        if (context == null) {
            a.b("BaseUtil", "context is null");
            return null;
        } else if (p.e(str)) {
            a.e("BaseUtil", "get account by userID failed, the userID is null!");
            return null;
        } else {
            List<HwAccount> a = f.a(context).a(context, o(context));
            if (!(a == null || a.isEmpty())) {
                for (HwAccount hwAccount : a) {
                    if (hwAccount != null && str.equalsIgnoreCase(hwAccount.c())) {
                        a.e("BaseUtil", "get account by userID success!");
                        return hwAccount;
                    }
                }
            }
            a.e("BaseUtil", "get account by userID failed, there is no matching account!");
            return null;
        }
    }

    public static String o(Context context) {
        Object b = i.b(context, "tokenType");
        if (!TextUtils.isEmpty(b)) {
            return b;
        }
        String d = d(context);
        i.a(context, "tokenType", d);
        return d;
    }

    public static Account p(Context context) {
        Account account = null;
        if (context == null) {
            return null;
        }
        Account[] accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
        if (accountsByType != null && accountsByType.length > 0) {
            account = accountsByType[0];
        }
        return account;
    }

    public static void g(String str) {
        a.b("BaseUtil", str + " set true");
        int e = com.huawei.hwid.a.a().e(str);
        if (e > 0) {
            com.huawei.hwid.a.a().a(str, e - 1);
        }
        com.huawei.hwid.a.a().a(str, true);
    }

    public static boolean q(Context context) {
        return context != null ? false : false;
    }

    public static boolean r(Context context) {
        Object c = c();
        int i;
        try {
            Object a = j.a("android.os.SystemProperties", "getInt", new Class[]{String.class, Integer.TYPE}, new Object[]{"ro.build.hw_emui_api_level", Integer.valueOf(0)});
            if (a == null) {
                i = 0;
            } else {
                i = ((Integer) a).intValue();
            }
        } catch (Exception e) {
            a.c("BaseUtil", e.getMessage());
            i = 0;
        }
        if (!TextUtils.isEmpty(c)) {
            c = c.toLowerCase(Locale.ENGLISH);
        }
        if (!TextUtils.isEmpty(c) && c.contains("3.0") && r0 == 0) {
            return true;
        }
        return false;
    }

    public static String c() {
        String str = "";
        try {
            Object a = j.a("android.os.SystemProperties", "get", new Class[]{String.class, String.class}, new Object[]{"ro.build.version.emui", ""});
            if (a == null) {
                return str;
            }
            return (String) a;
        } catch (Exception e) {
            a.c("BaseUtil", e.getMessage());
            return str;
        }
    }

    public static String h(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        if (str.contains("+")) {
            str = str.replace("+", "00");
        }
        if (str.startsWith("0086")) {
            str = str.substring(4);
        }
        return str;
    }

    public static String i(String str) {
        return new SimpleDateFormat(str, Locale.getDefault()).format(new Date(System.currentTimeMillis()));
    }

    public static boolean j(String str) {
        String str2 = "1";
        if (TextUtils.isEmpty(str) || str.indexOf(str2) < 0) {
            return false;
        }
        String[] a = HwAccountConstants.a();
        if (str.length() < a.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (str2.equals(str.substring(i, i + 1))) {
                return true;
            }
        }
        return false;
    }

    public static List a(List list) {
        String[] a = HwAccountConstants.a();
        List arrayList = new ArrayList();
        for (String str : a) {
            for (AgreementVersion agreementVersion : list) {
                if (str.equals(agreementVersion.a())) {
                    arrayList.add(agreementVersion);
                    break;
                }
            }
        }
        return arrayList;
    }

    public static String d() {
        return "B778D57C1D7C80E09C1FFDD68A2BCF74";
    }

    public static String a(Object[] objArr) {
        if (objArr == null || objArr.length == 0) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        try {
            stringBuffer.append("{");
            for (Object obj : objArr) {
                stringBuffer.append("[").append(obj.toString()).append("]").append(" ");
            }
            stringBuffer.append("}");
        } catch (Throwable e) {
            a.d("BaseUtil", "getArraysString error:" + e.getMessage(), e);
        }
        return stringBuffer.toString();
    }

    public static ArrayList a(List list, boolean z) {
        a.b("BaseUtil", "initUserAccountInfo");
        ArrayList arrayList = new ArrayList(4);
        if (z) {
            arrayList.addAll(a(list, true, true, "2", "6"));
        }
        arrayList.addAll(a(list, true, true, "1", "5"));
        return arrayList;
    }

    public static ArrayList a(List list, boolean z, boolean z2, String... strArr) {
        a.b("BaseUtil", "getAccountByType");
        ArrayList arrayList = new ArrayList(2);
        if (list == null || list.isEmpty()) {
            return arrayList;
        }
        String str = "1";
        for (UserAccountInfo userAccountInfo : list) {
            if (userAccountInfo.getAccountState().equals("1") || !z) {
                if (a(userAccountInfo, strArr) && !a(userAccountInfo, arrayList, z2)) {
                    arrayList.add(userAccountInfo);
                }
            }
        }
        return arrayList;
    }

    private static boolean a(UserAccountInfo userAccountInfo, String... strArr) {
        boolean z = false;
        for (Object equals : strArr) {
            if (userAccountInfo.getAccountType().equals(equals)) {
                z = true;
            }
        }
        return z;
    }

    private static boolean a(UserAccountInfo userAccountInfo, ArrayList arrayList, boolean z) {
        if (!z) {
            return false;
        }
        boolean z2;
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            if (userAccountInfo.getUserAccount().equals(((UserAccountInfo) it.next()).getUserAccount())) {
                z2 = true;
                break;
            }
        }
        z2 = false;
        return z2;
    }

    public static int e() {
        int i;
        try {
            Object a = j.a("android.os.UserHandle", "myUserId", null, null);
            if (a == null) {
                i = -1;
            } else {
                i = ((Integer) a).intValue();
            }
        } catch (Exception e) {
            a.c("BaseUtil", e.getMessage());
            i = -1;
        }
        a.b("BaseUtil", "getAndroidSystemUserId =" + i);
        return i;
    }

    public static boolean f() {
        return e() == 0;
    }

    public static boolean g() {
        return VERSION.SDK_INT > 22;
    }

    public static String k(String str) {
        if (TextUtils.isEmpty(str) || !str.contains("_")) {
            return str;
        }
        try {
            return str.substring(0, str.indexOf("_"));
        } catch (Exception e) {
            a.d("BaseUtil", "revertToOldWay" + e.getMessage());
            return str;
        }
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
            a.b("BaseUtil", "setHwFloating");
            j.a(window.getClass(), window, "setHwFloating", new Class[]{Boolean.TYPE}, new Object[]{bool});
            return true;
        } catch (Exception e) {
            a.d("BaseUtil", e.toString());
            return false;
        }
    }

    public static boolean h() {
        String str = "";
        String str2 = "";
        String str3;
        try {
            Object a = j.a("android.os.SystemProperties", "get", new Class[]{String.class}, new Object[]{"ro.product.locale.language"});
            Object a2 = j.a("android.os.SystemProperties", "get", new Class[]{String.class}, new Object[]{"ro.product.locale.region"});
            if (a != null) {
                str = (String) a;
            }
            if (a2 == null) {
                str3 = str2;
            } else {
                str3 = (String) a2;
            }
        } catch (Exception e) {
            String str4 = str;
            a.c("BaseUtil", e.getMessage());
            str3 = str2;
            str = str4;
        }
        if ("zh".equalsIgnoreCase(str) && "cn".equalsIgnoreCase(r1)) {
            return true;
        }
        return false;
    }

    public static Builder d(Context context, String str) {
        View inflate;
        a.b("BaseUtil", "createAppsDialog");
        LayoutInflater from = LayoutInflater.from(context);
        if (r(context)) {
            inflate = from.inflate(m.d(context, "cs_permission_list_3"), null);
        } else {
            inflate = from.inflate(m.d(context, "cs_permission_list"), null);
        }
        if (inflate != null) {
            TextView textView = (TextView) inflate.findViewById(m.e(context, "list_permission"));
            ((TextView) inflate.findViewById(m.e(context, "text_exiting_apps"))).setText(m.a(context, "CS_permission_warning_tip"));
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
        return new Builder(context, j.b(context)).setTitle(m.a(context, "CS_title_tips")).setView(inflate);
    }
}
