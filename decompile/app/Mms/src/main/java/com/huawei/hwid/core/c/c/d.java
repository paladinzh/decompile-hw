package com.huawei.hwid.core.c.c;

import com.huawei.hwid.core.c.b.a;
import java.lang.reflect.InvocationTargetException;

/* compiled from: MultiCardHwImpl */
public class d implements a {
    private static d a;

    public static synchronized d b() {
        d dVar;
        synchronized (d.class) {
            if (a == null) {
                a = new d();
            }
            dVar = a;
        }
        return dVar;
    }

    public int a() {
        try {
            Object c = c();
            if (c == null) {
                return 0;
            }
            return ((Integer) c.getClass().getMethod("getDefaultSubscription", new Class[0]).invoke(c, new Object[0])).intValue();
        } catch (Throwable e) {
            a.d("MutiCardHwImpl", " NoSuchMethodException wrong " + e.toString(), e);
            return -1;
        } catch (Throwable e2) {
            a.d("MutiCardHwImpl", " NullPointerException wrong " + e2.toString(), e2);
            return -1;
        } catch (Throwable e22) {
            a.d("MutiCardHwImpl", " IllegalArgumentException wrong " + e22.toString(), e22);
            return -1;
        } catch (Throwable e222) {
            a.d("MutiCardHwImpl", " InvocationTargetException wrong " + e222.toString(), e222);
            return -1;
        } catch (Throwable e2222) {
            a.d("MutiCardHwImpl", " Exception wrong " + e2222.toString(), e2222);
            return -1;
        }
    }

    public String a(int i) {
        String str;
        String str2 = "";
        Class[] clsArr = new Class[]{Integer.TYPE};
        Object[] objArr = new Object[]{Integer.valueOf(i)};
        try {
            Object c = c();
            if (c == null) {
                str = str2;
            } else {
                str = (String) c.getClass().getMethod("getDeviceId", clsArr).invoke(c, objArr);
            }
        } catch (NoSuchMethodException e) {
            a.d("MutiCardHwImpl", "getDeviceId exception:" + e.toString());
            str = str2;
        } catch (NullPointerException e2) {
            a.d("MutiCardHwImpl", "getDeviceId exception:" + e2.toString());
            str = str2;
        } catch (IllegalArgumentException e3) {
            a.d("MutiCardHwImpl", "getDeviceId exception:" + e3.toString());
            str = str2;
        } catch (InvocationTargetException e4) {
            a.d("MutiCardHwImpl", "getDeviceId exception:" + e4.toString());
            str = str2;
        } catch (Exception e5) {
            a.d("MutiCardHwImpl", "getDeviceId exception:" + e5.toString());
            str = str2;
        }
        if (str != null) {
            return str;
        }
        return "";
    }

    public String b(int i) {
        String str;
        String str2 = "";
        Class[] clsArr = new Class[]{Integer.TYPE};
        Object[] objArr = new Object[]{Integer.valueOf(i)};
        try {
            Object c = c();
            if (c == null) {
                str = str2;
            } else {
                str = (String) c.getClass().getMethod("getSubscriberId", clsArr).invoke(c, objArr);
            }
        } catch (Throwable e) {
            a.d("MutiCardHwImpl", "getSubscriberId exception:" + e.toString(), e);
            str = str2;
        } catch (Throwable e2) {
            a.d("MutiCardHwImpl", "getSubscriberId exception:" + e2.toString(), e2);
            str = str2;
        } catch (Throwable e22) {
            a.d("MutiCardHwImpl", "getSubscriberId exception:" + e22.toString(), e22);
            str = str2;
        } catch (Throwable e222) {
            a.d("MutiCardHwImpl", "getSubscriberId exception:" + e222.toString(), e222);
            str = str2;
        } catch (Throwable e2222) {
            a.d("MutiCardHwImpl", "getSubscriberId exception:" + e2222.toString(), e2222);
            str = str2;
        }
        if (str != null) {
            return str;
        }
        return "";
    }

    public int c(int i) {
        int i2;
        if (i != -1) {
            i2 = 0;
        } else {
            i2 = 5;
        }
        Class[] clsArr = new Class[]{Integer.TYPE};
        Object[] objArr = new Object[]{Integer.valueOf(i)};
        try {
            Object c = c();
            if (c != null) {
                i2 = ((Integer) c.getClass().getDeclaredMethod("getSimState", clsArr).invoke(c, objArr)).intValue();
            }
        } catch (Throwable e) {
            a.d("MutiCardHwImpl", " IllegalAccessException wrong " + e.toString(), e);
        } catch (Throwable e2) {
            a.d("MutiCardHwImpl", " NoSuchMethodException wrong " + e2.toString(), e2);
        } catch (Throwable e22) {
            a.d("MutiCardHwImpl", " NullPointerException wrong " + e22.toString(), e22);
        } catch (Throwable e222) {
            a.d("MutiCardHwImpl", " IllegalArgumentException wrong " + e222.toString(), e222);
        } catch (Throwable e2222) {
            a.d("MutiCardHwImpl", " getSimState wrong " + e2222.toString(), e2222);
        }
        return i2;
    }

    public static Object c() {
        try {
            Class cls = Class.forName("android.telephony.MSimTelephonyManager");
            return cls.getDeclaredMethod("getDefault", new Class[0]).invoke(cls, new Object[0]);
        } catch (Throwable e) {
            a.a("MutiCardHwImpl", " getDefaultMSimTelephonyManager wrong " + e.toString(), e);
            return null;
        }
    }

    public String d(int i) {
        String str = "";
        Class[] clsArr = new Class[]{Integer.TYPE};
        Object[] objArr = new Object[]{Integer.valueOf(i)};
        try {
            Object c = c();
            String str2 = c == null ? str : (String) c.getClass().getMethod("getSimOperator", clsArr).invoke(c, objArr);
        } catch (Throwable e) {
            a.d("MutiCardHwImpl", "getSimOperator exception:" + e.toString(), e);
            str2 = str;
        } catch (Throwable e2) {
            a.d("MutiCardHwImpl", "getSimOperator exception:" + e2.toString(), e2);
            str2 = str;
        } catch (Throwable e22) {
            a.d("MutiCardHwImpl", "getSimOperator exception:" + e22.toString(), e22);
            str2 = str;
        } catch (Throwable e222) {
            a.d("MutiCardHwImpl", "getSimOperator exception:" + e222.toString(), e222);
            str2 = str;
        } catch (Throwable e2222) {
            a.d("MutiCardHwImpl", "getSimOperator exception:" + e2222.toString(), e2222);
            str2 = str;
        } catch (Throwable e22222) {
            a.d("MutiCardHwImpl", "getSimOperator exception:" + e22222.toString(), e22222);
            str2 = str;
        }
        if (str2 != null) {
            return str2;
        }
        return "";
    }

    public String e(int i) {
        String str;
        String str2 = "";
        Class[] clsArr = new Class[]{Integer.TYPE};
        Object[] objArr = new Object[]{Integer.valueOf(i)};
        try {
            Object c = c();
            if (c == null) {
                str = str2;
            } else {
                str = (String) c.getClass().getMethod("getLine1Number", clsArr).invoke(c, objArr);
            }
        } catch (Throwable e) {
            a.d("MutiCardHwImpl", "getLine1Number exception:" + e.toString(), e);
            str = str2;
        } catch (Throwable e2) {
            a.d("MutiCardHwImpl", "getLine1Number exception:" + e2.toString(), e2);
            str = str2;
        } catch (Throwable e22) {
            a.d("MutiCardHwImpl", "getLine1Number exception:" + e22.toString(), e22);
            str = str2;
        } catch (Throwable e222) {
            a.d("MutiCardHwImpl", "getLine1Number exception:" + e222.toString(), e222);
            str = str2;
        } catch (Throwable e2222) {
            a.d("MutiCardHwImpl", "getLine1Number exception:" + e2222.toString(), e2222);
            str = str2;
        }
        if (str != null) {
            return str;
        }
        return "";
    }
}
