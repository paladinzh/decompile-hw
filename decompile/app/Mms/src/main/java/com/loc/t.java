package com.loc;

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

/* compiled from: ProxyUtil */
public class t {
    private static String a() {
        String str = null;
        try {
            str = Proxy.getDefaultHost();
        } catch (Throwable th) {
            aa.a(th, "ProxyUtil", "getDefHost");
        }
        return str != null ? str : "null";
    }

    public static java.net.Proxy a(Context context) {
        try {
            return VERSION.SDK_INT < 11 ? b(context) : a(context, new URI("http://restapi.amap.com"));
        } catch (Throwable e) {
            aa.a(e, "ProxyUtil", "getProxy");
            return null;
        } catch (Throwable e2) {
            aa.a(e2, "ProxyUtil", "getProxy");
            return null;
        }
    }

    private static java.net.Proxy a(Context context, URI uri) {
        if (q.m(context) == 0) {
            try {
                java.net.Proxy proxy;
                List select = ProxySelector.getDefault().select(uri);
                if (!(select == null || select.isEmpty())) {
                    proxy = (java.net.Proxy) select.get(0);
                    if (!(proxy == null || proxy.type() == Type.DIRECT)) {
                        return proxy;
                    }
                }
                proxy = null;
                return proxy;
            } catch (Throwable th) {
                aa.a(th, "ProxyUtil", "getProxySelectorCfg");
            }
        }
        return null;
    }

    private static boolean a(String str, int i) {
        return (str == null || str.length() <= 0 || i == -1) ? false : true;
    }

    private static int b() {
        int i = -1;
        try {
            i = Proxy.getDefaultPort();
        } catch (Throwable th) {
            aa.a(th, "ProxyUtil", "getDefPort");
        }
        return i;
    }

    private static java.net.Proxy b(Context context) {
        Cursor query;
        String a;
        int b;
        Cursor cursor;
        int i;
        Throwable th;
        String toLowerCase;
        int i2;
        Object obj;
        Cursor cursor2;
        int i3 = 80;
        int i4 = -1;
        Object obj2 = null;
        if (q.m(context) == 0) {
            String string;
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
                                    if (TextUtils.isEmpty(a) || a.equals("null")) {
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
                                                aa.a(th, "ProxyUtil", "getHostProxy");
                                                string = q.o(context);
                                                if (string != null) {
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
                                                        }
                                                        if (cursor != null) {
                                                            cursor.close();
                                                        }
                                                        if (a(a, i3)) {
                                                            return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                                        }
                                                        return null;
                                                    } else if (toLowerCase.indexOf("wap") != -1) {
                                                        if (TextUtils.isEmpty(string) || string.equals("null")) {
                                                            obj = null;
                                                            string = a;
                                                        } else {
                                                            i = 1;
                                                        }
                                                        if (obj == null) {
                                                            string = "10.0.0.200";
                                                        }
                                                        a = string;
                                                        if (cursor != null) {
                                                            try {
                                                                cursor.close();
                                                            } catch (Throwable th3) {
                                                                aa.a(th3, "ProxyUtil", "getHostProxy2");
                                                                th3.printStackTrace();
                                                            }
                                                        }
                                                        if (a(a, i3)) {
                                                            return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                                        }
                                                        return null;
                                                    }
                                                }
                                                i3 = i;
                                                if (cursor != null) {
                                                    cursor.close();
                                                }
                                                if (a(a, i3)) {
                                                    return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                                }
                                                return null;
                                            } catch (Throwable th4) {
                                                th3 = th4;
                                                query = cursor;
                                                if (query != null) {
                                                    try {
                                                        query.close();
                                                    } catch (Throwable e2) {
                                                        aa.a(e2, "ProxyUtil", "getHostProxy2");
                                                        e2.printStackTrace();
                                                    }
                                                }
                                                throw th3;
                                            }
                                        } catch (Throwable e22) {
                                            i4 = b;
                                            th3 = e22;
                                            try {
                                                aa.a(th3, "ProxyUtil", "getHostProxy1");
                                                th3.printStackTrace();
                                                if (query != null) {
                                                    try {
                                                        query.close();
                                                    } catch (Throwable th32) {
                                                        aa.a(th32, "ProxyUtil", "getHostProxy2");
                                                        th32.printStackTrace();
                                                    }
                                                }
                                                i3 = i4;
                                                if (a(a, i3)) {
                                                    return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
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
                                            aa.a(th322, "ProxyUtil", "getHostProxy2");
                                            th322.printStackTrace();
                                        }
                                    }
                                } catch (Throwable e222) {
                                    a = null;
                                    cursor2 = query;
                                    i = b;
                                    th322 = e222;
                                    cursor = cursor2;
                                    aa.a(th322, "ProxyUtil", "getHostProxy");
                                    string = q.o(context);
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
                                            }
                                            if (cursor != null) {
                                                cursor.close();
                                            }
                                            if (a(a, i3)) {
                                                return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                            }
                                            return null;
                                        } else if (toLowerCase.indexOf("wap") != -1) {
                                            if (TextUtils.isEmpty(string)) {
                                                i = 1;
                                                if (obj == null) {
                                                    string = "10.0.0.200";
                                                }
                                                a = string;
                                                if (cursor != null) {
                                                    cursor.close();
                                                }
                                                if (a(a, i3)) {
                                                    return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                                }
                                                return null;
                                            }
                                            obj = null;
                                            string = a;
                                            if (obj == null) {
                                                string = "10.0.0.200";
                                            }
                                            a = string;
                                            if (cursor != null) {
                                                cursor.close();
                                            }
                                            if (a(a, i3)) {
                                                return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                            }
                                            return null;
                                        }
                                    }
                                    i3 = i;
                                    if (cursor != null) {
                                        cursor.close();
                                    }
                                    if (a(a, i3)) {
                                        return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                    }
                                    return null;
                                } catch (Throwable e2222) {
                                    i4 = b;
                                    a = null;
                                    th322 = e2222;
                                    aa.a(th322, "ProxyUtil", "getHostProxy1");
                                    th322.printStackTrace();
                                    if (query != null) {
                                        query.close();
                                    }
                                    i3 = i4;
                                    if (a(a, i3)) {
                                        return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                    }
                                    return null;
                                }
                                if (a(a, i3)) {
                                    return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                }
                            } else if (string != null && string.contains("wap")) {
                                a = a();
                                i5 = b();
                                try {
                                    if (!TextUtils.isEmpty(a)) {
                                        if (!a.equals("null")) {
                                            b = 1;
                                            if (r0 == null) {
                                                try {
                                                    a = "10.0.0.172";
                                                } catch (SecurityException e3) {
                                                    th322 = e3;
                                                    cursor2 = query;
                                                    i = i5;
                                                    cursor = cursor2;
                                                    aa.a(th322, "ProxyUtil", "getHostProxy");
                                                    string = q.o(context);
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
                                                            }
                                                            if (cursor != null) {
                                                                cursor.close();
                                                            }
                                                            if (a(a, i3)) {
                                                                return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                                            }
                                                            return null;
                                                        } else if (toLowerCase.indexOf("wap") != -1) {
                                                            if (TextUtils.isEmpty(string)) {
                                                                i = 1;
                                                                if (obj == null) {
                                                                    string = "10.0.0.200";
                                                                }
                                                                a = string;
                                                                if (cursor != null) {
                                                                    cursor.close();
                                                                }
                                                                if (a(a, i3)) {
                                                                    return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                                                }
                                                                return null;
                                                            }
                                                            obj = null;
                                                            string = a;
                                                            if (obj == null) {
                                                                string = "10.0.0.200";
                                                            }
                                                            a = string;
                                                            if (cursor != null) {
                                                                cursor.close();
                                                            }
                                                            if (a(a, i3)) {
                                                                return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                                            }
                                                            return null;
                                                        }
                                                    }
                                                    i3 = i;
                                                    if (cursor != null) {
                                                        cursor.close();
                                                    }
                                                    if (a(a, i3)) {
                                                        return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                                    }
                                                    return null;
                                                } catch (Throwable th6) {
                                                    th322 = th6;
                                                    i4 = i5;
                                                    aa.a(th322, "ProxyUtil", "getHostProxy1");
                                                    th322.printStackTrace();
                                                    if (query != null) {
                                                        query.close();
                                                    }
                                                    i3 = i4;
                                                    if (a(a, i3)) {
                                                        return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
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
                                            if (a(a, i3)) {
                                                return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                            }
                                        }
                                    }
                                    Object obj4 = null;
                                    a = null;
                                    if (obj4 == null) {
                                        a = "10.0.0.172";
                                    }
                                    if (i5 != -1) {
                                        i3 = i5;
                                    }
                                    if (query != null) {
                                        query.close();
                                    }
                                } catch (SecurityException e4) {
                                    th322 = e4;
                                    a = null;
                                    int i6 = i5;
                                    cursor = query;
                                    i = i6;
                                    aa.a(th322, "ProxyUtil", "getHostProxy");
                                    string = q.o(context);
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
                                            }
                                            if (cursor != null) {
                                                cursor.close();
                                            }
                                            if (a(a, i3)) {
                                                return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                            }
                                            return null;
                                        } else if (toLowerCase.indexOf("wap") != -1) {
                                            if (TextUtils.isEmpty(string)) {
                                                i = 1;
                                                if (obj == null) {
                                                    string = "10.0.0.200";
                                                }
                                                a = string;
                                                if (cursor != null) {
                                                    cursor.close();
                                                }
                                                if (a(a, i3)) {
                                                    return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                                }
                                                return null;
                                            }
                                            obj = null;
                                            string = a;
                                            if (obj == null) {
                                                string = "10.0.0.200";
                                            }
                                            a = string;
                                            if (cursor != null) {
                                                cursor.close();
                                            }
                                            if (a(a, i3)) {
                                                return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                            }
                                            return null;
                                        }
                                    }
                                    i3 = i;
                                    if (cursor != null) {
                                        cursor.close();
                                    }
                                    if (a(a, i3)) {
                                        return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                    }
                                    return null;
                                } catch (Throwable th7) {
                                    th322 = th7;
                                    i4 = i5;
                                    a = null;
                                    aa.a(th322, "ProxyUtil", "getHostProxy1");
                                    th322.printStackTrace();
                                    if (query != null) {
                                        query.close();
                                    }
                                    i3 = i4;
                                    if (a(a, i3)) {
                                        return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                    }
                                    return null;
                                }
                                if (a(a, i3)) {
                                    return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                }
                            }
                        }
                    } catch (SecurityException e5) {
                        th322 = e5;
                        cursor = query;
                        a = null;
                        i = -1;
                        aa.a(th322, "ProxyUtil", "getHostProxy");
                        string = q.o(context);
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
                                }
                                if (cursor != null) {
                                    cursor.close();
                                }
                                if (a(a, i3)) {
                                    return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                }
                                return null;
                            } else if (toLowerCase.indexOf("wap") != -1) {
                                if (TextUtils.isEmpty(string)) {
                                    i = 1;
                                    if (obj == null) {
                                        string = "10.0.0.200";
                                    }
                                    a = string;
                                    if (cursor != null) {
                                        cursor.close();
                                    }
                                    if (a(a, i3)) {
                                        return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                    }
                                    return null;
                                }
                                obj = null;
                                string = a;
                                if (obj == null) {
                                    string = "10.0.0.200";
                                }
                                a = string;
                                if (cursor != null) {
                                    cursor.close();
                                }
                                if (a(a, i3)) {
                                    return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                                }
                                return null;
                            }
                        }
                        i3 = i;
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (a(a, i3)) {
                            return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                        }
                        return null;
                    } catch (Throwable th8) {
                        th322 = th8;
                        a = null;
                        aa.a(th322, "ProxyUtil", "getHostProxy1");
                        th322.printStackTrace();
                        if (query != null) {
                            query.close();
                        }
                        i3 = i4;
                        if (a(a, i3)) {
                            return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                        }
                        return null;
                    }
                }
                i3 = -1;
                a = null;
                if (query != null) {
                    query.close();
                }
            } catch (SecurityException e6) {
                th322 = e6;
                cursor = null;
                i = -1;
                a = null;
                aa.a(th322, "ProxyUtil", "getHostProxy");
                string = q.o(context);
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
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (a(a, i3)) {
                            return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                        }
                        return null;
                    } else if (toLowerCase.indexOf("wap") != -1) {
                        if (TextUtils.isEmpty(string)) {
                            i = 1;
                            if (obj == null) {
                                string = "10.0.0.200";
                            }
                            a = string;
                            if (cursor != null) {
                                cursor.close();
                            }
                            if (a(a, i3)) {
                                return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                            }
                            return null;
                        }
                        obj = null;
                        string = a;
                        if (obj == null) {
                            string = "10.0.0.200";
                        }
                        a = string;
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (a(a, i3)) {
                            return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                        }
                        return null;
                    }
                }
                i3 = i;
                if (cursor != null) {
                    cursor.close();
                }
                if (a(a, i3)) {
                    return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                }
                return null;
            } catch (Throwable th9) {
                th322 = th9;
                query = null;
                if (query != null) {
                    query.close();
                }
                throw th322;
            }
            try {
                if (a(a, i3)) {
                    return new java.net.Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i3));
                }
            } catch (Throwable th3222) {
                aa.a(th3222, "ProxyUtil", "getHostProxy2");
                th3222.printStackTrace();
            }
        }
        return null;
    }
}
