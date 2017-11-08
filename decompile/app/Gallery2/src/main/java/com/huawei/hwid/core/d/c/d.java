package com.huawei.hwid.core.d.c;

import com.huawei.hwid.core.d.b.e;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class d implements a {
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

    private d() {
    }

    public int a() {
        return c();
    }

    private static int c() {
        try {
            Class cls = Class.forName("android.telephony.TelephonyManager");
            Method declaredMethod = cls.getDeclaredMethod("getDefaultSim", (Class[]) null);
            Object invoke = cls.getDeclaredMethod("getDefault", (Class[]) null).invoke(null, (Object[]) null);
            declaredMethod.setAccessible(true);
            return ((Integer) declaredMethod.invoke(invoke, (Object[]) null)).intValue();
        } catch (Throwable e) {
            e.a("mutiCardMTKImpl", "" + e.getMessage(), e);
            return -1;
        } catch (Throwable e2) {
            e.a("mutiCardMTKImpl", "" + e2.toString(), e2);
            return -1;
        }
    }

    public String a(int i) {
        String str;
        String str2 = "";
        Class[] clsArr = new Class[]{Integer.TYPE};
        Object[] objArr = new Object[]{Integer.valueOf(i)};
        try {
            Object d = d();
            if (d == null) {
                str = str2;
            } else {
                str = (String) d.getClass().getMethod("getDeviceId", clsArr).invoke(d, objArr);
            }
        } catch (NoSuchMethodException e) {
            e.d("mutiCardMTKImpl", "getDeviceId exception:" + e.getMessage());
            str = str2;
        } catch (NullPointerException e2) {
            e.d("mutiCardMTKImpl", "getDeviceId exception:" + e2.getMessage());
            str = str2;
        } catch (IllegalArgumentException e3) {
            e.d("mutiCardMTKImpl", "getDeviceId exception:" + e3.getMessage());
            str = str2;
        } catch (InvocationTargetException e4) {
            e.d("mutiCardMTKImpl", "getDeviceId exception:" + e4.getMessage());
            str = str2;
        } catch (Exception e5) {
            e.d("mutiCardMTKImpl", "getDeviceId exception:" + e5.getMessage());
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
            Object d = d();
            if (d == null) {
                str = str2;
            } else {
                str = (String) d.getClass().getMethod("getSubscriberId", clsArr).invoke(d, objArr);
            }
        } catch (Throwable e) {
            e.d("mutiCardMTKImpl", "getSubscriberId exception:" + e.getMessage(), e);
            str = str2;
        } catch (Throwable e2) {
            e.d("mutiCardMTKImpl", "getSubscriberId exception:" + e2.getMessage(), e2);
            str = str2;
        } catch (Throwable e22) {
            e.d("mutiCardMTKImpl", "getSubscriberId exception:" + e22.getMessage(), e22);
            str = str2;
        } catch (Throwable e222) {
            e.d("mutiCardMTKImpl", "getSubscriberId exception:" + e222.getMessage(), e222);
            str = str2;
        } catch (Throwable e2222) {
            e.d("mutiCardMTKImpl", "getSubscriberId exception:" + e2222.getMessage(), e2222);
            str = str2;
        }
        if (str != null) {
            return str;
        }
        return "";
    }

    public int c(int i) {
        Class[] clsArr = new Class[]{Integer.TYPE};
        Object[] objArr = new Object[]{Integer.valueOf(i)};
        try {
            int i2;
            Object d = d();
            if (d == null) {
                i2 = 0;
            } else {
                i2 = ((Integer) d.getClass().getDeclaredMethod("getSimState", clsArr).invoke(d, objArr)).intValue();
            }
            return i2;
        } catch (Throwable e) {
            e.d("mutiCardMTKImpl", " getSimState wrong " + e.getMessage(), e);
            return 0;
        } catch (Throwable e2) {
            e.d("mutiCardMTKImpl", " getSimState wrong " + e2.getMessage(), e2);
            return 0;
        } catch (Throwable e22) {
            e.d("mutiCardMTKImpl", " getSimState wrong " + e22.getMessage(), e22);
            return 0;
        } catch (Throwable e222) {
            e.d("mutiCardMTKImpl", " getSimState wrong " + e222.getMessage(), e222);
            return 0;
        } catch (Throwable e2222) {
            e.d("mutiCardMTKImpl", " getSimState wrong " + e2222.getMessage(), e2222);
            return 0;
        }
    }

    public String d(int i) {
        return "";
    }

    private static Object d() {
        try {
            Class cls = Class.forName("com.mediatek.telephony.TelephonyManagerEx");
            return cls.getDeclaredMethod("getDefault", new Class[0]).invoke(cls, new Object[0]);
        } catch (Throwable e) {
            e.a("mutiCardMTKImpl", " getDefaultTelephonyManagerEx wrong " + e.getMessage(), e);
            return null;
        }
    }
}
