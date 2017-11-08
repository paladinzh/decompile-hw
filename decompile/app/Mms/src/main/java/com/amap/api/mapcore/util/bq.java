package com.amap.api.mapcore.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Environment;
import android.provider.Settings.System;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import java.io.File;
import java.util.List;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/* compiled from: DeviceInfo */
public class bq {
    private static String a = "";
    private static boolean b = false;
    private static String c = "";
    private static String d = "";
    private static String e = "";
    private static String f = "";

    /* compiled from: DeviceInfo */
    static class a extends DefaultHandler {
        a() {
        }

        public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
            if (str2.equals("string") && "UTDID".equals(attributes.getValue("name"))) {
                bq.b = true;
            }
        }

        public void characters(char[] cArr, int i, int i2) throws SAXException {
            if (bq.b) {
                bq.a = new String(cArr, i, i2);
            }
        }

        public void endElement(String str, String str2, String str3) throws SAXException {
            bq.b = false;
        }
    }

    static String a(Context context) {
        try {
            return u(context);
        } catch (Throwable th) {
            th.printStackTrace();
            return "";
        }
    }

    static String b(Context context) {
        String str = "";
        try {
            return x(context);
        } catch (Throwable th) {
            th.printStackTrace();
            return str;
        }
    }

    static int c(Context context) {
        try {
            return y(context);
        } catch (Throwable th) {
            th.printStackTrace();
            return -1;
        }
    }

    static int d(Context context) {
        try {
            return v(context);
        } catch (Throwable th) {
            th.printStackTrace();
            return -1;
        }
    }

    static String e(Context context) {
        try {
            return t(context);
        } catch (Throwable th) {
            th.printStackTrace();
            return "";
        }
    }

    public static void a() {
        try {
            if (VERSION.SDK_INT > 14) {
                TrafficStats.class.getDeclaredMethod("setThreadStatsTag", new Class[]{Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(40964)});
            }
        } catch (Throwable e) {
            cb.a(e, "DeviceInfo", "setTraficTag");
        } catch (Throwable e2) {
            cb.a(e2, "DeviceInfo", "setTraficTag");
        } catch (Throwable e22) {
            cb.a(e22, "DeviceInfo", "setTraficTag");
        } catch (Throwable e222) {
            cb.a(e222, "DeviceInfo", "setTraficTag");
        } catch (Throwable e2222) {
            cb.a(e2222, "DeviceInfo", "setTraficTag");
        }
    }

    public static String f(Context context) {
        try {
            if (a != null) {
                if (!"".equals(a)) {
                    return a;
                }
            }
            if (a(context, "android.permission.WRITE_SETTINGS")) {
                a = System.getString(context.getContentResolver(), "mqBRboGZkQPcAkyk");
            }
            if (a != null) {
                if (!"".equals(a)) {
                    return a;
                }
            }
        } catch (Throwable th) {
            cb.a(th, "DeviceInfo", "getUTDID");
        }
        try {
            if ("mounted".equals(Environment.getExternalStorageState())) {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.UTSystemConfig/Global/Alvin2.xml");
                if (file.exists()) {
                    SAXParserFactory.newInstance().newSAXParser().parse(file, new a());
                }
            }
        } catch (Throwable th2) {
            cb.a(th2, "DeviceInfo", "getUTDID");
        } catch (Throwable th22) {
            cb.a(th22, "DeviceInfo", "getUTDID");
        } catch (Throwable th222) {
            cb.a(th222, "DeviceInfo", "getUTDID");
        } catch (Throwable th2222) {
            cb.a(th2222, "DeviceInfo", "getUTDID");
        } catch (Throwable th22222) {
            cb.a(th22222, "DeviceInfo", "getUTDID");
        }
        return a;
    }

    private static boolean a(Context context, String str) {
        return context.checkCallingOrSelfPermission(str) == 0;
    }

    static String g(Context context) {
        if (context != null) {
            String bssid;
            try {
                if (a(context, "android.permission.ACCESS_WIFI_STATE")) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
                    if (wifiManager.isWifiEnabled()) {
                        bssid = wifiManager.getConnectionInfo().getBSSID();
                    } else {
                        bssid = null;
                    }
                    return bssid;
                }
            } catch (Throwable th) {
                cb.a(th, "DeviceInfo", "getWifiMacs");
                bssid = null;
            }
        }
        return null;
    }

    static String h(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        if (context != null && a(context, "android.permission.ACCESS_WIFI_STATE")) {
            WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
            if (wifiManager.isWifiEnabled()) {
                List scanResults = wifiManager.getScanResults();
                if (scanResults == null || scanResults.size() == 0) {
                    return stringBuilder.toString();
                }
                List a = a(scanResults);
                Object obj = 1;
                int i = 0;
                while (i < a.size() && i < 7) {
                    ScanResult scanResult = (ScanResult) a.get(i);
                    if (obj == null) {
                        stringBuilder.append(";");
                    } else {
                        obj = null;
                    }
                    stringBuilder.append(scanResult.BSSID);
                    i++;
                }
            }
        } else {
            try {
                return stringBuilder.toString();
            } catch (Throwable th) {
                cb.a(th, "DeviceInfo", "getWifiMacs");
            }
        }
        return stringBuilder.toString();
    }

    public static String i(Context context) {
        try {
            if (c != null) {
                if (!"".equals(c)) {
                    return c;
                }
            }
            if (!a(context, "android.permission.ACCESS_WIFI_STATE")) {
                return c;
            }
            c = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo().getMacAddress();
            return c;
        } catch (Throwable th) {
            cb.a(th, "DeviceInfo", "getDeviceMac");
        }
    }

    static String[] j(Context context) {
        try {
            if (a(context, "android.permission.READ_PHONE_STATE") && a(context, "android.permission.ACCESS_COARSE_LOCATION")) {
                CellLocation cellLocation = ((TelephonyManager) context.getSystemService("phone")).getCellLocation();
                int cid;
                int lac;
                if (cellLocation instanceof GsmCellLocation) {
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
                    cid = gsmCellLocation.getCid();
                    lac = gsmCellLocation.getLac();
                    return new String[]{lac + "||" + cid, "gsm"};
                }
                if (cellLocation instanceof CdmaCellLocation) {
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;
                    cid = cdmaCellLocation.getSystemId();
                    int networkId = cdmaCellLocation.getNetworkId();
                    lac = cdmaCellLocation.getBaseStationId();
                    if (cid >= 0 && networkId >= 0 && lac >= 0) {
                    }
                    return new String[]{cid + "||" + networkId + "||" + lac, "cdma"};
                }
                return new String[]{"", ""};
            }
            return new String[]{"", ""};
        } catch (Throwable th) {
            cb.a(th, "DeviceInfo", "cellInfo");
        }
    }

    static String k(Context context) {
        String str = "";
        String networkOperator;
        try {
            if (!a(context, "android.permission.READ_PHONE_STATE")) {
                return str;
            }
            networkOperator = z(context).getNetworkOperator();
            if (TextUtils.isEmpty(networkOperator) && networkOperator.length() < 3) {
                return str;
            }
            networkOperator = networkOperator.substring(3);
            return networkOperator;
        } catch (Throwable th) {
            th.printStackTrace();
            cb.a(th, "DeviceInfo", "getMNC");
            networkOperator = str;
        }
    }

    public static int l(Context context) {
        try {
            return y(context);
        } catch (Throwable th) {
            cb.a(th, "DeviceInfo", "getNetWorkType");
            return -1;
        }
    }

    public static int m(Context context) {
        try {
            return v(context);
        } catch (Throwable th) {
            cb.a(th, "DeviceInfo", "getActiveNetWorkType");
            return -1;
        }
    }

    public static NetworkInfo n(Context context) {
        if (!a(context, "android.permission.ACCESS_NETWORK_STATE")) {
            return null;
        }
        ConnectivityManager w = w(context);
        if (w != null) {
            return w.getActiveNetworkInfo();
        }
        return null;
    }

    static String o(Context context) {
        String extraInfo;
        try {
            NetworkInfo n = n(context);
            if (n == null) {
                return null;
            }
            extraInfo = n.getExtraInfo();
            return extraInfo;
        } catch (Throwable th) {
            cb.a(th, "DeviceInfo", "getNetworkExtraInfo");
            extraInfo = null;
        }
    }

    static String p(Context context) {
        try {
            if (d != null) {
                if (!"".equals(d)) {
                    return d;
                }
            }
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
            int i = displayMetrics.widthPixels;
            int i2 = displayMetrics.heightPixels;
            d = i2 <= i ? i2 + "*" + i : i + "*" + i2;
        } catch (Throwable th) {
            cb.a(th, "DeviceInfo", "getReslution");
        }
        return d;
    }

    public static String q(Context context) {
        try {
            if (e != null) {
                if (!"".equals(e)) {
                    return e;
                }
            }
            if (!a(context, "android.permission.READ_PHONE_STATE")) {
                return e;
            }
            e = z(context).getDeviceId();
            if (e == null) {
                e = "";
            }
            return e;
        } catch (Throwable th) {
            cb.a(th, "DeviceInfo", "getDeviceID");
        }
    }

    public static String r(Context context) {
        String str = "";
        try {
            return t(context);
        } catch (Throwable th) {
            cb.a(th, "DeviceInfo", "getSubscriberId");
            return str;
        }
    }

    static String s(Context context) {
        try {
            return u(context);
        } catch (Throwable th) {
            cb.a(th, "DeviceInfo", "getNetworkOperatorName");
            return "";
        }
    }

    private static String t(Context context) {
        if (f != null && !"".equals(f)) {
            return f;
        }
        if (!a(context, "android.permission.READ_PHONE_STATE")) {
            return f;
        }
        f = z(context).getSubscriberId();
        if (f == null) {
            f = "";
        }
        return f;
    }

    private static String u(Context context) {
        if (!a(context, "android.permission.READ_PHONE_STATE")) {
            return null;
        }
        String simOperatorName = z(context).getSimOperatorName();
        if (TextUtils.isEmpty(simOperatorName)) {
            simOperatorName = z(context).getNetworkOperatorName();
        }
        return simOperatorName;
    }

    private static int v(Context context) {
        if (context == null || !a(context, "android.permission.ACCESS_NETWORK_STATE")) {
            return -1;
        }
        ConnectivityManager w = w(context);
        if (w == null) {
            return -1;
        }
        NetworkInfo activeNetworkInfo = w.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            return activeNetworkInfo.getType();
        }
        return -1;
    }

    private static ConnectivityManager w(Context context) {
        return (ConnectivityManager) context.getSystemService("connectivity");
    }

    private static String x(Context context) {
        String str = "";
        String r = r(context);
        if (r != null && r.length() >= 5) {
            return r.substring(3, 5);
        }
        return str;
    }

    private static int y(Context context) {
        if (a(context, "android.permission.READ_PHONE_STATE")) {
            return z(context).getNetworkType();
        }
        return -1;
    }

    private static TelephonyManager z(Context context) {
        return (TelephonyManager) context.getSystemService("phone");
    }

    private static List<ScanResult> a(List<ScanResult> list) {
        int size = list.size();
        for (int i = 0; i < size - 1; i++) {
            for (int i2 = 1; i2 < size - i; i2++) {
                if (((ScanResult) list.get(i2 - 1)).level > ((ScanResult) list.get(i2)).level) {
                    ScanResult scanResult = (ScanResult) list.get(i2 - 1);
                    list.set(i2 - 1, list.get(i2));
                    list.set(i2, scanResult);
                }
            }
        }
        return list;
    }
}
