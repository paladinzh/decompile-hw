package com.huawei.hwid.simchange.a;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.j;
import java.lang.reflect.InvocationTargetException;

/* compiled from: NormalMutilSim */
public class c implements a {
    private Object a;
    private Object b;
    private String c;

    public c() {
        this.a = null;
        this.b = null;
        this.a = b();
        this.b = c();
        try {
            Class cls = Class.forName("ro.config.dsds_mode");
            this.c = (String) cls.getDeclaredMethod("get", new Class[]{String.class}).invoke(cls, new Object[]{""});
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e2) {
        } catch (IllegalArgumentException e3) {
        } catch (IllegalAccessException e4) {
        } catch (InvocationTargetException e5) {
        }
    }

    public boolean a() {
        return true;
    }

    private String c(Context context, int i) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            Object a = j.a(telephonyManager.getClass(), telephonyManager, "getSubscriberId", new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(i)});
            if (a != null) {
                String str = (String) a;
                a.b("NormalMutilSim", "getSubscriberIdFromMVersion id:" + i + " imsi:" + str);
                return str;
            }
        } catch (Exception e) {
            a.d("NormalMutilSim", e.toString());
        }
        return "";
    }

    public String a(Context context, int i) {
        String c = c(context, i);
        if (c != null && c.length() > 0) {
            return c;
        }
        String str;
        try {
            str = (String) Class.forName("android.telephony.MSimTelephonyManager").getMethod("getSubscriberId", new Class[]{Integer.TYPE}).invoke(this.a, new Object[]{Integer.valueOf(i)});
        } catch (ClassNotFoundException e) {
            a.a("NormalMutilSim", "getSubscriberId ClassNotFoundException exception:");
            str = c;
        } catch (NoSuchMethodException e2) {
            a.a("NormalMutilSim", "getSubscriberId NoSuchMethodException exception:");
            str = c;
        } catch (IllegalAccessException e3) {
            a.a("NormalMutilSim", "getSubscriberId IllegalAccessException exception:");
            str = c;
        } catch (IllegalArgumentException e4) {
            a.a("NormalMutilSim", "getSubscriberId IllegalArgumentException exception:");
            str = c;
        } catch (InvocationTargetException e5) {
            a.a("NormalMutilSim", "getSubscriberId InvocationTargetException exception:");
            str = c;
        }
        if (str == null) {
            str = "";
        }
        return str;
    }

    public int b(Context context, int i) {
        int i2 = -1;
        if (d.g()) {
            try {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                Object a = j.a(telephonyManager.getClass(), telephonyManager, "getSimState", new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(i)});
                if (a != null) {
                    i2 = ((Integer) a).intValue();
                    a.b("NormalMutilSim", "getSimStatusFromMversion id:" + i + " simStatus:" + i2);
                    return i2;
                }
            } catch (Exception e) {
                a.d("NormalMutilSim", e.toString());
            }
        }
        try {
            Object[] objArr = new Object[]{Integer.valueOf(i)};
            i2 = ((Integer) Class.forName("android.telephony.MSimTelephonyManager").getMethod("getSimState", new Class[]{Integer.TYPE}).invoke(this.a, objArr)).intValue();
        } catch (ClassNotFoundException e2) {
            a.a("NormalMutilSim", "getSimState ClassNotFoundException wrong ");
        } catch (NoSuchMethodException e3) {
            a.a("NormalMutilSim", "getSimState NoSuchMethodException wrong ");
        } catch (IllegalAccessException e4) {
            a.a("NormalMutilSim", "getSimState IllegalAccessException wrong ");
        } catch (IllegalArgumentException e5) {
            a.a("NormalMutilSim", "getSimState IllegalArgumentException wrong ");
        } catch (InvocationTargetException e6) {
            a.a("NormalMutilSim", "getSimState InvocationTargetException wrong ");
        }
        return i2;
    }

    private Object b() {
        Object obj = null;
        try {
            Class cls = Class.forName("android.telephony.MSimTelephonyManager");
            obj = cls.getDeclaredMethod("getDefault", new Class[0]).invoke(cls, new Object[0]);
        } catch (Exception e) {
            a.a("NormalMutilSim", "getHWMSimTelephonyManager wrong ");
        }
        return obj;
    }

    private Object c() {
        Object obj = null;
        try {
            Class cls = Class.forName("com.huawei.telephony.HuaweiTelephonyManager");
            obj = cls.getDeclaredMethod("getDefault", new Class[0]).invoke(cls, new Object[0]);
        } catch (Exception e) {
            Log.d("NormalMutilSim", " getDegaultMSimTelephonyManager wrong ");
        }
        return obj;
    }
}
