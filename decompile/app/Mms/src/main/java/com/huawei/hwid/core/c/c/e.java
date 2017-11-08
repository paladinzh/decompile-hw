package com.huawei.hwid.core.c.c;

import com.huawei.hwid.core.c.b.a;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* compiled from: MultiCardMTKImpl */
public final class e implements a {
    private static e a;

    public static synchronized e b() {
        e eVar;
        synchronized (e.class) {
            if (a == null) {
                a = new e();
            }
            eVar = a;
        }
        return eVar;
    }

    private e() {
    }

    public int a() {
        return c();
    }

    private static int c() {
        try {
            Class cls = Class.forName("android.telephony.TelephonyManager");
            Method declaredMethod = cls.getDeclaredMethod("getDefaultSim", new Class[]{null});
            Object invoke = cls.getDeclaredMethod("getDefault", new Class[]{null}).invoke(null, new Object[]{null});
            declaredMethod.setAccessible(true);
            return ((Integer) declaredMethod.invoke(invoke, new Object[]{null})).intValue();
        } catch (Throwable e) {
            a.a("mutiCardMTKImpl", "" + e.toString(), e);
            return -1;
        } catch (Throwable e2) {
            a.a("mutiCardMTKImpl", "" + e2.toString(), e2);
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
            a.d("mutiCardMTKImpl", "getDeviceId exception:" + e.toString());
            str = str2;
        } catch (NullPointerException e2) {
            a.d("mutiCardMTKImpl", "getDeviceId exception:" + e2.toString());
            str = str2;
        } catch (IllegalArgumentException e3) {
            a.d("mutiCardMTKImpl", "getDeviceId exception:" + e3.toString());
            str = str2;
        } catch (InvocationTargetException e4) {
            a.d("mutiCardMTKImpl", "getDeviceId exception:" + e4.toString());
            str = str2;
        } catch (Exception e5) {
            a.d("mutiCardMTKImpl", "getDeviceId exception:" + e5.toString());
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
            a.d("mutiCardMTKImpl", "getSubscriberId exception:" + e.toString(), e);
            str = str2;
        } catch (Throwable e2) {
            a.d("mutiCardMTKImpl", "getSubscriberId exception:" + e2.toString(), e2);
            str = str2;
        } catch (Throwable e22) {
            a.d("mutiCardMTKImpl", "getSubscriberId exception:" + e22.toString(), e22);
            str = str2;
        } catch (Throwable e222) {
            a.d("mutiCardMTKImpl", "getSubscriberId exception:" + e222.toString(), e222);
            str = str2;
        } catch (Throwable e2222) {
            a.d("mutiCardMTKImpl", "getSubscriberId exception:" + e2222.toString(), e2222);
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
            a.d("mutiCardMTKImpl", " getSimState wrong " + e.toString(), e);
            return 0;
        } catch (Throwable e2) {
            a.d("mutiCardMTKImpl", " getSimState wrong " + e2.toString(), e2);
            return 0;
        } catch (Throwable e22) {
            a.d("mutiCardMTKImpl", " getSimState wrong " + e22.toString(), e22);
            return 0;
        } catch (Throwable e222) {
            a.d("mutiCardMTKImpl", " getSimState wrong " + e222.toString(), e222);
            return 0;
        } catch (Throwable e2222) {
            a.d("mutiCardMTKImpl", " getSimState wrong " + e2222.toString(), e2222);
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
            a.a("mutiCardMTKImpl", " getDefaultTelephonyManagerEx wrong " + e.toString(), e);
            return null;
        }
    }

    public String e(int i) {
        String str;
        String str2 = "";
        Class[] clsArr = new Class[]{Integer.TYPE};
        Object[] objArr = new Object[]{Integer.valueOf(i)};
        try {
            Object d = d();
            if (d == null) {
                str = str2;
            } else {
                str = (String) d.getClass().getMethod("getLine1Number", clsArr).invoke(d, objArr);
            }
        } catch (Throwable e) {
            a.d("mutiCardMTKImpl", "getLine1Number exception:" + e.toString(), e);
            str = str2;
        } catch (Throwable e2) {
            a.d("mutiCardMTKImpl", "getLine1Number exception:" + e2.toString(), e2);
            str = str2;
        } catch (Throwable e22) {
            a.d("mutiCardMTKImpl", "getLine1Number exception:" + e22.toString(), e22);
            str = str2;
        } catch (Throwable e222) {
            a.d("mutiCardMTKImpl", "getLine1Number exception:" + e222.toString(), e222);
            str = str2;
        } catch (Throwable e2222) {
            a.d("mutiCardMTKImpl", "getLine1Number exception:" + e2222.toString(), e2222);
            str = str2;
        }
        if (str != null) {
            return str;
        }
        return "";
    }
}
