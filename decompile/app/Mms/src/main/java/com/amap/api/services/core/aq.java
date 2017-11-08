package com.amap.api.services.core;

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
public class aq {
    public static Proxy a(Context context) {
        try {
            if (VERSION.SDK_INT < 11) {
                return b(context);
            }
            return a(context, new URI("http://restapi.amap.com"));
        } catch (Throwable e) {
            ay.a(e, "ProxyUtil", "getProxy");
            return null;
        } catch (Throwable e2) {
            ay.a(e2, "ProxyUtil", "getProxy");
            return null;
        }
    }

    private static Proxy b(Context context) {
        String string;
        String a;
        Cursor cursor;
        int i;
        Throwable th;
        String toLowerCase;
        int i2;
        Object obj;
        Object obj2;
        int i3 = 80;
        int i4 = -1;
        Object obj3 = null;
        if (an.m(context) == 0) {
            int b;
            Cursor query;
            try {
                String str;
                query = context.getContentResolver().query(Uri.parse("content://telephony/carriers/preferapn"), null, null, null, null);
                if (query != null) {
                    try {
                        if (query.moveToFirst()) {
                            string = query.getString(query.getColumnIndex("apn"));
                            if (string != null) {
                                string = string.toLowerCase(Locale.US);
                            }
                            Object obj4;
                            int i5;
                            if (string != null && string.contains("ctwap")) {
                                a = a();
                                b = b();
                                try {
                                    if (TextUtils.isEmpty(a)) {
                                        obj4 = null;
                                        a = null;
                                    } else if (a.equals("null")) {
                                        obj4 = null;
                                        a = null;
                                    } else {
                                        i5 = 1;
                                    }
                                    if (obj4 != null) {
                                        str = a;
                                    } else {
                                        try {
                                            str = "10.0.0.200";
                                        } catch (Throwable e) {
                                            Throwable th2 = e;
                                            cursor = query;
                                            i = b;
                                            th = th2;
                                            try {
                                                ay.a(th, "ProxyUtil", "getHostProxy");
                                                string = an.o(context);
                                                if (string != null) {
                                                    i3 = i;
                                                } else {
                                                    toLowerCase = string.toLowerCase(Locale.US);
                                                    string = a();
                                                    i = b();
                                                    if (toLowerCase.indexOf("ctwap") != -1) {
                                                        i2 = 1;
                                                        a = string;
                                                        if (obj3 == null) {
                                                            a = "10.0.0.200";
                                                        }
                                                        if (i != -1) {
                                                            i3 = i;
                                                        }
                                                    } else if (toLowerCase.indexOf("wap") != -1) {
                                                        i3 = i;
                                                    } else {
                                                        if (!TextUtils.isEmpty(string)) {
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
                                                    }
                                                }
                                                if (cursor != null) {
                                                    try {
                                                        cursor.close();
                                                    } catch (Throwable th3) {
                                                        ay.a(th3, "ProxyUtil", "getHostProxy2");
                                                        th3.printStackTrace();
                                                    }
                                                }
                                                b = i3;
                                                if (a(a, b)) {
                                                    return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, b));
                                                }
                                                return null;
                                            } catch (Throwable th4) {
                                                th3 = th4;
                                                query = cursor;
                                                if (query != null) {
                                                    try {
                                                        query.close();
                                                    } catch (Throwable e2) {
                                                        ay.a(e2, "ProxyUtil", "getHostProxy2");
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
                                                        ay.a(th32, "ProxyUtil", "getHostProxy2");
                                                        th32.printStackTrace();
                                                    }
                                                }
                                                b = i4;
                                                if (a(a, b)) {
                                                    return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, b));
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
                                    if (query != null) {
                                        try {
                                            query.close();
                                        } catch (Throwable th6) {
                                            ay.a(th6, "ProxyUtil", "getHostProxy2");
                                            th6.printStackTrace();
                                        }
                                    }
                                    a = str;
                                } catch (Throwable e222) {
                                    obj2 = null;
                                    Cursor cursor2 = query;
                                    i = b;
                                    th32 = e222;
                                    cursor = cursor2;
                                    ay.a(th32, "ProxyUtil", "getHostProxy");
                                    string = an.o(context);
                                    if (string != null) {
                                        toLowerCase = string.toLowerCase(Locale.US);
                                        string = a();
                                        i = b();
                                        if (toLowerCase.indexOf("ctwap") != -1) {
                                            i2 = 1;
                                            a = string;
                                            if (obj3 == null) {
                                                a = "10.0.0.200";
                                            }
                                            if (i != -1) {
                                                i3 = i;
                                            }
                                        } else if (toLowerCase.indexOf("wap") != -1) {
                                            if (!TextUtils.isEmpty(string)) {
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
                                    b = i3;
                                    if (a(a, b)) {
                                        return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, b));
                                    }
                                    return null;
                                } catch (Throwable e2222) {
                                    i4 = b;
                                    a = null;
                                    th32 = e2222;
                                    ay.a(th32, "ProxyUtil", "getHostProxy1");
                                    th32.printStackTrace();
                                    if (query != null) {
                                        query.close();
                                    }
                                    b = i4;
                                    if (a(a, b)) {
                                        return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, b));
                                    }
                                    return null;
                                }
                                if (a(a, b)) {
                                    return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, b));
                                }
                            }
                            if (string != null) {
                                if (string.contains("wap")) {
                                    a = a();
                                    b = b();
                                    if (TextUtils.isEmpty(a)) {
                                        obj4 = null;
                                        a = null;
                                    } else if (a.equals("null")) {
                                        obj4 = null;
                                        a = null;
                                    } else {
                                        i5 = 1;
                                    }
                                    if (obj4 != null) {
                                        str = a;
                                    } else {
                                        str = "10.0.0.172";
                                    }
                                    if (b == -1) {
                                        b = 80;
                                    }
                                    if (query != null) {
                                        query.close();
                                    }
                                    a = str;
                                    if (a(a, b)) {
                                        return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, b));
                                    }
                                }
                            }
                            b = -1;
                            str = null;
                            if (query != null) {
                                query.close();
                            }
                            a = str;
                            if (a(a, b)) {
                                return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, b));
                            }
                        }
                    } catch (SecurityException e3) {
                        th32 = e3;
                        cursor = query;
                        obj2 = null;
                        i = -1;
                        ay.a(th32, "ProxyUtil", "getHostProxy");
                        string = an.o(context);
                        if (string != null) {
                            i3 = i;
                        } else {
                            toLowerCase = string.toLowerCase(Locale.US);
                            string = a();
                            i = b();
                            if (toLowerCase.indexOf("ctwap") != -1) {
                                if (!(TextUtils.isEmpty(string) || string.equals("null"))) {
                                    i2 = 1;
                                    a = string;
                                }
                                if (obj3 == null) {
                                    a = "10.0.0.200";
                                }
                                if (i != -1) {
                                    i3 = i;
                                }
                            } else if (toLowerCase.indexOf("wap") != -1) {
                                i3 = i;
                            } else {
                                if (!TextUtils.isEmpty(string)) {
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
                        b = i3;
                        if (a(a, b)) {
                            return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, b));
                        }
                        return null;
                    } catch (Throwable th7) {
                        th32 = th7;
                        a = null;
                        ay.a(th32, "ProxyUtil", "getHostProxy1");
                        th32.printStackTrace();
                        if (query != null) {
                            query.close();
                        }
                        b = i4;
                        if (a(a, b)) {
                            return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, b));
                        }
                        return null;
                    }
                }
                b = -1;
                str = null;
                if (query != null) {
                    query.close();
                }
                a = str;
            } catch (SecurityException e4) {
                th32 = e4;
                cursor = null;
                i = -1;
                a = null;
                ay.a(th32, "ProxyUtil", "getHostProxy");
                string = an.o(context);
                if (string != null) {
                    toLowerCase = string.toLowerCase(Locale.US);
                    string = a();
                    i = b();
                    if (toLowerCase.indexOf("ctwap") != -1) {
                        i2 = 1;
                        a = string;
                        if (obj3 == null) {
                            a = "10.0.0.200";
                        }
                        if (i != -1) {
                            i3 = i;
                        }
                    } else if (toLowerCase.indexOf("wap") != -1) {
                        if (!TextUtils.isEmpty(string)) {
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
                b = i3;
                if (a(a, b)) {
                    return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, b));
                }
                return null;
            } catch (Throwable th8) {
                th32 = th8;
                query = null;
                if (query != null) {
                    query.close();
                }
                throw th32;
            }
            try {
                if (a(a, b)) {
                    return new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(a, b));
                }
            } catch (Throwable th322) {
                ay.a(th322, "ProxyUtil", "getHostProxy2");
                th322.printStackTrace();
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
            ay.a(th, "ProxyUtil", "getDefHost");
        }
        if (str != null) {
            return str;
        }
        return "null";
    }

    private static Proxy a(Context context, URI uri) {
        if (an.m(context) == 0) {
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
                ay.a(th, "ProxyUtil", "getProxySelectorCfg");
            }
        }
        return null;
    }

    private static int b() {
        int i = -1;
        try {
            i = android.net.Proxy.getDefaultPort();
        } catch (Throwable th) {
            ay.a(th, "ProxyUtil", "getDefPort");
        }
        return i;
    }
}
