package com.amap.api.services.core;

import android.content.Context;
import android.database.Cursor;
import android.net.Proxy;
import android.net.Uri;
import android.os.Build.VERSION;
import android.text.TextUtils;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.apache.http.HttpHost;

/* compiled from: ProxyUtil */
public class ac {
    public static HttpHost a(Context context) {
        try {
            if (VERSION.SDK_INT < 11) {
                return b(context);
            }
            return a(context, new URI("http://restapi.amap.com"));
        } catch (Throwable e) {
            ay.a(e, "ProxyUtil", "getProxy");
            e.printStackTrace();
            return null;
        } catch (Throwable e2) {
            ay.a(e2, "ProxyUtil", "getProxy");
            e2.printStackTrace();
            return null;
        }
    }

    private static HttpHost b(Context context) {
        Cursor query;
        String string;
        String a;
        int b;
        Cursor cursor;
        int i;
        Throwable th;
        String toLowerCase;
        int i2;
        Object obj;
        ay b2;
        Cursor cursor2;
        int i3 = 80;
        int i4 = -1;
        Object obj2 = null;
        if (z.g(context) == 0) {
            try {
                query = context.getContentResolver().query(Uri.parse("content://telephony/carriers/preferapn"), null, null, null, null);
                if (query != null) {
                    try {
                        if (query.moveToFirst()) {
                            string = query.getString(query.getColumnIndex("apn"));
                            if (string != null) {
                                string = string.toLowerCase(Locale.US);
                            }
                            int i5;
                            if (string != null && string.contains("ctwap")) {
                                a = a();
                                b = b();
                                try {
                                    Object obj3;
                                    if (TextUtils.isEmpty(a)) {
                                        obj3 = null;
                                        a = null;
                                    } else if (a.equals("null")) {
                                        obj3 = null;
                                        a = null;
                                    } else {
                                        i5 = 1;
                                    }
                                    if (obj3 == null) {
                                        try {
                                            a = "10.0.0.200";
                                        } catch (Throwable e) {
                                            Throwable th2 = e;
                                            cursor = query;
                                            i = b;
                                            th = th2;
                                            try {
                                                ay.a(th, "ProxyUtil", "getHostProxy");
                                                string = z.i(context);
                                                if (string != null) {
                                                    toLowerCase = string.toLowerCase(Locale.US);
                                                    string = a();
                                                    i = b();
                                                    if (toLowerCase.indexOf("ctwap") == -1) {
                                                        i2 = 1;
                                                        a = string;
                                                        if (obj2 == null) {
                                                            a = "10.0.0.200";
                                                        }
                                                        if (i != -1) {
                                                            i3 = i;
                                                        }
                                                    } else if (toLowerCase.indexOf("wap") != -1) {
                                                        if (TextUtils.isEmpty(string)) {
                                                            obj = null;
                                                            string = a;
                                                        } else if (string.equals("null")) {
                                                            obj = null;
                                                            string = a;
                                                        } else {
                                                            i = 1;
                                                        }
                                                        if (obj == null) {
                                                            string = "10.0.0.200";
                                                        }
                                                        a = string;
                                                    } else {
                                                        i3 = i;
                                                    }
                                                } else {
                                                    i3 = i;
                                                }
                                                if (cursor != null) {
                                                    try {
                                                        cursor.close();
                                                    } catch (Throwable th3) {
                                                        b2 = ay.b();
                                                        if (b2 != null) {
                                                            b2.b(th3, "ProxyUtil", "getHostProxy2");
                                                        }
                                                        th3.printStackTrace();
                                                    }
                                                }
                                                if (a(a, i3)) {
                                                    return new HttpHost(a, i3, "http");
                                                }
                                                return null;
                                            } catch (Throwable th4) {
                                                th3 = th4;
                                                query = cursor;
                                                if (query != null) {
                                                    try {
                                                        query.close();
                                                    } catch (Throwable e2) {
                                                        ay b3 = ay.b();
                                                        if (b3 != null) {
                                                            b3.b(e2, "ProxyUtil", "getHostProxy2");
                                                        }
                                                        e2.printStackTrace();
                                                    }
                                                }
                                                throw th3;
                                            }
                                        } catch (Throwable e22) {
                                            i4 = b;
                                            th3 = e22;
                                            try {
                                                ay.a(th3, "ProxyUtil", "getHostProxy1");
                                                th3.printStackTrace();
                                                if (query != null) {
                                                    try {
                                                        query.close();
                                                    } catch (Throwable th32) {
                                                        b2 = ay.b();
                                                        if (b2 != null) {
                                                            b2.b(th32, "ProxyUtil", "getHostProxy2");
                                                        }
                                                        th32.printStackTrace();
                                                    }
                                                    i3 = i4;
                                                } else {
                                                    i3 = i4;
                                                }
                                                if (a(a, i3)) {
                                                    return new HttpHost(a, i3, "http");
                                                }
                                                return null;
                                            } catch (Throwable th5) {
                                                th32 = th5;
                                                if (query != null) {
                                                    query.close();
                                                }
                                                throw th32;
                                            }
                                        }
                                    }
                                    if (b == -1) {
                                        b = 80;
                                    }
                                    i3 = b;
                                    if (query != null) {
                                        try {
                                            query.close();
                                        } catch (Throwable th322) {
                                            b2 = ay.b();
                                            if (b2 != null) {
                                                b2.b(th322, "ProxyUtil", "getHostProxy2");
                                            }
                                            th322.printStackTrace();
                                        }
                                    }
                                } catch (Throwable e222) {
                                    a = null;
                                    cursor2 = query;
                                    i = b;
                                    th322 = e222;
                                    cursor = cursor2;
                                    ay.a(th322, "ProxyUtil", "getHostProxy");
                                    string = z.i(context);
                                    if (string != null) {
                                        i3 = i;
                                    } else {
                                        toLowerCase = string.toLowerCase(Locale.US);
                                        string = a();
                                        i = b();
                                        if (toLowerCase.indexOf("ctwap") == -1) {
                                            i2 = 1;
                                            a = string;
                                            if (obj2 == null) {
                                                a = "10.0.0.200";
                                            }
                                            if (i != -1) {
                                                i3 = i;
                                            }
                                        } else if (toLowerCase.indexOf("wap") != -1) {
                                            i3 = i;
                                        } else {
                                            if (TextUtils.isEmpty(string)) {
                                                obj = null;
                                                string = a;
                                            } else if (string.equals("null")) {
                                                obj = null;
                                                string = a;
                                            } else {
                                                i = 1;
                                            }
                                            if (obj == null) {
                                                string = "10.0.0.200";
                                            }
                                            a = string;
                                        }
                                    }
                                    if (cursor != null) {
                                        cursor.close();
                                    }
                                    if (a(a, i3)) {
                                        return new HttpHost(a, i3, "http");
                                    }
                                    return null;
                                } catch (Throwable e2222) {
                                    i4 = b;
                                    a = null;
                                    th322 = e2222;
                                    ay.a(th322, "ProxyUtil", "getHostProxy1");
                                    th322.printStackTrace();
                                    if (query != null) {
                                        i3 = i4;
                                    } else {
                                        query.close();
                                        i3 = i4;
                                    }
                                    if (a(a, i3)) {
                                        return new HttpHost(a, i3, "http");
                                    }
                                    return null;
                                }
                                if (a(a, i3)) {
                                    return new HttpHost(a, i3, "http");
                                }
                            }
                            if (string != null) {
                                if (string.contains("wap")) {
                                    a = a();
                                    i5 = b();
                                    try {
                                        Object obj4;
                                        if (TextUtils.isEmpty(a)) {
                                            obj4 = null;
                                            a = null;
                                        } else if (a.equals("null")) {
                                            obj4 = null;
                                            a = null;
                                        } else {
                                            b = 1;
                                        }
                                        if (obj4 == null) {
                                            try {
                                                a = "10.0.0.172";
                                            } catch (SecurityException e3) {
                                                th322 = e3;
                                                cursor2 = query;
                                                i = i5;
                                                cursor = cursor2;
                                                ay.a(th322, "ProxyUtil", "getHostProxy");
                                                string = z.i(context);
                                                if (string != null) {
                                                    toLowerCase = string.toLowerCase(Locale.US);
                                                    string = a();
                                                    i = b();
                                                    if (toLowerCase.indexOf("ctwap") == -1) {
                                                        i2 = 1;
                                                        a = string;
                                                        if (obj2 == null) {
                                                            a = "10.0.0.200";
                                                        }
                                                        if (i != -1) {
                                                            i3 = i;
                                                        }
                                                    } else if (toLowerCase.indexOf("wap") != -1) {
                                                        if (TextUtils.isEmpty(string)) {
                                                            obj = null;
                                                            string = a;
                                                        } else if (string.equals("null")) {
                                                            i = 1;
                                                        } else {
                                                            obj = null;
                                                            string = a;
                                                        }
                                                        if (obj == null) {
                                                            string = "10.0.0.200";
                                                        }
                                                        a = string;
                                                    } else {
                                                        i3 = i;
                                                    }
                                                } else {
                                                    i3 = i;
                                                }
                                                if (cursor != null) {
                                                    cursor.close();
                                                }
                                                if (a(a, i3)) {
                                                    return new HttpHost(a, i3, "http");
                                                }
                                                return null;
                                            } catch (Exception e4) {
                                                th322 = e4;
                                                i4 = i5;
                                                ay.a(th322, "ProxyUtil", "getHostProxy1");
                                                th322.printStackTrace();
                                                if (query != null) {
                                                    query.close();
                                                    i3 = i4;
                                                } else {
                                                    i3 = i4;
                                                }
                                                if (a(a, i3)) {
                                                    return new HttpHost(a, i3, "http");
                                                }
                                                return null;
                                            }
                                        }
                                        if (i5 != -1) {
                                            i3 = i5;
                                        }
                                        if (query != null) {
                                            query.close();
                                        }
                                    } catch (SecurityException e5) {
                                        th322 = e5;
                                        a = null;
                                        int i6 = i5;
                                        cursor = query;
                                        i = i6;
                                        ay.a(th322, "ProxyUtil", "getHostProxy");
                                        string = z.i(context);
                                        if (string != null) {
                                            i3 = i;
                                        } else {
                                            toLowerCase = string.toLowerCase(Locale.US);
                                            string = a();
                                            i = b();
                                            if (toLowerCase.indexOf("ctwap") == -1) {
                                                i2 = 1;
                                                a = string;
                                                if (obj2 == null) {
                                                    a = "10.0.0.200";
                                                }
                                                if (i != -1) {
                                                    i3 = i;
                                                }
                                            } else if (toLowerCase.indexOf("wap") != -1) {
                                                i3 = i;
                                            } else {
                                                if (TextUtils.isEmpty(string)) {
                                                    obj = null;
                                                    string = a;
                                                } else if (string.equals("null")) {
                                                    obj = null;
                                                    string = a;
                                                } else {
                                                    i = 1;
                                                }
                                                if (obj == null) {
                                                    string = "10.0.0.200";
                                                }
                                                a = string;
                                            }
                                        }
                                        if (cursor != null) {
                                            cursor.close();
                                        }
                                        if (a(a, i3)) {
                                            return new HttpHost(a, i3, "http");
                                        }
                                        return null;
                                    } catch (Exception e6) {
                                        th322 = e6;
                                        i4 = i5;
                                        a = null;
                                        ay.a(th322, "ProxyUtil", "getHostProxy1");
                                        th322.printStackTrace();
                                        if (query != null) {
                                            i3 = i4;
                                        } else {
                                            query.close();
                                            i3 = i4;
                                        }
                                        if (a(a, i3)) {
                                            return new HttpHost(a, i3, "http");
                                        }
                                        return null;
                                    }
                                    if (a(a, i3)) {
                                        return new HttpHost(a, i3, "http");
                                    }
                                }
                            }
                            i3 = -1;
                            a = null;
                            if (query != null) {
                                query.close();
                            }
                            if (a(a, i3)) {
                                return new HttpHost(a, i3, "http");
                            }
                        }
                    } catch (SecurityException e7) {
                        th322 = e7;
                        cursor = query;
                        a = null;
                        i = -1;
                        ay.a(th322, "ProxyUtil", "getHostProxy");
                        string = z.i(context);
                        if (string != null) {
                            toLowerCase = string.toLowerCase(Locale.US);
                            string = a();
                            i = b();
                            if (toLowerCase.indexOf("ctwap") == -1) {
                                i2 = 1;
                                a = string;
                                if (obj2 == null) {
                                    a = "10.0.0.200";
                                }
                                if (i != -1) {
                                    i3 = i;
                                }
                            } else if (toLowerCase.indexOf("wap") != -1) {
                                if (TextUtils.isEmpty(string)) {
                                    obj = null;
                                    string = a;
                                } else if (string.equals("null")) {
                                    i = 1;
                                } else {
                                    obj = null;
                                    string = a;
                                }
                                if (obj == null) {
                                    string = "10.0.0.200";
                                }
                                a = string;
                            } else {
                                i3 = i;
                            }
                        } else {
                            i3 = i;
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (a(a, i3)) {
                            return new HttpHost(a, i3, "http");
                        }
                        return null;
                    } catch (Exception e8) {
                        th322 = e8;
                        a = null;
                        ay.a(th322, "ProxyUtil", "getHostProxy1");
                        th322.printStackTrace();
                        if (query != null) {
                            query.close();
                            i3 = i4;
                        } else {
                            i3 = i4;
                        }
                        if (a(a, i3)) {
                            return new HttpHost(a, i3, "http");
                        }
                        return null;
                    }
                }
                i3 = -1;
                a = null;
                if (query != null) {
                    query.close();
                }
            } catch (SecurityException e9) {
                th322 = e9;
                cursor = null;
                i = -1;
                a = null;
                ay.a(th322, "ProxyUtil", "getHostProxy");
                string = z.i(context);
                if (string != null) {
                    i3 = i;
                } else {
                    toLowerCase = string.toLowerCase(Locale.US);
                    string = a();
                    i = b();
                    if (toLowerCase.indexOf("ctwap") == -1) {
                        if (!(TextUtils.isEmpty(string) || string.equals("null"))) {
                            i2 = 1;
                            a = string;
                        }
                        if (obj2 == null) {
                            a = "10.0.0.200";
                        }
                        if (i != -1) {
                            i3 = i;
                        }
                    } else if (toLowerCase.indexOf("wap") != -1) {
                        i3 = i;
                    } else {
                        if (TextUtils.isEmpty(string)) {
                            obj = null;
                            string = a;
                        } else if (string.equals("null")) {
                            obj = null;
                            string = a;
                        } else {
                            i = 1;
                        }
                        if (obj == null) {
                            string = "10.0.0.200";
                        }
                        a = string;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (a(a, i3)) {
                    return new HttpHost(a, i3, "http");
                }
                return null;
            } catch (Exception e10) {
                th322 = e10;
                query = null;
                a = null;
                ay.a(th322, "ProxyUtil", "getHostProxy1");
                th322.printStackTrace();
                if (query != null) {
                    i3 = i4;
                } else {
                    query.close();
                    i3 = i4;
                }
                if (a(a, i3)) {
                    return new HttpHost(a, i3, "http");
                }
                return null;
            } catch (Throwable th6) {
                th322 = th6;
                query = null;
                if (query != null) {
                    query.close();
                }
                throw th322;
            }
            if (a(a, i3)) {
                return new HttpHost(a, i3, "http");
            }
        }
        return null;
    }

    private static boolean a(String str, int i) {
        return (str == null || str.length() <= 0 || i == -1) ? false : true;
    }

    private static String a() {
        String str = null;
        try {
            str = Proxy.getDefaultHost();
        } catch (Throwable th) {
            th.printStackTrace();
            ay.a(th, "ProxyUtil", "getDefHost");
        }
        if (str != null) {
            return str;
        }
        return "null";
    }

    private static HttpHost a(Context context, URI uri) {
        if (z.g(context) == 0) {
            try {
                java.net.Proxy proxy;
                int i;
                String str;
                List select = ProxySelector.getDefault().select(uri);
                if (select == null || select.isEmpty()) {
                    proxy = null;
                } else {
                    proxy = (java.net.Proxy) select.get(0);
                    if (proxy == null || proxy.type() == Type.DIRECT) {
                        proxy = null;
                    }
                }
                if (proxy == null) {
                    i = -1;
                    str = null;
                } else {
                    InetSocketAddress inetSocketAddress = (InetSocketAddress) proxy.address();
                    if (inetSocketAddress == null) {
                        i = -1;
                        str = null;
                    } else {
                        str = inetSocketAddress.getHostName();
                        i = inetSocketAddress.getPort();
                    }
                }
                if (a(str, i)) {
                    return new HttpHost(str, i, "http");
                }
            } catch (Throwable e) {
                ay.a(e, "ProxyUtil", "getProxySelectorCfg");
                e.printStackTrace();
            }
        }
        return null;
    }

    private static int b() {
        int i = -1;
        try {
            i = Proxy.getDefaultPort();
        } catch (Throwable th) {
            ay.a(th, "ProxyUtil", "getDefPort");
            th.printStackTrace();
        }
        return i;
    }
}
