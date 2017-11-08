package com.huawei.hwid.core.d.c;

import com.huawei.hwid.core.d.b.e;
import java.lang.reflect.InvocationTargetException;

public class c implements a {
    private static c a;

    public static synchronized c b() {
        c cVar;
        synchronized (c.class) {
            if (a == null) {
                a = new c();
            }
            cVar = a;
        }
        return cVar;
    }

    public int a() {
        try {
            Object c = c();
            if (c == null) {
                return 0;
            }
            return ((Integer) c.getClass().getMethod("getDefaultSubscription", new Class[0]).invoke(c, new Object[0])).intValue();
        } catch (Throwable e) {
            e.d("MutiCardHwImpl", " NoSuchMethodException wrong " + e.getMessage(), e);
            return -1;
        } catch (Throwable e2) {
            e.d("MutiCardHwImpl", " NullPointerException wrong " + e2.getMessage(), e2);
            return -1;
        } catch (Throwable e22) {
            e.d("MutiCardHwImpl", " IllegalArgumentException wrong " + e22.getMessage(), e22);
            return -1;
        } catch (Throwable e222) {
            e.d("MutiCardHwImpl", " InvocationTargetException wrong " + e222.getMessage(), e222);
            return -1;
        } catch (Throwable e2222) {
            e.d("MutiCardHwImpl", " Exception wrong " + e2222.getMessage(), e2222);
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
            e.d("MutiCardHwImpl", "getDeviceId exception:" + e.getMessage());
            str = str2;
        } catch (NullPointerException e2) {
            e.d("MutiCardHwImpl", "getDeviceId exception:" + e2.getMessage());
            str = str2;
        } catch (IllegalArgumentException e3) {
            e.d("MutiCardHwImpl", "getDeviceId exception:" + e3.getMessage());
            str = str2;
        } catch (InvocationTargetException e4) {
            e.d("MutiCardHwImpl", "getDeviceId exception:" + e4.getMessage());
            str = str2;
        } catch (Exception e5) {
            e.d("MutiCardHwImpl", "getDeviceId exception:" + e5.getMessage());
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
            e.d("MutiCardHwImpl", "getSubscriberId exception:" + e.getMessage(), e);
            str = str2;
        } catch (Throwable e2) {
            e.d("MutiCardHwImpl", "getSubscriberId exception:" + e2.getMessage(), e2);
            str = str2;
        } catch (Throwable e22) {
            e.d("MutiCardHwImpl", "getSubscriberId exception:" + e22.getMessage(), e22);
            str = str2;
        } catch (Throwable e222) {
            e.d("MutiCardHwImpl", "getSubscriberId exception:" + e222.getMessage(), e222);
            str = str2;
        } catch (Throwable e2222) {
            e.d("MutiCardHwImpl", "getSubscriberId exception:" + e2222.getMessage(), e2222);
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
            e.d("MutiCardHwImpl", " IllegalAccessException wrong " + e.getMessage(), e);
        } catch (Throwable e2) {
            e.d("MutiCardHwImpl", " NoSuchMethodException wrong " + e2.getMessage(), e2);
        } catch (Throwable e22) {
            e.d("MutiCardHwImpl", " NullPointerException wrong " + e22.getMessage(), e22);
        } catch (Throwable e222) {
            e.d("MutiCardHwImpl", " IllegalArgumentException wrong " + e222.getMessage(), e222);
        } catch (Throwable e2222) {
            e.d("MutiCardHwImpl", " getSimState wrong " + e2222.getMessage(), e2222);
        }
        return i2;
    }

    public static Object c() {
        try {
            Class cls = Class.forName("android.telephony.MSimTelephonyManager");
            return cls.getDeclaredMethod("getDefault", new Class[0]).invoke(cls, new Object[0]);
        } catch (Throwable e) {
            e.a("MutiCardHwImpl", " getDefaultMSimTelephonyManager wrong " + e.getMessage(), e);
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
            e.d("MutiCardHwImpl", "getSimOperator exception:" + e.getMessage(), e);
            str2 = str;
        } catch (Throwable e2) {
            e.d("MutiCardHwImpl", "getSimOperator exception:" + e2.getMessage(), e2);
            str2 = str;
        } catch (Throwable e22) {
            e.d("MutiCardHwImpl", "getSimOperator exception:" + e22.getMessage(), e22);
            str2 = str;
        } catch (Throwable e222) {
            e.d("MutiCardHwImpl", "getSimOperator exception:" + e222.getMessage(), e222);
            str2 = str;
        } catch (Throwable e2222) {
            e.d("MutiCardHwImpl", "getSimOperator exception:" + e2222.getMessage(), e2222);
            str2 = str;
        } catch (Throwable e22222) {
            e.d("MutiCardHwImpl", "getSimOperator exception:" + e22222.getMessage(), e22222);
            str2 = str;
        }
        if (str2 != null) {
            return str2;
        }
        return "";
    }
}
