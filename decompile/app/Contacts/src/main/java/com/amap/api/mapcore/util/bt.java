package com.amap.api.mapcore.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.text.TextUtils;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;
import java.util.Locale;

/* compiled from: ProxyUtil */
public class bt {
    public static Proxy a(Context context) {
        try {
            if (VERSION.SDK_INT < 11) {
                return b(context);
            }
            return a(context, new URI("http://restapi.amap.com"));
        } catch (Throwable e) {
            cb.a(e, "ProxyUtil", "getProxy");
            return null;
        } catch (Throwable e2) {
            cb.a(e2, "ProxyUtil", "getProxy");
            return null;
        }
    }

    private static Proxy b(Context context) {
        Cursor query;
        String string;
        String a;
        int b;
        int i;
        Cursor cursor;
        int i2;
        Throwable th;
        String toLowerCase;
        int i3;
        Object obj;
        Cursor cursor2;
        int i4 = 80;
        int i5 = -1;
        Object obj2 = null;
        if (bq.m(context) == 0) {
            try {
                query = context.getContentResolver().query(Uri.parse("content://telephony/carriers/preferapn"), null, null, null, null);
                if (query != null) {
                    try {
                        if (query.moveToFirst()) {
                            string = query.getString(query.getColumnIndex("apn"));
                            if (string != null) {
                                string = string.toLowerCase(Locale.US);
                            }
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
                                        i = 1;
                                    }
                                    if (obj3 == null) {
                                        try {
                                            a = "10.0.0.200";
                                        } catch (Throwable e) {
                                            Throwable th2 = e;
                                            cursor = query;
                                            i2 = b;
                                            th = th2;
                                            try {
                                                cb.a(th, "ProxyUtil", "getHostProxy");
                                                string = bq.o(context);
                                                if (string != null) {
                                                    i4 = i2;
                                                } else {
                                                    toLowerCase = string.toLowerCase(Locale.US);
                                                    string = a();
                                                    i2 = b();
                                                    if (toLowerCase.indexOf("ctwap") == -1) {
                                                        if (!(TextUtils.isEmpty(string) || string.equals("null"))) {
                                                            i3 = 1;
                                                            a = string;
                                                        }
                                                        if (obj2 == null) {
                                                            a = "10.0.0.200";
                                                        }
                                                        if (i2 != -1) {
                                                            i4 = i2;
                                                        }
                                                    } else if (toLowerCase.indexOf("wap") != -1) {
                                                        i4 = i2;
                                                    } else {
                                                        if (TextUtils.isEmpty(string)) {
                                                            obj = null;
                                                            string = a;
                                                        } else if (string.equals("null")) {
                                                            i2 = 1;
                                                        } else {
                                                            obj = null;
                                                            string = a;
                                                        }
                                                        if (obj == null) {
                                                            string = "10.0.0.200";
                                                        }
                                                        a = string;
                                                    }
                                                }
                                                if (cursor != null) {
                                                    try {
                                                        cursor.close();
                                                    } catch (Throwable th3) {
                                                        cb.a(th3, "ProxyUtil", "getHostProxy2");
                                                        th3.printStackTrace();
                                                    }
                                                }
                                                if (a(a, i4)) {
                                                    return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i4));
                                                }
                                                return null;
                                            } catch (Throwable th4) {
                                                th3 = th4;
                                                query = cursor;
                                                if (query != null) {
                                                    try {
                                                        query.close();
                                                    } catch (Throwable e2) {
                                                        cb.a(e2, "ProxyUtil", "getHostProxy2");
                                                        e2.printStackTrace();
                                                    }
                                                }
                                                throw th3;
                                            }
                                        } catch (Throwable e22) {
                                            i5 = b;
                                            th3 = e22;
                                            try {
                                                cb.a(th3, "ProxyUtil", "getHostProxy1");
                                                th3.printStackTrace();
                                                if (query != null) {
                                                    i4 = i5;
                                                } else {
                                                    try {
                                                        query.close();
                                                    } catch (Throwable th32) {
                                                        cb.a(th32, "ProxyUtil", "getHostProxy2");
                                                        th32.printStackTrace();
                                                    }
                                                    i4 = i5;
                                                }
                                                if (a(a, i4)) {
                                                    return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i4));
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
                                    i4 = b;
                                    if (query != null) {
                                        try {
                                            query.close();
                                        } catch (Throwable th322) {
                                            cb.a(th322, "ProxyUtil", "getHostProxy2");
                                            th322.printStackTrace();
                                        }
                                    }
                                } catch (Throwable e222) {
                                    a = null;
                                    cursor2 = query;
                                    i2 = b;
                                    th322 = e222;
                                    cursor = cursor2;
                                    cb.a(th322, "ProxyUtil", "getHostProxy");
                                    string = bq.o(context);
                                    if (string != null) {
                                        i4 = i2;
                                    } else {
                                        toLowerCase = string.toLowerCase(Locale.US);
                                        string = a();
                                        i2 = b();
                                        if (toLowerCase.indexOf("ctwap") == -1) {
                                            i3 = 1;
                                            a = string;
                                            if (obj2 == null) {
                                                a = "10.0.0.200";
                                            }
                                            if (i2 != -1) {
                                                i4 = i2;
                                            }
                                        } else if (toLowerCase.indexOf("wap") != -1) {
                                            i4 = i2;
                                        } else {
                                            if (TextUtils.isEmpty(string)) {
                                                obj = null;
                                                string = a;
                                            } else if (string.equals("null")) {
                                                obj = null;
                                                string = a;
                                            } else {
                                                i2 = 1;
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
                                    if (a(a, i4)) {
                                        return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i4));
                                    }
                                    return null;
                                } catch (Throwable e2222) {
                                    i5 = b;
                                    a = null;
                                    th322 = e2222;
                                    cb.a(th322, "ProxyUtil", "getHostProxy1");
                                    th322.printStackTrace();
                                    if (query != null) {
                                        i4 = i5;
                                    } else {
                                        query.close();
                                        i4 = i5;
                                    }
                                    if (a(a, i4)) {
                                        return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i4));
                                    }
                                    return null;
                                }
                                if (a(a, i4)) {
                                    return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i4));
                                }
                            }
                            if (string != null) {
                                if (string.contains("wap")) {
                                    a = a();
                                    i = b();
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
                                                i2 = i;
                                                cursor = cursor2;
                                                cb.a(th322, "ProxyUtil", "getHostProxy");
                                                string = bq.o(context);
                                                if (string != null) {
                                                    i4 = i2;
                                                } else {
                                                    toLowerCase = string.toLowerCase(Locale.US);
                                                    string = a();
                                                    i2 = b();
                                                    if (toLowerCase.indexOf("ctwap") == -1) {
                                                        i3 = 1;
                                                        a = string;
                                                        if (obj2 == null) {
                                                            a = "10.0.0.200";
                                                        }
                                                        if (i2 != -1) {
                                                            i4 = i2;
                                                        }
                                                    } else if (toLowerCase.indexOf("wap") != -1) {
                                                        i4 = i2;
                                                    } else {
                                                        if (TextUtils.isEmpty(string)) {
                                                            obj = null;
                                                            string = a;
                                                        } else if (string.equals("null")) {
                                                            obj = null;
                                                            string = a;
                                                        } else {
                                                            i2 = 1;
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
                                                if (a(a, i4)) {
                                                    return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i4));
                                                }
                                                return null;
                                            } catch (Throwable th6) {
                                                th322 = th6;
                                                i5 = i;
                                                cb.a(th322, "ProxyUtil", "getHostProxy1");
                                                th322.printStackTrace();
                                                if (query != null) {
                                                    i4 = i5;
                                                } else {
                                                    query.close();
                                                    i4 = i5;
                                                }
                                                if (a(a, i4)) {
                                                    return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i4));
                                                }
                                                return null;
                                            }
                                        }
                                        if (i != -1) {
                                            i4 = i;
                                        }
                                        if (query != null) {
                                            query.close();
                                        }
                                    } catch (SecurityException e4) {
                                        th322 = e4;
                                        a = null;
                                        int i6 = i;
                                        cursor = query;
                                        i2 = i6;
                                        cb.a(th322, "ProxyUtil", "getHostProxy");
                                        string = bq.o(context);
                                        if (string != null) {
                                            toLowerCase = string.toLowerCase(Locale.US);
                                            string = a();
                                            i2 = b();
                                            if (toLowerCase.indexOf("ctwap") == -1) {
                                                i3 = 1;
                                                a = string;
                                                if (obj2 == null) {
                                                    a = "10.0.0.200";
                                                }
                                                if (i2 != -1) {
                                                    i4 = i2;
                                                }
                                            } else if (toLowerCase.indexOf("wap") != -1) {
                                                if (TextUtils.isEmpty(string)) {
                                                    obj = null;
                                                    string = a;
                                                } else if (string.equals("null")) {
                                                    i2 = 1;
                                                } else {
                                                    obj = null;
                                                    string = a;
                                                }
                                                if (obj == null) {
                                                    string = "10.0.0.200";
                                                }
                                                a = string;
                                            } else {
                                                i4 = i2;
                                            }
                                        } else {
                                            i4 = i2;
                                        }
                                        if (cursor != null) {
                                            cursor.close();
                                        }
                                        if (a(a, i4)) {
                                            return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i4));
                                        }
                                        return null;
                                    } catch (Throwable th7) {
                                        th322 = th7;
                                        i5 = i;
                                        a = null;
                                        cb.a(th322, "ProxyUtil", "getHostProxy1");
                                        th322.printStackTrace();
                                        if (query != null) {
                                            query.close();
                                            i4 = i5;
                                        } else {
                                            i4 = i5;
                                        }
                                        if (a(a, i4)) {
                                            return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i4));
                                        }
                                        return null;
                                    }
                                    if (a(a, i4)) {
                                        return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i4));
                                    }
                                }
                            }
                            i4 = -1;
                            a = null;
                            if (query != null) {
                                query.close();
                            }
                            if (a(a, i4)) {
                                return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i4));
                            }
                        }
                    } catch (SecurityException e5) {
                        th322 = e5;
                        cursor = query;
                        a = null;
                        i2 = -1;
                        cb.a(th322, "ProxyUtil", "getHostProxy");
                        string = bq.o(context);
                        if (string != null) {
                            toLowerCase = string.toLowerCase(Locale.US);
                            string = a();
                            i2 = b();
                            if (toLowerCase.indexOf("ctwap") == -1) {
                                i3 = 1;
                                a = string;
                                if (obj2 == null) {
                                    a = "10.0.0.200";
                                }
                                if (i2 != -1) {
                                    i4 = i2;
                                }
                            } else if (toLowerCase.indexOf("wap") != -1) {
                                if (TextUtils.isEmpty(string)) {
                                    obj = null;
                                    string = a;
                                } else if (string.equals("null")) {
                                    i2 = 1;
                                } else {
                                    obj = null;
                                    string = a;
                                }
                                if (obj == null) {
                                    string = "10.0.0.200";
                                }
                                a = string;
                            } else {
                                i4 = i2;
                            }
                        } else {
                            i4 = i2;
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (a(a, i4)) {
                            return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i4));
                        }
                        return null;
                    } catch (Throwable th8) {
                        th322 = th8;
                        a = null;
                        cb.a(th322, "ProxyUtil", "getHostProxy1");
                        th322.printStackTrace();
                        if (query != null) {
                            query.close();
                            i4 = i5;
                        } else {
                            i4 = i5;
                        }
                        if (a(a, i4)) {
                            return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i4));
                        }
                        return null;
                    }
                }
                i4 = -1;
                a = null;
                if (query != null) {
                    query.close();
                }
            } catch (SecurityException e6) {
                th322 = e6;
                cursor = null;
                i2 = -1;
                a = null;
                cb.a(th322, "ProxyUtil", "getHostProxy");
                string = bq.o(context);
                if (string != null) {
                    toLowerCase = string.toLowerCase(Locale.US);
                    string = a();
                    i2 = b();
                    if (toLowerCase.indexOf("ctwap") == -1) {
                        i3 = 1;
                        a = string;
                        if (obj2 == null) {
                            a = "10.0.0.200";
                        }
                        if (i2 != -1) {
                            i4 = i2;
                        }
                    } else if (toLowerCase.indexOf("wap") != -1) {
                        if (TextUtils.isEmpty(string)) {
                            obj = null;
                            string = a;
                        } else if (string.equals("null")) {
                            i2 = 1;
                        } else {
                            obj = null;
                            string = a;
                        }
                        if (obj == null) {
                            string = "10.0.0.200";
                        }
                        a = string;
                    } else {
                        i4 = i2;
                    }
                } else {
                    i4 = i2;
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (a(a, i4)) {
                    return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i4));
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
                if (a(a, i4)) {
                    return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, i4));
                }
            } catch (Throwable th3222) {
                cb.a(th3222, "ProxyUtil", "getHostProxy2");
                th3222.printStackTrace();
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
            str = android.net.Proxy.getDefaultHost();
        } catch (Throwable th) {
            cb.a(th, "ProxyUtil", "getDefHost");
        }
        if (str != null) {
            return str;
        }
        return "null";
    }

    private static Proxy a(Context context, URI uri) {
        if (bq.m(context) == 0) {
            try {
                Proxy proxy;
                List select = ProxySelector.getDefault().select(uri);
                if (select == null || select.isEmpty()) {
                    proxy = null;
                } else {
                    proxy = (Proxy) select.get(0);
                    if (proxy == null || proxy.type() == Type.DIRECT) {
                        proxy = null;
                    }
                }
                return proxy;
            } catch (Throwable th) {
                cb.a(th, "ProxyUtil", "getProxySelectorCfg");
            }
        }
        return null;
    }

    private static int b() {
        int i = -1;
        try {
            i = android.net.Proxy.getDefaultPort();
        } catch (Throwable th) {
            cb.a(th, "ProxyUtil", "getDefPort");
        }
        return i;
    }
}
